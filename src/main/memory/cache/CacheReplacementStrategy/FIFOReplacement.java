package memory.cache.CacheReplacementStrategy;

import memory.Memory;
import memory.cache.Cache;

/**
 * 先进先出算法
 */

public class FIFOReplacement implements ReplacementStrategy{
    /**
     * FIFO不用hit函数
     * 因为其时间戳是进入Cache后就确定下来了
     */
    @Override
    public void hit(int rowNO) {
    }

    @Override
    public int replace(int start, int end, char[] addrTag, char[] input) {
        long minTimeStamp = Cache.getCache().getTimeStamp(start);
        int rowNo = start;
        for (int i = start; i < end; ++i){
            // Cache中存在空行
            if (!Cache.getCache().getValidBit(i)){
                rowNo = i;
                break;
            }
            // 找最小的时间戳
            long timeStamp = Cache.getCache().getTimeStamp(i);
            if (timeStamp < minTimeStamp){
                rowNo = i;
                minTimeStamp = timeStamp;
            }
        }
        // 写回
        write(rowNo);
        // 替换更新
        Cache.getCache().update(rowNo,addrTag,input);
        return rowNo;
    }
}
