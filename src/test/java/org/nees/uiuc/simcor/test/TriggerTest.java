package org.nees.uiuc.simcor.test;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nees.uiuc.simcor.factories.TransactionFactory;
import org.nees.uiuc.simcor.listener.ClientConnections;
import org.nees.uiuc.simcor.listener.ClientId;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpLinkDto;
import org.nees.uiuc.simcor.tcp.TcpListen;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.test.util.TriggerConnectionsClient;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;

public class TriggerTest {
	private List<TriggerConnectionsClient> clients = new ArrayList<TriggerConnectionsClient>();
	private ClientConnections cc = new ClientConnections();
	private TcpListen listener = new TcpListen();
	private TcpParameters lparams = new TcpParameters();
	private TcpParameters cparams = new TcpParameters();
	private int clientIdx = 0;
	private TransactionFactory tf = new TransactionFactory();
	private TransactionIdentity tId;
	private final Logger log = Logger.getLogger(TriggerTest.class);


	@Before
	public void setUp() throws Exception {
		cparams.setRemoteHost("127.0.0.1");
		cparams.setRemotePort(6445);
		cparams.setTcpTimeout(2000);
		lparams.setLocalPort(6445);
		lparams.setTcpTimeout(2000);
		listener.setParams(lparams);
		tf.setSystemDescription("Broadcaster");
		tId = tf.createTransactionId(0, 0, 0);
		listener.startListening();
	}
	@After
	public void tearDown() throws Exception {
		listener.stopListening();
	}
	
	@Test
	public void tesOneTriggering() {
		startClient();
		broadcast();
		log.debug("Response: " + cc.getMessage());
		broadcast();
		log.debug("Response: " + cc.getMessage());
		endClient();
		broadcast();
		log.debug("Response: " + cc.getMessage());
	}
	
	private void startClient() {
		String sys = "Client " + clientIdx;
		TriggerConnectionsClient client = new TriggerConnectionsClient(cparams, sys);
		client.connect();
		TcpLinkDto link = listener.listen();
		Connection c = new Connection(link);
		ClientId id = new ClientId(sys, link.getRemoteHost());
		cc.addClient(c, id);
		clients.add(client);
	}
	private void broadcast() {
		tId.setStep(tId.getStep() + 1);
		SimCorMsg msg = tf.createCommand("trigger", "MDL-00-01", null, "Broadcast " + tf.getSystemDescription());
		cc.broadcast(msg, tId);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
		}

		for(TriggerConnectionsClient c : clients) {
			c.checkForMessages();
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
		}		
		while(cc.areResponsesFinished() == false) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
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
