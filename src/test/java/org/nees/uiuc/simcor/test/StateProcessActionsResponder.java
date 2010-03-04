package org.nees.uiuc.simcor.test;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class StateProcessActionsResponder extends Thread {
	private StateActionsProcessor sap;

	private enum LifeSpanType {
		OPEN_COMMAND, OPEN_RESPONSE, CLOSE_COMMAND, END
	};

	private LifeSpanType lifeSpan = LifeSpanType.END;
	private TcpParameters params;
	private boolean sendSession = false;
	private Transaction transaction;
	private final Logger log = Logger
			.getLogger(StateProcessActionsResponder.class);

	public StateProcessActionsResponder(LifeSpanType lifeSpan,
			TcpParameters params, boolean sendOpenSession) {
		super();
		this.lifeSpan = lifeSpan;
		this.params = params;
		this.sendSession = sendOpenSession;
		sap = new StateActionsProcessor();
	}

	@Override
	public void run() {
		sap.setParams(params);
		transaction = sap.getTf().createTransaction(null);
		transaction.setPosted(true);
		transaction.setState(TransactionStateNames.OPENING_CONNECTION);
		int count = 0;
		while (transaction.getState().equals(
				TransactionStateNames.OPENING_CONNECTION)
				&& count < 50) {
			sap.openConnection(transaction);
			count++;
			try {
				sleep(300);
			} catch (InterruptedException e) {
			}
			if (count >= 50) {
				return;
			}
		}
		if (lifeSpan.equals(LifeSpanType.OPEN_COMMAND)) {
			return;
		}
		if (sendSession) {
			if (sendSessionCommand(true) == false) {
				return;
			}
			log.debug("Current transaction: " + transaction);
			if (lifeSpan.equals(LifeSpanType.OPEN_RESPONSE)) {
				return;
			}
			if (receiveSessionResponse() == false) {
				return;
			}

		} else {
			if (receiveSessionCommand() == false) {
				return;
			}
			log.debug("Current transaction: " + transaction);
			if (lifeSpan.equals(LifeSpanType.OPEN_RESPONSE)) {
				return;
			}
			if (sendSessionResponse(true) == false) {
				return;
			}
		}
		if (lifeSpan.equals(LifeSpanType.CLOSE_COMMAND)) {
			return;
		}

		if (sendSession) {
			if (sendSessionCommand(false) == false) {
				return;
			}
			log.debug("Current transaction: " + transaction);
		} else {
			if (receiveSessionCommand() == false) {
				return;
			}
			log.debug("Current transaction: " + transaction);
		}

	}

	private boolean sendSessionCommand(boolean isOpen) {
		int count = 0;
		sap.assembleSessionMessage(transaction, isOpen, true,
				TransactionStateNames.SENDING_COMMAND);
		while ((transaction.getState().equals(
				TransactionStateNames.WAIT_FOR_RESPONSE) == false)
				&& (count < 50)) {
			count++;
			sap.waitForSend(transaction,
					TransactionStateNames.WAIT_FOR_RESPONSE);
			try {
				sleep(300);
			} catch (InterruptedException e) {
			}
		}
		if (count >= 50) {
			return false;
		}
		return true;
	}

	private boolean sendSessionResponse(boolean isOpen) {
		int count = 0;
		sap.assembleSessionMessage(transaction, isOpen, false,
				TransactionStateNames.SENDING_RESPONSE);
		while ((transaction.getState().equals(
				TransactionStateNames.TRANSACTION_DONE) == false)
				&& (count < 50)) {
			count++;
			sap
					.waitForSend(transaction,
							TransactionStateNames.TRANSACTION_DONE);
			try {
				sleep(300);
			} catch (InterruptedException e) {
			}
		}
		if (count >= 50) {
			return false;
		}
		return true;
	}

	private boolean receiveSessionCommand() {
		int count = 0;
		sap
				.setUpRead(transaction, true,
						TransactionStateNames.WAIT_FOR_COMMAND);
		while ((transaction.getState().equals(
				TransactionStateNames.WAIT_FOR_COMMAND) == false)
				&& (count < 50)) {
			sap.waitForRead(transaction, true,
					TransactionStateNames.COMMAND_AVAILABLE);
			count++;
			try {
				sleep(300);
			} catch (InterruptedException e) {
			}
		}
		if (count >= 50) {
			return false;
		}
		return true;
	}

	private boolean receiveSessionResponse() {
		int count = 0;
		sap.setUpRead(transaction, false,
				TransactionStateNames.WAIT_FOR_RESPONSE);
		while ((transaction.getState().equals(
				TransactionStateNames.WAIT_FOR_RESPONSE) == false)
				&& (count < 50)) {
			sap.waitForRead(transaction, true,
					TransactionStateNames.RESPONSE_AVAILABLE);
			count++;
			try {
				sleep(300);
			} catch (InterruptedException e) {
			}
		}
		if (count >= 50) {
			return false;
		}
		return true;
	}

	public StateActionsProcessor getSap() {
		return sap;
	}

	public void setSap(StateActionsProcessor sap) {
		this.sap = sap;
	}

	public LifeSpanType getLifeSpan() {
		return lifeSpan;
	}

	public void setLifeSpan(LifeSpanType lifeSpan) {
		this.lifeSpan = lifeSpan;
	}

	public TcpParameters getParams() {
		return params;
	}

	public void setParams(TcpParameters params) {
		this.params = params;
	}
}
