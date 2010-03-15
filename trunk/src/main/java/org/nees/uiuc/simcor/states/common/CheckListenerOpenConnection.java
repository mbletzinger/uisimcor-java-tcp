package org.nees.uiuc.simcor.states.common;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithLsm;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class CheckListenerOpenConnection extends TransactionState {
	private final Logger log = Logger
			.getLogger(CheckListenerOpenConnection.class);
	public CheckListenerOpenConnection(StateActionsProcessor sap) {
		super(TransactionStateNames.CHECK_LISTENER_OPEN_CONNECTION, sap, TransactionStateNames.TRANSACTION_DONE);
	}

	@Override
	public void execute(Transaction transaction) {
		if(transaction instanceof SimpleTransaction == false) {
			log.fatal("Transaction not simple",new Exception());
		}

		StateActionsProcessorWithLsm sapwl = (StateActionsProcessorWithLsm) sap;
		sapwl.checkListenerForConnection((SimpleTransaction) transaction, next);

	}

}
