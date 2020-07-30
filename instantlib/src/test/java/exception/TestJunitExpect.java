package exception;

import org.junit.Assert;
import org.junit.Test;

/**
 * 异常测试,可以测试代码是否它抛出了想要得到的异常。expected 参数和 @Test 注释搭配使用
 */
public class TestJunitExpect {

    String message = "Robert";
    MessageUtil messageUtil = new MessageUtil(message);

    @Test(expected = ArithmeticException.class)
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