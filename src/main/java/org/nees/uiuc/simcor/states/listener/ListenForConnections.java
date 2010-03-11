package org.nees.uiuc.simcor.states.listener;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class ListenForConnections extends TransactionState {

	public ListenForConnections(StateActionsProcessor sap, TransactionStateNames next) {
		super(TransactionStateNames.LISTEN_FOR_CONNECTIONS, sap, next);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.ListenerForConnection(transaction, next);
	}

}