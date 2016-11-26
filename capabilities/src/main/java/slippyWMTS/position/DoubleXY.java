package slippyWMTS.position;

import java.text.DecimalFormat;

public class DoubleXY {
    public double x;
    public double y;

    public DoubleXY() {
    }

    public DoubleXY(double[] xy) {
        this.x = xy[0];
        this.y = xy[1];
    }

    public DoubleXY(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        DecimalFormat fmt = new DecimalFormat("0.00");
        return fmt.format(x) + "," + fmt.format(y);
    }
}
