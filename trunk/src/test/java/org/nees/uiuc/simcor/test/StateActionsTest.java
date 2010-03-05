package org.nees.uiuc.simcor.test;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.test.StateActionsResponder.LifeSpanType;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class StateActionsTest {
	private StateActionsResponder rspdr;
	private StateActionsProcessor sap;
	private TcpParameters rparams = new TcpParameters();
	private TcpParameters lparams = new TcpParameters();
	private Transaction transaction;
	private final Logger log = Logger.getLogger(StateActionsTest.class);

	@Before
	public void setUp() throws Exception {
		sap = new StateActionsProcessor();
		rparams.setRemoteHost("127.0.0.1");
		rparams.setRemotePort(6445);
		rparams.setTcpTimeout(2000);
		lparams.setLocalPort(6445);
		lparams.setTcpTimeout(2000);
		sap.setParams(lparams);
		sap.setIdentity("MDL-00-00", "Connection Test");

		transaction = sap.getTf().createTransaction(new SimCorMsg());
		transaction.setTimeout(2000);
	}

	@After
	public void tearDown() throws Exception {

		while (rspdr.isAlive()) {
			log.debug("Waiting for responder shutdown");
			Thread.sleep(1000);
		}
	}

	@Test
	public void testStartListenerFail() {
		sap.startListening(transaction);
		rspdr = new StateActionsResponder(LifeSpanType.OPEN_COMMAND, rparams,
				true); // never started

		sap.checkOpenConnection(transaction,
				TransactionStateNames.TRANSACTION_DONE);
		log.debug("Local Transaction after open connection: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.OPENING_CONNECTION,
				transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
				.getError().getType());
		sap.closingConnection(transaction,
				TransactionStateNames.CLOSING_CONNECTION);
		while (transaction.getState().equals(
				TransactionStateNames.CLOSING_CONNECTION)) {
			sap.closingConnection(transaction,
					TransactionStateNames.STOP_LISTENING);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
		}
		log.debug("Local Transaction after close connection: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.STOP_LISTENING,
				transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
				.getError().getType());
		shutdown();
	}

	@Test
	public void testOpenSessionReadFail() {
		setupConnection(LifeSpanType.OPEN_COMMAND);
		read(TransactionStateNames.READ_COMMAND,
				TransactionStateNames.COMMAND_AVAILABLE, true);
		shutdown();
	}

	@Test
	public void testOpenSessionWriteFail() {
		setupConnection(LifeSpanType.OPEN_RESPONSE);
		write(TransactionStateNames.SEND_OPEN_SESSION,
				TransactionStateNames.WAIT_FOR_RESPONSE);
		read(TransactionStateNames.SEND_OPEN_SESSION,
				TransactionStateNames.SEND_OPEN_SESSION_RESPONSE, true);
		shutdown();
	}

	@Test
	public void testCloseSessionWriteFail() {
		setupConnection(LifeSpanType.CLOSE_COMMAND);
		write(TransactionStateNames.SEND_OPEN_SESSION,
				TransactionStateNames.WAIT_FOR_RESPONSE);
		read(TransactionStateNames.SEND_OPEN_SESSION,
				TransactionStateNames.WAIT_FOR_RESPONSE, false);
		write(TransactionStateNames.SEND_CLOSE_SESSION,
				TransactionStateNames.TRANSACTION_DONE);
		shutdown();
	}

	@Test
	public void testCloseSessionReadFail() {
		setupConnection(LifeSpanType.CLOSE_COMMAND);
		read(TransactionStateNames.READ_COMMAND,
				TransactionStateNames.COMMAND_AVAILABLE, false);
		write(TransactionStateNames.SEND_OPEN_SESSION_RESPONSE,
				TransactionStateNames.TRANSACTION_DONE);
		read(TransactionStateNames.READ_COMMAND,
				TransactionStateNames.COMMAND_AVAILABLE, true);
		shutdown();
	}

	private void shutdown() {
		sap.closingConnection(transaction,
				TransactionStateNames.CLOSING_CONNECTION);
		while (transaction.getState().equals(
				TransactionStateNames.CLOSING_CONNECTION)) {
			sap.closingConnection(transaction,
					TransactionStateNames.STOP_LISTENING);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
		}
		log.debug("Local Transaction after close connection: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.TRANSACTION_DONE,
				transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
				.getError().getType());
		sap.stopListening(transaction);
		log.debug("Local Transaction after stop listener: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.TRANSACTION_DONE,
				transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
				.getError().getType());
	}

	private void setupConnection(LifeSpanType lfsp) {
		sap.startListening(transaction);

		rspdr = new StateActionsResponder(lfsp, rparams, true);
		rspdr.start();

		sap.checkOpenConnection(transaction,
				TransactionStateNames.TRANSACTION_DONE);
		log.debug("Local Transaction after open connection: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.TRANSACTION_DONE,
				transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
				.getError().getType());
	}

	private void read(TransactionStateNames current,
			TransactionStateNames next, boolean errorExpected) {
		sap.setUpRead(transaction, false, current);
		while (transaction.getState().equals(current)) {
			sap.waitForRead(transaction, false, next);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
		}
		log.debug("Local Transaction after read open session command: "
				+ transaction);
		log.debug("Remote Transaction: " + rspdr.getTransaction());
		if (errorExpected) {
			org.junit.Assert.assertEquals(
					TransactionStateNames.CLOSING_CONNECTION, transaction
							.getState());
			org.junit.Assert.assertEquals(TcpErrorTypes.IO_ERROR, transaction
					.getError().getType());
		} else {
			org.junit.Assert.assertEquals(next, transaction.getState());
			org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
					.getError().getType());
		}
	}

	private void write(TransactionStateNames current, TransactionStateNames next) {
		sap.assembleSessionMessage(transaction, true, false, current);
		while (transaction.getState().equals(current)) {
			sap.waitForSend(transaction, next);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
		}
		log.debug("Local Transaction after write close session command: "
				+ transaction);
		org.junit.Assert.assertEquals(next, transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
				.getError().getType());
	}
}
