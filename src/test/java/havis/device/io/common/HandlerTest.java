package havis.device.io.common;

import havis.device.io.Configuration;
import havis.device.io.Direction;
import havis.device.io.IOConfiguration;
import havis.device.io.IOConsumer;
import havis.device.io.KeepAliveConfiguration;
import havis.device.io.State;
import havis.device.io.StateEvent;
import havis.device.io.Type;
import havis.device.io.exception.ConnectionException;
import havis.device.io.exception.ImplementationException;
import havis.device.io.exception.ParameterException;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HandlerTest {

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

				log.isLoggable(Level.FINER);
				result = true;
			}
		};
	}

	@Test
	public void test(@Mocked final TestHardwareManager hardware) throws Exception, InterruptedException, java.io.IOException {

		// preparing configuration
		Deencapsulation.setField(Environment.class, "HARDWARE_CLASS", TestHardwareManager.class.getName());
		Deencapsulation.setField(Environment.class, "HARDWARE_LIBRARY", "mica-gpio");
		Deencapsulation.setField(Environment.class, "HARDWARE_DEFAULT", null);

		Deencapsulation.setField(MainController.class, "hardware", null);
		final CommunicationHandler handler = new CommunicationHandler();

		// trying non opened connection
		try {
			handler.closeConnection();
			Assert.fail();
		} catch (ConnectionException e) {
		}

		try {
			handler.setConfiguration(null);
			Assert.fail();
		} catch (ConnectionException e) {
		}

		try {
			handler.getConfiguration(null, (short) 0);
			Assert.fail();
		} catch (ConnectionException e) {
		}

		try {
			handler.resetConfiguration();
			Assert.fail();
		} catch (ConnectionException e) {
		}

		try {
			handler.openConnection(null, 0);
			Assert.fail();
		} catch (ImplementationException e) {
		}

		final File file = File.createTempFile("config", "json");
		file.deleteOnExit();

		// preparing configuration
		Deencapsulation.setField(Environment.class, "CONFIG_FILE", file.getPath());
		Deencapsulation
				.setField(
						Environment.class,
						"HARDWARE_DEFAULT",
						"{\"iolist\": [{\"id\": 1, \"direction\": \"INPUT\", \"state\": \"LOW\", \"enable\": true}, {\"id\": 2, \"direction\": \"OUTPUT\", \"state\": \"HIGH\", \"enable\": false}, {\"id\": 3, \"direction\": \"INPUT\", \"state\": \"LOW\", \"enable\": true}, {\"id\": 4, \"direction\": \"OUTPUT\", \"state\": \"LOW\", \"enable\": false}]}");

		try {
			handler.openConnection(null, 0);
			Assert.fail();
		} catch (ImplementationException e) {
		}

		// opening connection with semaphore
		final Semaphore semaphore = new Semaphore(-3);

		handler.openConnection(new IOConsumer() {

			public void keepAlive() {
				semaphore.release();
			}

			public void connectionAttempted() {
				new Timer().schedule(new TimerTask() {

					public void run() {
						try {
							handler.closeConnection();
						} catch (Exception e) {
						}
					}
				}, 500);
			}

			public void stateChanged(StateEvent e) {
			}
		}, 1000);

		// setting invalid IO count
		new NonStrictExpectations() {
			{
				hardware.getCount();
				result = 5;
			}
		};
		// requesting non existing IO configuration
		try {
			handler.getConfiguration(Type.IO, (short) 5);
			Assert.fail();
		} catch (ImplementationException e) {
		}

		new NonStrictExpectations() {
			{
				hardware.getCount();
				result = 4;
			}
		};

		Configuration[] configurations = new Configuration[] { new IOConfiguration((short) 1, Direction.INPUT, State.LOW, true),
				new IOConfiguration((short) 2, Direction.INPUT, State.LOW, true), new IOConfiguration((short) 3, Direction.OUTPUT, State.HIGH, false),
				new IOConfiguration((short) 4, Direction.OUTPUT, State.HIGH, false) };
		handler.setConfiguration(Arrays.asList(configurations));

		new Verifications() {
			{
				hardware.setDirection((short) 1, Direction.INPUT);
				times = 0;

				hardware.setState((short) 1, State.LOW);
				times = 0;

				hardware.setEnable((short) 1, anyBoolean);
				times = 0;

				hardware.setDirection((short) 2, Direction.INPUT);
				times = 1;

				hardware.setState((short) 2, State.LOW);
				times = 1;

				hardware.setEnable((short) 2, true);
				times = 1;

				hardware.setDirection((short) 3, Direction.OUTPUT);
				times = 1;

				hardware.setState((short) 3, State.HIGH);
				times = 1;

				hardware.setEnable((short) 3, false);
				times = 1;

				hardware.setDirection((short) 4, Direction.OUTPUT);
				times = 0;

				hardware.setState((short) 4, State.HIGH);
				times = 1;

				hardware.setEnable((short) 4, anyBoolean);
				times = 0;
			}
		};

		// requesting ALL configuration
		List<Configuration> actuals = handler.getConfiguration(Type.ALL, (short) 0);

		Assert.assertTrue(actuals.containsAll(Arrays.asList(configurations)));

		// requesting KEEP_ALIVE configuration
		actuals = handler.getConfiguration(Type.KEEP_ALIVE, (short) 0);
		Assert.assertEquals(1, actuals.size());
		Assert.assertTrue(actuals.get(0) instanceof KeepAliveConfiguration);

		// requesting whole IO configuration
		actuals = handler.getConfiguration(Type.IO, (short) 0);
		Assert.assertEquals(4, actuals.size());

		// requesting specific IO configuration
		actuals = handler.getConfiguration(Type.IO, (short) 3);
		Assert.assertEquals(1, actuals.size());
		Assert.assertEquals(configurations[2], actuals.get(0));

		// requesting invalid IO configuration
		try {
			actuals = handler.getConfiguration(Type.IO, (short) 9);
			Assert.fail();
		} catch (ParameterException e) {
		}

		// setting keep alive configuration
		handler.setConfiguration(Arrays.asList(new Configuration[] { new KeepAliveConfiguration(true, 100) }));
		if (semaphore.tryAcquire(300, TimeUnit.MILLISECONDS)) {
			try {
				handler.setConfiguration(Arrays.asList(new Configuration[] { new KeepAliveConfiguration(true, 0) }));
				Assert.fail();
			} catch (ParameterException e) {
			}
			handler.setConfiguration(Arrays.asList(new Configuration[] { new KeepAliveConfiguration(true, 250) }));
			handler.setConfiguration(Arrays.asList(new Configuration[] { new KeepAliveConfiguration(false, 0) }));
			semaphore.drainPermits();
			if (semaphore.tryAcquire(300, TimeUnit.MILLISECONDS)) {
				Assert.fail("Should not be possible to aquire");
			}
		} else {
			Assert.fail();
		}

		// setting invalid output pin enable state
		handler.setConfiguration(Arrays.asList(new Configuration[] { new IOConfiguration((short) 3, Direction.OUTPUT, State.LOW, true) }));

		new NonStrictExpectations() {
			{
				hardware.setEnable((short) 3, anyBoolean);
				times = 0;
			}
		};

		// setting invalid input pin state
		handler.setConfiguration(Arrays.asList(new Configuration[] { new IOConfiguration((short) 1, Direction.INPUT, State.HIGH, true) }));

		new NonStrictExpectations() {
			{
				hardware.setState((short) 1, State.HIGH);
				times = 0;
			}
		};

		// setting pin configuration with non positive pin id
		try {
			handler.setConfiguration(Arrays.asList(new Configuration[] { new IOConfiguration((short) 0, Direction.INPUT, State.LOW, true) }));
			Assert.fail();
		} catch (ParameterException e) {
		}

		new NonStrictExpectations() {
			{
				hardware.getCount();
				result = 1;
			}
		};

		// setting pin configuration with invalid pin id
		try {
			handler.setConfiguration(Arrays.asList(new Configuration[] { new IOConfiguration((short) 9, Direction.INPUT, State.LOW, true) }));
			Assert.fail();
		} catch (ParameterException e) {
		}

		// setting invalid direction
		try {
			handler.setConfiguration(Arrays.asList(new Configuration[] { new IOConfiguration((short) 1, (Direction) null, (State) null, false) }));
			Assert.fail();
		} catch (ParameterException e) {
		}

		// preparing invalid configuration
		Deencapsulation.setField(Environment.class, "HARDWARE_DEFAULT", null);

		try {
			handler.resetConfiguration();
			Assert.fail();
		} catch (ImplementationException e) {
		}

		CommunicationHandler tmp = new CommunicationHandler();
		try {
			tmp.openConnection(null, 250);
		} catch (ConnectionException e) {
		}

		tmp.openConnection(null, 250);
	}
}