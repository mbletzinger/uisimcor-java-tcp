package org.nees.uiuc.simcor.test;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.test.util.StateActionsResponder;
import org.nees.uiuc.simcor.test.util.StateActionsResponder.LifeSpanType;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.Transaction;

public class T04_ListenerStateMachineTest {
	private final Logger log = Logger.getLogger(T04_ListenerStateMachineTest.class);
	private TcpParameters lparams = new TcpParameters();
	private TcpParameters rparams = new TcpParameters();
	private StateActionsResponder rspdr;
	private StateActionsProcessor sap;
	private Transaction transaction;

	private void read(TransactionStateNames current,
			TransactionStateNames next, boolean errorExpected, boolean isCommand) {
		String cmdStr = isCommand ? "command" : "response";
		sap.setUpRead(transaction, false, current);
		while (transaction.getState().equals(current)) {
			sap.waitForRead(transaction, isCommand, next);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
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

	private void setupConnection(LifeSpanType lfsp, boolean sendOpenSession) {
		sap.startListening(transaction);

		rspdr = new StateActionsResponder(lfsp, rparams, sendOpenSession);
		rspdr.start();

		sap.checkOpenConnection(transaction,
				TransactionStateNames.TRANSACTION_DONE);
		log.debug("Local Transaction after open connection: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.TRANSACTION_DONE,
				transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
				.getError().getType());
	}

	private void shutdown(boolean errorExpected) {
		sap.closingConnection(transaction,
				TransactionStateNames.STOP_LISTENER);
		while (transaction.getState().equals(
				TransactionStateNames.CLOSING_CONNECTION)) {
			sap.closingConnection(transaction,
					TransactionStateNames.STOP_LISTENER);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
		}
		log.debug("Local Transaction after close connection: " + transaction);
		if(errorExpected) {
		org.junit.Assert.assertEquals(TransactionStateNames.TRANSACTION_DONE,
				transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.IO_ERROR, transaction
				.getError().getType());
		} else {
			org.junit.Assert.assertEquals(TransactionStateNames.STOP_LISTENER,
					transaction.getState());
			org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
					.getError().getType());
		}
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
	public void test01StartListenerFail() {
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
					TransactionStateNames.STOP_LISTENER);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
		}
		log.debug("Local Transaction after close connection: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.STOP_LISTENER,
				transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
				.getError().getType());
		shutdown(false);
	}

	@Test
	public void test02OpenSessionReadFail() {
		setupConnection(LifeSpanType.OPEN_COMMAND,true);
		read(TransactionStateNames.READ_COMMAND,
				TransactionStateNames.COMMAND_AVAILABLE, true,true);
		shutdown(true);
	}

	@Test
	public void test03OpenSessionWriteFail() {
		setupConnection(LifeSpanType.OPEN_RESPONSE,false);
		write(TransactionStateNames.ASSEMBLE_OPEN_COMMAND,
				TransactionStateNames.WAIT_FOR_RESPONSE,true,true);
		read(TransactionStateNames.READ_RESPONSE,
				TransactionStateNames.RESPONSE_AVAILABLE, true,false);
		shutdown(true);
	}

	@Test
	public void test04CloseSessionWriteFail() {
		setupConnection(LifeSpanType.CLOSE_COMMAND,false);
		write(TransactionStateNames.ASSEMBLE_CLOSE_COMMAND,
				TransactionStateNames.WAIT_FOR_RESPONSE,true,true);
		read(TransactionStateNames.READ_RESPONSE,
				TransactionStateNames.RESPONSE_AVAILABLE, false,false);
		write(TransactionStateNames.ASSEMBLE_CLOSE_COMMAND,
				TransactionStateNames.TRANSACTION_DONE,true,false);
		shutdown(false);
	}

	@Test
	public void test05CloseSessionReadFail() {
		setupConnection(LifeSpanType.CLOSE_COMMAND,true);
		read(TransactionStateNames.READ_COMMAND,
				TransactionStateNames.COMMAND_AVAILABLE, false,true);
		write(TransactionStateNames.ASSEMBLE_OPEN_RESPONSE,
				TransactionStateNames.TRANSACTION_DONE,false,true);
		read(TransactionStateNames.READ_COMMAND,
				TransactionStateNames.COMMAND_AVAILABLE, true,true);
		shutdown(true);
	}

	@Test
	public void test06CloseSessionWritePass() {
		setupConnection(LifeSpanType.END,false);
		write(TransactionStateNames.ASSEMBLE_OPEN_COMMAND,
				TransactionStateNames.WAIT_FOR_RESPONSE,true,true);
		read(TransactionStateNames.READ_RESPONSE,
				TransactionStateNames.RESPONSE_AVAILABLE, false,false);
		write(TransactionStateNames.ASSEMBLE_CLOSE_COMMAND,
				TransactionStateNames.TRANSACTION_DONE,true,false);
		shutdown(false);
	}

	@Test
	public void test07CloseSessionReadPass() {
		setupConnection(LifeSpanType.END,true);
		read(TransactionStateNames.READ_COMMAND,
				TransactionStateNames.COMMAND_AVAILABLE, false,true);
		write(TransactionStateNames.ASSEMBLE_OPEN_RESPONSE,
				TransactionStateNames.TRANSACTION_DONE,false,true);
		read(TransactionStateNames.READ_COMMAND,
				TransactionStateNames.COMMAND_AVAILABLE, false,true);
		shutdown(false);
	}
	private void write(TransactionStateNames current, TransactionStateNames next, boolean isCommand, boolean isOpen) {
		sap.assembleSessionMessage(transaction, isOpen, isCommand, current);
		String openStr = isOpen ? "open" : "close";
		String cmdStr = isCommand ? "command" : "response";
		while (transaction.getState().equals(current)) {
			sap.waitForSend(transaction, next);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
		}
		log.debug("Local Transaction after write " + openStr + " session " + cmdStr + ": "
				+ transaction);
		org.junit.Assert.assertEquals(next, transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction
				.getError().getType());
	}
}
