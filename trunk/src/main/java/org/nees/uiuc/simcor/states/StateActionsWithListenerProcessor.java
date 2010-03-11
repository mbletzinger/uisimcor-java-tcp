package org.nees.uiuc.simcor.states;

import org.nees.uiuc.simcor.listener.ListenerStateMachine;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.transaction.Transaction;

public class StateActionsWithListenerProcessor extends StateActionsProcessor {
	private ListenerStateMachine lsm;

	public StateActionsWithListenerProcessor(ListenerStateMachine lsm) {
		super();
		this.lsm = lsm;
	}
	public void startListener(Transaction transaction, TransactionStateNames next) {
		lsm.start();
		TcpError error = lsm.getError();
		setStatus(transaction, error, next);
	}

}
