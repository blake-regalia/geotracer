package net.blurcast.geotracer_decoder.runner;

import net.blurcast.geotracer_decoder.decoder._Decoder;
import net.blurcast.geotracer_decoder.helper.TraceInfo;
import net.blurcast.geotracer_decoder.logger._Log;
import net.blurcast.geotracer_decoder.surrogate._Surrogate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * Created by blake on 1/11/15.
 */
public abstract class _Runner<LogType extends _Log> {

    protected HashMap<Class<? extends _Decoder>, _Surrogate> mSurrogates;
    protected TraceInfo mInfo;
    protected Class<? extends LogType> mLogClass;

    public _Runner() { }

    @SuppressWarnings("unused")
    public final void setup(HashMap<Class<? extends _Decoder>, _Surrogate> surrogates, TraceInfo info) {
        mSurrogates = surrogates;
        mInfo = info;
        mLogClass = getLogClass();
    }

    protected abstract Class<? extends LogType> getLogClass();

    @SuppressWarnings("unchecked")
    public final Class<? extends LogType> getDefaultLogClass() {
        try {
            int modifiers = mLogClass.getModifiers();
            if(Modifier.isAbstract(modifiers)) {
                return (Class<? extends LogType>) mLogClass.getMethod("getDefaultLogClass").invoke(null);
            }
            else if(!Modifier.isInterface(modifiers)) {
                return mLogClass;
            }
            else {
                return null;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // ignore these exceptions
//            e.printStackTrace();
        }
        return null;
    }

    public abstract void run(LogType log);

    public static class Map {
        private HashMap<String, Class<? extends _Runner>> mMap = new HashMap<String, Class<? extends _Runner>>();

        public Map(Pair... pairs) {
            for(Pair pair: pairs) {
                mMap.put(pair.getAppKey(), pair.getRunnerClass());
            }
        }

        public Class<? extends _Runner> get(String key) {
            return mMap.get(key);
        }

        public _Runner newInstance(String appKey, HashMap<Class<? extends _Decoder>, _Surrogate> surrogates, TraceInfo info) {
            try {
                Class<? extends _Runner> runnerClass = mMap.get(appKey);
                _Runner runner = runnerClass.getConstructor().newInstance();
                runnerClass.getMethod("setup", HashMap.class, TraceInfo.class).invoke(runner, surrogates, info);
                return runner;
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static class Pair {
        private String mAppKey;
        private Class<? extends _Runner> mRunnerClass;

        public Pair(String appKey, Class<? extends _Runner> runnerClass) {
            mAppKey = appKey;
            mRunnerClass = runnerClass;
        }

        public String getAppKey() {
            return mAppKey;
        }
        public Class<? extends _Runner> getRunnerClass() {
            return mRunnerClass;
        }
    }

}
