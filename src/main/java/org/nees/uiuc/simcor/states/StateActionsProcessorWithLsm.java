package org.nees.uiuc.simcor.states;

import org.nees.uiuc.simcor.listener.ClientId;
import org.nees.uiuc.simcor.listener.ListenerStateMachine;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.transaction.Transaction;

public class StateActionsProcessorWithLsm extends StateActionsProcessor {
	private ListenerStateMachine lsm;

	public StateActionsProcessorWithLsm(ListenerStateMachine lsm) {
		super();
		this.lsm = lsm;
	}
	public void startListener(Transaction transaction, TransactionStateNames next) {
		lsm.getSap().setParams(params);
		lsm.start();
		TcpError error = lsm.getError();
		setStatus(transaction, error, next);
	}
	
	public void stopListener(Transaction transaction, TransactionStateNames next) {
		lsm.setRunning(false);
		TransactionStateNames state = transaction.getState();
		if(lsm.isAlive()) {
			return;
		}
		TcpError error = lsm.getError();
		setStatus(transaction, error, state);
	}
	public void checkListenerForConnection(Transaction transaction, TransactionStateNames next) {
		TransactionStateNames state = transaction.getState();
		ClientId id = lsm.getOneClient();
		TcpError error = lsm.getError();
		setStatus(transaction, error, next);
	}

}
