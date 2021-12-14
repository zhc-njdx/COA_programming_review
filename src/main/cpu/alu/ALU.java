package cpu.alu;

import util.DataType;
import util.Transform;

import java.util.Arrays;

/**
 * ALU
 * 作业三
 * 实现32位二进制整数的加减、乘除
 */

public class ALU {

    Transform tf = new Transform();
    private int sub; // sub信号，来选择做加法还是减法
    private int carry; // 进位，由于本题只需算出结果，不用考虑标志位

    public DataType remain; // 除法的余数

    /**
     * 取反
     */
    private int[] reverse(int[] src)
    {
        int[] reverse_src = new int[src.length];
        for (int i = 0; i < src.length; ++i)
        {
            reverse_src[i] = src[i] == 1 ? 0 : 1;
        }
        return reverse_src;
    }

    /**
     * 多路选择器
     * 根据 sub 信号 选择是否取反
     * @param Y 操作数
     * @return 选择后的操作数
     */
    private int[] MUX(int[] Y)
    {
        if(sub == 1)
            return reverse(Y);
        else
            return Y;
    }

    /**
     * 加法器
     * @param X 加数
     * @param Y 加数
     * @return 和
     */
    private int[] addMachine(int[] X, int[] Y)
    {
        int[] ans = new int[X.length];
        int carry = sub;
        for (int i = X.length - 1; i >= 0; --i)
        {
            ans[i] = X[i] ^ Y[i] ^ carry;
            carry = (X[i] & Y[i]) | (X[i] & carry) | (Y[i] & carry);
        }
        this.carry = carry;
        return ans;
    }

    /**
     * dst + src
     * @param src 加数
     * @param dst 加数
     * @return 和
     */
    public DataType add(DataType src, DataType dst)
    {
        sub = 0; //加法
        return new DataType(addMachine(dst.getData_int(),MUX(src.getData_int())));
    }

    /**
     * dst - src
     * @param src 减数
     * @param dst 被减数
     * @return 差
     */
    public DataType subtract(DataType src, DataType dst)
    {
        sub = 1; //减法
        return new DataType(addMachine(dst.getData_int(),MUX(src.getData_int())));
    }

    /**
     * 实现布斯乘法
     * dst * src
     * @param src 乘数
     * @param dst 乘数
     * @return 积 取低32位
     */
    public DataType mul(DataType src, DataType dst)
    {
        int[] X = src.getData_int();
        int[] Y = dst.getData_int();
        int[] P = new int[X.length];
        // init
        Arrays.fill(P, 0);

        int Y0 = 0;
        for (int i = 0; i < Y.length; ++i)
        {
            int ch = Y0 - Y[Y.length - 1];
            switch (ch) {
                case 1 :
                    // P + X
                    P = add(new DataType(X), new DataType(P)).getData_int();
                    break;
                case -1 :
                    // P - X
                    P = subtract(new DataType(X), new DataType(P)).getData_int();
                    break;
                default:
                    break;
            }
            int PLast = P[P.length - 1];
            Y0 = Y[Y.length - 1];
            RightShift(P, P[0]);
            RightShift(Y, PLast);
        }
        //System.out.println(new DataType(P));
        return new DataType(Y);
    }

    private void RightShift(int[] src, int addition)
    {
        for (int i = src.length - 1; i > 0; --i)
            src[i] = src[i - 1];
        src[0] = addition;
    }

    /**
     * dst / src
     * 不恢复余数
     * @param src 除数
     * @param dst 被除数
     * @return 商 并将余数放在 remain 中
     *
     * 不恢复余数除法的步骤:
     * 1、对被除数进行符号扩展
     * 2、若被除数和除数符号相同，做减法；反之做加法
     * 3、余数和被除数符号相同 上商为1，不同，上商为0
     * 4、如果前一次上商为1，则做减法；如果前一次上商为0，则做加法
     * （循环3、4步）最后一次 余数不用左移
     * 5、进行商和余数的修正:
     *      商： 如果商为负（除数与被除数符号不同），商加一
     *      余数：余数与被除数符号不同，要修正
     *          如果被除数与除数符号相同，余数加除数
     *          如果被除数与除数符号不同，余数减除数
     */
    public DataType div(DataType src, DataType dst)
    {
        if(src.toString().equals("00000000000000000000000000000000"))
            throw new ArithmeticException("src is zero!");

        if(dst.toString().equals("00000000000000000000000000000000"))
            return dst;

        /**
         * 考虑溢出
         */
        if (dst.toString().substring(1).equals("0000000000000000000000000000000")
                && dst.toString().charAt(0) == '1'
                && src.toString().equals("11111111111111111111111111111111"))
            throw new ArithmeticException("Overflow!");


        int[] divided = dst.getData_int();
        int[] division = src.getData_int();

        int sign = divided[0];
        // 余数
        int[] remain = new int[divided.length];
        // 初始化，符号扩展
        Arrays.fill(remain,sign);

        boolean isSameSign = remain[0] == division[0];
        int sang = 0;

        for (int i = 0; i < divided.length + 1; ++i)
        {
            // 中间余数和除数符号相同，相减
            /**
             * 只有第一次是需要根据符号是否相同来判断相加和相减
             * 之后都是根据商来判断
             * 此处 第一次 sang=0 只有 isSameSign 为 true 即符号相同时，进行相减
             * 之后 isSameSign 被置为 false
             * if 语句由 sang 决定
             */
            if (sang == 1 || isSameSign)
            {
                remain = subtract(new DataType(division),new DataType(remain)).getData_int();
                isSameSign = false;
            }
            // 中间余数和除数符号不同，相加
            // 当后面上商为 0 时，即不够减，此时无论符号是否相同都是 +Y
            else
                remain = add(new DataType(division),new DataType(remain)).getData_int();

            // 上商
            sang = remain[0] == division[0] ? 1 : 0;

            //System.out.println(i + ": " + new DataType(remain) + " " + new DataType(divided) + " " + sang);

            // 右移
            if (i != divided.length)
                // remain 的左移一定要在 divided 的前面，不然 divided[0]就变了
                LeftShift(remain,divided[0]);
            LeftShift(divided,sang);

            // 不恢复余数的除法
            //System.out.println(i + ": " + new DataType(remain) + " " + new DataType(divided) + " " + sang);
        }

        // 后续的修正

        // 商的修正
        if(division[0] != sign)
        {
            divided = add(new DataType("00000000000000000000000000000001"),
                    new DataType(divided)).getData_int();
        }
        // 余数的修正
        if(remain[0] != sign)
        {
            if(division[0] == sign)
                remain = add(new DataType(division),new DataType(remain)).getData_int();
            else
                remain = subtract(new DataType(division), new DataType(remain)).getData_int();
        }

        // 余数和商的二次修订
        // 针对整除时的问题
        // 当 被除数为负 会出现
        if(Math.abs(Integer.parseInt(tf.BinaryToDecimal(new DataType(remain).toString())))
        == Math.abs(Integer.parseInt(tf.BinaryToDecimal(new DataType(division).toString()))))
        {
            // 余数置 0
            remain = new DataType("00000000000000000000000000000000").getData_int();
            // 除数和被除数符号不同，商减1
            if(division[0] != sign)
                divided = subtract(new DataType("00000000000000000000000000000001"),
                        new DataType(divided)).getData_int();
                // 除数和被除数符号不同，商加1
            else
                divided = add(new DataType("00000000000000000000000000000001"),
                        new DataType(divided)).getData_int();
        }

        this.remain = new DataType(remain);
        return new DataType(divided);
    }

    private void LeftShift(int[] src, int addition)
    {
        for (int i = 0; i < src.length - 1; ++i)
            src[i] = src[i + 1];
        src[src.length - 1] = addition;
    }

    public static void main(String[] args)
    {
        ALU alu = new ALU();
        Transform tf = new Transform();
        System.out.println(tf.DecimalToBinary("-1435175740"));
        System.out.println(tf.DecimalToBinary("1573665112"));
        System.out.println(alu.div(new DataType(tf.DecimalToBinary("1573665112"))
                ,new DataType(tf.DecimalToBinary("-1435175740"))));
        System.out.println(alu.remain);
    }
}
