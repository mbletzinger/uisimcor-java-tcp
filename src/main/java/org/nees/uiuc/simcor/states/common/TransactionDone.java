package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessorWithLcf;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;

public class TransactionDone extends TransactionState {

	public TransactionDone(
			StateActionsProcessorWithLcf sap, TransactionStateNames next) {
		super(TransactionStateNames.TRANSACTION_DONE, sap, next);
	}

	@Override
	public void execute(SimpleTransaction transaction) {
		sap.recordTransaction(transaction,next);
	}

}
