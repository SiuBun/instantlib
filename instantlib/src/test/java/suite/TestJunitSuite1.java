package suite;

import org.junit.Assert;
import org.junit.Test;

import util.MessageUtil;

public class TestJunitSuite1 {

   String message = "Robert";   
   MessageUtil messageUtil = new MessageUtil(message);

   @Test
   public void testPrintMessage() { 
      System.out.println("Inside testPrintMessage()");    
      Assert.assertEquals(message, messageUtil.printMessage());
   }
}