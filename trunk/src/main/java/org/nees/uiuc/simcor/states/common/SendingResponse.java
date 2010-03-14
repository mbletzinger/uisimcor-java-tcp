package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessorWithLcf;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;

public class SendingResponse extends TransactionState {

	public SendingResponse(StateActionsProcessorWithLcf sap) {
		super(TransactionStateNames.SENDING_RESPONSE, sap,
				TransactionStateNames.TRANSACTION_DONE);
	}

	@Override
	public void execute(SimpleTransaction transaction) {
		sap.waitForSend(transaction, next);
	}

}
