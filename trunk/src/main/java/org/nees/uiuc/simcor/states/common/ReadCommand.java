package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class ReadCommand extends TransactionState {

	public ReadCommand(StateActionsProcessor sap) {
		super(TransactionStateNames.READ_COMMAND, sap);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.setUpRead(transaction, true, TransactionStateNames.WAIT_FOR_COMMAND);
	}

}
