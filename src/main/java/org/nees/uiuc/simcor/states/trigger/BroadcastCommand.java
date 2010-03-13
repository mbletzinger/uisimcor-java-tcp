package org.nees.uiuc.simcor.states.trigger;

import org.nees.uiuc.simcor.listener.ClientConnections;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class BroadcastCommand extends TriggeringState {

	public BroadcastCommand(TransactionStateNames state,
			StateActionsProcessor sap, ClientConnections cc) {
		super(state, sap, cc, TransactionStateNames.WAIT_FOR_TRIGGER_RESPONSES);
	}

	@Override
	public void execute(Transaction transaction) {
		cc.broadcast(transaction.getCommand(), transaction.getId());

	}

}
