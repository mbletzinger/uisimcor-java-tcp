package org.nees.uiuc.simcor.states.old;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class OpeningConnection extends TransactionState {

	public OpeningConnection(StateActionsProcessor sap) {
		super(TransactionStateNames.OPENING_CONNECTION, sap);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.openConnection(transaction);
	}

}
