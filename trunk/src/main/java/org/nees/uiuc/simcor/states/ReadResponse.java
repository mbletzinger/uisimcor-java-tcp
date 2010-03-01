package org.nees.uiuc.simcor.states;

import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.TcpActionsDto.ActionsType;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class ReadResponse extends TransactionState {

	public ReadResponse() {
		super(TransactionStateNames.READ_RESPONSE);
	}

	@Override
	public void execute(Transaction transaction, Connection connection) {
		TcpActionsDto action = new TcpActionsDto();
		action.setAction(ActionsType.READ);
		connection.setMsgTimeout(transaction.getTimeout());
		connection.setToRemoteMsg(action);
		transaction.setState(TransactionStateNames.WAIT_FOR_RESPONSE);
	}

}
