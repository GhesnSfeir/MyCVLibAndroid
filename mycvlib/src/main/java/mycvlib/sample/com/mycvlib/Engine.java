package mycvlib.sample.com.mycvlib;

import java.nio.ByteBuffer;

public class Engine {

    static {
        System.loadLibrary("mycvlib");
    }

    private static native String nGetVersionString();

    public static String getVersionString() {
        return nGetVersionString();
    }

    private static native int nGetAverageValue(int rows, int cols, ByteBuffer data);

    public static int getAverageValue(int rows, int cols, ByteBuffer data) {
        return nGetAverageValue(rows, cols, data);
    }
}
