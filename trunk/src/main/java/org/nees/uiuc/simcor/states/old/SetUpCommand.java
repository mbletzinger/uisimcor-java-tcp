package org.nees.uiuc.simcor.states.old;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class SetUpCommand extends TransactionState {
	private final Logger log = Logger
			.getLogger(SetUpCommand.class);

	public SetUpCommand(StateActionsProcessor sap) {
		super(TransactionStateNames.SETUP_COMMAND, sap);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.setUpWrite(transaction, true,TransactionStateNames.SENDING_COMMAND);
	}

}
