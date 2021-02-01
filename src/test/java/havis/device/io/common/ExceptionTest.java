package havis.device.io.common;

import havis.device.io.common.CommunicationHandler;
import havis.device.io.common.MainController;
import havis.device.io.exception.IOException;
import havis.device.io.exception.ImplementationException;

import java.util.logging.Level;
import java.util.logging.Logger;

import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ExceptionTest {

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

	@SuppressWarnings("serial")
	@Test
	public void test(@Mocked final MainController controller)
			throws IOException {

		CommunicationHandler handler = new CommunicationHandler();

		// preparing invalid controller
		new NonStrictExpectations() {
			{
				MainController.getInstance();
				result = controller;

				controller.openConnection(null, 0);
			}
		};
		handler.openConnection(null, 0);

		// preparing invalid controller
		new NonStrictExpectations() {
			{
				controller.openConnection(null, 0);
				result = new Throwable();

				controller.closeConnection();
				result = new Throwable();

				controller.setConfiguration(null);
				result = new Throwable();

				controller.getConfiguration(null, (short) 0);
				result = new Throwable();

				controller.resetConfiguration();
				result = new Throwable() {
				};
			}
		};

		try {
			handler.openConnection(null, 0);
			Assert.fail();
		} catch (ImplementationException e) {
		}

		try {
			handler.closeConnection();
			Assert.fail();
		} catch (ImplementationException e) {
		}

		try {
			handler.setConfiguration(null);
			Assert.fail();
		} catch (ImplementationException e) {
		}

		try {
			handler.getConfiguration(null, (short) 0);
			Assert.fail();
		} catch (ImplementationException e) {
		}

		try {
			handler.resetConfiguration();
			Assert.fail();
		} catch (ImplementationException e) {
		}
	}
}