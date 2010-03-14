package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;

public class ResponseAvailable extends TransactionState {

	public ResponseAvailable(StateActionsProcessor sap) {
		super(TransactionStateNames.RESPONSE_AVAILABLE, sap, TransactionStateNames.TRANSACTION_DONE);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(SimpleTransaction transaction) {
		// TODO Auto-generated method stub

	}

}
