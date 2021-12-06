package util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

public class TransformTest {

    Transform tf = new Transform();
    Random random = new Random();

    @Test
    /**
     * Test1
     * 测试 二进制补码 和 十进制整数 的 转换
     */
    //测试失败1: 负数没有考虑
    //测试失败2: 没有考虑位数
    public void Test1()
    {
        for (int i = 1; i <= 100; ++i)
        {
            int num = random.nextInt();
            String res = tf.BinaryToDecimal(tf.DecimalToBinary(num+""));
            Assert.assertEquals((num + ""), res);
            if(i == 100) System.out.println("PASS!");
        }
    }

    @Test
    /**
     * Test2
     * 测试 浮点数 和二进制数 的 转换
     */
    public void Test2()
    {
        for (int i = 1; i <= 100; ++i)
        {
            float value = random.nextFloat();
            String res = tf.BinaryToFloat(tf.FloatToBinary(value+""));
            float ans = Float.parseFloat(res);
            Assert.assertEquals(value,ans,Math.pow(2,-23));
            if(i == 100)
            {
                res = tf.BinaryToFloat("11111111100000000000000000000000");
                Assert.assertEquals("-INF",res);
                res = tf.BinaryToFloat("01111111100000000000000000000000");
                Assert.assertEquals("+INF",res);
                res = tf.BinaryToFloat("11111111111111111111111100001011");
                Assert.assertEquals("NaN",res);

                res = tf.FloatToBinary("1.21e40");
                Assert.assertEquals("01111111100000000000000000000000",res);
                res = tf.FloatToBinary("0.123e-40");
                Assert.assertEquals("00000000000000000000000000000000",res);

                System.out.println("PASS!");
            }
        }
    }

    @Test
    /**
     *
     */
    // 第一次测试错误: java.lang.StringIndexOutOfBoundsException: begin 28, end 32, length 28
    // 原因是 DecimalToNBCD 函数中 没有限制长度
    public void Test3()
    {
        for(int i = 1; i <= 100; ++i)
        {
            int num = random.nextInt(9999999);
            String res = tf.NBCDToDecimal(tf.DecimalToNBCD(num+""));
            Assert.assertEquals(num+"",res);
            if(i == 100)
            {
                System.out.println("PASS!");
            }
        }
    }
}
