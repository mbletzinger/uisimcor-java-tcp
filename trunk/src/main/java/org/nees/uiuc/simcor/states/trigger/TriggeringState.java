package org.nees.uiuc.simcor.states.trigger;

import org.nees.uiuc.simcor.listener.ClientConnections;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;

public abstract class TriggeringState extends TransactionState {

	protected ClientConnections cc;
	public TriggeringState(TransactionStateNames state,
			StateActionsProcessor sap, ClientConnections cc, TransactionStateNames next) {
		super(state, sap, next);
		this.cc = cc;
	}


}
