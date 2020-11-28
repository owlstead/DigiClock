package digiclock;

import java.awt.Dimension;
import java.awt.Toolkit;

public final class ClockUtil {
    
    private static final float MM_IN_INCH = 0.0393701F;
    private static final int RES = Toolkit.getDefaultToolkit().getScreenResolution();

    private ClockUtil() {
        // avoid instantiation
    }

    public static Dimension convertFromDimensionInMM(Dimension dimInMM) {
        return new Dimension(mmToPixels(dimInMM.width), mmToPixels(dimInMM.height));
    }

    public static int mmToPixels(int mm) {
        return (int) (mm * MM_IN_INCH * RES);
    }
}
