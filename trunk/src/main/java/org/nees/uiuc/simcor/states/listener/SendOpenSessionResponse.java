package org.nees.uiuc.simcor.states.listener;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class SendOpenSessionResponse extends TransactionState {

	public SendOpenSessionResponse(StateActionsProcessor sap) {
		super(TransactionStateNames.SEND_OPEN_SESSION_RESPONSE,sap);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.assembleSessionMessage(transaction, false, false, TransactionStateNames.SENDING_RESPONSE);
	}

}
