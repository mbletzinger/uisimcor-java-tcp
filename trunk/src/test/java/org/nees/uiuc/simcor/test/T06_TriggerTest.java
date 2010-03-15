package org.nees.uiuc.simcor.test;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nees.uiuc.simcor.factories.TransactionFactory;
import org.nees.uiuc.simcor.listener.ClientConnections;
import org.nees.uiuc.simcor.listener.ListenerStateMachine;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithCc;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.test.util.TriggerConnectionsClient;
import org.nees.uiuc.simcor.transaction.BroadcastTransaction;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;

public class T06_TriggerTest {
	private List<TriggerConnectionsClient> clients = new ArrayList<TriggerConnectionsClient>();
	private StateActionsProcessorWithCc sap;
	private TcpParameters lparams = new TcpParameters();
	private TcpParameters cparams = new TcpParameters();
	private int clientIdx = 0;
	private TransactionFactory tf;
	private final Logger log = Logger.getLogger(T06_TriggerTest.class);


	@Before
	public void setUp() throws Exception {
		cparams.setRemoteHost("127.0.0.1");
		cparams.setRemotePort(6445);
		cparams.setTcpTimeout(5000);
		lparams.setLocalPort(6445);
		lparams.setTcpTimeout(5000);
		ListenerStateMachine lsm = new ListenerStateMachine(new ClientConnections(), false);
		sap = new StateActionsProcessorWithCc(lsm);
		sap.setParams(lparams);
		tf = sap.getTf();
		tf.setSystemDescription("Broadcaster");
		TransactionIdentity id = tf.createTransactionId(0, 0, 0);
		tf.setId(id);
		SimpleTransaction transaction  = tf.createSimpleTransaction(null);
		log.debug("Start transaction: " + transaction);
		sap.startListener(transaction, TransactionStateNames.TRANSACTION_DONE);
	}
	@After
	public void tearDown() throws Exception {
		SimpleTransaction transaction  = tf.createSimpleTransaction(null);
		sap.stopListener(transaction, TransactionStateNames.TRANSACTION_DONE);
	}
	
	@Test
	public void tesOneTriggering() {
		startClient();
		BroadcastTransaction transaction = broadcast();
		log.debug("Broadcasts: " + transaction.getBroadcastMsg());
		log.debug("Responses: " + transaction.getResponseMsg());
		transaction = broadcast();
		log.debug("Broadcasts: " + transaction.getBroadcastMsg());
		log.debug("Responses: " + transaction.getResponseMsg());
		endClient();
		transaction = broadcast();
		log.debug("Broadcasts: " + transaction.getBroadcastMsg());
		log.debug("Responses: " + transaction.getResponseMsg());
	}
	
	private void startClient() {
		String sys = "Client " + clientIdx;
		TriggerConnectionsClient client = new TriggerConnectionsClient(cparams, sys);
		client.connect();
		clients.add(client);
	}
	private BroadcastTransaction broadcast() {
		SimCorMsg msg = tf.createCommand("trigger", "MDL-00-01", null, "Broadcast " + tf.getSystemDescription());
		BroadcastTransaction transaction  = tf.createBroadcastTransaction(msg);
		sap.assembleTriggerCommands(transaction,TransactionStateNames.BROADCAST_COMMAND, false);
		while(transaction.getState().equals(TransactionStateNames.BROADCAST_COMMAND)) {
			sap.broadcastCommands(transaction, TransactionStateNames.SETUP_TRIGGER_READ_COMMANDS);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
		}
		sap.setupTriggerResponses(transaction, TransactionStateNames.WAIT_FOR_TRIGGER_RESPONSES);
		while(transaction.getState().equals(TransactionStateNames.WAIT_FOR_TRIGGER_RESPONSES)) {
			sap.waitForTriggerResponse(transaction, TransactionStateNames.TRANSACTION_DONE);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
			checkMsgs();
		}
		return transaction;
	}
	private void checkMsgs() {
		for(TriggerConnectionsClient c : clients) {
			c.checkForMessages();
		}
	}
	private void endClient() {
		TriggerConnectionsClient client = clients.get(0);
		client.closeConnection();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		clients.remove(0);
	}
}
