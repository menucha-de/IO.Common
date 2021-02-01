package havis.device.io.common;

import havis.device.io.common.ext.NativeHardwareManager;
import mockit.Deencapsulation;

import org.junit.Test;

public class NativeTest {

	@Test
	public void test() throws java.io.IOException {

		// preparing invalid configuration
		Deencapsulation.setField(Environment.class, "HARDWARE_LIBRARY", "java");

		new NativeHardwareManager();
	}
}