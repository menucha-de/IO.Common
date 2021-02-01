package havis.device.io.common.ext;

import havis.device.io.Direction;
import havis.device.io.State;
import havis.device.io.StateListener;
import havis.device.io.common.Environment;
import havis.device.io.common.HardwareManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class NativeHardwareManager implements HardwareManager {

	private static final Logger log = Logger.getLogger(NativeHardwareManager.class.getName());

	static {
		try {
			System.loadLibrary(Environment.HARDWARE_LIBRARY);
		} catch (Throwable e) {
			log.log(Level.SEVERE, "Failed to load system library", e);
		}
	}

	/**
	 * Gets the state
	 * 
	 * @param id
	 *            The pin id
	 * @return The pin state
	 */
	public native State getState(short id);

	/**
	 * Sets the state of an output port
	 * 
	 * @param id
	 *            The pin id
	 * @param state
	 *            The pin state
	 * @throws IllegalArgumentException
	 *             If pin direction is {@link Direction#INPUT} instead of
	 *             {@link Direction#OUTPUT}
	 */
	public native void setState(short id, State state) throws IllegalArgumentException;

	/**
	 * Gets the pin direction.
	 * 
	 * @param id
	 *            The pin id
	 * @return The pin direction
	 */
	public native Direction getDirection(short id);

	/**
	 * Sets the pin direction. Set direction to {@link Direction#INPUT} to use
	 * pin as input or {@link Direction#OUTPUT} to use pin as output.
	 * 
	 * @param id
	 *            The pin id
	 * @param direction
	 *            The pin direction
	 */
	public native void setDirection(short id, Direction direction);

	/**
	 * Gets the enable state of input pin
	 * 
	 * @param id
	 *            The pin id
	 * @return The enable state of input pin
	 * @throws IllegalArgumentException
	 *             If pin direction is {@link Direction#OUTPUT} instead of
	 *             {@link Direction#INPUT}
	 */
	public native boolean getEnable(short id) throws IllegalArgumentException;

	/**
	 * Sets the enable state of input pin
	 * 
	 * @param id
	 *            The pin id
	 * @param enable
	 *            The enable state of input pin
	 * @throws IllegalArgumentException
	 *             If pin direction is {@link Direction#OUTPUT} instead of
	 *             {@link Direction#INPUT}
	 */
	public native void setEnable(short id, boolean enable) throws IllegalArgumentException;

	/**
	 * Gets the pin count
	 * 
	 * @return The pin count
	 */
	public native short getCount();

	/**
	 * Sets the state listener. If listener is a valid reference, each state
	 * change of inputs between two polls will be reported. If the reference is
	 * invalid, reporting will we stopped.
	 * 
	 * @param listener
	 *            The state listener
	 */
	public native void setListener(StateListener listener);
}