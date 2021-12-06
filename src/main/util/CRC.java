package util;

import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;

/**
 * 循环冗余校验码
 * 作业二
 * 实现通用的 CRC 计算器
 */


public class CRC {

    /**
     * CRC 循环冗余校验的思想
     * 数据(data)有 M 位
     * 左移数据 K 位 (右侧补0)
     * 并用 K + 1 位生成多项式(polynomial)除它 (模2运算)
     * 将 K 位 余数作为校验码 放在数据的最后，一同传输或存储
     *
     * 校错
     * 如果 M+K 位内容能够被生成多项式除尽，则没有检测到错误
     */

    // 左移数据 K 位
    private static char[] LeftShift(char[] data, String poly)
    {
        char[] new_data = new char[data.length + poly.length() - 1]; // M + K
        for (int i = 0; i < new_data.length; ++i)
        {
            if(i < data.length)
                new_data[i] = data[i];
            else
                new_data[i] = '0';
        }
        return new_data;
    }

    /**
     * 每一步的除法，按照异或来理解似乎没错
     * @param temp 其中一步的被除数
     * @param poly 多项式除数
     * @return 余数
     */
    private static char[] XOR(char[] temp, char[] poly)
    {
        char[] remain = new char[temp.length - 1];
        // 被除数首位为 0
        // 和 "00...00" 异或
        if(temp[0] == '0'){
            for (int i = 0; i < remain.length; ++i)
                remain[i] = temp[i+1];
        }
        // 被除数首位不为 0
        // 和 poly 异或
        else {
            for (int i = 1; i < temp.length; ++i)
            {
                remain[i - 1] = temp[i] == poly[i] ? '0' : '1';
            }
        }
        return remain;
    }

    /**
     * 这个函数才是关键的地方
     * 这个除法非常特殊，此处也仅仅是根据对PPT的理解断定的，可能存在问题，但是能过oj
     * @param data 待除的 补了0的 M + K 位数据
     * @param poly 生成多项式
     * @return K 位余数
     */
    private static char[] divide(char[] data, String poly)
    {
        char[] p = poly.toCharArray(); // poly 的数组表示
        char[] remain; // 余数
        char[] temp = new char[poly.length()]; // 每次做除法的那部分

        int ptr = 0; //ptr 为指向 data 的下一个元素
        // 初始化 temp
        for (; ptr < temp.length; ++ptr)
            temp[ptr] = data[ptr];

        ptr--; // ptr-- 是为了下面的循环能成一体
        while (true){
            remain = XOR(temp,p); // 每次的余数
            ptr++;
            // 全部除完，退出循环
            // 不要放在 while 中
            // 因为 ptr == data.length 时 data[ptr] 会报错
            if(ptr == data.length) break;
            // 余数加上 data 的下一位就是 下一轮做除法的 被除数
            for (int i = 0; i < remain.length; ++i)
                temp[i] = remain[i];
            temp[temp.length - 1] = data[ptr];
        }

        return remain;
    }


    /**
     *
     * @param data 数据
     * @param polynomial 多项式
     * @return 校验码
     */
    public static char[] Calculate(char[] data, String polynomial){
        char[] new_data = LeftShift(data,polynomial);
        return divide(new_data,polynomial);
    }

    /**
     *
     * @param data 数据
     * @param polynomial 多项式
     * @param CheckCode 校验码
     * @return 余数
     */
    public static char[] Check(char[] data, String polynomial, char[] CheckCode)
    {
        // 将校验码连到数据最后端
        char[] new_data = new char[data.length + CheckCode.length];
        for(int i = 0; i < new_data.length; ++i)
        {
            if(i < data.length)
                new_data[i] = data[i];
            else
                new_data[i] = CheckCode[i - data.length];
        }

        return divide(new_data,polynomial);
    }
}
