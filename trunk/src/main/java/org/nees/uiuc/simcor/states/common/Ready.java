package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class Ready extends TransactionState {

	public Ready(StateActionsProcessor sap) {
		super(TransactionStateNames.READY,sap);
	}

	@Override
	public void execute(Transaction transaction) {
	}

}
