package memory.cache.CacheReplacementStrategy;

import memory.cache.Cache;

/**
 * 最近最少使用算法
 */

public class LRUReplacement implements ReplacementStrategy{
    // 命中时更新时间戳
    @Override
    public void hit(int rowNO) {
        Cache.getCache().setTimeStamp(rowNO,System.currentTimeMillis());
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
            // 找到最小时间戳的行
            long timeStamp = Cache.getCache().getTimeStamp(i);
            if (timeStamp < minTimeStamp){
                minTimeStamp = timeStamp;
                rowNo = i;
            }
        }
        // 写回
        write(rowNo);
        // 替换更新
        Cache.getCache().update(rowNo,addrTag,input);
        return rowNo;
    }
}
