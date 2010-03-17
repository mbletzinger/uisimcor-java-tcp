package org.nees.uiuc.simcor.states.broadcast;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithCc;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.BroadcastTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class WaitForTriggerResposnes extends TransactionState {

	public WaitForTriggerResposnes(StateActionsProcessor sap) {
		super(TransactionStateNames.WAIT_FOR_TRIGGER_RESPONSES, sap, TransactionStateNames.TRANSACTION_DONE);
	}

	@Override
	public void execute(Transaction transaction) {
		((StateActionsProcessorWithCc)sap).waitForTriggerResponse((BroadcastTransaction) transaction, next);
	}

}
