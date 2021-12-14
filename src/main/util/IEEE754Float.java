package util;

import java.lang.ref.SoftReference;

public class IEEE754Float {
    public static final String N_ZERO = "10000000000000000000000000000000";

    public static final String P_ZERO = "00000000000000000000000000000000";

    public static final String N_INF  = "11111111100000000000000000000000";

    public static final String P_INF  = "01111111100000000000000000000000";

    public static final String NaN    = "01111111110000000000000000000000";

    public static final String NaN_Regular = "(0|1){1}1{8}(1+0+|0+1+)(0|1)*";  //NaN的正则表达式
}
