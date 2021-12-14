package util;

/**
 * DataType类
 * 在后续的计算中，操作数均以 Datatype 的形式出现
 * length 数据的位数
 * data 数据的字符数组形式
 */
public class DataType {

    private final int length = 32;
    private final char[] data_char = new char[length];
    private final int[] data_int = new int[length];

    public DataType(String dataStr)
    {
        if (dataStr.length() != length)
            throw new NumberFormatException(dataStr.length() + " != " + length);
        for (int i = 0; i < dataStr.length(); ++i)
        {
            char bit = dataStr.charAt(i);
            if(bit != '1' && bit != '0')
                throw new NumberFormatException(bit + " is not '1' or '0'");
            data_char[i] = bit;
            data_int[i] = bit - '0';
        }
    }

    public DataType(int[] data) {
        if (data.length != length)
            throw new NumberFormatException(data.length + " != " + length);
        for (int i = 0; i < data.length; ++i) {
            int bit = data[i];
            if (bit != 1 && bit != 0)
                throw new NumberFormatException(bit + " is not 1 or 0");
            data_int[i] = bit;
            data_char[i] = bit == 1 ? '1' : '0';
        }
    }

    public int[] getData_int()
    {
        return data_int;
    }

    public void setBit(int index, char bit){
        data_char[index] = bit;
        data_int[index] = bit - '0';
    }

    public char getBit(int index){
        return data_char[index];
    }

    @Override
    public String toString()
    {
        return String.valueOf(data_char);
    }
}
