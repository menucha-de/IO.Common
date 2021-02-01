package havis.device.io.common;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ EnvironmentTest.class, ActivatorTest.class, ExceptionTest.class, HandlerTest.class, NativeTest.class })
public class TestSuite {
}