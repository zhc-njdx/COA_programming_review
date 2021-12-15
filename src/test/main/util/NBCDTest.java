package util;

import cpu.nbcdu.NBCDU;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NBCDTest {
    private final NBCDU nbcdu = new NBCDU();
    private final Transform transformer = new Transform();
    private DataType src;
    private DataType dest;
    private DataType result;

    @Test
    public void AddTest1() {
        src = new DataType("11000000000000000000000010011000");
        dest = new DataType("11000000000000000000000001111001");
        result = nbcdu.add(src, dest);
        assertEquals("11000000000000000000000101110111", result.toString());
    }

    @Test
    public void SubTest1() {
        src = new DataType("11000000000000000000000100100101");
        dest = new DataType("11000000000000000000001100001001");
        result = nbcdu.sub(src, dest);
        assertEquals("11000000000000000000000110000100", result.toString());
    }


}
