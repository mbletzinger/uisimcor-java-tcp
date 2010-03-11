package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class CheckListenerOpenConnection extends TransactionState {

	public CheckListenerOpenConnection(
			StateActionsProcessor sap, TransactionStateNames next) {
		super(TransactionStateNames.CHECK_LISTENER_OPEN_CONNECTION, sap, next);
	}

	@Override
	public void execute(Transaction transaction) {

	}

}
