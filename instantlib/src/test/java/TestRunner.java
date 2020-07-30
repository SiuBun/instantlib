import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import params.PrimeNumberCheckerTest;

/**
 * 执行测试,运用JUnit的JUnitCore 类的 runClasses 方法来运行上述测试类的测试案例.得到成功和失败结果
 */
public class TestRunner {
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(PrimeNumberCheckerTest.class);
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }
        System.out.println("test result is " + result.wasSuccessful() + " ,and count is " + result.getRunCount() + " with runtime(milliseconds) " + result.getRunTime());
    }
}