package org.nees.uiuc.simcor.states.common;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithLcf;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class WaitForResponse extends TransactionState {
	private final Logger log = Logger.getLogger(WaitForResponse.class);

	public WaitForResponse(StateActionsProcessorWithLcf sap) {
		super(TransactionStateNames.WAIT_FOR_RESPONSE, sap,
				TransactionStateNames.RESPONSE_AVAILABLE);
	}

	@Override
	public void execute(Transaction transaction) {
		if (transaction instanceof SimpleTransaction == false) {
			log.fatal("Transaction not simple", new Exception());
		}
		sap.waitForRead((SimpleTransaction) transaction, true, next);
	}

}
