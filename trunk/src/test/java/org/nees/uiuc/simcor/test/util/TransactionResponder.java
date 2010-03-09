package org.nees.uiuc.simcor.test.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.ConnectionPeer;
import org.nees.uiuc.simcor.UiSimCorTcp;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.DirectionType;

public class TransactionResponder extends Thread {
	public boolean connected;

	private TransactionMsgs data;

	private final Logger log = Logger.getLogger(TransactionResponder.class);

	private TcpParameters params = new TcpParameters();

	private List<TransactionStateNames> readyStates = new ArrayList<TransactionStateNames>();

	private ConnectionPeer simcor;

	public TransactionResponder() {
		readyStates.add(TransactionStateNames.TRANSACTION_DONE);
		readyStates.add(TransactionStateNames.READY);
	}
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
		params.setLocalPort(6445);
		params.setTcpTimeout(200000);
		simcor = new ConnectionPeer(DirectionType.RECEIVE_COMMAND, "MDL-00-01");
		simcor.startup(params);
		connected = false;
		int count = 0;

		while (simcor.isReady().equals(TransactionStateNames.OPENING_CONNECTION)
				&& count < 40) {
			log.info("Waiting for a connection");
			count++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.info("My sleep was interrupted.");
			}
			
		}
		org.junit.Assert.assertTrue(count < 40);
		log.info("Open result [" + simcor.getTransaction() + "]");
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, simcor.getTransaction().getError().getType());
		org.junit.Assert.assertEquals(TransactionStateNames.TRANSACTION_DONE, simcor.getTransaction().getState());
		org.junit.Assert.assertNotNull(simcor.getSap().getCm().getConnection());
		connected = true;
		log.info("Connection established");

		while (connected) {
			simcor.startTransaction();
			TransactionStateNames state = simcor.isReady();
			while ( state != TransactionStateNames.COMMAND_AVAILABLE) {
				try {
					Thread.sleep(200);
					state = simcor.isReady();
					log.debug("Current state: " + state);
				} catch (InterruptedException e) {
					log.info("My sleep was interrupted.");
				}
			}
			log.debug("Current state: " + state);
			Transaction transaction = simcor.pickupTransaction();
			log.debug("Received command" + transaction.getCommand());
			if (transaction.getError().getType() != TcpErrorTypes.NONE) {
				log.error("Transaction error " + transaction.getError());
			}
			org.junit.Assert.assertEquals(TcpErrorTypes.NONE, transaction.getError().getType());
//			org.junit.Assert.assertNotNull(transaction.getCommand().getContent());
			Transaction expected = data.transactions.get(transaction
					.getCommand().toString());
			if(expected == null) {
				String hash = "";
				for(Iterator<String> s = data.transactions.keySet().iterator(); s.hasNext();) {
					String m = s.next();
					hash += "\t" + m + "=" + m.hashCode() + "\n";
				}
				log.error("Command [" + transaction.getCommand() + "]=" + transaction.getCommand().hashCode() + " not recognized\n" + hash);
				org.junit.Assert.fail();
			}
			org.junit.Assert.assertNotNull(expected);
			SimCorMsg resp = expected.getResponse();
			SimCorMsg cmd = transaction.getCommand();
			if (cmd.getCommand().equals("propose")
					|| cmd.getCommand().equals("execute")) {
				transaction.setId(expected.getId());
			}
			if (cmd.getCommand().equals("close-session")) {
				connected = false;
			}
			simcor.continueTransaction(resp);
			state = simcor.isReady();
			log.debug("Sending response " + resp);
			while (state != TransactionStateNames.TRANSACTION_DONE) {
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
		simcor.shutdown();
		TransactionStateNames state = simcor.isReady();
		while (readyStates.contains(state) == false) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.info("Sleep interrupted");
			}
			state = simcor.isReady();
		}
		org.junit.Assert.assertEquals(TcpErrorTypes.NONE, simcor.getErrors().getType());
		org.junit.Assert.assertEquals(TransactionStateNames.TRANSACTION_DONE, simcor.getTransaction().getState());

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
	public static void main(String[] args) {
		TransactionResponder responder = new TransactionResponder();
		TransactionMsgs data = new TransactionMsgs();
		try {
			data.setUp();
		} catch (Exception e) {
			e.printStackTrace();
		}
		responder.setData(data);
		responder.start();
	}

}
