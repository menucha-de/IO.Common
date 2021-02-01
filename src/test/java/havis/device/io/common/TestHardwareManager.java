package havis.device.io.common;

import havis.device.io.Direction;
import havis.device.io.State;
import havis.device.io.StateListener;

public class TestHardwareManager implements HardwareManager {

	Direction[] direction = { Direction.INPUT, Direction.OUTPUT, Direction.INPUT, Direction.OUTPUT };
	State[] state = { State.LOW, State.HIGH, State.LOW, State.LOW };
	boolean[] enable = { true, false, true, false };

	@Override
	public State getState(short id) {
		return state[id - 1];
	}

	@Override
	public void setState(short id, State state) throws IllegalArgumentException {
		this.state[id - 1] = state;
	}

	@Override
	public Direction getDirection(short id) {
		return direction[id - 1];
	}

	@Override
	public void setDirection(short id, Direction direction) {
		this.direction[id - 1] = direction;
	}

	@Override
	public boolean getEnable(short id) throws IllegalArgumentException {
		return enable[id - 1];
	}

	@Override
	public void setEnable(short id, boolean enable) throws IllegalArgumentException {
		this.enable[id - 1] = enable;
	}

	@Override
	public short getCount() {
		return 4;
	}

	@Override
	public void setListener(StateListener listener) {
	}
}