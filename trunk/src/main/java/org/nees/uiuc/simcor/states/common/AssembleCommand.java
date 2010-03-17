package org.nees.uiuc.simcor.states.common;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class AssembleCommand extends TransactionState {
	public enum AssembleCommandType { CLOSE, OPEN, OTHER };
	private AssembleCommandType cmdType;
	private final Logger log = Logger.getLogger(AssembleCommand.class);
	public AssembleCommand(TransactionStateNames state,
			StateActionsProcessor sap, AssembleCommandType cmdType) {
		super(state, sap, TransactionStateNames.SENDING_COMMAND);
		this.cmdType = cmdType;
	}

	@Override
	public void execute(Transaction transaction) {
		if(transaction instanceof SimpleTransaction == false) {
			log.fatal("Transaction not simple",new Exception());
		}
		if(cmdType.equals(AssembleCommandType.OPEN)) {
			sap.assembleSessionMessage((SimpleTransaction) transaction, true, true, next);
			return;
		}
		if(cmdType.equals(AssembleCommandType.CLOSE)) {
			sap.assembleSessionMessage((SimpleTransaction) transaction, false, true, next);
			return;
		}
		if(cmdType.equals(AssembleCommandType.OTHER)) {
			sap.setUpWrite((SimpleTransaction) transaction, true, next);
		}
	}

}
