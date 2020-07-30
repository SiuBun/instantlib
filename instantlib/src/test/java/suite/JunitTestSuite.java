package suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * 套件测试，在类中附上 @RunWith(Suite.class) 注释,使用 @Suite.SuiteClasses 注释给 JUnit 测试类加上引用
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({TestJunitSuite1.class, TestJunitSuite2.class})
public class JunitTestSuite {
}  