package org.nees.uiuc.simcor.states;

import org.nees.uiuc.simcor.listener.ClientIdWithConnection;
import org.nees.uiuc.simcor.listener.ListenerStateMachine;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;

public class StateActionsProcessorWithLsm extends StateActionsProcessor {
	private ListenerStateMachine lsm;

	public StateActionsProcessorWithLsm(ListenerStateMachine lsm) {
		super();
		this.lsm = lsm;
	}
	public void checkListenerForConnection(SimpleTransaction transaction, TransactionStateNames next) {
		ClientIdWithConnection id = lsm.getOneClient();
		TcpError error = lsm.getError();
		if(id == null && error.getType().equals(TcpErrorTypes.NONE)) {
			return;
		}
		if(id != null) {
			cm.setConnection(id.connection);
		}
		setStatus(transaction, error,next);
	}
	
	public void startListener(SimpleTransaction transaction, TransactionStateNames next) {
		lsm.getSap().setParams(params);
		lsm.start();
		TcpError error = lsm.getError();
		setStatus(transaction, error, next);
	}
	public void stopListener(SimpleTransaction transaction, TransactionStateNames next) {
		lsm.setRunning(false);
		if(lsm.isAlive()) {
			return;
		}
		TcpError error = lsm.getError();
		setStatus(transaction, error, next);
	}

}
