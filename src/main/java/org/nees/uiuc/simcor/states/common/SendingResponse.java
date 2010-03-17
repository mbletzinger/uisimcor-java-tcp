package org.nees.uiuc.simcor.states.common;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class SendingResponse extends TransactionState {
private final Logger log = Logger.getLogger(SendingResponse.class);
	public SendingResponse(StateActionsProcessor sap) {
		super(TransactionStateNames.SENDING_RESPONSE, sap,
				TransactionStateNames.TRANSACTION_DONE);
	}

	@Override
	public void execute(Transaction transaction) {
		if(transaction instanceof SimpleTransaction == false) {
			log.fatal("Transaction not simple",new Exception());
		}
		sap.waitForSend((SimpleTransaction) transaction, next);
	}

}
