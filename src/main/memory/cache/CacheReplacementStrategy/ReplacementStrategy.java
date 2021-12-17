package memory.cache.CacheReplacementStrategy;

import memory.Memory;
import memory.cache.Cache;

/**
 * 替换策略的接口
 */

public interface ReplacementStrategy {
    /**
     * 结合具体的替换策略，进行命中后进行相关操作
     * @param rowNO 行号
     */
    void hit(int rowNO);

    /**
     * 结合具体的映射策略，在给定范围内对cache中的数据进行替换
     * @param start 起始行
     * @param end 结束行 闭区间
     * @param addrTag tag
     * @param input  数据
     */
    int replace(int start, int end, char[] addrTag, char[] input);

    /**
     * 写回
     * 这个write函数是统一的，所以可以放在接口里作为default函数，实现的三个策略也不用去重写它，直接调用即可
     * @param rowNo 需要写回的行号
     */
    default void write(int rowNo){
        if (Cache.getCache().getValidBit(rowNo) && Cache.getCache().getDirtyBit(rowNo) && Cache.isWriteBack){
            String address = Cache.getCache().calAddress(rowNo);
            Memory.getMemory().write(address,Cache.LINE_SIZE_B,Cache.getCache().getData(rowNo));
            Cache.getCache().setDirtyBit(rowNo,false);
        }
    }
}
