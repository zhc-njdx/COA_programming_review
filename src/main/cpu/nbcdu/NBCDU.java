package cpu.nbcdu;

import util.DataType;
import util.TransformHelper;

/**
 * NBCDU
 * 作业六
 * 实现NBCDU码的加减
 */

public class NBCDU {

    boolean sub;
    TransformHelper tfh = new TransformHelper();

    /**
     * dest + src
     * @param src NBCD码加数
     * @param dest NBCD码被加数
     * @return 和
     */
    public DataType add(DataType src, DataType dest){
        sub = false;
        return addMachine(src, dest);
    }

    /**
     * dest - src
     * @param src NBCD码减数
     * @param dest NBCD码被减数
     * @return　差
     */
    public DataType sub(DataType src, DataType dest){
        sub = true;
        return addMachine(src,dest);
    }


    /**
     * 步骤：
     * 1、判断加减。减--->将src变号
     * 2、其中一个为0，直接返回另一个
     * 3、判断符号是否相同。符号不同--->src"取反加一"
     *      这里的"取反"不是二进制里的取反
     *      取反是: 0001 ---> 1000  0100 ---> 0101  即 1001 减去对应位
     * 4、运算。
     *      每四位进行相加，如果 加法有进位 或者 相加结果大于等于 1010 则要进位，且结果加 0110
     * 5、结果修正。
     *      符号相反情况。最终没有进位1说明不够减，需要将结果取反加一，并且符号取反
     */
    private DataType addMachine(DataType src, DataType dest){
        // 变成字符串
        String srcStr = src.toString();
        String destStr = dest.toString();

        // 如果是减，先改变符号
        if (sub)
            srcStr = (srcStr.substring(0,4).equals("1100") ? "1101" : "1100") + srcStr.substring(4);

        // 如果其中有一个是0 直接返回另一个
        if (srcStr.substring(4).equals("0000000000000000000000000000"))
            return dest;
        else if(destStr.substring(4).equals("0000000000000000000000000000"))
            return src;

        // 判断是否符号相同
        boolean isSameSign = srcStr.substring(0,4).equals(destStr.substring(0,4));
        String res_sign = destStr.substring(0,4); // 暂定结果的符号为dest的符号

        int len = srcStr.length();
        String res = ""; // 结果字符串

        // 如果符号不同，将src"取反"
        if (!isSameSign)
            srcStr = negationAddOne(srcStr);

        int carry = 0; // 指示是否有进位
        for (int i = len - 4; i >= 4; i -= 4){

            int[] srcTmp = tfh.StringToIntArray(srcStr.substring(i,i+4));
            int[] destTmp = tfh.StringToIntArray(destStr.substring(i,i+4));

            // 有进位 先加1
            if (carry == 1)
                destTmp = tfh.addIntArray(tfh.StringToIntArray("0001"),destTmp,0);

            // 相加
            int[] resTmp = tfh.addIntArray(srcTmp,destTmp,0);

            // 判断是否有进位!!!(关键步骤)
            boolean isCarry = ((resTmp[0] == 1) && (resTmp[1] == 1 || resTmp[2] == 1)) || tfh.cf == 1;
            if (isCarry){
                resTmp = tfh.addIntArray(tfh.StringToIntArray("0110"),resTmp,0);
                carry = 1;
            }
            else
                carry = 0;

            res = tfh.IntArrayToString(resTmp) + res;
        }

        res = res_sign + res; // 结果加上符号

        // 结果修正
        if (!isSameSign)
            if (carry != 1) {
                res = negationAddOne(res);
                res = srcStr.substring(0,4) + res.substring(4);
            }

        return new DataType(res);
    }

    /**
     * 取反加一
     *
     */
    private String negationAddOne(String src){
        StringBuilder new_src = new StringBuilder(src);
        int len = src.length();
        for (int i = 4; i <= len - 4; i += 4){
            // 取反
            String tmp = tfh.IntArrayToString(
                    tfh.addIntArray(
                            tfh.StringToIntArray(src.substring(i,i+4)), tfh.StringToIntArray("1001"),1  ));
            // 加一
            if (i == len - 4)
                tmp = tfh.IntArrayToString(
                        tfh.addIntArray(
                                tfh.StringToIntArray("0001"), tfh.StringToIntArray(tmp),0 ));

            new_src.replace(i,i+4,tmp);
        }
        return new_src.toString();
    }

}
