package ignore;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import util.MessageUtil;

/**
 * 忽略测试，一个含有 @Ignore 注释的测试方法将不会被执行;如果一个测试类有 @Ignore 注释，则它的测试方法将不会执行
 */
//@Ignore
public class TestJunitIgnore {

    String message = "Robert";
    MessageUtil messageUtil = new MessageUtil(message);

    @Ignore
    @Test
    public void testPrintMessage() {
        System.out.println("Inside testPrintMessage()");
        message = "Robert";
        Assert.assertEquals(message, messageUtil.printMessage());
    }

    @Test
    public void testSalutationMessage() {
        System.out.println("Inside testSalutationMessage()");
        message = "Hi!" + "Robert";
        Assert.assertEquals(message, messageUtil.salutationMessage());
    }
}