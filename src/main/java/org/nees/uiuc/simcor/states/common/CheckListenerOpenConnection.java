package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class CheckListenerOpenConnection extends TransactionState {

	public CheckListenerOpenConnection(TransactionStateNames state,
			StateActionsProcessor sap, TransactionStateNames next) {
		super(state, sap, next);
	}

	@Override
	public void execute(Transaction transaction) {

	}

}
