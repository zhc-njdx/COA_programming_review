package memory.cache.CacheReplacementStrategy;

import memory.cache.Cache;

/**
 * 最不经常使用算法
 */

public class LFUReplacement implements ReplacementStrategy{
    // 命中时更新访问次数
    @Override
    public void hit(int rowNO) {
        Cache.getCache().addVisitedTimes(rowNO);
    }

    @Override
    public int replace(int start, int end, char[] addrTag, char[] input) {
        int minVisited = Cache.getCache().getVisitedTimes(start);
        int rowNo = start;
        for (int i = start; i < end; ++i){
            // Cache中存在空行
            if (!Cache.getCache().getValidBit(i)){
                rowNo = i;
                break;
            }
            // 找到访问次数最少的
            int visited = Cache.getCache().getVisitedTimes(i);
            if (visited < minVisited){
                minVisited = visited;
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
