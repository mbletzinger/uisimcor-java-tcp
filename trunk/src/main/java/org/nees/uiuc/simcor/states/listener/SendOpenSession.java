package org.nees.uiuc.simcor.states.listener;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class SendOpenSession extends TransactionState {

	public SendOpenSession(StateActionsProcessor sap) {
		super(TransactionStateNames.WAIT_FOR_COMMAND,sap);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.assembleSessionMessage(transaction, true, true,TransactionStateNames.SENDING_COMMAND);	
		}


}
