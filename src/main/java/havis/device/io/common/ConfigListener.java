package havis.device.io.common;

import havis.device.io.Direction;
import havis.device.io.State;

interface ConfigListener {

	short getCount();

	void setDirection(short id, Direction direction);

	void setState(short id, State state);

	State getState(short id);

	void setEnable(short id, boolean enable);

	void keepAlive();

}