package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.transaction.Transaction;

public class StartListening extends TransactionState {


	public StartListening(StateActionsProcessor sap) {
		super(Transaction.TransactionStateNames.START_LISTENING, sap);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.startListening(transaction);
	}

}
