package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessorWithLcf;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class SetupReadMessage extends TransactionState {

	private boolean isCommand;
	public SetupReadMessage(TransactionStateNames state, StateActionsProcessorWithLcf sap, boolean isCommand, TransactionStateNames next) {
		super(state, sap,next);
		this.isCommand = isCommand;
	}

	@Override
	public void execute(Transaction transaction) {
		sap.setUpRead(transaction, isCommand, next);
	}

}
