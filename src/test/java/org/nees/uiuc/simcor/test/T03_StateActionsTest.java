package org.nees.uiuc.simcor.test;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithLcf;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.test.util.StateActionsResponder;
import org.nees.uiuc.simcor.test.util.StateActionsResponder.DieBefore;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;

public class T03_StateActionsTest {
	private final Logger log = Logger.getLogger(T03_StateActionsTest.class);
	private TcpParameters lparams = new TcpParameters();
	private TcpParameters rparams = new TcpParameters();
	private StateActionsResponder rspdr;
	private StateActionsProcessorWithLcf sap;
	private SimpleTransaction transaction;

	private void read(boolean errorExpected, boolean isCommand) {
		String cmdStr;
		TransactionStateNames wastate;
		TransactionStateNames setstate;
		TransactionStateNames next;
		if (isCommand) {
			cmdStr = "command";
			setstate = TransactionStateNames.SETUP_READ_OPEN_COMMAND;
			wastate = TransactionStateNames.WAIT_FOR_OPEN_COMMAND;
			next = TransactionStateNames.COMMAND_AVAILABLE;
		} else {
			cmdStr = "response";
			setstate = TransactionStateNames.SETUP_READ_RESPONSE;
			wastate = TransactionStateNames.WAIT_FOR_RESPONSE;
			next = TransactionStateNames.RESPONSE_AVAILABLE;
		}
		transaction.setState(setstate);
		sap.setUpRead(transaction, false, wastate);
		while (transaction.getState().equals(wastate)) {
			transaction.setState(wastate);
			sap.waitForRead(transaction, isCommand, next);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
			log.debug("Local Transaction during read " + cmdStr + " message: "
					+ transaction);
		}
		log.debug("Local Transaction after read " + cmdStr + " message: "
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

	@Before
	public void setUp() throws Exception {
		sap = new StateActionsProcessorWithLcf();
		rparams.setRemoteHost("127.0.0.1");
		rparams.setRemotePort(6445);
		rparams.setTcpTimeout(2000);
		lparams.setLocalPort(6445);
		lparams.setTcpTimeout(2000);
		sap.setParams(lparams);
		sap.setIdentity("MDL-00-00", "Connection Test");

		transaction = sap.getTf().createSendCommandTransaction(new SimCorMsg(),
				2000);
	}

	private void setupConnection(DieBefore lfsp, boolean sendOpenSession) {
		transaction.setState(TransactionStateNames.START_LISTENER);
		sap.startListening(transaction);

		rspdr = new StateActionsResponder(lfsp, rparams, sendOpenSession);
		rspdr.start();
		transaction.setState(TransactionStateNames.LISTEN_FOR_CONNECTIONS);

		sap.listenForConnection(transaction,
				TransactionStateNames.TRANSACTION_DONE);
		log.debug("Local Transaction after open connection: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.TRANSACTION_DONE,
				transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
				.getError().getType());
	}

	private void shutdown(boolean errorExpected) {
		transaction.setState(TransactionStateNames.CLOSING_CONNECTION);
		sap.closingConnection(transaction,
				TransactionStateNames.TRANSACTION_DONE);
		log.debug("Local Transaction during close connection: " + transaction);
		while (transaction.getState().equals(
				TransactionStateNames.CLOSING_CONNECTION)) {
			sap.closingConnection(transaction,
					TransactionStateNames.TRANSACTION_DONE);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
			log.debug("Local Transaction during close connection: "
					+ transaction);
		}
		log.debug("Local Transaction after close connection: " + transaction);
		if (errorExpected) {
			org.junit.Assert.assertEquals(
					TransactionStateNames.TRANSACTION_DONE, transaction
							.getState());
			org.junit.Assert.assertEquals(TcpErrorTypes.IO_ERROR, transaction
					.getError().getType());
		} else {
			org.junit.Assert.assertEquals(
					TransactionStateNames.TRANSACTION_DONE, transaction
							.getState());
			org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
					.getError().getType());
		}
		transaction.setState(TransactionStateNames.TRANSACTION_DONE);
		sap.recordTransaction(transaction, TransactionStateNames.READY);
		org.junit.Assert.assertEquals(TransactionStateNames.READY, transaction
				.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
				.getError().getType());

		transaction.setState(TransactionStateNames.STOP_LISTENER);
		sap.stopListening(transaction);
		log.debug("Local Transaction after stop listener: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.TRANSACTION_DONE,
				transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
				.getError().getType());
	}

	@After
	public void tearDown() throws Exception {

		while (rspdr.isAlive()) {
			log.debug("Waiting for responder shutdown");
			Thread.sleep(1000);
		}
		sap.stopListening(transaction);
	}

	@Test
	public void test00StartListenerBadPortFail() {
		rspdr = new StateActionsResponder(DieBefore.OPEN_COMMAND, rparams, true); // never
																					// started
		lparams.setLocalPort(80);
		transaction.setState(TransactionStateNames.START_LISTENER);
		sap.startListening(transaction);
		log.debug("after start listening: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.STOP_LISTENER,
				transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.IO_ERROR, transaction
				.getError().getType());

		transaction.setState(TransactionStateNames.TRANSACTION_DONE);
		sap.recordTransaction(transaction, TransactionStateNames.READY);
		org.junit.Assert.assertEquals(TransactionStateNames.READY, transaction
				.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
				.getError().getType());
		transaction.setState(TransactionStateNames.STOP_LISTENER);
		sap.stopListening(transaction);
		log.debug("Local Transaction after stop listener: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.TRANSACTION_DONE,
				transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.IO_ERROR, transaction
				.getError().getType());
	}

	@Test
	public void test01StartListenerFail() {
		transaction.setState(TransactionStateNames.START_LISTENER);
		sap.startListening(transaction);
		rspdr = new StateActionsResponder(DieBefore.OPEN_COMMAND, rparams, true); // never
																					// started
		transaction.setState(TransactionStateNames.LISTEN_FOR_CONNECTIONS);
		sap.listenForConnection(transaction,
				TransactionStateNames.TRANSACTION_DONE);
		log.debug("Local Transaction after open connection: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.LISTEN_FOR_CONNECTIONS,
				transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
				.getError().getType());
		transaction.setState(TransactionStateNames.STOP_LISTENER);
		sap.stopListening(transaction);
		while (transaction.getState().equals(
				TransactionStateNames.STOP_LISTENER)) {
			sap.stopListening(transaction);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
		}
		log.debug("Local Transaction after stop listening: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.TRANSACTION_DONE,
				transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
				.getError().getType());
	}

	@Test
	public void test02OpenSessionReadFail() {
		setupConnection(DieBefore.OPEN_COMMAND, true);
		read(true, true);
		shutdown(true);
	}

	@Test
	public void test03OpenSessionWriteFail() {
		setupConnection(DieBefore.OPEN_RESPONSE, false);
		write(TransactionStateNames.WAIT_FOR_RESPONSE, true, true);
		read(true, false);
		shutdown(true);
	}

	@Test
	public void test04CloseSessionWriteFail() {
		setupConnection(DieBefore.CLOSE_COMMAND, false);
		write(TransactionStateNames.WAIT_FOR_RESPONSE, true, true);
		read(false, false);
		write(TransactionStateNames.TRANSACTION_DONE, true, false);
		shutdown(false);
	}

	@Test
	public void test05CloseSessionReadFail() {
		setupConnection(DieBefore.CLOSE_COMMAND, true);
		read(false, true);
		write(TransactionStateNames.TRANSACTION_DONE, false, true);
		read(true, true);
		shutdown(true);
	}

	@Test
	public void test06CloseSessionWritePass() {
		setupConnection(DieBefore.END, false);
		write(TransactionStateNames.WAIT_FOR_RESPONSE, true, true);
		read(false, false);
		write(TransactionStateNames.TRANSACTION_DONE, true, false);
		shutdown(false);
	}

	@Test
	public void test07CloseSessionReadPass() {
		setupConnection(DieBefore.END, true);
		read(false, true);
		write(TransactionStateNames.TRANSACTION_DONE, false, true);
		read(false, true);
		shutdown(false);
	}

	private void write(TransactionStateNames next, boolean isCommand,
			boolean isOpen) {
		TransactionStateNames curstate = TransactionStateNames.ASSEMBLE_OPEN_RESPONSE;
		TransactionStateNames wastate = TransactionStateNames.WAIT_FOR_OPEN_RESPONSE;
		if (isCommand) {
			curstate = (isOpen ? TransactionStateNames.ASSEMBLE_OPEN_COMMAND
					: TransactionStateNames.ASSEMBLE_CLOSE_COMMAND);
			wastate = TransactionStateNames.WAIT_FOR_OPEN_COMMAND;
		}
		transaction.setState(curstate);
		sap.assembleSessionMessage(transaction, isOpen, isCommand, wastate);
		org.junit.Assert.assertEquals(wastate, transaction.getState());
		
		String openStr = isOpen ? "open" : "close";
		String cmdStr = isCommand ? "command" : "response";
		while (transaction.getState().equals(wastate)) {
			transaction.setState(wastate);
			sap.waitForSend(transaction, next);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
		}
		log.debug("Local Transaction after write " + openStr + " session "
				+ cmdStr + ": " + transaction);
		org.junit.Assert.assertEquals(next, transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
				.getError().getType());
	}
}
