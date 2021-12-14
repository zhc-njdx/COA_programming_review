package util;

import javax.management.ListenerNotFoundException;

/**
 * Transform类
 * 完成数据类型的转化,主要有
 * 作业一
 * 二进制整数（补码）   和  十进制数
 * 二进制浮点数 和  十进制浮点数  IEEE 754
 * NBCD码     和  十进制数
 */

public class Transform {

    private static final int length = 32; // 32位

    /**
     * Function 1
     * Binary Integer(补码) To Decimal Integer
     */
    public String BinaryToDecimal(String BinaryInt)
    {
        // 先判断符号
        // 看首位是否未为 1
        boolean isNeg = BinaryInt.charAt(0) == '1';
        // 如果是负数，先进行取反加一
        if(isNeg) BinaryInt = NegationAddOne(BinaryInt);
        int len = BinaryInt.length();
        int res = 0;
//        for (int i = len - 1; i >= 0; --i) 每一步加都会对之前乘2，所以应该从0开始
        for (int i = 0; i < len; ++i)
        {
            res = res * 2 + BinaryInt.charAt(i) - '0';
        }
        // 如果是负数，再把符号还回去
        if(isNeg) res = -1 * res;
        return res+"";
    }

    /**
     * 取反加一
     */
    public String NegationAddOne(String BinaryInt)
    {
        return AddOne(Negation(BinaryInt));
    }

    /**
     * 取反
     */
    private String Negation(String BinaryInt)
    {
        StringBuffer negated = new StringBuffer();
        for (int i = 0; i < BinaryInt.length(); ++i)
        {
            negated.append(BinaryInt.charAt(i) == '0' ? '1' : '0');
        }
        return negated.toString();
    }

    /**
     * 加一
     */
    private String AddOne(String BinaryInt)
    {
        StringBuffer res = new StringBuffer();
        int carry = 1;
        for (int i = BinaryInt.length() - 1; i >= 0; --i)
        {
            int num = BinaryInt.charAt(i) - '0' ^ carry;
            carry = BinaryInt.charAt(i) & carry;
            res.append(num);
        }
        return res.reverse().toString();
    }


    /**
     * Function 2
     * Decimal Integer To Binary Integer(补码)
     */
    public String DecimalToBinary(String DecimalInt)
    {
        int num = Integer.parseInt(DecimalInt);
        // 判断符号
        boolean isNeg = num < 0;
        if(isNeg) num = Math.abs(num);
        StringBuffer BinaryInt = new StringBuffer();
        while (num != 0)
        {
            BinaryInt.append(num % 2);
            num /= 2;
        }
        // 看位数是否够 length
        // 不够再添0
        while (BinaryInt.length() < length) BinaryInt.append("0");
        String res = BinaryInt.reverse().toString();
        // 如果超出32位范围就 取低32位
        if (res.length() > length)
            res = res.substring(res.length()-length);
        // 如果是负数，还需要取反加一
        return isNeg ? NegationAddOne(res) : res;
    }

    /**=================================================================================================================

    /**
     * 浮点数 转 二进制数
     * IEEE 规范
     */
    public String FloatToBinary(String floatStr)
    {
        // 转换为 double 数值
        double value = Double.parseDouble(floatStr);

        // 记录符号
        boolean isNeg = value < 0;
        // 负数需要先取绝对值
        if(isNeg) value = Math.abs(value);

        // 计算浮点数的阶码是多少
        // 得出的 小数部分为 1.******
        int expo = 0;
        if(value < 1)
        {
            while (value < 1)
            {
                value *= 2;
                expo--;
            }
        }
        else
        {
            while (value >= 2)
            {
                value /= 2;
                expo++;
            }
        }

        // 当阶码太大或太小
        expo += 127;
        // 阶码太小 为 0
        if (expo < 0) return isNeg ? "10000000000000000000000000000000" : "00000000000000000000000000000000";
        // 阶码太大 为 无穷
        if (expo >= 255) return isNeg ? "11111111100000000000000000000000" : "01111111100000000000000000000000";
        // NaN 是不存在的，因为它已经是表示出来的浮点数了

        // 把 value 转成对应的二进制码
        String significant = significantToBinary(value + "",24);
        String exponent = BinaryForm(expo + "",8);

        // 非规格化数
        if(exponent.equals("00000000"))
        {
            String val = exponent + significant.substring(0,23);
            return isNeg ? "1" + val : "0" + val;
        }

        // 规格化数
        else
        {
            String val = exponent + significant.substring(1);
            return isNeg ? "1" + val : "0" + val;
        }

    }

    /**
     *
     * @param valueStr 浮点数 1.**** / 0.****
     * @param len 需要转化的二进制的长度
     * @return 给定长度的形如 1**** 或 0**** 带小数点前一位的 二进制串
     *
     */
    private String significantToBinary(String valueStr,int len)
    {
        StringBuffer significant = new StringBuffer();
        double value = Double.parseDouble(valueStr);
        // 小数点前的一位
        if(value >= 1){
            significant.append("1");
            value -= 1;
        }
        else
            significant.append("0");
        // 由于小数可能是不能完全转化为二进制串的
        // 我们只需要取到我们需要长度即可
        // 例如 IEEE 754 32位需要23位尾数，所以这里我们会传 len = 24 (加上首位)
        while (significant.length() < len)
        {
            value *= 2;
            significant.append(value >= 1 ? "1" : "0");
            if(value >= 1) value -= 1;
        }
        return significant.toString();
    }

    /**
     * 将 整数 转换成 指定长度的 二进制原码
     */
    private String BinaryForm(String valueStr, int len)
    {
        int value = Integer.parseInt(valueStr);
        StringBuffer binary = new StringBuffer();
        while (value != 0)
        {
            binary.append(value % 2);
            value /= 2;
        }
        while (binary.length() < len)
            binary.append("0");
        return binary.reverse().toString();
    }



    /**
     * 二进制数 转 浮点数
     * IEEE 规范
     * 1 11111111 11111111111111111111111
     */
    public String BinaryToFloat(String binaryStr)
    {
        char sign = binaryStr.charAt(0);
        String exponent = binaryStr.substring(1,9);
        String significant = binaryStr.substring(9);

        // 阶码全1 无穷或者是非数
        if(exponent.equals("11111111"))
        {
            if(significant.equals("00000000000000000000000"))
            {
                return sign == '1' ? "-INF" : "+INF";
            }
            else
                return "NaN";
        }
        // 阶码全0 非规格化小数
        else if (exponent.equals("00000000"))
        {
            float significant_value = BinaryToSignificant(significant,'0');
            significant_value *= Math.pow(2,-126);
            return sign == '1' ? "-" + significant_value : significant_value + "";
        }
        // 规格化数
        int expo_value = binaryValue(exponent) - 127;
        float significant_value = BinaryToSignificant(significant,'1');
        double float_value = significant_value * Math.pow(2,expo_value);
        return sign == '1' ? "-" + float_value : float_value + "";
    }

    /**
     *
     * @param binaryStr 二进制数（原码）
     * @return 对应十进制数
     */
    private int binaryValue(String binaryStr)
    {
        int value = 0;
        for(int i = 0; i < binaryStr.length(); ++i)
        {
            value = value * 2 + binaryStr.charAt(i)- '0';
        }
        return value;
    }

    private float BinaryToSignificant(String Binary, char start)
    {
        float value = start - '0';
        for (int i = 0; i < Binary.length(); ++i)
        {
            value += (Binary.charAt(i) - '0') * Math.pow(2,-1*(i+1));
        }
        return value;
    }

    /**=================================================================================================================

     /* *
     *
     *
     */
    public String NBCDToDecimal(String nbcdStr)
    {
        // 符号
        boolean isNeg = nbcdStr.substring(0,4).equals("1101");
//        StringBuffer DecimalStr = new StringBuffer(); 用字符串存在一个问题，就是当NBCD码有一些0000时，DecimalStr中会有前缀0
        int value = 0;
        for (int i = 1; i < 8; ++i)
        {
            value = value * 10 + binaryValue(nbcdStr.substring(i*4,(i+1)*4));
        }
        return isNeg ? "-" + value : value + "";
    }

    /**
     *
     */
    // 要转成32位
    public String DecimalToNBCD(String DecimalStr)
    {
        boolean isNeg = DecimalStr.charAt(0) == '-';
        if(isNeg) DecimalStr = DecimalStr.substring(1);

        StringBuffer nbcdStr = new StringBuffer();
        nbcdStr.append(isNeg ? "1101" : "1100");

        // 转换成 length 长度的nbcd码
        while (nbcdStr.length() < length - DecimalStr.length() * 4)
            nbcdStr.append("0000");

        for (int i = 0; i < DecimalStr.length(); ++i)
            nbcdStr.append(BinaryForm(DecimalStr.charAt(i)+"",4));
        return nbcdStr.toString();
    }


    public int originCodeValue(String binaryString)
    {
        return binaryValue(binaryString);
    }


    public static void main(String[] args)
    {
        Transform tf = new Transform();
        System.out.println(tf.BinaryToDecimal("10000000000000000000000000000000"));
    }
}
