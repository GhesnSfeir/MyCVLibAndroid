package mycvlib.sample.com.mycvlib;

public class MyCVLib {

    static {
        System.loadLibrary("mycvlib");
    }

    private static native int nGetInt();

    public static int getInt() {
        return nGetInt();
    }
}
