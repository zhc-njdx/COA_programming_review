package util;

import java.util.Arrays;

/**
 * 放置一些功能函数
 */

public class TransformHelper {

    public int cf;

    public int[] StringToIntArray(String str)
    {
        int[] arr = new int[str.length()];
        for (int i = 0; i < str.length(); ++i)
            arr[i] = str.charAt(i) - '0';
        return arr;
    }

    public String IntArrayToString(int[] arr)
    {
        StringBuilder str = new StringBuilder();
        for (int i : arr)
            str.append(i);
        return str.toString();
    }

    /**
     * 实现一个简单的加法器
     * 每次调用ALU的add麻烦了
     * @param src int数组形式的加数
     * @param dest int数组形式的加数
     * @return int数组形式的和
     */
    public int[] addIntArray(int[] src, int[] dest, int sub)
    {
        int[] src1 = new int[src.length];
        System.arraycopy(src, 0, src1, 0, src.length);
        int[] res = new int[src.length];
        cf = sub;
        if (sub == 1)
            for (int i = 0; i < src1.length; ++i)
                // 由于src指的是对象  所以这里的修改是永远的
                // 解决方案  重新new一个数组，将src复制过去
//                src[i] = src[i] == 1 ? 0 : 1;
                src1[i] = src1[i] == 1 ? 0 : 1;
        for (int j = src1.length - 1; j >= 0; --j)
        {
            res[j] = src1[j] ^ dest[j] ^ cf;
            cf = (src1[j] & dest[j]) | (src1[j] & cf) | (dest[j] & cf);
        }
        return res;
    }

    public char[] StringToCharArray(String str){
        char[] charArray = new char[str.length()];
        for (int i = 0; i < str.length(); ++i)
            charArray[i] = str.charAt(i);
        return charArray;
    }

    public String CharArrayToString(char[] chars){
        StringBuilder str = new StringBuilder();
        for (char aChar : chars) str.append(aChar);
        return str.toString();
    }

    public static void main(String[] args){
        TransformHelper tfh = new TransformHelper();
        System.out.println(tfh.IntArrayToString(tfh.addIntArray(tfh.StringToIntArray("101000000000000000000000000"),
                tfh.StringToIntArray("011100000000000000000000000"), 1)));
        System.out.println(tfh.cf);
    }
}
