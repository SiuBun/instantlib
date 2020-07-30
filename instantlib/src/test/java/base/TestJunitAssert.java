package base;

import org.junit.Assert;
import org.junit.Test;


/**
 * 类提供了一系列的编写测试的有用的声明方法。只有失败的声明方法才会被记录
 */
public class TestJunitAssert {
    @Test
    public void testAdd() {
        //test data
        int num = 5;
        String temp = null;
        String str = "Junit is working fine";

        //check for equality
        Assert.assertEquals("Junit is working fine", str);

        //check for false condition
        Assert.assertFalse(num > 6);

        //check for not null value
        Assert.assertNotNull(str);
    }
}