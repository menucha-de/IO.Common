package havis.device.io.common;

import havis.device.io.Configuration;
import havis.device.io.IOConsumer;
import havis.device.io.IODevice;
import havis.device.io.Type;
import havis.device.io.exception.ConnectionException;
import havis.device.io.exception.IOException;
import havis.device.io.exception.ImplementationException;
import havis.device.io.exception.ParameterException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommunicationHandler implements IODevice {

	private static final Logger log = Logger.getLogger(CommunicationHandler.class.getName());

	private MainController controller;

	public CommunicationHandler() {
		MainController.init();
	}

	public void openConnection(IOConsumer consumer, int timeout) throws ConnectionException, ImplementationException {
		try {
			MainController controller = MainController.getInstance();
			controller.openConnection(consumer, timeout);
			this.controller = controller;
		} catch (IOException e) {
			throw e;
		} catch (Throwable e) {
			if (log.isLoggable(Level.SEVERE))
				log.log(Level.SEVERE, "Failed to open connection", e);
			throw new ImplementationException("Failed to open connection", e);
		}
	}

	public void closeConnection() throws ConnectionException, ImplementationException {
		if (controller == null)
			throw new ConnectionException();
		try {
			controller.closeConnection();
			controller = null;
		} catch (Throwable e) {
			if (log.isLoggable(Level.SEVERE))
				log.log(Level.SEVERE, "Failed to close connection", e);
			throw new ImplementationException("Failed to close connection", e);
		}
	}

	public void setConfiguration(List<Configuration> configuration) throws ConnectionException, ParameterException, ImplementationException {
		if (controller == null)
			throw new ConnectionException();
		try {
			controller.setConfiguration(configuration);
		} catch (IOException e) {
			throw e;
		} catch (Throwable e) {
			if (log.isLoggable(Level.SEVERE))
				log.log(Level.SEVERE, "Failed to set configuration", e);
			throw new ImplementationException("Failed to set configuration", e);
		}
	}

	public List<Configuration> getConfiguration(Type type, short pin) throws ConnectionException, ParameterException, ImplementationException {
		if (controller == null)
			throw new ConnectionException();
		try {
			return controller.getConfiguration(type, pin);
		} catch (IOException e) {
			throw e;
		} catch (Throwable e) {
			if (log.isLoggable(Level.SEVERE))
				log.log(Level.SEVERE, "Failed to get configuration", e);
			throw new ImplementationException("Failed to get configuration", e);
		}
	}

	public void resetConfiguration() throws ConnectionException, ImplementationException {
		if (controller == null)
			throw new ConnectionException();
		try {
			controller.resetConfiguration();
		} catch (IOException e) {
			throw e;
		} catch (Throwable e) {
			if (log.isLoggable(Level.SEVERE))
				log.log(Level.SEVERE, "Failed to reset configuration", e);
			throw new ImplementationException("Failed to reset connection", e);
		}
	}
}