package org.nees.uiuc.simcor.test;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.ConnectionPeer;
import org.nees.uiuc.simcor.UiSimCorTcp;
import org.nees.uiuc.simcor.factories.ConnectionFactory;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.tcp.Connection.ConnectionStatus;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.test.simcor.tcp.TcpListenerDto;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.DirectionType;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class TriggerListener extends Thread {

	public static void main(String[] args) {
		TriggerListener responder = new TriggerListener();
		TransactionMsgs data = new TransactionMsgs();
		try {
			data.setUp();
		} catch (Exception e) {
			e.printStackTrace();
		}
		responder.setData(data);
		responder.start();
	}

	private boolean connected;

	private TransactionMsgs data;

	private final Logger log = Logger.getLogger(TriggerListener.class);

	private TcpParameters params = new TcpParameters();

	private ConnectionPeer simcor;
	public TransactionMsgs getData() {
		return data;
	}
	public TcpParameters getParams() {
		return params;
	}
	
	public UiSimCorTcp getSimcor() {
		return simcor;
	}

	public boolean isConnected() {
		return connected;
	}

	@Override
	public void run() {
		params.setRemoteHost("127.0.0.1");
		params.setRemotePort(6342);
		params.setTcpTimeout(2000);
		simcor = new ConnectionPeer(DirectionType.RECEIVE_COMMAND, params);
		simcor.startup();
		connected = false;
		ConnectionFactory cf = simcor.getConnectionManager();
		Connection c = null;
		try {
			c = cf.getConnection();
		} catch (Exception e1) {
			log.error("Connection failed because ",e1);
		}
		while(c.getConnectionState() == ConnectionStatus.BUSY) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				log.debug("Someone interrupted me");
			}
		}
		TcpActionsDto dto = c.getFromRemoteMsg();
		log.info("Open result [" + dto + "]");
		assertEquals(TcpErrorTypes.NONE, dto.getError().getType());
		
		connected = true;
		log.info("Connection established");
		execute(data.openTransaction);
		while (connected) {
			execute(data.triggerTransaction);
		}
		while (cf.closeConnection() == false) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.info("Sleep interrupted");
			}
		}
		TcpListenerDto rsp = cf.getListener().getDto();
		log.info("Close result [" + rsp + "]");
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, rsp.getError().getType());

	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public void setData(TransactionMsgs data) {
		this.data = data;
	}

	public void setParams(TcpParameters rparams) {
		this.params = rparams;
	}
	public void setSimcor(ConnectionPeer simcor) {
		this.simcor = simcor;
	}

	private void execute(Transaction t) {
		simcor.startTransaction();
		TransactionStateNames state = simcor.isReady();
		while ( state != TransactionStateNames.COMMAND_AVAILABLE && state != TransactionStateNames.ERRORS_EXIST) {
			try {
				Thread.sleep(200);
				state = simcor.isReady();
				log.debug("Current state: " + state);
			} catch (InterruptedException e) {
				log.info("My sleep was interrupted.");
			}
		}
		Transaction transaction = simcor.pickupTransaction();
		log.debug("Received command" + transaction);
		if (transaction.getError().getType() != TcpErrorTypes.NONE) {
			log.error("Transaction error " + transaction.getError());
		}
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction.getError().getType());
		SimCorMsg resp = t.getResponse();
		SimCorMsg cmd = transaction.getCommand();
		if (cmd.getCommand().equals("trigger")) {
			transaction.setId(t.getId());
		}
		if (cmd.getCommand().equals("close-session")) {
			connected = false;
			resp = data.closeTransaction.getResponse();
		}
		simcor.continueTransaction(resp);
		state = simcor.isReady();
		log.debug("Sending response " + resp);
		while (state != TransactionStateNames.TRANSACTION_DONE && state != TransactionStateNames.ERRORS_EXIST) {
			try {
				Thread.sleep(200);
				state = simcor.isReady();
			} catch (InterruptedException e) {
				log.info("My sleep was interrupted.");
			}
		}
		transaction = simcor.getTransaction();
		if (transaction.getError().getType() != TcpErrorTypes.NONE) {
			log.error("Transaction error " + transaction.getError());
		}
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction.getError().getType());

	}
}
