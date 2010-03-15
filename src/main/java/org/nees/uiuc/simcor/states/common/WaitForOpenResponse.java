package org.nees.uiuc.simcor.states.common;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithLcf;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class WaitForOpenResponse extends TransactionState {
	private final Logger log = Logger.getLogger(WaitForOpenResponse.class);

	public WaitForOpenResponse(StateActionsProcessorWithLcf sap) {
		super(TransactionStateNames.WAIT_FOR_OPEN_RESPONSE, sap,
				TransactionStateNames.TRANSACTION_DONE);
	}

	@Override
	public void execute(Transaction transaction) {
		if (transaction instanceof SimpleTransaction == false) {
			log.fatal("Transaction not simple", new Exception());
		}
		sap.waitForSessionMsgRead((SimpleTransaction) transaction, false, next);
	}

}
