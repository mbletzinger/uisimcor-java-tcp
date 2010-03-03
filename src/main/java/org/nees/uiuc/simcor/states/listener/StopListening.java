package org.nees.uiuc.simcor.states.listener;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.transaction.Transaction;

public class StopListening extends TransactionState {


	public StopListening(StateActionsProcessor sap) {
		super(Transaction.TransactionStateNames.STOP_LISTENING, sap);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.stopListening(transaction);
	}

}
