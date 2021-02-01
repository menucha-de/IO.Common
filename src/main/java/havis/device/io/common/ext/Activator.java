package havis.device.io.common.ext;

import havis.device.io.IODevice;
import havis.device.io.common.CommunicationHandler;
import havis.device.io.common.MainController;

import java.io.IOException;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleWiring;

public class Activator implements BundleActivator {

	Logger log = Logger.getLogger(Activator.class.getName());
	private ServiceRegistration<?> serviceRegistration;

	@Override
	public void start(BundleContext context) throws Exception {
		ClassLoader current = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(context.getBundle().adapt(BundleWiring.class).getClassLoader());
			registerService(context);
		} finally {
			Thread.currentThread().setContextClassLoader(current);
		}
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		unregisterService();
	}

	private void registerService(BundleContext context) throws IOException {
		MainController.init();
		if (serviceRegistration == null) {
			serviceRegistration = context.registerService(IODevice.class.getName(), new ServiceFactory<IODevice>() {
				@Override
				public IODevice getService(Bundle bundle, ServiceRegistration<IODevice> registration) {
					return new CommunicationHandler();
				}

				@Override
				public void ungetService(Bundle bundle, ServiceRegistration<IODevice> registration, IODevice service) {
					/* RFU */
				}
			}, null);
		}
	}

	private void unregisterService() {
		MainController.dispose();
		if (serviceRegistration != null)
			serviceRegistration.unregister();
	}
}
