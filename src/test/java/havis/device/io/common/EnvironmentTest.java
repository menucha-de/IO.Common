package havis.device.io.common;

import java.util.logging.Level;
import java.util.logging.Logger;

import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EnvironmentTest {

	@Mocked
	Logger log;

	@Before
	public void init() {
		new NonStrictExpectations() {
			{
				Logger.getLogger(withInstanceOf(String.class));
				result = log;
			}
		};

		new NonStrictExpectations() {
			{
				log.isLoggable(Level.SEVERE);
				result = true;

				log.isLoggable(Level.FINE);
				result = true;
			}
		};
	}

	@Test
	public void test() {
		Assert.assertEquals("havis.device.io.config", Environment.CONFIG_FILE);
		Assert.assertEquals("havis.device.io.common.TestHardwareManager", Environment.HARDWARE_CLASS);
		Assert.assertEquals("mica-gpio", Environment.HARDWARE_LIBRARY);
		Assert.assertNotNull(Environment.HARDWARE_DEFAULT);
	}
}