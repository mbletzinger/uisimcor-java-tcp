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
		
		transaction = sap.getTf().createTransaction(new SimCorMsg());
		transaction.setTimeout(2000);
	}

	@After
	public void tearDown() throws Exception {
		
		while(rspdr.isAlive()) {
			log.debug("Waiting for responder shutdown");
			Thread.sleep(1000);
		}
	}

	@Test
	public void testStartListenerFail() {
		sap.startListening(transaction);
		rspdr = new StateActionsResponder(LifeSpanType.OPEN_COMMAND, rparams,
				true); // never started
		
		sap.checkOpenConnection(transaction, TransactionStateNames.TRANSACTION_DONE);
		log.debug("Local Transaction after open connection: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.OPENING_CONNECTION, transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction.getError().getType());
		sap.stopListening(transaction);
		log.debug("Local Transaction after stop listener: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.TRANSACTION_DONE, transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction.getError().getType());
	}

	@Test
	public void testOpenSessionReadFail() {
		sap.startListening(transaction);

		rspdr = new StateActionsResponder(LifeSpanType.OPEN_COMMAND, rparams,
				true);
		rspdr.start();
		
		sap.checkOpenConnection(transaction, TransactionStateNames.TRANSACTION_DONE);
		log.debug("Local Transaction after open connection: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.TRANSACTION_DONE, transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction.getError().getType());
		sap.setUpRead(transaction, false, TransactionStateNames.READ_COMMAND);
		while(transaction.getState().equals(TransactionStateNames.READ_COMMAND)) {
			sap.waitForRead(transaction, false, TransactionStateNames.COMMAND_AVAILABLE);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
		}
		log.debug("Local Transaction after read open session command: " + transaction);
		log.debug("Remote Transaction: " + rspdr.getTransaction());
		org.junit.Assert.assertEquals(TransactionStateNames.CLOSING_CONNECTION, transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.IO_ERROR, transaction.getError().getType());
		sap.stopListening(transaction);
		log.debug("Local Transaction after stop listener: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.TRANSACTION_DONE, transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction.getError().getType());
	}
	@Test
	public void testOpenSessionWriteFail() {
		sap.startListening(transaction);

		rspdr = new StateActionsResponder(LifeSpanType.OPEN_COMMAND, rparams,
				false);
		rspdr.start();
		
		sap.checkOpenConnection(transaction, TransactionStateNames.TRANSACTION_DONE);
		log.debug("Local Transaction after open connection: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.TRANSACTION_DONE, transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction.getError().getType());
		sap.assembleSessionMessage(transaction, true, true,TransactionStateNames.SEND_OPEN_SESSION);
		while(transaction.getState().equals(TransactionStateNames.SEND_OPEN_SESSION)) {
			sap.waitForSend(transaction, TransactionStateNames.WAIT_FOR_RESPONSE);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
		}
		log.debug("Local Transaction after write open session command: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.WAIT_FOR_RESPONSE, transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction.getError().getType());
		sap.setUpRead(transaction, false, TransactionStateNames.SEND_OPEN_SESSION);
		while(transaction.getState().equals(TransactionStateNames.SEND_OPEN_SESSION)) {
			sap.waitForRead(transaction, false,TransactionStateNames.RESPONSE_AVAILABLE);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
		}
		log.debug("Local Transaction after read open session response: " + transaction);
		log.debug("Remote Transaction: " + rspdr.getTransaction());
		org.junit.Assert.assertEquals(TransactionStateNames.CLOSING_CONNECTION, transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.IO_ERROR, transaction.getError().getType());
		sap.stopListening(transaction);
		log.debug("Local Transaction after stop listener: " + transaction);
		org.junit.Assert.assertEquals(TransactionStateNames.TRANSACTION_DONE, transaction.getState());
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction.getError().getType());
	}
}
