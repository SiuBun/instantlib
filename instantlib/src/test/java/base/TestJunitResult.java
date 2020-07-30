package base;

import junit.framework.AssertionFailedError;
import junit.framework.TestResult;

import org.junit.Test;

/**
 * TestResult 类收集所有执行测试案例的结果。它是收集参数层面的一个实例。这个实验框架区分失败和错误。失败是可以预料的并且可以通过假设来检查。
 */
public class TestJunitResult extends TestResult {
    // add the error
    public synchronized void addError(Test test, Throwable t) {
        super.addError((junit.framework.Test) test, t);
    }

    // add the failure
    public synchronized void addFailure(Test test, AssertionFailedError t) {
        super.addFailure((junit.framework.Test) test, t);
    }

    @Test
    public void testAdd() {
        // add any test
    }

    // Marks that the test run should stop.
    public synchronized void stop() {
        //stop the test here
    }
}