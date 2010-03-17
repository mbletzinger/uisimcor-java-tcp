package org.nees.uiuc.simcor.states.common;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithLcf;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class SetupReadMessage extends TransactionState {
	private boolean isCommand;
	private final Logger log = Logger.getLogger(SetupReadMessage.class);

	public SetupReadMessage(TransactionStateNames state,
			StateActionsProcessorWithLcf sap, boolean isCommand,
			TransactionStateNames next) {
		super(state, sap, next);
		this.isCommand = isCommand;
	}

	@Override
	public void execute(Transaction transaction) {
		if (transaction instanceof SimpleTransaction == false) {
			log.fatal("Transaction not simple", new Exception());
		}
		sap.setUpRead(transaction, isCommand, next);
	}

}
