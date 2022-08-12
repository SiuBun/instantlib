import okhttp3.internal.toHexString
import org.junit.Test

class BitTextKt {
    private val MODE_SHIFT = 30
    private val MODE_MASK = 0x3 shl MODE_SHIFT

    val UNSPECIFIED = 0 shl MODE_SHIFT
    val EXACTLY = 1 shl MODE_SHIFT
    val AT_MOST = 2 shl MODE_SHIFT


    @Test
    fun baseStudy() {
        val result = 1.shl(3)
//        println("result is  $result")
//        println("result is  ${Integer.toBinaryString(result)}")

        println("0x3.shl(1) is                                           ${Integer.toBinaryString((0x3).shl(1))}")
        println()

        println("MODE_MASK  is                      ${Integer.toBinaryString(MODE_MASK)}")
        println("EXACTLY    is                       ${Integer.toBinaryString(EXACTLY)}")
        println("===================================================================")
        // 都是1才能得到1，否则为0
        println("modecalculate                       ${Integer.toBinaryString(EXACTLY and MODE_MASK)}")

        println()

        println("MODE_MASK          is              ${Integer.toBinaryString(MODE_MASK)}")
        println("===================================================================")
        println("MODE_MASK.inv                        ${Integer.toBinaryString(MODE_MASK.inv())}")

        println()

        val i = 253
        println("253                   is                                   ${Integer.toBinaryString(i)}")
        println("===================================================================")
        println("MODE_MASK.inv and 253 is                                   ${Integer.toBinaryString(MODE_MASK.inv() and i)}")

        println()
        println("modecalculate                       ${Integer.toBinaryString(EXACTLY and MODE_MASK)}")
        println("===================================================================")
        println("(MODE_MASK.inv and 253) or modecal  ${Integer.toBinaryString((EXACTLY and MODE_MASK) or (MODE_MASK.inv() and i))}")
        println()

//        val count = 10
//        val measureSpecCount = makeMeasureSpecCount(i, EXACTLY, count)
//        println("EXACTLY $EXACTLY")
//        val preProcessCount = count.shl(8)
//        println("count and (MODE_MASK.inv())  ${Integer.toBinaryString((count and (MODE_MASK.inv())))}")
//        println("(count.shl(8) and (MODE_MASK.inv()))  ${Integer.toBinaryString((preProcessCount and (MODE_MASK.inv())))}")
//        println("$measureSpecCount")
//        println(Integer.toBinaryString(measureSpecCount))
//        println("${getSize1(measureSpecCount)}")
//        println("${getMode(measureSpecCount)}")
//        println("${getCount(measureSpecCount)}")
//
//        println()
    }

    @Test
    fun bitCalculate() {
        println("bitCalculate 100:" + Integer.toHexString(255))
        println("bitCalculate 21:" + "21".toInt(16))

        val measureSpec = makeMeasureSpec(10, EXACTLY)
        println("EXACTLY:" + EXACTLY)
        println("EXACTLY toBinaryString:" + Integer.toBinaryString(EXACTLY))
        println("measureSpec(10,EXACTLY) :$measureSpec")
        println("getMode(measureSpec) :" + getMode(measureSpec))
        println("getSize(measureSpec) :" + getSize(measureSpec))
    }

    private fun makeMeasureSpec(size: Int, mode: Int): Int {
        return (size and (MODE_MASK.inv())) or (mode and MODE_MASK)
    }

    private fun getMode(measureSpec: Int): Int {
        return measureSpec and MODE_MASK
    }

    private fun getSize(measureSpec: Int): Int {
        return measureSpec and (MODE_MASK.inv())
    }
    private fun getSize1(measureSpec: Int): Int {
    // 实际 1010 11111101
    // 期望 0000 11111101
        return measureSpec and (MODE_MASK.inv())
    }

    private fun getCount(count:Int):Int{
        // 实际 10101111110100000000
        // 期望 1010
        return count.shl(8) and (MODE_MASK.inv())
    }

    private fun makeMeasureSpecCount(size: Int, mode: Int,count:Int): Int {
        return (size and (MODE_MASK.inv())) or
                (mode and MODE_MASK) or
                (count.shl(8) and (MODE_MASK.inv()))
    }
}