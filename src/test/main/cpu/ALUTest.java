package cpu;

import cpu.alu.ALU;
import org.junit.Assert;
import org.junit.Test;
import util.DataType;
import util.Transform;

import java.security.PublicKey;
import java.util.Random;

public class ALUTest {

    ALU alu = new ALU();
    Random random = new Random();
    Transform tf = new Transform();

    @Test
    /**
     * 测试加法
     */
    public void Test1()
    {
        for (int i = 0; i < 100; ++i)
        {
            int src = random.nextInt();
            int dst = random.nextInt();
            int sum = src + dst;

            String res = alu.add(new DataType(tf.DecimalToBinary(src+"")),
                    new DataType(tf.DecimalToBinary(dst+""))).toString();

            Assert.assertEquals(sum+"",tf.BinaryToDecimal(res));
        }
        System.out.println("PASS!");
    }

    @Test
    /**
     * 测试减法
     */
    public void Test2()
    {
        for (int i = 0; i < 100; ++i)
        {
            int src = random.nextInt();
            int dst = random.nextInt();
            int sum = dst - src;

            String res = alu.subtract(new DataType(tf.DecimalToBinary(src+"")),
                    new DataType(tf.DecimalToBinary(dst+""))).toString();

            Assert.assertEquals(sum+"",tf.BinaryToDecimal(res));
        }
        System.out.println("PASS!");
    }

    @Test
    /**
     * 测试乘法
     * 测试1: 两个除数都写成了 src
     *
     */
    public void Test3()
    {
        for (int i = 0; i < 100; ++i)
        {
            int src = random.nextInt();
            int dst = random.nextInt();
            int p = dst * src;

            String res = alu.mul(new DataType(tf.DecimalToBinary(src+"")),
                    new DataType(tf.DecimalToBinary(dst+""))).toString();

            Assert.assertEquals(tf.DecimalToBinary(p+""),res);
        }
        System.out.println("PASS!");
    }

    @Test
    /**
     * 测试除法
     * 测试1: 对于某些数，被除数小于除数时，出现错误
     *      发现是 算法出现了错误
     *      真正应该是 第一次是 同号相见，异号相加
     *      之后就是 根据商（除数和余数是否同号） 进行 2 * Ri - Y 或 2 * Ri + Y
     *      后面就没有 同号相减或异号相加
     *      之所以只有少部分出现错误，而大部分都能通过
     *      是因为 商为1时，除数和余数符号大部分情况是相同的，即进行相减
     */
    public void Test4()
    {
        for (int i = 0; i < 100; ++i)
        {
            int dst = random.nextInt();
            int src = random.nextInt();
            int p = dst / src;
            int r = dst % src;
            String res = alu.div(new DataType(tf.DecimalToBinary(src+"")),
                    new DataType(tf.DecimalToBinary(dst+""))).toString();

            Assert.assertEquals(tf.DecimalToBinary(p+""),res);
            Assert.assertEquals(tf.DecimalToBinary(r+""),alu.remain.toString());
        }
        System.out.println("PASS!");
    }

}
