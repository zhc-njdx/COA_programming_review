package Memory.Cache;

import memory.Memory;
import memory.cache.Cache;
import memory.cache.CacheReplacementStrategy.FIFOReplacement;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class WriteThroughTest {

    private Memory memory;
    private Cache cache;

    @Before
    public void init(){
        cache = Cache.getCache();
        memory = Memory.getMemory();
        Cache.getCache().setSETS(256);
        Cache.getCache().setSetSize(4);
        cache.setReplacementStrategy(new FIFOReplacement());
        Cache.isWriteBack = false;
    }

    /**
     * 写入数据，同时写入内存
     */
    @Test
    public void test01() {
        String pAddr = "00000000000000000000000000000000";
        String pAddr2 = "00000000000000000000000000000010";
        char[] input1 = {0b11110100, 0b11010100};
        char[] input2 = {0b00000000, 0b00000011};
        char[] input3 = "这样你满意了吗？".toCharArray();
        char[] input4 = "这样我满意了".toCharArray();
        char[] dataRead;

        memory.write(pAddr, input1.length, input1);
        dataRead = cache.read(pAddr, input1.length);
        assertArrayEquals(input1, dataRead);
        assertTrue(cache.checkStatus(new int[]{0}, new boolean[]{true}, new char[][]{"0000000000000000000000".toCharArray()}));

        cache.write(pAddr, input2.length, input2);
        dataRead = cache.read(pAddr, input2.length);
        assertArrayEquals(input2, dataRead);
        assertArrayEquals(input2, memory.read(pAddr, input2.length));

        cache.write(pAddr2, input3.length, input3);
        dataRead = cache.read(pAddr2, input3.length);
        assertArrayEquals(input3, dataRead);
        assertTrue(cache.checkStatus(new int[]{0}, new boolean[]{true}, new char[][]{"0000000000000000000000".toCharArray()}));
        assertArrayEquals(input3, memory.read(pAddr2, input3.length));

        cache.write(pAddr2, input4.length, input4);
        dataRead = cache.read(pAddr2, input4.length);
        assertArrayEquals(input4, dataRead);
        assertTrue(cache.checkStatus(new int[]{0}, new boolean[]{true}, new char[][]{"0000000000000000000000".toCharArray()}));
        assertArrayEquals(input4, memory.read(pAddr2, input4.length));

        cache.clear();
    }

    /**
     * 从cache读入数据，然后修改cache同时修改内存，然后再读内存
     */
    @Test
    public void test02(){
        char[] input1 = new char[1024 * 1024];
        char[] input2 = new char[1024];
        char[] input3 = new char[1024];
        Arrays.fill(input1, 'a');
        Arrays.fill(input2, 'b');
        Arrays.fill(input3, 'c');
        String pAddr1 = "00000000000000000000000000000000";
        String pAddr2 = "00000000000000000000010000000000";
        String pAddr3 = "00000000101000000000010000000000";
        char[] dataRead;

        memory.write(pAddr1, input1.length, input1);
        dataRead = cache.read(pAddr1,input1.length);
        assertArrayEquals(input1, dataRead);

        cache.write(pAddr2, input2.length, input2);
        dataRead = cache.read(pAddr2, input2.length);
        assertArrayEquals(input2, dataRead);
        assertArrayEquals(input2, memory.read(pAddr2, input2.length));

        memory.write(pAddr3, input3.length, input3);
        dataRead = cache.read(pAddr3, input3.length);
        assertArrayEquals(input3, dataRead);

        cache.clear();
    }

    /**
     * 写入数据同时写入内存，再修改内存，发生替换，cache数据更新
     */
    @Test
    public void test03(){
        char[] input1 = new char[1024 * 1024];
        char[] input2 = new char[1024];
        Arrays.fill(input1, 'd');
        Arrays.fill(input2, 'e');
        String pAddr1 = "00000000000000000000000000000000";
        String pAddr2 = "00000000101000000000010000000000";
        char[] dataRead;

        // 数据位于cache第1组的第0行
        cache.write(pAddr2, input2.length, input2);
        dataRead = cache.read(pAddr2, input2.length);
        assertArrayEquals(input2, dataRead);
        assertArrayEquals(input2, memory.read(pAddr2, input2.length));

        // memory 被修改，所以cache中的所有被Match的行的valid都变成了False，再次替换的时候，有一行会被插入到第1组第0行的位置，此时发生替换
        memory.write(pAddr1, input1.length, input1);
        dataRead = cache.read(pAddr1, input1.length);
        assertArrayEquals(input1, dataRead);
        assertTrue(cache.checkStatus(new int[]{4}, new boolean[]{true}, new char[][]{"0000000000001100000000".toCharArray()}));

        cache.clear();
    }

    /**
     * same to test03
     */
    @Test
    public void test04(){
        char[] input1 = new char[1024 * 1024];
        char[] input2 = new char[1025];
        Arrays.fill(input1, 'f');
        Arrays.fill(input2, 'g');
        String pAddr1 = "00000000000000000000000000000000";
        String pAddr2 = "00000000101000000000010000000000";
        char[] dataRead;

        // 数据位于cache第1组的第0行
        cache.write(pAddr2, input2.length, input2);
        dataRead = cache.read(pAddr2, input2.length);
        assertArrayEquals(input2, dataRead);
        assertArrayEquals(input2, memory.read(pAddr2, input2.length));

        // memory 被修改，所以cache中的所有被Match的行的valid都变成了False，再次替换的时候，有一行会被插入到第二组第一行的位置，此时发生替换
        memory.write(pAddr1, input1.length, input1);
        dataRead = cache.read(pAddr1, input1.length);
        assertArrayEquals(input1, dataRead);
        assertTrue(cache.checkStatus(new int[]{4, 8}, new boolean[]{true, true}, new char[][]{"0000000000001100000000".toCharArray(), "0000000000001100000000".toCharArray()}));

        cache.clear();
    }

}
