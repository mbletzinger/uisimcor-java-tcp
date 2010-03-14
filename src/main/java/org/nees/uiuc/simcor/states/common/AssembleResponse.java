package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessorWithLcf;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;

public class AssembleResponse extends TransactionState {
	private boolean isSessionResponse;

	public AssembleResponse(TransactionStateNames state,
			StateActionsProcessorWithLcf sap, boolean isSessionResponse) {
		super(state, sap, TransactionStateNames.SENDING_RESPONSE);
		this.isSessionResponse = isSessionResponse;
	}

	@Override
	public void execute(SimpleTransaction transaction) {
		if (isSessionResponse) {
			sap.assembleSessionMessage(transaction, true, false, next);
		} else {
			sap.setUpWrite(transaction, false, next);
		}
	}

}
