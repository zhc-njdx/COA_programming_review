package cpu.fpu;
import cpu.alu.ALU;
import util.DataType;

/**
 * FPU
 * 作业四
 * 实现浮点数的加减
 * 作业五
 * 实现浮点数的乘除
 */

import util.IEEE754Float;
import util.Transform;
import util.TransformHelper;

import java.util.Arrays;

public class FPU {

    private final String[][] addCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_INF, IEEE754Float.NaN}
    };

    private final String[][] subCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_INF, IEEE754Float.NaN}
    };

    private final String[][] mulCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.P_ZERO, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_ZERO, IEEE754Float.NaN}
    };

    private final String[][] divCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
    };

    private boolean isSub;
    Transform tf = new Transform();
    TransformHelper tfh = new TransformHelper();
    ALU alu = new ALU();


    /**
     * 浮点数相加
     * dest + src
     * @param src  加数
     * @param dest 加数
     * @return 和
     */
    public DataType add(DataType src, DataType dest)
    {
        isSub = false;
        return addMachine(src,dest);
    }

    /**
     * 浮点数相减
     * dest - src
     * @param src  减数
     * @param dest 被减数
     * @return 差
     */
    public DataType sub(DataType src, DataType dest)
    {
        isSub = true;
        return addMachine(src,dest);
    }

    /**
     * 浮点数的加减是真地复杂，乱
     *
     * 步骤：
     * 1、边界检查
     * 2、如果其中一个为0，直接返回另一个
     * 3、找到阶码的更大的浮点数maxFloat 和阶码更小的浮点数 minFloat 并抽取它们的符号、阶码和尾数
     * 4、对阶操作，小阶码向大阶码对齐。  如果minFloat的阶码太小，导致其小数部分全为0，直接返回maxFloat
     * 5、小数的加减。
     *      加法：相加后隐藏位可能有进位，小数点前可能有2位，此时需要右移，阶码加1。阶码加1，判断阶码是否会溢出。
     *      减法：相减后有可能结果是负数(由于调用时使用maxFloat-minFloat，按理是不会出现负数的。只有一种情况---对了，它们的阶码相同的时候，然后maxFloat的小数部分更小)
     *           这个时候就要进行取反加一，并改变最终结果的符号。
     *           相减后如果小数全为0了，那就直接返回0。
     * 6、进行结果的规格化或者非规格化处理
     *      如果小数隐藏位为0，且阶码为00000001--->非规格化数
     *      如果小数隐藏位为0，且阶码大于00000001
     *          小数左移，阶码减1
     *      (如果是非规格化数，要把阶码变成00000000)
     *
     */
    private DataType addMachine(DataType srcStr, DataType destStr)
    {
        // 转换成字符串
        String src = srcStr.toString();
        String dest = destStr.toString();

        // 边界检查
        // 检查 0 INF
        String res = cornerCheck(isSub ? subCorner : addCorner,src,dest);
        if (res != null)
            return new DataType(res);
        // 检查 NaN
        if (src.matches(IEEE754Float.NaN_Regular) || dest.matches(IEEE754Float.NaN_Regular))
            return new DataType(IEEE754Float.NaN);

        // 判断加减 改变符号
        if (isSub){
            // 符号改变了，src 要更新
            srcStr.setBit(0,srcStr.getBit(0) == '1' ? '0' : '1');
            src = srcStr.toString();
        }

        // 如果其中有一个为0
        if (src.equals(IEEE754Float.N_ZERO) || src.equals(IEEE754Float.P_ZERO))
            return new DataType(dest);
        if (dest.equals(IEEE754Float.P_ZERO) || dest.equals(IEEE754Float.N_ZERO))
            return new DataType(src);

        // 找到更大阶码的maxFloat
        // 避免了后面讨论
        String maxFloat,minFloat;
        if (Integer.parseInt(src.substring(1,9),2) > Integer.parseInt(dest.substring(1,9),2)){
            maxFloat = src;
            minFloat = dest;
        }
        else {
            maxFloat = dest;
            minFloat = src;
        }

        // 抽取出各个项
        char max_sign = maxFloat.charAt(0); // 符号
        char min_sign = minFloat.charAt(0);
        String max_expo = maxFloat.substring(1,9); // 阶码
        String min_expo = minFloat.substring(1,9);
        // 尾数的抽取需要考虑 阶码 和 GRS
        String max_significant = max_expo.equals("00000000") ? "0" : "1";
        String min_significant = min_expo.equals("00000000") ? "0" : "1";
        max_significant += maxFloat.substring(9) + "000";
        min_significant += minFloat.substring(9) + "000";

        // 小trick
        // 因为后期要减去 127
        // 对于非规格化数
        if (max_expo.equals("00000000"))
            max_expo = "00000001";
        if (min_expo.equals("00000000"))
            min_expo = "00000001";

        // 对阶
        int distance = Integer.parseInt(max_expo,2) - Integer.parseInt(min_expo,2);  // 尾数需要右移的次数
        min_significant = rightShift(min_significant,distance);
        // 如果src太小，右移操作可能使其变为0
        if (min_significant.equals("000000000000000000000000000"))
            return new DataType(maxFloat);

        char res_sign = max_sign;
        String res_expo = max_expo;
        String res_significant;
        if (max_sign == min_sign) {
            res_significant = alu.add(new DataType("00000"+min_significant),
                    new DataType("00000"+max_significant)).toString();
            // 加法后可能溢出，要判断
            if (res_significant.charAt(4) == '1')
            {
                res_significant = rightShift(res_significant,1).substring(5); // 尾数右移，并取后27位
                res_expo = oneAdder(res_expo).substring(1); // 阶码加一
                // 阶码溢出
                if (res_expo.equals("11111111"))
                    return new DataType(max_sign == '1' ? IEEE754Float.N_INF : IEEE754Float.P_INF);
            }
            else
                res_significant = res_significant.substring(5);
        }
        else {
            res_significant = alu.subtract(new DataType("00000"+min_significant),
                    new DataType("00000"+max_significant)).toString();
            // 当src和dest阶码相同的时候，会出现结果尾数为负的情况
            if (res_significant.charAt(0) == '1')
            {
                res_significant = tf.NegationAddOne(res_significant).substring(5);
                res_sign = res_sign == '1' ? '0' : '1';
            }
            else
                res_significant = res_significant.substring(5);
            // 减法尾数不会出现溢出，但可能为0
            if (res_significant.equals("000000000000000000000000000"))
                return new DataType(IEEE754Float.P_ZERO);
        }

        // 规格化
        if (res_significant.charAt(0) == '0')
        {
            // 如果阶码是 00000001 （因为之前在提取阶码的时候对0000000的阶码加过一）
            // 或者直接用值来理解，00000001 代表 -126 由于尾数首位是0，所以是非规格化数
            // 非规格化数
            if (res_expo.equals("00000001")){
                res_expo = "00000000";
            }
            else {
                // 退出条件是 尾数首位变成1 或者 ***阶码变成了 00000001***
                while (res_significant.charAt(0) == '0' && !res_expo.equals("00000001")){
//                    System.out.println(1);
                    res_significant = leftShift(res_significant,1); // 尾数左移
                    // 阶码减一
                    res_expo = alu.subtract(new DataType("00000000000000000000000000000001"),
                            new DataType("000000000000000000000000" + res_expo)).toString().substring(24);
                }
                // 如果尾数首位还是0，非规格化数，阶码要变回00000000
                if (res_significant.charAt(0) == '0')
                    res_expo = "00000000";
            }
        }

        return new DataType(round(res_sign,res_expo,res_significant));
    }

    private String leftShift(String str,int times)
    {
        StringBuilder tmp = new StringBuilder(str);
        for (int i = 0; i < times; ++i)
            tmp.append("0");
        return tmp.substring(times);
    }

    /**
     * 浮点数的乘除
     * 1、阶码的运算比较简单。加减即可
     * 2、尾数的运算比较复杂。原码的乘除(不是补码！！！)
     *
     * 步骤:
     * 1、边界检查。
     * 2、判断是否一个为0，可以直接返回0
     * 3、提取符号、阶码、尾数
     * 4、对阶
     *      注意：
     *      如果是 00000000 要加一，其阶码的真实值是-126
     * 5、小数乘法
     *      这个小数乘法主要就是原码的乘法:
     *      1)初始化部分积为0
     *      2)每次取src最后一位，如果是0就直接右移，如果是1先部分积先加上dest再右移
     *      3)对于右移，是将部分积和src当作一个整体来进行右移的，所以src的第0位应该是部分积的最后一位。而部分积的第0位是carry位
     *      4)这里的carry位一定要记得置0!!!不然会debug一天的。
     * 6、处理一些边界情况
     *      ***阶码为1，如果小数隐藏位为0，即为非规格化数；隐藏位为1，即为规格化数；如果前27位全为0，则返回0。
     *      1)阶码大于1的，且小数隐藏位为0
     *          阶码减1;
     *          小数左移;
     *      2)阶码小于1的，且小数前27位不全为0的
     *          阶码加1;
     *          小数右移;
     *      ***记住如果是非规格化数，要把阶码变成0!!!
     */

    /**
     * 浮点数乘法
     * dest * src
     * @param src 乘数
     * @param dest 被乘数
     * @return 积
     *
     */
    public DataType mul(DataType src, DataType dest)
    {
        String srcStr = src.toString();
        String destStr = dest.toString();

        // 边界检查
        String res = cornerCheck(mulCorner,srcStr,destStr);
        if (res != null)
            return new DataType(res);
        if (srcStr.matches(IEEE754Float.NaN_Regular) || destStr.matches(IEEE754Float.NaN_Regular))
            return new DataType(IEEE754Float.NaN);

        // 结果的符号
        int res_sign = (srcStr.charAt(0) - '0') ^ (destStr.charAt(0) - '0');

        // 乘数中是否有一个为0
        if (srcStr.substring(1).equals(IEEE754Float.N_ZERO.substring(1)) || destStr.substring(1).equals(IEEE754Float.N_ZERO.substring(1)))
            return new DataType(res_sign == 0 ? IEEE754Float.P_ZERO : IEEE754Float.N_ZERO);

        // 提取阶码和尾数
        String src_expo = srcStr.substring(1,9);
        String dest_expo = destStr.substring(1,9);

        String src_significant = src_expo.equals("00000000") ? "0" : "1";
        String dest_significant = dest_expo.equals("00000000") ? "0" : "1";
        src_significant += srcStr.substring(9) + "000";
        dest_significant += destStr.substring(9) + "000";

        // 进行阶码的计算
        int src_expo_value = src_expo.equals("00000000") ? -126 : Integer.parseInt(src_expo,2) - 127;
        int dest_expo_value = dest_expo.equals("00000000") ? -126 : Integer.parseInt(dest_expo,2) - 127;
        int res_expo_value = src_expo_value + dest_expo_value + 127; //暂时得到的阶码值

        // 尾数相乘(原码乘法)
        String res_significant = significant_mul(src_significant,dest_significant);
        // 得到54位小数，小数点前有2位，所以先要尾数右移，阶码加一
        res_expo_value++;
        //直接阶码加1就行了
        //res_significant = rightShift(res_significant,1);

        // 对结果进行处理
        // 尾数和阶码都很小 为0
        if (res_significant.startsWith("000000000000000000000000000") && res_expo_value <= 1)
            return new DataType(res_sign == 1 ? IEEE754Float.N_ZERO : IEEE754Float.P_ZERO);
        //!!!阶码为1时，不用处理!!!隐藏位为1就是规格化数，隐藏位为0就是非规格化数
        //1、阶码大于1且尾数隐藏位为0,两种结果:
        // a、成功变成规格化数  b、成为非规格化数
        if (res_significant.charAt(0) == '0' && res_expo_value > 1) {

            while (res_significant.charAt(0) == '0' && res_expo_value > 1) {
                res_expo_value--;
                res_significant = leftShift(res_significant, 1);
            }
            if (res_significant.charAt(0) == '0') {
                if (res_significant.startsWith("000000000000000000000000000"))
                    return new DataType(res_sign == 1 ? IEEE754Float.N_ZERO : IEEE754Float.P_ZERO);
                res_expo_value = 0;
            } else if (res_expo_value >= 255)
                return new DataType(res_sign == 1 ? IEEE754Float.N_INF : IEEE754Float.P_INF);

        }
        //2、尾数前27位不全为0且阶码小于0,两种结果:
        // a、成功变成非规格化数  b、成为0
        else if (!res_significant.startsWith("000000000000000000000000000") && res_expo_value < 1) {
            while (!res_significant.startsWith("000000000000000000000000000") && res_expo_value < 1) {
                res_expo_value++;
                res_significant = rightShift(res_significant, 1);
            }
            if (res_significant.startsWith("000000000000000000000000000"))
                return new DataType(res_sign == 1 ? IEEE754Float.N_ZERO : IEEE754Float.P_ZERO);
            else
                res_expo_value = 0;
        }

        if (res_expo_value == 1 && res_significant.charAt(0) == '0')
            res_expo_value = 0;

        String res_expo = tf.DecimalToBinary(res_expo_value+"").substring(24);

        return new DataType(round1((char) (res_sign+'0'),res_expo,res_significant));
    }

    private String significant_mul(String src, String dest)
    {
        int len = src.length();

        int[] src1 = tfh.StringToIntArray(src);
        int[] dest1 = tfh.StringToIntArray(dest);

        int[] res = new int[len];
        Arrays.fill(res,0);

        int carry;
        for (int i = len - 1; i >= 0; --i)
        {
            /**
             * 一开始直接让 res[0] = tfh.cf
             * 但是如果前一次src1[len - 1] = 1 进行了运算 然后 tfh.cf = 1
             * 下一次 src1[len - 1] = 0 不进行运算 tfh.cf 就不会更新还是 1
             * 所以每次要置0
             */
            carry = 0;
            if (src1[len - 1] == 1){
                res = tfh.addIntArray(res,dest1,0);  // +X
                carry = tfh.cf;
            }
            // 右移
            int resLast = res[len - 1];
            for (int j = len - 1; j > 0; --j)
            {
                res[j] = res[j-1];
                src1[j] = src1[j-1];
            }
            // 又是这里出问题,前面加的是carry,不是直接加0
            res[0] = carry;
            src1[0] = resLast;
        }
        return tfh.IntArrayToString(res) + tfh.IntArrayToString(src1);
    }

    /**
     * 浮点数的除法
     * dest / src
     * @param src 除数
     * @param dest 被除数
     * @return 商
     */
    public DataType div(DataType src, DataType dest)
    {
        return null;
    }

    /**
     * 下面是tfgg的代码
     *=============================================================================================
     */

    /**
     * 进行边界检查
     */

    private String cornerCheck(String[][] cornerMatrix,String oprA, String oprB) {
        for (String[] matrix : cornerMatrix) {
            if (oprA.equals(matrix[0]) && oprB.equals(matrix[1])) {
                return matrix[2];
            }
        }
        return null;
    }

    /**
     * right shift a num without considering its sign using its string format
     *
     * @param operand to be moved
     * @param n       moving nums of bits
     * @return after moving
     */
    private String rightShift(String operand, int n) {
        StringBuilder result = new StringBuilder(operand);  //保证位数不变
        boolean sticky = false;
        for (int i = 0; i < n; i++) {
            sticky = sticky || result.toString().endsWith("1");
            result.insert(0, "0");
            result.deleteCharAt(result.length() - 1);
        }
        if (sticky) {
            result.replace(operand.length() - 1, operand.length(), "1");
        }
        return result.substring(0, operand.length());
    }

    /**
     * 针对加减法
     * 对GRS保护位进行舍入
     *
     * @param sign    符号位
     * @param exp     阶码
     * @param sig_grs 带隐藏位和保护位的尾数
     * @return 舍入后的结果
     */
    private String round(char sign, String exp, String sig_grs) {
        int grs = Integer.parseInt(sig_grs.substring(24), 2);
        String sig = sig_grs.substring(0, 24);
        if (grs > 4) {
            sig = oneAdder(sig);
        } else if (grs == 4 && sig.endsWith("1")) {
            sig = oneAdder(sig);
        }

        if (Integer.parseInt(sig.substring(0, sig.length() - 23), 2) > 1) {
            sig = rightShift(sig, 1);
            exp = oneAdder(exp).substring(1);
        }
        return sign + exp + sig.substring(sig.length() - 23);
    }

    /**
     * 针对乘除法
     * 对GRS保护位进行舍入
     *
     * @param sign    符号位
     * @param exp     阶码
     * @param sig_grs 带隐藏位和保护位的尾数
     * @return 舍入后的结果
     */
    private String round1(char sign, String exp, String sig_grs) {
        int grs = Integer.parseInt(sig_grs.substring(24, 27), 2);
        if ((sig_grs.substring(27).contains("1")) && (grs % 2 == 0)) {
            grs++;
        }
        String sig = sig_grs.substring(0, 24); // 隐藏位+23位
        if (grs > 4) {
            sig = oneAdder(sig);
        } else if (grs == 4 && sig.endsWith("1")) {
            sig = oneAdder(sig);
        }

        if (Integer.parseInt(sig.substring(0, sig.length() - 23), 2) > 1) {
            sig = rightShift(sig, 1);
            exp = oneAdder(exp).substring(1);
        }
        if (exp.equals("11111111")) {
            return IEEE754Float.P_INF;
        }

        return sign + exp + sig.substring(sig.length() - 23);
    }

    /**
     * add one to the operand
     *
     * @param operand the operand
     * @return result after adding, the first position means overflow (not equal to the carry to the next) and the remains means the result
     */
    private String oneAdder(String operand) {
        int len = operand.length();
        StringBuilder temp = new StringBuilder(operand);
        temp.reverse();
        int[] num = new int[len];
        for (int i = 0; i < len; i++) num[i] = temp.charAt(i) - '0';  //先转化为反转后对应的int数组
        int bit = 0x0;
        int carry = 0x1;
        char[] res = new char[len];
        for (int i = 0; i < len; i++) {
            bit = num[i] ^ carry;
            carry = num[i] & carry;
            res[i] = (char) ('0' + bit);  //显示转化为char
        }
        String result = new StringBuffer(new String(res)).reverse().toString();
        return "" + (result.charAt(0) == operand.charAt(0) ? '0' : '1') + result;  //注意有进位不等于溢出，溢出要另外判断
    }
}
