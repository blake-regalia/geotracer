package net.blurcast.tracer.encoder;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;

import net.blurcast.android.util.ByteBuilder;
import net.blurcast.android.util.Encoder;
import net.blurcast.tracer.app.Geotracer;
import net.blurcast.tracer.callback.Attempt;
import net.blurcast.tracer.callback.EventDetails;
import net.blurcast.tracer.callback.EventFilter;
import net.blurcast.tracer.callback.IpcSubscriber;
import net.blurcast.tracer.callback.Subscriber;
import net.blurcast.tracer.driver.Wifi_Driver;
import net.blurcast.tracer.helper.ParcelableScanResultList;
import net.blurcast.tracer.logger._Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by blake on 10/10/14.
 */
public class Wap_Encoder extends _Encoder<ParcelableScanResultList> {

    private static final String TAG = Wap_Encoder.class.getSimpleName();

    private boolean bStopFlag = false;
    private Attempt mStopAttempt;

    private Wifi_Driver mWifi;
    private EventFilter<ScanResult> mEventFilter;

    // for handling the scan events
    private Subscriber<List<ScanResult>> mScanResultSubscriber;

    // for the curious user to see info
    private IpcSubscriber<ParcelableScanResultList> mCuriousSubscriber;


    public Wap_Encoder(Context context, _Logger logger) {
        super(logger);
        this.init(context, null);
    }

//    public Wap_Encoder(Context context, _Logger logger, EventFilter<ScanResult> eventFilter) {
//        super(logger);
//        this.init(context, eventFilter);
//    }

    private void init(Context context, EventFilter<ScanResult> eventFilter) {
        mWifi = Wifi_Driver.getInstance(context);
        mEventFilter = eventFilter;
        this.subscribe();
    }


    private void subscribe() {

        // friending
        final Wap_Encoder friend = this;

        // no need to filter data
        if(mEventFilter == null) {
            mScanResultSubscriber = new Subscriber<List<ScanResult>>() {

                @Override
                public void event(List<ScanResult> eventData, EventDetails eventDetails) {

                    // notify ourselves that the scan completed
                    friend.scanFinished();

                    // then commit events to log
                    friend.logEvents(eventData, eventDetails);
                }
            };
        }

        // data wants to be filtered
        else {
            mScanResultSubscriber = new Subscriber<List<ScanResult>>() {

                @Override
                public void event(List<ScanResult> eventData, EventDetails eventDetails) {

                    // notify ourselves that the scan completed
                    friend.scanFinished();

                    // first, filter out any scan results the user doesn't want
                    Iterator<ScanResult> iterator = eventData.iterator();
                    while(iterator.hasNext()) {
                        if(!mEventFilter.filter(iterator.next())) {
                            iterator.remove();
                        }
                    }

                    // now commit the events to log
                    friend.logEvents(eventData, eventDetails);
                }
            };
        }
    }


    protected void scanFinished() {
        if(!bStopFlag) {
            mWifi.startScan();
        }
    }

    protected void logEvents(List<ScanResult> eventData, EventDetails eventDetails) {

        // the curious user wants to see what's going on
        if(mCuriousSubscriber != null) {
            mCuriousSubscriber.event(new ParcelableScanResultList(eventData), eventDetails);
        }

        // decode details
        long[] timeSpan = eventDetails.getTimeSpan();

        // prepare byte builder, generate data segment header & entry
        ByteBuilder bytes = new ByteBuilder(1+1+3+2+eventData.size()*3);

        // event type: 1 byte
        bytes.append(_Logger.TYPE_WAP_EVENT);

        // number of wireless access points for this scan [0-255]: 1 byte
        bytes.append(
                Encoder.encode_byte(eventData.size())
        );

        // time span (1000Hz resolution - milliseconds) start offset: 3 bytes (279.6 minutes run-time)
        bytes.append_3(
                mLogger.encodeOffsetTime(timeSpan[0])
        );

        // compute scan time
        long scanTime = timeSpan[1] - timeSpan[0];

        // scan time larger than 2 bytes can represent
        if(scanTime > 0xFFFF) {
            scanTime = 0xFFFF;
        }

        // time span (1000Hz resolution) scan time: 2 bytes (65 seconds max)
        bytes.append_2(
                Encoder.encode_char(
                        scanTime
                )
        );

        // iterate through all scan results
        for(ScanResult wap: eventData) {

            // encode id of this access point: 2 bytes
            bytes.append_2(
                    commitWapAndEncodeId(wap)
            );

            // encode signal strength right now [-128, 127]: 1 byte
            bytes.append(
                    Encoder.encode_byte(wap.level)
            );
        }

        // submit bytes
        mLogger.submit(bytes.getBytes());

        // stop recording
        if(bStopFlag) {

            // relax wifi scan
            mWifi.relaxScanning();

            // remove subscriber from set
            mWifi.unsubscribeScanResults(mScanResultSubscriber);

            // submit pending data to logger
            this.close();
        }

    }


    private void close() {

        // wapInfoId: 2
        // wapInfo: 1+6+2+2+1

        // prepare byte builder
        ByteBuilder bytes = new ByteBuilder(1+2
                + mWapsLength*(2+6+2+2+2)
        );

        // first write type of this block: 1 byte
        bytes.append(_Logger.TYPE_WAP_INFO);

        // next, how many wapInfo will go here: 2 bytes
        bytes.append_2(
                Encoder.encode_char(mWapsLength)
        );

        // encode each and every wapInfo
        for(WapInfo wapInfo: mWaps.keySet()) {

            // start with id of this wapInfo: 2 bytes
            bytes.append_2(
                    Encoder.encode_char(mWaps.get(wapInfo))
            );

            // append the whole wap info
            wapInfo.encodeInto(bytes);
        }

        // submit wap info
        mLogger.submit(bytes.getBytes());


        // new byte builder
        bytes = new ByteBuilder(1+2
                +(2+1+32)*mWapSsidLength);

        // keep track of how many actual bytes get used
        int cBytes = 1+2;

        // start of new block: 1 byte
        bytes.append(_Logger.TYPE_WAP_SSID);

        // how many ssids will go here: 2 bytes
        bytes.append_2(
                Encoder.encode_char(mWapSsidLength)
        );

        // encode each and every ssid
        for(String ssid: mWapSsids.keySet()) {

            // start with id of this wapSsidId: 2 bytes
            bytes.append_2(
                    Encoder.encode_char(mWapSsids.get(ssid))
            );

            // decode the string to bytes
            byte[] ssidBytes = ssid.getBytes(CHARSET_ISO_8859_1);

            // encode length of ssid string: 1 byte
            bytes.append(
                    (byte) ssidBytes.length
            );

            // encode sequence of chars
            bytes.append(
                    ssidBytes
            );

            // increment byte counter
            cBytes += 2+1+ssidBytes.length;
        }

        // submit ssids
        mLogger.submit(bytes.getBytes(cBytes));


        // all done!
        mStopAttempt.ready();
    }


    // data structures for storing wireless access points
    private HashMap<WapInfo, Integer> mWaps = new HashMap<WapInfo, Integer>();
    private int mWapsLength = 0;

    private byte[] commitWapAndEncodeId(ScanResult wap) {

        // create uid for wap
        WapInfo wapInfo = new WapInfo(wap);
        int wapId = mWapsLength;

        // hashmap doesn't have this wap yet
        if(!mWaps.containsKey(wapInfo)) {

            // give it an id
            mWaps.put(wapInfo, mWapsLength++);
        }

        // hashmap already has this wap
        else {

            // fetch the id
            wapId = mWaps.get(wapInfo);
        }

        return Encoder.encode_char(wapId);
    }


    // hash of ssids
    private HashMap<String, Character> mWapSsids = new HashMap<String, Character>();
    private int mWapSsidLength = 0;

    private class WapInfo {

        // generic security types
        private static final String WEP      = "WEP";
        private static final String WPA      = "WPA";
        private static final String PSK      = "PSK";
        private static final String WPA_PSK  = "WPA-PSK";
        private static final String WPA2_PSK = "WPA2-PSK";
        private static final String EAP      = "EAP";
        private static final String WPA_EAP  = "WPA-EAP";
        private static final String WPA2_EAP = "WPA2-EAP";

        // additional security type
        private static final String WPS  = "[WPS]";

        // host mode types
        private static final String IBSS = "[IBSS]";
        private static final String ESS  = "[ESS]";
        private static final String P2P  = "[P2P]";
        private static final String HS20 = "[HS20]";

        private static final String CCMP_PA = "CCMP-preauth";
        private static final String TKIP_PA = "TKIP-preauth";
        private static final String TKIP = "TKIP";
        private static final String CCMP = "CCMP";

        private static final int SECURITY_NONE     = 0x00;
        private static final int SECURITY_WEP      = 0x01;
        private static final int SECURITY_WPA_PSK  = 0x02;
        private static final int SECURITY_WPA2_PSK = 0x03;
        private static final int SECURITY_BOTH_PSK = 0x04;
        private static final int SECURITY_WPA_EAP  = 0x05;
        private static final int SECURITY_WPA2_EAP = 0x06;
        private static final int SECURITY_BOTH_EAP = 0x07;

        private static final int BIT_WPS  = 1<<3;
        private static final int BIT_IBSS = 1<<4;
        private static final int BIT_ESS  = 1<<5;
        private static final int BIT_P2P  = 1<<6;
        private static final int BIT_HS20 = 1<<7;

        private static final int BIT_WPA_KEY_BOTH = 1<<8;
        private static final int BIT_WPA_CCMP_FKT = 1<<9;
        private static final int BIT_WPA_FKT_PA   = 1<<10;
        private static final int BIT_WPA_NKT_PA   = 1<<11;

        private static final int BIT_WPA2_KEY_BOTH = 1<<12;
        private static final int BIT_WPA2_CCMP_FKT = 1<<13;
        private static final int BIT_WPA2_FKT_PA   = 1<<14;
        private static final int BIT_WPA2_NKT_PA   = 1<<15;

        private final Pattern PATTERN_WPA_KEY = Pattern.compile("\\[WPA\\-(PSK|EAP)\\-([^\\]]+)\\]");
        private final Pattern PATTERN_WPA2_KEY = Pattern.compile("\\[WPA2\\-(PSK|EAP)\\-([^\\]]+)\\]");


        private char[] mBssid = new char[6];
        private char mSecurity = 0;
        private char mSsidId;
        private char mFrequency;

        private char[] mUid = new char[9];


        public WapInfo(ScanResult wap) {

            // first, encode bssid and beginning of uid simultaneously
            int k = 0;

            // bssid strings are 17 characters long
            for(int i=0; i<17; i+=3) {

                // encode into bssid character array
                mBssid[k] = mUid[k] = (char) ((Character.digit(wap.BSSID.charAt(i), 16) << 4)
                        + Character.digit(wap.BSSID.charAt(i+1), 16));

                // increment k
                k += 1;
            }

            // security type(s)
            if(wap.capabilities.contains(WEP)) {
                mSecurity = SECURITY_WEP;
            }
            // password protected
            else if(wap.capabilities.contains(WPA)) {
                // psk
                if (wap.capabilities.contains(PSK)) {
                    // wpa2
                    if (wap.capabilities.contains(WPA2_PSK)) {
                        // wpa
                        if (wap.capabilities.contains(WPA_PSK)) {
                            // both wpa and wpa2
                            mSecurity = SECURITY_BOTH_PSK;
                        } else {
                            // just wpa2
                            mSecurity = SECURITY_WPA2_PSK;
                        }
                    } else {
                        // just wpa
                        mSecurity = SECURITY_WPA_PSK;
                    }
                }
                // eap
                else if(wap.capabilities.contains(EAP)) {
                    // wpa2
                    if(wap.capabilities.contains(WPA2_EAP)) {
                        // wpa
                        if(wap.capabilities.contains(WPA_EAP)) {
                            // both wpa and wpa2
                            mSecurity = SECURITY_BOTH_EAP;
                        } else {
                            // just wpa2
                            mSecurity = SECURITY_WPA2_EAP;
                        }
                    } else {
                        // just wpa
                        mSecurity = SECURITY_WPA_EAP;
                    }
                }
                else {
                    // TODO handle unknown security types
                }
            }
            // none (open)
            else {
                mSecurity = SECURITY_NONE;
            }

            // wifi-protected-setup
            if(wap.capabilities.contains(WPS)) {
                mSecurity |= BIT_WPS;
            }

            // independent bss (ad-hoc)
            if(wap.capabilities.contains(IBSS)) {
                mSecurity |= BIT_IBSS;
            }

            // extended service set
            if(wap.capabilities.contains(ESS)) {
                mSecurity |= BIT_ESS;
            }

            // peer-to-peer
            if(wap.capabilities.contains(P2P)) {
                mSecurity |= BIT_P2P;
            }

            // hot-spot 2.0
            if(wap.capabilities.contains(HS20)) {
                mSecurity |= BIT_HS20;
            }

            // break down wpa
            Matcher wpaMatcher = PATTERN_WPA_KEY.matcher(wap.capabilities);
            if(wpaMatcher.find()) {
                String wpaKey = wpaMatcher.group(1);

                int tkipIndex = wpaKey.indexOf(TKIP);
                int ccmpIndex = wpaKey.indexOf(CCMP);

                // tkip present
                if(tkipIndex != -1) {

                    // ccmp is present
                    if(ccmpIndex != -1) {

                        // both key types present
                        mSecurity |= BIT_WPA_KEY_BOTH;

                        // ccmp before tkip (first key type)
                        if (ccmpIndex < tkipIndex) {
                            mSecurity |= BIT_WPA_CCMP_FKT;

                            // ccmp has pa
                            if (wpaKey.contains(CCMP_PA)) {
                                mSecurity |= BIT_WPA_FKT_PA;
                            }
                            // tkip has pa (next key type)
                            if (wpaKey.contains(TKIP_PA)) {
                                mSecurity |= BIT_WPA_NKT_PA;
                            }
                        }

                        // tkip before ccmp
                        else {

                            // tkip has pa
                            if(wpaKey.contains(TKIP_PA)) {
                                mSecurity |= BIT_WPA_FKT_PA;
                            }
                            // ccmp has pa
                            if(wpaKey.contains(CCMP_PA)) {
                                mSecurity |= BIT_WPA_NKT_PA;
                            }
                        }
                    }
                    // only tkip present
                    else {

                        // tkip has pa (first key type)
                        if(wpaKey.contains(TKIP_PA)) {
                            mSecurity |= BIT_WPA_FKT_PA;
                        }
                    }
                }
                // ccmp (must be) present
                else {

                    // ccmp is first key type
                    mSecurity |= BIT_WPA_CCMP_FKT;

                    // ccmp has pa (first key type)
                    if(wpaKey.contains(CCMP_PA)) {
                        mSecurity |= BIT_WPA_FKT_PA;
                    }
                }
            }

            // break down wpa2
            Matcher wpa2Matcher = PATTERN_WPA2_KEY.matcher(wap.capabilities);
            if(wpa2Matcher.find()) {
                String wpa2Key = wpa2Matcher.group(1);

                int tkipIndex = wpa2Key.indexOf(TKIP);
                int ccmpIndex = wpa2Key.indexOf(CCMP);

                // tkip present
                if(tkipIndex != -1) {

                    // ccmp is present
                    if(ccmpIndex != -1) {

                        // both key types present
                        mSecurity |= BIT_WPA2_KEY_BOTH;

                        // ccmp before tkip (first key type)
                        if (ccmpIndex < tkipIndex) {
                            mSecurity |= BIT_WPA2_CCMP_FKT;

                            // ccmp has pa
                            if (wpa2Key.contains(CCMP_PA)) {
                                mSecurity |= BIT_WPA2_FKT_PA;
                            }
                            // tkip has pa (next key type)
                            if (wpa2Key.contains(TKIP_PA)) {
                                mSecurity |= BIT_WPA2_NKT_PA;
                            }
                        }

                        // tkip before ccmp
                        else {

                            // tkip has pa
                            if(wpa2Key.contains(TKIP_PA)) {
                                mSecurity |= BIT_WPA2_FKT_PA;
                            }
                            // ccmp has pa
                            if(wpa2Key.contains(CCMP_PA)) {
                                mSecurity |= BIT_WPA2_NKT_PA;
                            }
                        }
                    }
                    // only tkip present
                    else {

                        // tkip has pa (first key type)
                        if(wpa2Key.contains(TKIP_PA)) {
                            mSecurity |= BIT_WPA2_FKT_PA;
                        }
                    }
                }
                // ccmp (must be) present
                else {

                    // ccmp is first key type
                    mSecurity |= BIT_WPA2_CCMP_FKT;

                    // ccmp has pa (first key type)
                    if(wpa2Key.contains(CCMP_PA)) {
                        mSecurity |= BIT_WPA2_FKT_PA;
                    }
                }
            }


            // capabilities identity
            mUid[6] = mSecurity;

            // finally add truncated hash of ssid string
            mUid[7] = (char) wap.SSID.hashCode();

            // frequency
            mUid[8] = mFrequency = (char) wap.frequency;

            // ssid id
            Character ssid = mWapSsids.get(wap.SSID);
            if(ssid != null) {
                mSsidId = ssid;
            }
            else {
                mSsidId = (char) mWapSsidLength++;
                mWapSsids.put(wap.SSID, mSsidId);
            }
        }


        public void encodeInto(ByteBuilder bytes) {

            // encode BSSID: 6 bytes
            bytes.append_6(
                    Encoder.encode_bytes(mBssid)
            );

            // encode SSID name: 2 byte identifier
            bytes.append_2(
                    Encoder.encode_char(mSsidId)
            );

            // encode frequency: 2 bytes
            bytes.append_2(
                    Encoder.encode_char(mFrequency)
            );

            // encode security: 2 bytes
            bytes.append_2(
                    Encoder.encode_char(mSecurity)
            );
        }


        public boolean uidMatches(char[] uid) {
            for(int i=0; i<uid.length; i++) {
                if(uid[i] != mUid[i]) return false;
            }
            return true;
        }


        @Override
        public int hashCode() {
            return    mUid[0]*31*31*31*31*31*31*31*31
                    + mUid[1]*31*31*31*31*31*31*31
                    + mUid[2]*31*31*31*31*31*31
                    + mUid[3]*31*31*31*31*31
                    + mUid[4]*31*31*31*31
                    + mUid[5]*31*31*31
                    + mUid[6]*31*31
                    + mUid[7]*31
                    + mUid[8];
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null || obj.getClass() != this.getClass()) return false;
            WapInfo other = (WapInfo) obj;
            return other.uidMatches(mUid);
        }

    }


    @Override
    public void start() {
        this.start(null);
    }

    @Override
    public void start(IpcSubscriber<ParcelableScanResultList> subscriber) {
        Log.i(TAG, "using _Logger: "+mLogger);
        mCuriousSubscriber = subscriber;
        bStopFlag = false;
        mStopAttempt = null;
        mWifi.enableScanning(new Attempt() {
            @Override
            public void ready() {
                mWifi.subscribeScanResults(mScanResultSubscriber);
                mWifi.startScan();
            }
        });
    }

    @Override
    public void stop(Attempt attempt) {
        bStopFlag = true;
        mStopAttempt = attempt;
    }

    @Override
    public String toString() {
        return TAG;
    }

}
