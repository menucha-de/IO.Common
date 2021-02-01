package havis.device.io.common;

import havis.device.io.Configuration;
import havis.device.io.Direction;
import havis.device.io.IOConsumer;
import havis.device.io.State;
import havis.device.io.StateEvent;
import havis.device.io.StateListener;
import havis.device.io.Type;
import havis.device.io.exception.ConnectionException;
import havis.device.io.exception.ImplementationException;
import havis.device.io.exception.ParameterException;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class MainController {

	private static final Logger log = Logger.getLogger(MainController.class.getName());

	static Class<?> clazz;

	private final Lock lock = new ReentrantLock();
	private Condition condition = lock.newCondition();
	private boolean pending;

	private static IOConsumer consumer;
	private static MainController instance;

	private static HardwareManager hardware;
	private static ConfigManager config;

	public static void init() {
		if (clazz == null) {
			try {
				clazz = Class.forName(Environment.HARDWARE_CLASS, true, Thread.currentThread().getContextClassLoader());

				hardware = (HardwareManager) clazz.newInstance();

				if (log.isLoggable(Level.FINE))
					log.log(Level.FINE, "Hardware manager instance {0} created", hardware);

				config = getConfig();
			} catch (Throwable e) {
				log.log(Level.SEVERE, "Initialization failed", e);
			}
		}
	}

	public static void dispose() {
		clazz = null;
		instance = null;
	}

	private MainController() throws ImplementationException {
		if (hardware == null)
			throw new ImplementationException("Invalid hardware mananger instance");
	}

	public static synchronized MainController getInstance() throws ImplementationException {
		if (instance == null)
			instance = new MainController();
		return instance;
	}

	private static ConfigManager getConfig() throws JsonParseException, JsonMappingException, ImplementationException, IOException {
		return new ConfigManager(new ConfigListener() {

			@Override
			public short getCount() {
				if (log.isLoggable(Level.FINE))
					log.log(Level.FINE, "Getting hardware count");
				return hardware.getCount();
			}

			@Override
			public void setDirection(short id, Direction direction) {
				if (log.isLoggable(Level.FINE))
					log.log(Level.FINE, "Setting hardware direction of pin {0} to {1}", new Object[] { id, direction });
				hardware.setDirection(id, direction);
			}

			@Override
			public void setState(short id, State state) {
				if (log.isLoggable(Level.FINE))
					log.log(Level.FINE, "Setting hardware state of pin {0} to {1}", new Object[] { id, state });
				hardware.setState(id, state);
			}

			@Override
			public State getState(short id) {
				if (log.isLoggable(Level.FINE))
					log.log(Level.FINE, "Getting hardware state of pin {0}", id);
				return hardware.getState(id);
			}

			@Override
			public void setEnable(short id, boolean enable) {
				if (log.isLoggable(Level.FINE))
					log.log(Level.FINE, "Setting hardware enable state of pin {0} to {1}", new Object[] { id, enable });
				hardware.setEnable(id, enable);
			}

			@Override
			public void keepAlive() {
				if (log.isLoggable(Level.FINER))
					log.log(Level.FINER, "Sending keep alive");
				if (consumer != null)
					consumer.keepAlive();
			}
		});
	}

	public void openConnection(IOConsumer consumer, int timeout) throws ConnectionException {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "Opening connection");

		lock.lock();
		try {

			if (MainController.consumer != null) {
				if (log.isLoggable(Level.FINE))
					log.log(Level.FINE, "Annother client is connected. Sending connection attempted to current client.");

				MainController.consumer.connectionAttempted();

				if (MainController.consumer != null)
					try {
						if (log.isLoggable(Level.FINE))
							log.log(Level.FINE, "Waiting {0} milliseconds for foreign client to close connection.", timeout);

						pending = true;
						Date date = new Date(new Date().getTime() + timeout);
						while (pending)
							if (!condition.awaitUntil(date)) {
								if (log.isLoggable(Level.FINE))
									log.log(Level.FINE, "Time out reached. Foreign client connection remains unchanged.");
								throw new ConnectionException("Timeout reached.");
							}
					} catch (InterruptedException e) {
						throw new ConnectionException("Timeout interrupted.");
					}
			}
			if (log.isLoggable(Level.FINE))
				log.log(Level.FINE, "Setting hardware listener to new consumer");

			MainController.consumer = consumer;

			hardware.setListener(new StateListener() {
				@Override
				public void stateChanged(StateEvent e) {
					if (log.isLoggable(Level.FINE))
						log.log(Level.FINE, "State of pin {0} changed to {1}", new Object[] { e.getId(), e.getState() });
					MainController.consumer.stateChanged(e);
				}
			});
		} finally {
			lock.unlock();
		}
	}

	public void closeConnection() {
		lock.lock();
		try {
			hardware.setListener(MainController.consumer = null);
			pending = false;
			condition.signal();
		} finally {
			lock.unlock();
		}
	}

	public void setConfiguration(List<Configuration> configuration) throws ParameterException, ImplementationException {
		lock.lock();
		try {
			config.set(configuration);
		} finally {
			lock.unlock();
		}
	}

	public List<Configuration> getConfiguration(Type type, short pin) throws ParameterException, ImplementationException {
		lock.lock();
		try {
			return config.get(type, pin);
		} finally {
			lock.unlock();
		}
	}

	public void resetConfiguration() throws ImplementationException {
		lock.lock();
		try {
			config.reset();
		} finally {
			lock.unlock();
		}
	}
}