package memory.cache;

import memory.Memory;
import memory.cache.CacheReplacementStrategy.FIFOReplacement;
import memory.cache.CacheReplacementStrategy.ReplacementStrategy;
import util.Transform;
import util.TransformHelper;

import java.awt.desktop.ScreenSleepEvent;
import java.util.Arrays;

/**
 * 第七、八次作业
 * 实现Cache
 * 1、完成fetch()、map()函数,兼容三种映射策略
 *          能够正确地根据地址来判断数据块是否在Cache中，在则直接返回行号，不在则在fetch()进行中内存的数据调取入Cache后返回行号
 * 2、完成三种替换策略的replace()、hit()
 * 3、完成两种写策略
 */

/**
 * 高速缓存抽象类
 *
 * CPU给出一个地址，想获取该地址的内容，会先到Cache中搜索
 *
 * read():
 *      read()会调用fetch()先去Cache中进行搜索
 *      fetch()根据地址算出块号，用 map() 查看是否在该数据块是否在Cache中,如果命中调用替换策略中的 hit(),进行时间戳或者访问次数的更新,并返回行号。不在返回-1
 *      不在Cache,fetch()要进行数据块的调取:计算出组号,tag,然后到Memory中读出数据data,调用替换策略中的replace()进行替换
 *      fetch()返回行号给read(),到Cache中进行数据的读取
 *      replace根据替换策略的不同的指标(时间戳、访问次数)选择待替换的行,替换前根据dirty位以及写策略判断是否需要写回内存(write函数)。
 *      最后进行替换(update函数)
 *
 * write():
 *      当向Cache中写数据时，如果是直写式，则同时需要向内存写入数据。
 */

public class Cache {

    public static final boolean isAvailable = true; // 默认启用Cache

    public static final int CACHE_SIZE_B = 1024 * 1024; // 1 MB 总大小

    public static final int LINE_SIZE_B = 1024; // 1 KB 行大小

    private final CacheLinePool cache = new CacheLinePool(CACHE_SIZE_B / LINE_SIZE_B);  // 总大小1MB / 行大小1KB = 1024个行

    private int SETS;   // 组数

    private int setSize;    // 每组行数

    // 单例模式
    private static final Cache cacheInstance = new Cache();

    private Cache() {
    }

    public static Cache getCache() {
        return cacheInstance;
    }

    private ReplacementStrategy replacementStrategy;    // 替换策略

    public static boolean isWriteBack;   // 写策略

    private final Transform transformer = new Transform();

    private final TransformHelper tfh = new TransformHelper();

    /**
     * 读取[pAddr, pAddr + len)范围内的连续数据，可能包含多个数据块的内容
     *
     * @param pAddr 数据起始点(32位物理地址 = 22位块号 + 10位块内地址)
     * @param len   待读数据的字节数
     * @return 读取出的数据，以char数组的形式返回
     */
    public char[] read(String pAddr, int len) {
        char[] data = new char[len];
        int addr = Integer.parseInt(transformer.BinaryToDecimal("0" + pAddr));
        int upperBound = addr + len;
        int index = 0;
        while (addr < upperBound) {
            int nextSegLen = LINE_SIZE_B - (addr % LINE_SIZE_B);
            if (addr + nextSegLen >= upperBound) {
                nextSegLen = upperBound - addr;
            }
            int rowNO = fetch(transformer.DecimalToBinary(String.valueOf(addr)));
            char[] cache_data = cache.get(rowNO).getData();
            int i = 0;
            while (i < nextSegLen) {
                data[index] = cache_data[addr % LINE_SIZE_B + i];
                index++;
                i++;
            }
            addr += nextSegLen;
        }
        return data;
    }

    /**
     * 向cache中写入[pAddr, pAddr + len)范围内的连续数据，可能包含多个数据块的内容
     *
     * @param pAddr 数据起始点(32位物理地址 = 22位块号 + 10位块内地址)
     * @param len   待写数据的字节数
     * @param data  待写数据
     */
    public void write(String pAddr, int len, char[] data) {
        // 直写式，直接写进内存
        if(!isWriteBack)
            Memory.getMemory().write(pAddr,len,data);

        int addr = Integer.parseInt(transformer.BinaryToDecimal("0" + pAddr));
        int upperBound = addr + len;
        int index = 0;
        while (addr < upperBound) {
            int nextSegLen = LINE_SIZE_B - (addr % LINE_SIZE_B);
            if (addr + nextSegLen >= upperBound) {
                nextSegLen = upperBound - addr;
            }
            int rowNO = fetch(transformer.DecimalToBinary(String.valueOf(addr)));
            char[] cache_data = cache.get(rowNO).getData();
            int i = 0;
            while (i < nextSegLen) {
                cache_data[addr % LINE_SIZE_B + i] = data[index];
                index++;
                i++;
            }
            // 如果是写回策略，则要将dirty置true
            if(isWriteBack) cache.get(rowNO).dirty = true;

            addr += nextSegLen;
        }
    }

    /**
     * 将 每组行数=2^n 的n求出来
     * @return n
     */
    private int setSizesToN(){
        int i = 0;
        while (setSize > Math.pow(2,i))
            i++;
        return i;
    }

    /**
     * 查询{@link Cache#cache}表以确认包含pAddr的数据块是否在cache内
     * 如果目标数据块不在Cache内，则将其从内存加载到Cache
     *
     * 直接映射 SETS=1024=1024/1=2^(10-0) setSize=1=2^0
     * 关联映射 SETS=1024/1024=2^(10-10)  setSize=1024=2^10
     * k-路组关联映射 SETS=1024/k=2^(10-n) setSize=k=2^n
     *
     * 根据一个地址求块号，把三种映射方法都当作组关联映射
     * 块号 = 组大小 * 标记号 + 组号
     *
     * 注意理解组号和行号的联系和区别:
     *      如果将三种映射策略统一为组关联映射，地址的划分应该就是 tag、组号、块内地址  直接映射中的行号就是组号
     *      当返回行号的时候，还是确定的某一行
     *      除了返回时需要确定的一行，其他时候均使用组号
     *
     * @param pAddr 数据起始点(32位物理地址 = 22位块号 + 10位块内地址)
     * @return 数据块在Cache中的对应行号
     */
    private int fetch(String pAddr) {
        // TODO
        int n = setSizesToN();
        // 获取块号
        // 也可用方法 getBlockNo(String pAddr);
        int blockNo = getBlockNO(pAddr);
//        int blockNo = SETS * Integer.parseInt(transformer.BinaryToDecimal(pAddr.substring(0,22-(10-n))))
//                            + Integer.parseInt(transformer.BinaryToDecimal(pAddr.substring(22-(10-n),22)));

        // 根据计算出来的块号进行映射
        int lineNo = map(blockNo);
        // 命中 返回行号
        if (lineNo != -1) return lineNo;
        // 未命中 去内存中加载相应块进入Cache
        char[] data = Memory.getMemory().read(pAddr.substring(0,22) + "0000000000",LINE_SIZE_B);

        // 先计算出tag
        StringBuilder tag = new StringBuilder(pAddr.substring(0,22-(10-n)));
        while (tag.length() < 22) tag.append("0"); // 由于CacheLine中的tag是22位，所以需要补全
        // 计算出Cache中放置该数据块的组号
        int setNo = blockNo % SETS;
        // 进行替换，返回新行号
        if (setSize == 1) replacementStrategy = new FIFOReplacement(); // 注意: 由于测试中直接映射没有替换策略，为了统一性，当为直接映射时，默认替换策略为FIFO
        return replacementStrategy.replace(setNo * setSize,(setNo + 1) * setSize,
                tfh.StringToCharArray(tag.toString()), data);
    }

    /**
     * 根据目标数据内存地址前22位的int表示，进行映射
     *
     * @param blockNO 数据在内存中的块号
     * @return 返回cache中所对应的行，-1表示未命中
     */
    private int map(int blockNO) {
        // TODO
        int setNo = blockNO % SETS; // 组号
        int lineNo;
        for (lineNo = setNo * setSize; lineNo < (setNo + 1) * setSize; ++lineNo){
            // 判断tag是否能匹配上
            if (cache.get(lineNo).validBit && judgeTag(blockNO,lineNo)) {// 有效的前提下 tag匹配上了
                // FIFO在match到的时候不用更改时间戳
                if(!(replacementStrategy instanceof FIFOReplacement))
                    // match到的时候要更新时间戳或者访问次数
                    replacementStrategy.hit(lineNo);
                return lineNo;
            }
        }
        return -1;
    }

    /**
     * 判断一个数据块的tag是否和Cache中某一行的tag匹配
     * @param blockNo 块号
     * @param lineNo 行号
     * @return tag是否相同
     */
    private boolean judgeTag(int blockNo, int lineNo){
        int blockTag = blockNo / SETS;
        String tag = tfh.CharArrayToString(cache.get(lineNo).tag).substring(0,22-(10-setSizesToN())); // CacheLine中的tag不是真的tag(直接映射高12位是tag)
        return blockTag == transformer.originCodeValue(tag);
    }


    /**
     * replace中用来更新cache的一行
     *
     * @param rowNo 需要更新的cache行号
     * @param tag   待更新数据的Tag
     * @param input 待更新的数据
     */
    public void update(int rowNo, char[] tag, char[] input) {
        // TODO
        setTag(rowNo,tag);
        setData(rowNo,input);
        setValidBit(rowNo,true);
        setTimeStamp(rowNo,System.currentTimeMillis());
        resetVisitedTime(rowNo);
    }

    /**
     * 在写回write()中，需要把dirty为true的行写回主存
     * 根据行号计算相应的内存地址
     * @param rowNo 行号
     * @return 该行对应的内存地址
     */
    public String calAddress(int rowNo){
        StringBuilder address = new StringBuilder();
        int n = setSizesToN();
        // tag部分
        address.append(tfh.CharArrayToString(cache.get(rowNo).getTag()).substring(0,22-(10-n)));
        // 组号部分
        address.append(transformer.DecimalToBinary(rowNo / setSize + "").substring(32-(10-n),32));
        // 加0(最后10位是块内地址，由于Cache的一行就是一块，写回内存也是一块一块地写，所以这里就是块的起始地址，为0000000000)
        address.append("0000000000");
        return address.toString();
    }


    /**
     * 从32位物理地址(22位块号 + 10位块内地址)获取目标数据在内存中对应的块号
     *
     * @param pAddr 32位物理地址
     * @return 数据在内存中的块号
     */
    private int getBlockNO(String pAddr) {
        return Integer.parseInt(transformer.BinaryToDecimal("0" + pAddr.substring(0, 22)));
    }


    /**
     * 使用策略模式，设置cache的替换策略与写策略
     *
     * @param replacementStrategy 替换策略
     */
    public void setReplacementStrategy(ReplacementStrategy replacementStrategy) {
        this.replacementStrategy = replacementStrategy;
    }

    /**
     * 该方法会被用于测试，请勿修改
     *
     * @param SETS 组数
     */
    public void setSETS(int SETS) {
        this.SETS = SETS;
    }

    /**
     * 该方法会被用于测试，请勿修改
     *
     * @param setSize 每组行数
     */
    public void setSetSize(int setSize) {
        this.setSize = setSize;
    }

    /**
     * 告知Cache某个连续地址范围内的数据发生了修改，缓存失效
     * 该方法仅在memory类中使用，请勿修改
     *
     * @param pAddr 发生变化的数据段的起始地址
     * @param len   数据段长度
     */
    public void invalid(String pAddr, int len) {
        int from = getBlockNO(pAddr);
        Transform t = new Transform();
        int to = getBlockNO(t.DecimalToBinary(String.valueOf(Integer.parseInt(t.BinaryToDecimal("0" + pAddr)) + len - 1)));

        for (int blockNO = from; blockNO <= to; blockNO++) {
            int rowNO = map(blockNO);
            if (rowNO != -1) {
                cache.get(rowNO).validBit = false;
            }
        }
    }

    /**
     * 清除Cache全部缓存
     * 该方法会被用于测试，请勿修改
     */
    public void clear() {
        for (CacheLine line : cache.clPool) {
            if (line != null) {
                line.validBit = false;
            }
        }
    }

    /**
     * 输入行号和对应的预期值，判断Cache当前状态是否符合预期
     * 这个方法仅用于测试，请勿修改
     *
     * @param lineNOs     行号
     * @param validations 有效值
     * @param tags        tag
     * @return 判断结果
     */
    public boolean checkStatus(int[] lineNOs, boolean[] validations, char[][] tags) {
        if (lineNOs.length != validations.length || validations.length != tags.length) {
            return false;
        }
        for (int i = 0; i < lineNOs.length; i++) {
            CacheLine line = cache.get(lineNOs[i]);
            if (line.validBit != validations[i]) {
                return false;
            }
            if (!Arrays.equals(line.getTag(), tags[i])) {
                return false;
            }
        }
        return true;
    }


    /**
     * 负责对CacheLine进行动态初始化
     */
    private static class CacheLinePool {

        private final CacheLine[] clPool;

        /**
         * @param lines Cache的总行数
         */
        CacheLinePool(int lines) {
            clPool = new CacheLine[lines];
        }

        private CacheLine get(int rowNO) {
            CacheLine l = clPool[rowNO];
            if (l == null) {
                clPool[rowNO] = new CacheLine();
                l = clPool[rowNO];
            }
            return l;
        }
    }

    //方便外部访问的方法
    public boolean getValidBit(int rowNo)
    {
        return cache.get(rowNo).validBit;
    }

    public void setValidBit(int rowNo,boolean isValid)
    {
        cache.get(rowNo).validBit = isValid;
    }

    public boolean getDirtyBit(int rowNo)
    {
        return cache.get(rowNo).dirty;
    }

    public void setDirtyBit(int rowNo, boolean isDirty)
    {
        cache.get(rowNo).dirty = isDirty;
    }

    public int getVisitedTimes(int rowNo)
    {
        return cache.get(rowNo).visited;
    }

    public void addVisitedTimes(int rowNo)
    {
        cache.get(rowNo).visited++;
    }

    public void resetVisitedTime(int rowNo)
    {
        cache.get(rowNo).visited = 0;
    }


    public long getTimeStamp(int rowNo)
    {
        return cache.get(rowNo).timeStamp;
    }

    public void setTimeStamp(int rowNo, long new_timeStamp)
    {
        cache.get(rowNo).timeStamp = new_timeStamp;
    }

    public void setData(int rowNo,char[] input) {cache.get(rowNo).setData(input);}

    public void setTag(int rowNo, char[] newTag)
    {
        cache.get(rowNo).setTag(newTag);
    }

    public char[] getData(int rowNo)
    {
        return cache.get(rowNo).getData();
    }

    /**
     * Cache行，每行长度为(1+22+{@link Cache#LINE_SIZE_B})
     */
    private static class CacheLine {

        // 有效位，标记该条数据是否有效
        // 做很多判断，要记得在validBit=true情况下进行
        // 无效数据就是空行，可直接替换
        boolean validBit = false;

        // 脏位
        boolean dirty = false;

        // 用于LFU算法，记录该条cache使用次数
        int visited = 0;

        // 用于LRU和FIFO算法，记录该条数据时间戳
        Long timeStamp = 0L;

        // 标记，占位长度为22位，有效长度取决于映射策略：
        // 直接映射: 12 位
        // 全关联映射: 22 位
        // (2^n)-路组关联映射: 22-(10-n) 位
        // 注意，tag在物理地址中用高位表示，如：直接映射(32位)=tag(12位)+行号(10位)+块内地址(10位)，
        // 那么对于值为0b1111的tag应该表示为0000000011110000000000，其中前12位为有效长度
        char[] tag = new char[22];

        // 数据
        char[] data = new char[LINE_SIZE_B];

        char[] getData() {
            return this.data;
        }

        char[] getTag() {
            return this.tag;
        }

        void setData(char[] new_data){this.data = new_data;}

        void setTag(char[] new_tag){this.tag = new_tag;}

    }
}
