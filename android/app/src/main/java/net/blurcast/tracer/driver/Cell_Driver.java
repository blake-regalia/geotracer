package net.blurcast.tracer.driver;

import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.util.List;

/**
 * Created by blake on 10/8/14.
 */
public class Cell_Driver {

    private static final String TAG = Cell_Driver.class.getSimpleName();

    private static Cell_Driver mInstance;
    private static TelephonyManager mTelephony;
    private static Context mContext;

    private static int iPhoneType;
    private static int iNetworkType;

    public static Cell_Driver getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new Cell_Driver(context);
        }
        return mInstance;
    }

    public Cell_Driver(Context context) {
        mContext = context;
        mTelephony = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

        neighbors();

        CellLocation cl = mTelephony.getCellLocation();
        if(cl == null) Log.e(TAG, "No cell location");
        else {
            GsmCellLocation gsmLoc;
            CdmaCellLocation cdmaLoc;
            try {
                gsmLoc = (GsmCellLocation) cl;
                Log.d(TAG, "Cell id " + gsmLoc.getCid());
                Log.d(TAG, "Lac - " + gsmLoc.getLac());
                Log.d(TAG, "Psc - " + gsmLoc.getPsc());
            } catch (ClassCastException e) {
                cdmaLoc = (CdmaCellLocation) cl;
                Log.d(TAG, "Base station ID - " + cdmaLoc.getBaseStationId());
                Log.d(TAG, "Base station Latitude - " + cdmaLoc.getBaseStationLatitude());
                Log.d(TAG, "Network Id - " + cdmaLoc.getNetworkId());
                Log.d(TAG, "System ID -" + cdmaLoc.getSystemId());
            }
        }
    }

    private void neighbors() {

        List<NeighboringCellInfo> neighboringCellInfoList = mTelephony.getNeighboringCellInfo();
        Log.d(TAG, "Neighboring cells: "+neighboringCellInfoList.size());
        for(NeighboringCellInfo neighboringCellInfo: neighboringCellInfoList) {
            int cid = neighboringCellInfo.getCid();
            int lac = neighboringCellInfo.getLac();
            int psc = neighboringCellInfo.getPsc();
            int rssi = neighboringCellInfo.getRssi();
            int type = neighboringCellInfo.getNetworkType();

            Log.d(TAG, "Cell {cid:"+cid+"; lac:"+lac+"; psc:"+psc+"; rssi:"+rssi+"; type:"+type+"}");
        }
    }

//    public void initApi17() {
//
//
//        iPhoneType = mTelephony.getPhoneType();
//        iNetworkType = mTelephony.getNetworkType();
//
//        //
//        String phoneType = "other";
//        switch(iPhoneType) {
//            case TelephonyManager.PHONE_TYPE_CDMA:
//                phoneType = "CDMA";
//                break;
//            case TelephonyManager.PHONE_TYPE_GSM:
//                phoneType = "GSM";
//                break;
//            case TelephonyManager.PHONE_TYPE_NONE:
//                phoneType = "NONE";
//                break;
//            case TelephonyManager.PHONE_TYPE_SIP:
//                phoneType = "SIP";
//                break;
//        }
//
//        String networkType = "other";
//        switch(iNetworkType) {
//            case TelephonyManager.NETWORK_TYPE_CDMA:
//                networkType = "CDMA";
//                break;
//            case TelephonyManager.NETWORK_TYPE_LTE:
//                networkType = "LTE";
//                break;
//            case TelephonyManager.NETWORK_TYPE_UMTS:
//                networkType = "UMTS";
//                break;
//        }
//
//        Log.d(TAG, "phone: " + phoneType + "; network: " + networkType);
//
//        List<CellInfo> cellInfoList = mTelephony.getAllCellInfo();
//        Log.d(TAG, "Cell Info Objects: "+cellInfoList.size());
//
//
//        switch(iNetworkType) {
//            case TelephonyManager.NETWORK_TYPE_CDMA:
//                for(CellInfo cellInfo: cellInfoList) {
//                    CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
//                    CellIdentityCdma cellIdentity = cellInfoCdma.getCellIdentity();
//                    CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
//
//                    int baseStationId = cellIdentity.getBasestationId();
//                    int latitude = cellIdentity.getLatitude();
//                    int longitude = cellIdentity.getLongitude();
//                    int networkId = cellIdentity.getNetworkId();
//                    int systemId = cellIdentity.getSystemId();
//
//                    int asuLevel = cellSignalStrengthCdma.getAsuLevel();
//                    int cdmaDbm = cellSignalStrengthCdma.getCdmaDbm();
//                    int cdmaEcio = cellSignalStrengthCdma.getCdmaEcio();
//                    int dbm = cellSignalStrengthCdma.getDbm();
//                    int evoDbm = cellSignalStrengthCdma.getEvdoDbm();
//                    int evoEcio = cellSignalStrengthCdma.getEvdoEcio();
//                    int evoSnr = cellSignalStrengthCdma.getEvdoSnr();
//
//                    Log.d(TAG, "Base Station {id:"+baseStationId+"; [lat,long]:"+latitude+","+longitude+"; network-id:"+networkId+"; system-id:"+systemId+"}");
//                    Log.d(TAG, "CDMA {asu:"+asuLevel+"; dbm:"+cdmaDbm+"; ecio:"+cdmaEcio+"; }");
//                    Log.d(TAG, "Cell {dbm:"+dbm+"; }");
//                    Log.d(TAG, "EVO {dbm:"+evoDbm+"; ecio:"+evoEcio+"; snr:"+evoSnr+"}");
//                }
//                break;
//
//            case TelephonyManager.NETWORK_TYPE_LTE:
//                for(CellInfo cellInfo: cellInfoList) {
//                    CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
//                    CellIdentityLte cellIdentity = cellInfoLte.getCellIdentity();
//                    CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
//
//                    int ci = cellIdentity.getCi();
//                    int mcc = cellIdentity.getMcc();
//                    int mnc = cellIdentity.getMnc();
//                    int pci = cellIdentity.getPci();
//                    int tac = cellIdentity.getTac();
//
//                    int asuLevel = cellSignalStrengthLte.getAsuLevel();
//                    int dbm = cellSignalStrengthLte.getDbm();
//                    int ta = cellSignalStrengthLte.getTimingAdvance();
//
//                    Log.d(TAG, "Cell {ci:"+ci+"; mcc:"+mcc+"; mnc:"+mnc+" ; pci:"+pci+" ; tac:"+tac+" }");
//                    Log.d(TAG, "LTE {asu:"+asuLevel+"; dbm:"+dbm+"; ta:"+ta+"}");
//                }
//                break;
//        }
//
//        int simState = mTelephony.getSimState();
//        String simCountry = mTelephony.getSimCountryIso();
//        String simOperator = mTelephony.getSimOperator();
//        String simSpn = mTelephony.getSimOperatorName();
//        String simSerial = mTelephony.getSimSerialNumber();
//        Log.d(TAG, "SIM {state:"+simState+"; country:"+simCountry+"; operator:"+simOperator+"; spn:"+simSpn+"; serial:"+simSerial+"}");
//
//        String netCountry = mTelephony.getNetworkCountryIso();
//        String netOperator = mTelephony.getNetworkOperator();
//        String netSpn = mTelephony.getNetworkOperatorName();
//        Log.d(TAG, "Network {country:"+netCountry+"; operator:"+netOperator+"; spn:"+netSpn+"}");
//
//    }

}
