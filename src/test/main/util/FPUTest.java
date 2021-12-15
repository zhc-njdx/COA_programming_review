package util;

import cpu.fpu.FPU;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FPUTest {

    private final FPU fpu = new FPU();
    private final Transform tf = new Transform();
    private DataType src;
    private DataType dest;
    private DataType result;

    /**
     * 测试浮点数加法
     */
    @Test
    public void fpuAddTest1() {
        src = new DataType("00111111101000000000000000000000");
        dest = new DataType("01000000010001100110011001100110");
        result = fpu.add(src, dest);
        assertEquals("01000000100010110011001100110011", result.toString());
    }

    @Test
    public void fpuAddTest2() {
        String deNorm1 = "00000000000000000000000000000001";
        String deNorm2 = "00000000000000000000000000000010";
        String deNorm3 = "10000000010000000000000000000000";
        String small1 = "00000000100000000000000000000000";
        String small2 = "00000000100000000000000000000001";
        String big1 = "01111111000000000000000000000001";
        String big2 = "11111111000000000000000000000001";
        String[] strings = {deNorm1, deNorm2, deNorm3, small1, small2, big1, big2};
        double[] doubles = {10000000, 1.2, 1.1, 1, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, -0.1, -0.2, -0.3, -0.4, -0.5, -0.6, -0.7, -0.8, -0.9, -1, -10000000};

        float[] input = new float[strings.length + doubles.length];
        for (int i = 0; i < strings.length; i++) {
            input[i] = Float.parseFloat(tf.BinaryToFloat(strings[i]));
        }
        for (int i = 0; i < doubles.length; i++) {
            input[i + strings.length] = (float) doubles[i];
        }

        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input.length; j++) {
                src = new DataType(tf.DecimalToBinary(Integer.toString(Float.floatToIntBits(input[i]))));
                dest = new DataType(tf.DecimalToBinary(Integer.toString(Float.floatToIntBits(input[j]))));
//                System.out.println(src);
//                System.out.println(dest);
                System.out.println();
                result = fpu.add(src, dest);
                assertEquals(tf.DecimalToBinary(Integer.toString(Float.floatToIntBits(input[i] + input[j]))), result.toString());
            }
        }
    }

    /**
     * 测试浮点数减法
     */
    @Test
    public void fpuSubTest1(){
        src = new DataType( "11000001110101100000000000000000");
        dest = new DataType("01000000111011011100000000000000");
        result = fpu.sub(src, dest);
        assertEquals("01000010000010001011100000000000", result.toString());
    }

    @Test
    public void fpuSubTest2() {
        String deNorm1 = "00000000000000000000000000000001";
        String deNorm2 = "00000000000000000000000000000010";
        String deNorm3 = "10000000010000000000000000000000";
        String small1 = "00000000100000000000000000000000";
        String small2 = "00000000100000000000000000000001";
        String big1 = "01111111000000000000000000000001";
        String big2 = "11111111000000000000000000000001";
        String[] strings = {deNorm1, deNorm2, deNorm3, small1, small2, big1, big2};
        double[] doubles = {10000000, 1.2, 1.1, 1, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, -0.1, -0.2, -0.3, -0.4, -0.5, -0.6, -0.7, -0.8, -0.9, -1, -10000000};

        float[] input = new float[strings.length + doubles.length];
        for (int i = 0; i < strings.length; i++) {
            input[i] = Float.parseFloat(tf.BinaryToFloat(strings[i]));
        }
        for (int i = 0; i < doubles.length; i++) {
            input[i + strings.length] = (float) doubles[i];
        }

        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input.length; j++) {
                src = new DataType(tf.DecimalToBinary(Integer.toString(Float.floatToIntBits(input[i]))));
                dest = new DataType(tf.DecimalToBinary(Integer.toString(Float.floatToIntBits(input[j]))));
                result = fpu.sub(src, dest);
                assertEquals(tf.DecimalToBinary(Integer.toString(Float.floatToIntBits(input[j] - input[i]))), result.toString());
            }
        }
    }


    /**
     * 测试浮点数乘法
     */
    @Test
    public void fpuMulTest1(){
        src = new DataType(tf.FloatToBinary( "0.25" ));
        dest = new DataType(tf.FloatToBinary( "4" ));
        result = fpu.mul(src, dest);
        assertEquals( tf.FloatToBinary( "1.0" ), result.toString() );
    }

    @Test
    public void fpuMulTest2() {
        String deNorm1 = "00000000000000000000000000000001";
        String deNorm2 = "00000000000000000000000000000010";
        String deNorm3 = "10000000010000000000000000000000";
        String small1 = "00000000100000000000000000000000";
        String small2 = "00000000100000000000000000000001";
        String big1 = "01111111000000000000000000000001";
        String big2 = "11111111000000000000000000000001";
        String[] strings = {deNorm1, deNorm2, deNorm3, small1, small2, big1, big2};
        double[] doubles = {10000000, 1.2, 1.1, 1, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, -0.1, -0.2, -0.3, -0.4, -0.5, -0.6, -0.7, -0.8, -0.9, -1, -10000000};

        float[] input = new float[strings.length + doubles.length];
        for (int i = 0; i < strings.length; i++) {
            input[i] = Float.parseFloat(tf.BinaryToFloat(strings[i]));
        }
        for (int i = 0; i < doubles.length; i++) {
            input[i + strings.length] = (float) doubles[i];
        }

        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input.length; j++) {
                src = new DataType(tf.DecimalToBinary(Integer.toString(Float.floatToIntBits(input[i]))));
                dest = new DataType(tf.DecimalToBinary(Integer.toString(Float.floatToIntBits(input[j]))));
                result = fpu.mul(src, dest);
                System.out.println("src:  " + src);
                System.out.println("dest: " + dest);
                assertEquals(tf.DecimalToBinary(Integer.toString(Float.floatToIntBits(input[i] * input[j]))), result.toString());
            }
        }
    }

    /**
     * 浮点数的除法
     */
    @Test
    public void fpuDivTest1(){
        dest = new DataType(tf.FloatToBinary( "0.4375" ));
        src = new DataType(tf.FloatToBinary( "0.5" ));
        result = fpu.div(src, dest);
        assertEquals(tf.FloatToBinary( "0.875" ), result.toString());
    }

    @Test
    public void fpuDivTest2(){
        dest = new DataType(tf.FloatToBinary( "1.0" ));
        src = new DataType(tf.FloatToBinary( "4.0" ));
        result = fpu.div(src, dest);
        assertEquals(tf.FloatToBinary( "0.25" ), result.toString());
    }

    @Test
    public void fpuDivTest3(){
        dest = new DataType(tf.FloatToBinary( "-2.0" ));
        src = new DataType(tf.FloatToBinary( "1.0" ));
        result = fpu.div(src, dest);
        assertEquals(tf.FloatToBinary( "-2.0" ), result.toString());
    }

    @Test
    public void fpuDivTest4(){
        dest = new DataType(tf.FloatToBinary( "1" ));
        src = new DataType(tf.FloatToBinary( "-2.0" ));
        result = fpu.div(src, dest);
        assertEquals(tf.FloatToBinary( "-0.5" ), result.toString());
    }

    @Test
    public void fpuDivTest5(){
        dest = new DataType(tf.FloatToBinary( "0.4375" ));
        src = new DataType(tf.FloatToBinary( "0.625" ));
        result = fpu.div(src, dest);
        assertEquals(tf.FloatToBinary("0.7"), result.toString());
    }

    @Test(expected = ArithmeticException.class)
    public void fpuDivExceptionTest(){
        dest = new DataType(tf.FloatToBinary( "2.2" ));
        System.out.println(111);
        src = new DataType(tf.FloatToBinary( "0.0" ));
        System.out.println(111);
        result = fpu.div(src, dest);
    }

}
