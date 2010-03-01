package org.nees.uiuc.simcor.states.trigger;

import org.nees.uiuc.simcor.factories.TransactionFactory;
import org.nees.uiuc.simcor.states.SendSessionMessageState;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class SendCloseSession extends SendSessionMessageState {

	public SendCloseSession(TransactionFactory tf) {
		super(TransactionStateNames.WAIT_FOR_COMMAND,tf);
		isOpen = true;
	}


}
