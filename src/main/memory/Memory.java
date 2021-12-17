package memory;

import memory.cache.Cache;
import util.Transform;

public class Memory {
    private static final int MEM_SIZE_B = 32 * 1024 * 1024;      // 32 MB

    private static final char[] memory = new char[MEM_SIZE_B];

    private static final Memory memoryInstance = new Memory();

    private Memory() {
    }

    public static Memory getMemory() {
        return memoryInstance;
    }

    public char[] read(String pAddr, int len) {
        char[] data = new char[len];
        for (int ptr = 0; ptr < len; ptr++) {
            data[ptr] = memory[Integer.parseInt(new Transform().BinaryToDecimal(pAddr)) + ptr];
            //if(ptr < 4) System.out.println("data[ptr]read: "+data[ptr]);
        }
        return data;
    }

    public void write(String pAddr, int len, char[] data) {
        // 通知Cache缓存失效
        Cache.getCache().invalid(pAddr, len);
        // 更新数据
        for (int ptr = 0; ptr < len; ptr++) {
            //System.out.println("data[ptr]write: "+data[ptr]);
            memory[Integer.parseInt(new Transform().BinaryToDecimal(pAddr)) + ptr] = data[ptr];
        }
    }
}
