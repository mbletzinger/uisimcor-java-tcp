package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessorWithLcf;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class CloseConnection extends TransactionState {

	public CloseConnection(StateActionsProcessorWithLcf sap) {
		super(TransactionStateNames.CLOSING_CONNECTION, sap,
				TransactionStateNames.TRANSACTION_DONE);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.closingConnection(transaction, next);
	}

}
