package org.nees.uiuc.simcor.states.listener;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class ListenForConnections extends TransactionState {

	public ListenForConnections(TransactionStateNames state,
			StateActionsProcessor sap, TransactionStateNames next) {
		super(state, sap, next);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.ListenerForConnection(transaction, next);
	}

}
