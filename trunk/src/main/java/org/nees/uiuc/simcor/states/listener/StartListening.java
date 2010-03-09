package org.nees.uiuc.simcor.states.listener;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class StartListening extends TransactionState {


	public StartListening(StateActionsProcessor sap) {
		super(TransactionStateNames.START_LISTENING, sap);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.startListening(transaction);
	}

}
