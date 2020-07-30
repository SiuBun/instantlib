package params;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * 参数化测试,允许开发人员使用不同的值反复运行同一个测试
 * <p>
 * 1.用 @RunWith(Parameterized.class) 来注释 test 类
 * <p>
 * 2.创建一个由 @Parameters 注释的公共的静态方法，它返回一个对象的集合(数组)来作为测试数据集合
 * <p>
 * 3.创建一个公共的构造函数，它接受和一行测试数据相等同的东西
 * <p>
 * 4.为每一列测试数据创建一个实例变量
 * <p>
 * 5.用实例变量作为测试数据的来源来创建你的测试用例
 */
@RunWith(Parameterized.class)
public class PrimeNumberCheckerTest {
    private Integer inputNumber;
    private Boolean expectedResult;
    private PrimeNumberChecker primeNumberChecker;

    @Before
    public void initialize() {
        primeNumberChecker = new PrimeNumberChecker();
    }

    // Each parameter should be placed as an argument here
    // Every time runner triggers, it will pass the arguments
    // from parameters we defined in primeNumbers() method
    public PrimeNumberCheckerTest(Integer inputNumber,
                                  Boolean expectedResult) {
        this.inputNumber = inputNumber;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters
    public static Collection primeNumbers() {
        return Arrays.asList(new Object[][]{
                {2, true},
                {6, false},
                {19, true},
                {22, false},
                {23, true}
        });
    }

    // This test will run 4 times since we have 5 parameters defined
    @Test
    public void testPrimeNumberChecker() {
        System.out.println("Parameterized Number is : " + inputNumber);
        Assert.assertEquals(expectedResult,
                primeNumberChecker.validate(inputNumber));
    }
}