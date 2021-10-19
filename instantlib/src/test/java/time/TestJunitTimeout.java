package time;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;


/**
 * 时间测试,如果一个测试用例比起指定的毫秒数花费了更多的时间，那么 Junit 将自动将它标记为失败。timeout 参数和 @Test 注释搭配使用
 */
public class TestJunitTimeout {

    String message = "Robert";
    MessageUtil messageUtil = new MessageUtil(message);

    @Ignore
    @Test(timeout = 5)
    public void testPrintMessage() {
        System.out.println("Inside testPrintMessage()");
        messageUtil.printMessage();
    }

    @Test
    public void testSalutationMessage() {
        System.out.println("Inside testSalutationMessage()");
        message = "Hi!" + "Robert";
        Assert.assertEquals(message, messageUtil.salutationMessage());
    }
}