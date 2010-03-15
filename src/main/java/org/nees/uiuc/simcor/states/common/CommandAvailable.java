package org.nees.uiuc.simcor.states.common;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class CommandAvailable extends TransactionState {
private final Logger log = Logger.getLogger(CommandAvailable.class);
	public CommandAvailable(StateActionsProcessor sap) {
		super(TransactionStateNames.COMMAND_AVAILABLE, sap,
				TransactionStateNames.WAIT_FOR_RESPONSE_POSTING);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(Transaction transaction) {
		if(transaction instanceof SimpleTransaction == false) {
			log.fatal("Transaction not simple",new Exception());
		}

		sap.waitForPickUp((SimpleTransaction) transaction, next);
	}

}
