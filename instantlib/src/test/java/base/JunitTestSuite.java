package base;

import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * TestSuite 类是测试的组成部分。它可运行多个测试案例。
 * */
public class JunitTestSuite {
    public static void main(String[] a) {
        // add the test's in the suite
        TestSuite suite = new TestSuite(TestJunitAssert.class, TestJunitCase.class, TestJunitResult.class);
        TestResult result = new TestResult();
        suite.run(result);
        System.out.println("Number of test cases = " + result.runCount());
    }
}