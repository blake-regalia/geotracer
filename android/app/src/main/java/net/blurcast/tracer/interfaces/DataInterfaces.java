package net.blurcast.tracer.interfaces;

/**
 * Created by blake on 12/28/14.
 */
public enum DataInterfaces {


    WifiAccessPoints(Wap_Interface.class),
    BluetoothLowEnergy(Btle_Interface.class),
    Environment(Env_Interface.class)
    ;


    public _Interface mInterface;

    DataInterfaces(Class<? extends _Interface> interfaceClass) {
        try {
            mInterface = interfaceClass.newInstance();
        } catch(InstantiationException x) {
            x.printStackTrace();
        } catch(IllegalAccessException x) {
            x.printStackTrace();
        }
    }

    private static _Interface[] aInterfaces;
    public static _Interface[] getInterfaces() {
        if(aInterfaces == null) {
            DataInterfaces[] dataInterfaces = DataInterfaces.values();
            aInterfaces = new _Interface[dataInterfaces.length];
            for(int i=0; i<dataInterfaces.length; i++) {
                aInterfaces[i] = dataInterfaces[i].mInterface;
            }
        }
        return aInterfaces;
    }

}
