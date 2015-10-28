package net.blurcast.geotracer_decoder.decoder;

import net.blurcast.geotracer_decoder.logger.ByteDecodingFileReader;
import net.blurcast.geotracer_decoder.surrogate.Wap_Surrogate;
import net.blurcast.geotracer_decoder.surrogate._Surrogate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by blake on 1/9/15.
 */
public class Wap extends _Decoder {

    public LinkedHashMap<Integer, ArrayList<Event>> events = new LinkedHashMap<Integer, ArrayList<Event>>();
    public HashMap<Integer, Info> infos = new HashMap<Integer, Info>();
    public HashMap<Integer, String> ssids = new HashMap<Integer, String>();

    public Class<? extends _Decoder> getRealClass() {
        return this.getClass();
    }

    @SuppressWarnings("unused")
    public void decode_event(ByteDecodingFileReader source) {

        // meta-data about the scan
        ScanInfo scanInfo = new ScanInfo(source);

        // loop
        for(int i=0; i<scanInfo.numWaps; i++) {

            // local id of wap
            int wapId = source.read_int_2();

            // first time seeing this wap
            if(!events.containsKey(wapId)) {
                events.put(wapId, new ArrayList<Event>());
            }

            // add this event to the list
            events.get(wapId).add(new Event(source, scanInfo));
        }
    }

    public class ScanInfo {
        public int numWaps;
        public int elapsed;
        public int scanDuration;

        public ScanInfo(ByteDecodingFileReader source) {
            numWaps = source.read();
            // time elapsed (1000Hz resolution) since start: 3 bytes
            elapsed = source.read_int_3();
            scanDuration = source.read_int_2();
        }
    }

    public class Event {
        public byte rssi;
        public ScanInfo scanInfo;

        public Event(ByteDecodingFileReader source, ScanInfo _scanInfo) {
            rssi = source.read_byte();
            scanInfo = _scanInfo;
        }
    }


    @SuppressWarnings("unused")
    public void decode_info(ByteDecodingFileReader source) {

        // how many entries we're about to read
        int numWaps = source.read_int_2();

        // loop
        for(int i=0; i<numWaps; i++) {

            // local id of wap
            int wapId = source.read_int_2();

            // add wap info
            Info info = new Info(source);
            infos.put(wapId, info);
        }
    }


    public class Info {

        private static final int BIT_SECURITY_TYPES  = 0x07;

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

        private byte[] mBssid = new byte[6];
        public int ssidId;
        public int frequency;
        public int security;

        public Info(ByteDecodingFileReader source) {

            // bssid: 6 bytes
            source.read(mBssid);

            // ssid identifier: 2 bytes
            ssidId = source.read_int_2();

            // frequency: 2 bytes
            frequency = source.read_int_2();

            // security: 2 bytes
            security = source.read_char();
        }

        public String getBssid() {
            return hwToString(mBssid);
        }

        public String getSsid() {
            return ssids.get(ssidId);
        }

        public String getSecurity() {
            StringBuilder b = new StringBuilder();
            if((security & BIT_WPS) != 0) b.append("[WPS]");

            int securityType = security & BIT_SECURITY_TYPES;
            switch(securityType) {
                case SECURITY_NONE:
                    b.append("[Open]");
                    break;
                case SECURITY_WEP:
                    b.append("[WEP]");
                    break;
                case SECURITY_WPA_PSK:
                    b.append("[WPA-PSK").append(getWpaKeyTypes());
                    break;
                case SECURITY_WPA2_PSK:
                    b.append("[WPA2-PSK").append(getWpa2KeyTypes());
                    break;
                case SECURITY_BOTH_PSK:
                    b.append("[WPA-PSK").append(getWpaKeyTypes()).append("[WPA2-PSK").append(getWpa2KeyTypes());
                    break;
                case SECURITY_WPA_EAP:
                    b.append("[WPA-EAP").append(getWpaKeyTypes());
                    break;
                case SECURITY_WPA2_EAP:
                    b.append("[WPA2-EAP").append(getWpa2KeyTypes());
                    break;
                case SECURITY_BOTH_EAP:
                    b.append("[WPA-EAP").append(getWpaKeyTypes()).append("[WPA2-EAP").append(getWpa2KeyTypes());
                    break;
            }

            if((security & BIT_ESS) != 0) b.append("[ESS]");
            if((security & BIT_IBSS) != 0) b.append("[IBSS]");
            if((security & BIT_P2P) != 0) b.append("[P2P]");
            if((security & BIT_HS20) != 0) b.append("[HS20]");

            return b.toString();
        }

        private String getWpaKeyTypes() {
            StringBuilder b = new StringBuilder();
            if((security & BIT_WPA_CCMP_FKT) != 0) {
                b.append("-CCMP");
                if((security & BIT_WPA_FKT_PA) != 0) {
                    b.append("-preauth");
                }
                if((security & BIT_WPA_KEY_BOTH) != 0) {
                    b.append("+TKIP");
                    if((security & BIT_WPA_NKT_PA) != 0) {
                        b.append("-preauth");
                    }
                }
            }
            else {
                b.append("-TKIP");
                if((security & BIT_WPA_FKT_PA) != 0) {
                    b.append("-preauth");
                }
                if((security & BIT_WPA_KEY_BOTH) != 0) {
                    b.append("+CCMP");
                    if((security & BIT_WPA_NKT_PA) != 0) {
                        b.append("-preauth");
                    }
                }
            }
            return b.append("]").toString();
        }

        private String getWpa2KeyTypes() {
            StringBuilder b = new StringBuilder();
            if((security & BIT_WPA2_CCMP_FKT) != 0) {
                b.append("-CCMP");
                if((security & BIT_WPA2_FKT_PA) != 0) {
                    b.append("-preauth");
                }
                if((security & BIT_WPA2_KEY_BOTH) != 0) {
                    b.append("+TKIP");
                    if((security & BIT_WPA2_NKT_PA) != 0) {
                        b.append("-preauth");
                    }
                }
            }
            else {
                b.append("-TKIP");
                if((security & BIT_WPA2_FKT_PA) != 0) {
                    b.append("-preauth");
                }
                if((security & BIT_WPA2_KEY_BOTH) != 0) {
                    b.append("+CCMP");
                    if((security & BIT_WPA2_NKT_PA) != 0) {
                        b.append("-preauth");
                    }
                }
            }
            return b.append("]").toString();
        }

        public String toString() {
            return getBssid()+" \""+getSsid()+"\"; "+frequency+"mHz "+getSecurity();
        }
    }

    @SuppressWarnings("unused")
    public void decode_ssid(ByteDecodingFileReader source) {

        // how many entries we're about to read
        int numSsids = source.read_int_2();

        StringBuilder sb = new StringBuilder();

        // loop
        for(int i=0; i<numSsids; i++) {

            // local id of this ssid
            int ssidId = source.read_int_2();

            // decode string
            String ssid = source.read_string();

            // store ssid
            ssids.put(ssidId, ssid);

            sb.append(ssid).append(", ");
        }
    }

    @Override
    public _Surrogate getSurrogate() {
        return new Wap_Surrogate(this);
    }

}
