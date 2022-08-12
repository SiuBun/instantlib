import org.junit.Test;

public class BitTest {

    private static final int MODE_SHIFT = 30;
    private static final int MODE_MASK = 0x3 << MODE_SHIFT;

    public static final int UNSPECIFIED = 0 << MODE_SHIFT;
    public static final int EXACTLY = 1 << MODE_SHIFT;
    public static final int AT_MOST = 2 << MODE_SHIFT;

//    A = 0011 1100
//    B = 0000 1101
//    -----------------
//    A & B = 0000 1100     相对应位都是1，则结果为1，否则为0 与
//    A | B = 0011 1101     相对应位都是0，则结果为 0，否则为1 或
//    A ^ B = 0011 0001     相对应位值相同，则结果为0，否则为1 异或
//    ~A= 1100 0011         按位取反运算符翻转操作数的每一位，即0变成1，1变成0 反向
    @Test
    public void bitCalculate() {

        System.out.println("bitCalculate 100:"+Integer.toHexString(100));
        System.out.println("bitCalculate 21:"+Integer.parseInt("21", 16));

        int measureSpec = makeMeasureSpec(10, EXACTLY);
        System.out.println("EXACTLY:"+EXACTLY);
        System.out.println("measureSpec(10,EXACTLY) :"+measureSpec);
        System.out.println("getMode(measureSpec) :"+getMode(measureSpec));
        System.out.println("getSize(measureSpec) :"+getSize(measureSpec));

    }

    public static int makeMeasureSpec(int size,int mode){
        return (size & ~MODE_MASK) | (mode & MODE_MASK);
    }

    public static int getMode(int measureSpec) {
        return (measureSpec & MODE_MASK);
    }

    public static int getSize(int measureSpec) {
        return (measureSpec & ~MODE_MASK);
    }
}
