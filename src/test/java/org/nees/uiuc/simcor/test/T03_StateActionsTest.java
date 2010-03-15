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
		TransactionStateNames current;
		TransactionStateNames next;
		if(isCommand) {
			cmdStr = "command";
			current = TransactionStateNames.WAIT_FOR_OPEN_COMMAND;
			next = TransactionStateNames.COMMAND_AVAILABLE;
		} else {
			cmdStr = "response";
			current = TransactionStateNames.WAIT_FOR_RESPONSE;
			next = TransactionStateNames.RESPONSE_AVAILABLE;			
		}
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
		sap = new StateActionsProcessorWithLcf();
		rparams.setRemoteHost("127.0.0.1");
		rparams.setRemotePort(6445);
		rparams.setTcpTimeout(2000);
		lparams.setLocalPort(6445);
		lparams.setTcpTimeout(2000);
		sap.setParams(lparams);
		sap.setIdentity("MDL-00-00", "Connection Test");

		transaction = sap.getTf().createSimpleTransaction(new SimCorMsg());
		transaction.setTimeout(2000);
	}

	private void setupConnection(DieBefore lfsp, boolean sendOpenSession) {
		sap.startListening(transaction);

		rspdr = new StateActionsResponder(lfsp, rparams, sendOpenSession);
		rspdr.start();

		sap.listenForConnection(transaction,
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
		rspdr = new StateActionsResponder(DieBefore.OPEN_COMMAND, rparams,
				true); // never started

		sap.listenForConnection(transaction,
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
		setupConnection(DieBefore.OPEN_COMMAND,true);
		read(true,true);
		shutdown(true);
	}

	@Test
	public void test03OpenSessionWriteFail() {
		setupConnection(DieBefore.OPEN_RESPONSE,false);
		write(TransactionStateNames.ASSEMBLE_OPEN_COMMAND,
				TransactionStateNames.WAIT_FOR_RESPONSE,true,true);
		read(true,false);
		shutdown(true);
	}

	@Test
	public void test04CloseSessionWriteFail() {
		setupConnection(DieBefore.CLOSE_COMMAND,false);
		write(TransactionStateNames.ASSEMBLE_CLOSE_COMMAND,
				TransactionStateNames.WAIT_FOR_RESPONSE,true,true);
		read(false,false);
		write(TransactionStateNames.ASSEMBLE_CLOSE_COMMAND,
				TransactionStateNames.TRANSACTION_DONE,true,false);
		shutdown(false);
	}

	@Test
	public void test05CloseSessionReadFail() {
		setupConnection(DieBefore.CLOSE_COMMAND,true);
		read(false,true);
		write(TransactionStateNames.ASSEMBLE_OPEN_RESPONSE,
				TransactionStateNames.TRANSACTION_DONE,false,true);
		read(true,true);
		shutdown(true);
	}

	@Test
	public void test06CloseSessionWritePass() {
		setupConnection(DieBefore.END,false);
		write(TransactionStateNames.ASSEMBLE_OPEN_COMMAND,
				TransactionStateNames.WAIT_FOR_RESPONSE,true,true);
		read(false,false);
		write(TransactionStateNames.ASSEMBLE_CLOSE_COMMAND,
				TransactionStateNames.TRANSACTION_DONE,true,false);
		shutdown(false);
	}

	@Test
	public void test07CloseSessionReadPass() {
		setupConnection(DieBefore.END,true);
		read( false,true);
		write(TransactionStateNames.ASSEMBLE_OPEN_RESPONSE,
				TransactionStateNames.TRANSACTION_DONE,false,true);
		read(false,true);
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
