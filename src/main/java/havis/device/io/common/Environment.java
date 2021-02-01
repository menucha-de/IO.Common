package havis.device.io.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Environment {

	private static final Logger log = Logger.getLogger(Environment.class.getName());

	private static final Properties properties = new Properties();
	static {
		try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("Environment.properties")) {
			if (stream != null)
				properties.load(stream);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Failed to load environment properties", e);
		}
	}

	public final static String CONFIG_FILE = properties.getProperty("havis.device.io.configManagerFile", "conf/havis/device/io/config.json");
	public final static String HARDWARE_CLASS = properties.getProperty("havis.device.io.hardwareManagerClass",
			"havis.device.io.common.ext.NativeHardwareManager");
	public final static String HARDWARE_LIBRARY = properties.getProperty("havis.device.io.hardwareManagerLibrary");
	public final static String HARDWARE_DEFAULT = properties.getProperty("havis.device.io.hardwareManagerDefault");
}