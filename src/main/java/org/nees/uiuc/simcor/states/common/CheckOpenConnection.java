package org.nees.uiuc.simcor.states.common;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class CheckOpenConnection extends TransactionState {
	private final Logger log = Logger.getLogger(CheckOpenConnection.class);

	public CheckOpenConnection(StateActionsProcessor sap, TransactionStateNames next) {
		super(TransactionStateNames.CHECK_OPEN_CONNECTION, sap, next);
	}

	@Override
	public void execute(Transaction transaction) {
		if (transaction instanceof SimpleTransaction == false) {
			log.fatal("Transaction not simple", new Exception());
		}
		sap.checkOpenConnection((SimpleTransaction) transaction, next);
	}

}
