package org.nees.uiuc.simcor.test.util;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.transaction.Transaction;

public class StateActionsResponder extends Thread {
	public enum LifeSpanType {
		CLOSE_COMMAND, END, OPEN_COMMAND, OPEN_RESPONSE
	}

	private LifeSpanType lifeSpan = LifeSpanType.END;;

	private final Logger log = Logger.getLogger(StateActionsResponder.class);
	private TcpParameters params;
	private StateActionsProcessor sap;
	private boolean sendSession = false;
	private Transaction transaction;

	public StateActionsResponder(LifeSpanType lifeSpan, TcpParameters params,
			boolean sendOpenSession) {
		super();
		this.lifeSpan = lifeSpan;
		this.params = params;
		this.sendSession = sendOpenSession;
		sap = new StateActionsProcessor();
		sap.setIdentity("MDL-00-01", "Connection Test Responder");
	}

	public LifeSpanType getLifeSpan() {
		return lifeSpan;
	}

	public TcpParameters getParams() {
		return params;
	}

	public StateActionsProcessor getSap() {
		return sap;
	}

	public synchronized Transaction getTransaction() {
		return transaction;
	}

	public boolean isSendSession() {
		return sendSession;
	}

	private boolean receiveSessionCommand() {
		int count = 0;
		Transaction tr = getTransaction();
		sap.setUpRead(tr, true, TransactionStateNames.WAIT_FOR_COMMAND);
		while ((tr.getState().equals(TransactionStateNames.WAIT_FOR_COMMAND))
				&& (count < 50)) {
			sap.waitForRead(tr, true, TransactionStateNames.COMMAND_AVAILABLE);
			count++;
			try {
				sleep(300);
			} catch (InterruptedException e) {
			}
		}
		if (count >= 50) {
			setTransaction(tr);
			log.error("Receive failed");
			return false;
		}
		setTransaction(tr);
		return true;
	}

	private boolean receiveSessionResponse() {
		int count = 0;
		Transaction tr = getTransaction();
		sap.setUpRead(tr, false, TransactionStateNames.WAIT_FOR_RESPONSE);
		while ((tr.getState().equals(TransactionStateNames.WAIT_FOR_RESPONSE))
				&& (count < 50)) {
			sap.waitForRead(tr, true, TransactionStateNames.RESPONSE_AVAILABLE);
			count++;
			try {
				sleep(300);
			} catch (InterruptedException e) {
			}
		}
		if (count >= 50) {
			setTransaction(tr);
			log.error("Receive failed");
			return false;
		}
		setTransaction(tr);
		return true;
	}

	@Override
	public void run() {
		sap.setParams(params);
		Transaction tr = getTransaction();
		tr = sap.getTf().createTransaction(null);
		tr.setPosted(true);
		tr.setState(TransactionStateNames.OPENING_CONNECTION);
		tr.setTimeout(2000);
		int count = 0;
		while (tr.getState().equals(TransactionStateNames.OPENING_CONNECTION)
				&& count < 50) {
			sap.openConnection(tr);
			count++;
			try {
				sleep(300);
			} catch (InterruptedException e) {
			}
			if (count >= 50) {
				setTransaction(tr);
				log.error("Ending because count = " + count);
				shutdown();
				return;
			}
		}

		setTransaction(tr);

		if (lifeSpan.equals(LifeSpanType.OPEN_COMMAND)) {
			log.debug("Ending because lifespan is " + lifeSpan);
			shutdown();
			return;
		}
		log.debug("Current transaction after open connection: " + getTransaction());
		if (sendSession) {
			if (sendSessionCommand(true) == false) {
				log.debug("Ending because sendSendSessionCommand died");
				shutdown();
				return;
			}
			log.debug("Current transaction after send open session command: " + getTransaction());
			if (lifeSpan.equals(LifeSpanType.OPEN_RESPONSE)) {
				log.debug("Ending because lifespan is " + lifeSpan);
				shutdown();
				return;
			}
			if (receiveSessionResponse() == false) {
				log.debug("Ending because receiveSessionResponse died");
				shutdown();
				return;
			}
			log.debug("Current transaction after receive open session response: " + getTransaction());

		} else {
			if (receiveSessionCommand() == false) {
				log.debug("Ending because receiveSessionCommand died");
				shutdown();
				return;
			}
			log.debug("Current transaction: " + getTransaction());
			if (lifeSpan.equals(LifeSpanType.OPEN_RESPONSE)) {
				log.debug("Ending because lifespan is " + lifeSpan);
				shutdown();
				return;
			}
			log.debug("Current transaction after receive open session command: " + getTransaction());
			if (sendSessionResponse(true) == false) {
				log.debug("Ending because sendSessionResponse died");
				shutdown();
				return;
			}
			log.debug("Current transaction after send open session response: " + getTransaction());
		}
		if (lifeSpan.equals(LifeSpanType.CLOSE_COMMAND)) {
			log.debug("Ending because lifespan is " + lifeSpan);
			shutdown();
			return;
		}

		if (sendSession) {
			if (sendSessionCommand(false) == false) {
				log.debug("Ending because sendSessionCommand died");
				shutdown();
				return;
			}
		} else {
			if (receiveSessionCommand() == false) {
				log.debug("Ending because receiveSessionCommand died");
				shutdown();
				return;
			}
			log.debug("Current transaction after receive close session command: " + getTransaction());
			tr = getTransaction();
			tr.setState(TransactionStateNames.TRANSACTION_DONE);
			count = 0; 
			while (tr.getState().equals(
					TransactionStateNames.CLOSING_CONNECTION) && count < 50) {
				sap.closingConnection(tr,
						TransactionStateNames.TRANSACTION_DONE);
				count++;
				try {
					sleep(200);
				} catch (InterruptedException e) {
				}
			}
			setTransaction(tr);
		}
		shutdown();
	}

	private boolean sendSessionCommand(boolean isOpen) {
		int count = 0;
		Transaction tr = getTransaction();
		sap.assembleSessionMessage(tr, isOpen, true,
				TransactionStateNames.SENDING_COMMAND);
		while ((tr.getState().equals(TransactionStateNames.SENDING_COMMAND))
				&& (count < 50)) {
			count++;
			sap.waitForSend(tr, TransactionStateNames.WAIT_FOR_RESPONSE);
			try {
				sleep(300);
			} catch (InterruptedException e) {
			}
		}
		if (count >= 50) {
			setTransaction(tr);
			log.error("Send failed");
			return false;
		}
		setTransaction(tr);
		return true;
	}

	private boolean sendSessionResponse(boolean isOpen) {
		int count = 0;
		Transaction tr = getTransaction();
		sap.assembleSessionMessage(tr, isOpen, false,
				TransactionStateNames.SENDING_RESPONSE);
		while ((tr.getState().equals(TransactionStateNames.SENDING_RESPONSE))
				&& (count < 50)) {
			count++;
			sap.waitForSend(tr, TransactionStateNames.TRANSACTION_DONE);
			try {
				sleep(300);
			} catch (InterruptedException e) {
			}
		}
		if (count >= 50) {
			setTransaction(tr);
			log.error("Send failed");
			return false;
		}
		setTransaction(tr);
		return true;
	}

	public void setLifeSpan(LifeSpanType lifeSpan) {
		this.lifeSpan = lifeSpan;
	}

	public void setParams(TcpParameters params) {
		this.params = params;
	}

	public void setSap(StateActionsProcessor sap) {
		this.sap = sap;
	}

	public void setSendSession(boolean sendSession) {
		this.sendSession = sendSession;
	}

	public synchronized void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}
	private void shutdown() {
		Transaction tr = getTransaction();
		sap.closingConnection(tr, TransactionStateNames.TRANSACTION_DONE);
	}
}
