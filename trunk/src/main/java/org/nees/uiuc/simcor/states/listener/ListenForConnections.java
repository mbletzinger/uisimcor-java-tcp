package org.nees.uiuc.simcor.states.listener;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class ListenForConnections extends TransactionState {
	private boolean isP2p = false;

	public ListenForConnections(StateActionsProcessor sap, boolean isP2p) {
		super(TransactionStateNames.LISTEN_FOR_CONNECTIONS, sap);
		this.isP2p = isP2p;
	}

	public boolean isP2p() {
		return isP2p;
	}

	public void setP2p(boolean isP2p) {
		this.isP2p = isP2p;
	}

	@Override
	public void execute(Transaction transaction) {
		sap.checkOpenConnection(transaction,
				(isP2p ? TransactionStateNames.WAIT_FOR_OPEN_SESSION
						: TransactionStateNames.SEND_OPEN_SESSION));
	}

}
