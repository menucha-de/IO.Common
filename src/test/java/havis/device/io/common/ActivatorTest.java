package havis.device.io.common;

import havis.device.io.IODevice;
import havis.device.io.common.ext.Activator;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleWire;

public class ActivatorTest {

	ServiceFactory<IODevice> factory;
	ServiceRegistration<?> registration;

	@Test
	public void test(@Mocked final BundleContext bundleContext, @Mocked final Bundle bundle,@Mocked final BundleWire bundleWire) throws Exception {

		new NonStrictExpectations() {
			{
				bundleContext.getBundle();
				result = bundle;

				bundle.adapt(BundleWire.class);
				result = bundleWire;
			}
		};

		Activator activator = new Activator();

		activator.start(bundleContext);
		new Verifications() {
			{
				ServiceFactory<IODevice> factory;

				bundleContext.registerService(IODevice.class.getName(), factory = withCapture(), null);
				times = 1;

				ActivatorTest.this.factory = factory;
			}
		};

		factory.getService(null, null);
		factory.ungetService(null, null, null);

		activator.stop(bundleContext);

	}
}
