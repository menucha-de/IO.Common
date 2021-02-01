package havis.device.io.common;

import havis.device.io.Configuration;
import havis.device.io.Direction;
import havis.device.io.GlobalConfiguration;
import havis.device.io.IOConfiguration;
import havis.device.io.KeepAliveConfiguration;
import havis.device.io.State;
import havis.device.io.Type;
import havis.device.io.exception.ImplementationException;
import havis.device.io.exception.ParameterException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigManager {

	private static final Logger log = Logger.getLogger(ConfigManager.class.getName());
	File file;

	ObjectMapper mapper = new ObjectMapper();

	GlobalConfiguration global;

	private ConfigListener listener;

	Timer timer = new Timer();

	ConfigManager(ConfigListener listener) throws ImplementationException {
		this.listener = listener;

		file = new File(Environment.CONFIG_FILE);

		read();
	}

	public void set(List<Configuration> configurations) throws ParameterException, ImplementationException {
		for (Configuration configuration : configurations) {
			if (configuration instanceof IOConfiguration) {
				set(((IOConfiguration) configuration).clone());
				continue;
			}
			if (configuration instanceof KeepAliveConfiguration) {
				set(((KeepAliveConfiguration) configuration).clone());
				continue;
			}
		}
		write();
	}

	private void set(IOConfiguration configuration) throws ParameterException {
		short id = configuration.getId();
		if (id > 0) {
			short count = listener.getCount();
			if (id < count + 1) {
				int index = global.getIOList().indexOf(new IOConfiguration(id));
				if (index > -1) {
					List<IOConfiguration> configurations = global.getIOList();
					IOConfiguration current = configurations.get(index);
					if (configuration.getDirection() != null) {
						switch (configuration.getDirection()) {
						case OUTPUT:
							switch (current.getDirection()) {
							case INPUT:
								if (configuration.getState() == State.HIGH) {
									listener.setState(id, State.HIGH);
								}
								listener.setDirection(id, Direction.OUTPUT);
								if (current.isEnable())
									listener.setEnable(id, false);
								break;
							case OUTPUT:
								State state = configuration.getState();
								if (current.getState() != state)
									listener.setState(id, state);
								break;
							}
							break;
						case INPUT:
							if (current.getState() == State.HIGH) {
								listener.setState(id, State.LOW);
							}
							switch (current.getDirection()) {
							case OUTPUT:
								listener.setDirection(id, Direction.INPUT);
								break;
							case INPUT:
								break;
							}
							boolean enable = configuration.isEnable();
							if (current.isEnable() != enable)
								listener.setEnable(id, enable);
							break;
						}
						configurations.set(index, configuration);
					} else {
						throw new ParameterException("Direction is undefined");
					}
				} else {
					throw new ParameterException("Pin is unknown");
				}
			} else {
				throw new ParameterException("Pin is higher then pin count");
			}
		} else {
			throw new ParameterException("Pin must be positive");
		}
	}

	private void setKeepAlive(int interval) {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (interval > 0) {
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (timer != null)
						listener.keepAlive();
				}
			}, 0, interval);
		}
	}

	private void set(KeepAliveConfiguration keepAlive) throws ParameterException {
		if (global.getKeepAlive() != null && global.getKeepAlive().isEnable() && timer != null) {
			timer.cancel();
			timer = null;
		}
		if (keepAlive != null) {
			if (keepAlive.isEnable()) {
				if (keepAlive.getInterval() > 0) {
					setKeepAlive(keepAlive.getInterval());
				} else {
					throw new ParameterException("Keep alive interval must me greater then zero!");
				}
			} else {
				setKeepAlive(0);
			}
		} else {
			keepAlive = new KeepAliveConfiguration();
		}
		global.setKeepAlive(keepAlive);
	}

	private void add(List<Configuration> configurations, IOConfiguration configuration) {
		if (configuration.getDirection() == Direction.INPUT) {
			configuration.setState(listener.getState(configuration.getId()));
			if (log.isLoggable(Level.FINE))
				log.log(Level.FINE, "Current state of input pin {0} is {1}", new Object[] { configuration.getId(), configuration.getState() });
		}
		configurations.add(configuration.clone());
	}

	private void add(List<Configuration> configurations, short pin) throws ParameterException, ImplementationException {
		if (pin == 0) {
			for (IOConfiguration configuration : global.getIOList()) {
				add(configurations, configuration);
			}
		} else {
			if (pin < listener.getCount() + 1) {
				IOConfiguration configuration = global.getIOList().get(global.getIOList().indexOf(new IOConfiguration(pin)));
				if (configuration == null) {
					throw new ImplementationException("Pin id '" + pin + "' is not available");
				} else {
					add(configurations, configuration);
				}
			} else {
				throw new ParameterException("Pin id '" + pin + "' is out of range");
			}
		}
	}

	public List<Configuration> get(Type type, short pin) throws ParameterException, ImplementationException {
		List<Configuration> configurations = new ArrayList<Configuration>();
		switch (type) {
		case ALL:
			add(configurations, pin = 0);
			configurations.add(global.getKeepAlive().clone());
			break;
		case IO:
			add(configurations, pin);
			break;
		case KEEP_ALIVE:
			configurations.add(global.getKeepAlive().clone());
			break;
		}
		return configurations;
	}

	private void init() throws ImplementationException {
		List<IOConfiguration> ios = global.getIOList();
		if (ios != null) {
			for (IOConfiguration io : ios) {
				short id = io.getId();
				listener.setDirection(id, io.getDirection());
				switch (io.getDirection()) {
				case INPUT:
					listener.setState(id, State.LOW);
					listener.setEnable(id, io.isEnable());
					break;
				case OUTPUT:
					State state = io.getInitialState() == null ? io.getState() : io.getInitialState();
					io.setState(state);
					listener.setState(id, state);
					listener.setEnable(id, false);
					break;
				}
			}
		}
		try {
			set(global.getKeepAlive());
		} catch (ParameterException e) {
			throw new ImplementationException("Initialization failed", e);
		}
	}

	public void reset() throws ImplementationException {
		try {
			global = mapper.readValue(Environment.HARDWARE_DEFAULT, GlobalConfiguration.class);
			init();
		} catch (Exception e) {
			throw new ImplementationException("Failed to read default configuration", e);
		}
		if (file != null && file.isFile())
			file.delete();
	}

	private void read() throws ImplementationException {
		if (file != null && file.isFile()) {
			try {
				global = mapper.readValue(file, GlobalConfiguration.class);
				init();
			} catch (Exception e) {
				reset();
				throw new ImplementationException("Failed to read configuration!", e);
			}
		} else {
			reset();
		}
	}

	private void write() throws ImplementationException {
		if (file != null) {
			try {

				if (!file.exists()) {
					Path parent = file.toPath().getParent();
					if (parent != null)
						Files.createDirectories(parent, new FileAttribute<?>[] {});
				}
				File tmpFile = File.createTempFile(Environment.CONFIG_FILE, ".tmp", file.getParentFile());
				mapper.writeValue(tmpFile, global);
				Files.move(tmpFile.toPath(), this.file.toPath(), StandardCopyOption.ATOMIC_MOVE);
			} catch (Exception e) {
				throw new ImplementationException("Failed to write configuration!", e);
			}
		}
	}
}