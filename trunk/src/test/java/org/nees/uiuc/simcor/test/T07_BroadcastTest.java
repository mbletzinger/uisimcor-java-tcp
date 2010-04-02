package org.nees.uiuc.simcor.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nees.uiuc.simcor.UiSimCorTriggerBroadcast;
import org.nees.uiuc.simcor.factories.TransactionFactory;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.test.util.TriggerStateMachine;
import org.nees.uiuc.simcor.transaction.BroadcastTransaction;

public class T07_BroadcastTest {
	private int clientIdx = 0;
	private List<TriggerStateMachine> clients = new ArrayList<TriggerStateMachine>();
	private TcpParameters cparams = new TcpParameters();
	private final Logger log = Logger.getLogger(T07_BroadcastTest.class);
	private TcpParameters lparams = new TcpParameters();
	private int number = 0;

	private UiSimCorTriggerBroadcast simcor;

	private BroadcastTransaction broadcast() {
		number++;
		TransactionFactory tf = simcor.getTf();
		BroadcastTransaction transaction = tf.createBroadcastTransaction(
				number, 0, 0, 5000);
		log.debug("Assemble Broadcast " + transaction);
		simcor.startTransaction(transaction);
		TransactionStateNames state = simcor.isReady();
		while (state.equals(TransactionStateNames.TRANSACTION_DONE) == false) {
			state = simcor.isReady();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
			log.debug("Broadcasting " + simcor.getTransaction());
		}
		return transaction;
	}

	private void checkClientList(int expected) {
		String clientsStr = "";
		int activeC = 0;
		for (TriggerStateMachine c : clients) {
			clientsStr += c.getClientId() + "\n";
			if (c.isDone() == false) {
				activeC++;
			}
		}
		log.debug("Client List:\n" + clientsStr + "\nend list");
		Assert.assertEquals(expected, activeC);
	}

	private void checkTransaction(BroadcastTransaction transaction,
			boolean bmsgExpected, boolean rmsgExpected, int expected) {
		if (bmsgExpected) {
			Assert.assertNotNull(transaction.getBroadcastMsg());
		} else {
			Assert.assertNull(transaction.getBroadcastMsg());
		}
		if (rmsgExpected) {
			Assert.assertNotNull(transaction.getResponseMsg());
		} else {
			Assert.assertNull(transaction.getResponseMsg());
		}
		Assert.assertEquals(expected, transaction.getResponses().size());
	}

	private void endClient() {
		for (TriggerStateMachine client : clients) {
			if (client.isDone() == false) {
				client.setDone(true);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
				return;
			}
		}
	}

	@Before
	public void setUp() throws Exception {
		cparams.setRemoteHost("127.0.0.1");
		cparams.setRemotePort(6445);
		cparams.setTcpTimeout(5000);
		lparams.setLocalPort(6445);
		lparams.setTcpTimeout(5000);
		simcor = new UiSimCorTriggerBroadcast("MDL-00-00", "Broadcaster Test");
		simcor.startup(lparams);
		TransactionStateNames state = simcor.isReady();
		while (state.equals(TransactionStateNames.READY) == false) {
			state = simcor.isReady();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
			log.debug("Start Listening " + simcor.getTransaction());
		}
	}

	private void startClient() {
		String sys = "Client " + clientIdx;
		TriggerStateMachine client = new TriggerStateMachine(cparams, sys);
		client.start();
		clients.add(client);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		clientIdx++;
	}

	@After
	public void tearDown() throws Exception {
		for (TriggerStateMachine client : clients) {
			client.setDone(true);
			log.debug("Close " + client.getClientId()
					+ (client.isDone() ? " DONE" : " STILL RUNNING"));
		}

		boolean allDone = false;
		while (allDone == false) {
			allDone = true;
			for (TriggerStateMachine client : clients) {
				if (client.isAlive()) {
					allDone = false;
					log.debug("Waithing for " + client.getClientId()
							+ " to die"
							+ (client.isDone() ? " DONE" : " STILL RUNNING"));
				}
				Thread.sleep(500);
			}
			simcor.shutdown();
			TransactionStateNames state = simcor.isReady();
			while (state.equals(TransactionStateNames.READY) == false) {
				state = simcor.isReady();
				Thread.sleep(500);
				log.debug("Still Stopping Listener " + simcor.getTransaction());
			}

		}
		log.debug("TEST DONE");
	}

	@Test
	public void test00NoClientTriggering() {
		checkClientList(0);
		BroadcastTransaction transaction = broadcast();
		log.debug("Results for " + number + ": " + transaction);
		checkTransaction(transaction, false, false, 0);
	}

	@Test
	public void test01OneTriggering() {
		startClient();
		checkClientList(1);
		BroadcastTransaction transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, true, false, 1);
		transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, false, false, 1);
		endClient();
		checkClientList(0);
		transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, false, true, 0);
	}

	@Test
	public void test02TwoTriggering() {
		startClient();
		checkClientList(1);
		BroadcastTransaction transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, true, false, 1);
		startClient();
		checkClientList(2);
		transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, true, false, 2);
		endClient();
		checkClientList(1);
		transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, false, true, 1);
		endClient();
		checkClientList(0);
		transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, false, true, 0);
	}

	@Test
	public void test03BatchWithCloseTriggering() {
		startClient();
		startClient();
		startClient();
		startClient();
		checkClientList(4);
		BroadcastTransaction transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, true, false, 4);
		transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, false, false, 4);
		endClient();
		endClient();
		checkClientList(2);
		transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, false, true, 2);
		simcor.shutdown();
		TransactionStateNames state = simcor.isReady();
		while (state.equals(TransactionStateNames.READY) == false) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			state = simcor.isReady();
			log.debug("Closing down " + simcor.getTransaction());
		}
		checkClientList(0);
	}
	@Test
	public void test04OneClientWithDeadClientClose() {
		startClient();
		checkClientList(1);
		BroadcastTransaction transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, true, false, 1);
		transaction = broadcast();
		log.debug("Results for broadcast " + number + ": " + transaction);
		checkTransaction(transaction, false, false, 1);
		endClient();
		checkClientList(0);
		simcor.shutdown();
		TransactionStateNames state = simcor.isReady();
		while (state.equals(TransactionStateNames.READY) == false) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			state = simcor.isReady();
			log.debug("Closing down " + simcor.getTransaction());
		}
	}
}
