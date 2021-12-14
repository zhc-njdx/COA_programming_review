package util;

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
        int[] res = new int[src.length];
        cf = sub;
        if (sub == 1)
            for (int i = 0; i < src.length; ++i)
                src[i] = src[i] == 1 ? 0 : 1;
        for (int j = src.length - 1; j >= 0; --j)
        {
            res[j] = src[j] ^ dest[j] ^ cf;
            cf = (src[j] & dest[j]) | (src[j] & cf) | (dest[j] & cf);
        }
        return res;
    }
}
