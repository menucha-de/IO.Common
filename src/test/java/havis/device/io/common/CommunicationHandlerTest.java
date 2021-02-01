package havis.device.io.common;

import havis.device.io.Configuration;
import havis.device.io.Direction;
import havis.device.io.IOConfiguration;
import havis.device.io.IOConsumer;
import havis.device.io.State;
import havis.device.io.StateEvent;
import havis.device.io.Type;
import havis.device.io.common.CommunicationHandler;
import havis.device.io.exception.IOException;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;

public class CommunicationHandlerTest {

	// @Capturing

	@Ignore
	public void test() throws IOException, java.io.IOException {
		CommunicationHandler handler = new CommunicationHandler();
		handler.openConnection(new IOConsumer() {

			public void keepAlive() {
			}

			public void connectionAttempted() {
			}

			public void stateChanged(StateEvent e) {
				if (e != null) {
					System.out.println(String.format("State changed %d %s",
							e.getId(), e.getState()));
				}
			}
		}, 1000);

		IOConfiguration c1 = new IOConfiguration();
		c1.setId((short) 1);
		c1.setDirection(Direction.INPUT);
		c1.setEnable(false);

		IOConfiguration c2 = new IOConfiguration();
		c2.setId((short) 2);
		c2.setDirection(Direction.OUTPUT);
		c2.setState(State.HIGH);

		IOConfiguration c3 = new IOConfiguration();
		c3.setId((short) 3);
		c3.setDirection(Direction.OUTPUT);
		c3.setState(State.HIGH);

		IOConfiguration c4 = new IOConfiguration();
		c4.setId((short) 4);
		c4.setDirection(Direction.OUTPUT);
		c4.setState(State.HIGH);

		IOConfiguration c7 = new IOConfiguration();
		c7.setId((short) 7);
		c7.setDirection(Direction.OUTPUT);
		c7.setState(State.HIGH);

		IOConfiguration c8 = new IOConfiguration();
		c8.setId((short) 8);
		c8.setDirection(Direction.INPUT);
		c8.setEnable(true);

		List<Configuration> configurations = new ArrayList<>();
		configurations.add(c1);
		configurations.add(c2);
		configurations.add(c3);
		configurations.add(c4);
		configurations.add(c7);
		configurations.add(c8);
		handler.setConfiguration(configurations);

		IOConfiguration r;

		configurations = handler.getConfiguration(Type.IO, (short) 8);
		r = (IOConfiguration) configurations.get(0);
		System.out.println("State 1: " + r.getState());
		System.out.println("Press <ENTER> to continue.");
		System.in.read();

		configurations = handler.getConfiguration(Type.IO, (short) 8);
		r = (IOConfiguration) configurations.get(0);
		System.out.println("State 2: " + r.getState());
		System.out.println("Press <ENTER> to continue.");
		System.in.read();

		configurations = handler.getConfiguration(Type.IO, (short) 8);
		r = (IOConfiguration) configurations.get(0);
		System.out.println("State 3: " + r.getState());
		System.out.println("Press <ENTER> to continue.");
		System.in.read();

		handler.closeConnection();
	}
}