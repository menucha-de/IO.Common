package havis.device.io.common;

import havis.device.io.Direction;
import havis.device.io.State;
import havis.device.io.StateListener;

public interface HardwareManager {

	/**
	 * Gets the state
	 * 
	 * @param id
	 *            The pin id
	 * @return The pin state
	 */
	State getState(short id);

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
	void setState(short id, State state) throws IllegalArgumentException;

	/**
	 * Gets the pin direction.
	 * 
	 * @param id
	 *            The pin id
	 * @return The pin direction
	 */
	Direction getDirection(short id);

	/**
	 * Sets the pin direction. Set direction to {@link Direction#INPUT} to use
	 * pin as input or {@link Direction#OUTPUT} to use pin as output.
	 * 
	 * @param id
	 *            The pin id
	 * @param direction
	 *            The pin direction
	 */
	void setDirection(short id, Direction direction);

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
	boolean getEnable(short id) throws IllegalArgumentException;

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
	void setEnable(short id, boolean enable) throws IllegalArgumentException;

	/**
	 * Gets the pin count
	 * 
	 * @return The pin count
	 */
	short getCount();

	/**
	 * Sets the state listener. If listener is a valid reference, each state
	 * change of inputs between two polls will be reported. If the reference is
	 * invalid, reporting will we stopped.
	 * 
	 * @param listener
	 *            The state listener
	 */
	void setListener(StateListener listener);
}