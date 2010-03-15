package org.nees.uiuc.simcor.states.common;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithLcf;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class CloseConnection extends TransactionState {
private final Logger log = Logger.getLogger(CloseConnection.class);
	public CloseConnection(StateActionsProcessorWithLcf sap) {
		super(TransactionStateNames.CLOSING_CONNECTION, sap,
				TransactionStateNames.TRANSACTION_DONE);
	}

	@Override
	public void execute(Transaction transaction) {
		if(transaction instanceof SimpleTransaction == false) {
			log.fatal("Transaction not simple",new Exception());
		}

		sap.closingConnection((SimpleTransaction) transaction, next);
	}

}
