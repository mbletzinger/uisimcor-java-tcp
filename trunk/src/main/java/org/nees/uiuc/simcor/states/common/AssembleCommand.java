package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessorWithLcf;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class AssembleCommand extends TransactionState {
	public enum AssembleCommandType { OPEN, CLOSE, OTHER };
	private AssembleCommandType cmdType;
	public AssembleCommand(TransactionStateNames state,
			StateActionsProcessorWithLcf sap, AssembleCommandType cmdType) {
		super(state, sap, TransactionStateNames.SENDING_COMMAND);
		this.cmdType = cmdType;
	}

	@Override
	public void execute(Transaction transaction) {
		if(cmdType.equals(AssembleCommandType.OPEN)) {
			sap.assembleSessionMessage(transaction, true, true, next);
			return;
		}
		if(cmdType.equals(AssembleCommandType.CLOSE)) {
			sap.assembleSessionMessage(transaction, false, true, next);
			return;
		}
		if(cmdType.equals(AssembleCommandType.OTHER)) {
			sap.setUpWrite(transaction, true, next);
		}
	}

}
