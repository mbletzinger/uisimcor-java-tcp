package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class CommandAvailable extends TransactionState {

	public CommandAvailable(StateActionsProcessor sap) {
		super(TransactionStateNames.COMMAND_AVAILABLE,sap);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.waitForPickUp(transaction, TransactionStateNames.WAIT_FOR_RESPONSE);
	}

}
