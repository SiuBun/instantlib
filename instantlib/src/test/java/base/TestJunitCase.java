package base;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

/**
 * 测试样例定义了运行多重测试的固定格式
 */
public class TestJunitCase extends TestCase {
    protected double fValue1;
    protected double fValue2;

    @Before
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("setUp in TestCase");
        fValue1 = 2.0;
        fValue2 = 3.0;
    }

    @Test
    public void testAdd() {
        //count the number of test cases
        System.out.println("No of Test Case = " + this.countTestCases());

        //test getName
        String name = this.getName();
        System.out.println("Test Case Name = " + name);

        //test setName
        this.setName("testNewAdd");
        String newName = this.getName();
        System.out.println("Updated Test Case Name = " + newName);
    }
    //tearDown used to close the connection or clean up activities

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.out.println("tearDown in TestCase");
    }
}