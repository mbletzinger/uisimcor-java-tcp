package org.nees.uiuc.simcor.states;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.TcpActionsDto.ActionsType;
import org.nees.uiuc.simcor.transaction.Msg2Tcp;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class ReceiveCommandWaitForResponse extends TransactionState {
	private final Logger log = Logger
			.getLogger(ReceiveCommandWaitForResponse.class);

	public ReceiveCommandWaitForResponse() {
		super(TransactionStateNames.WAIT_FOR_RESPONSE);
	}

	@Override
	public void execute(Transaction transaction, Connection connection) {
		if (transaction.isPosted()) {
			TcpActionsDto action = new TcpActionsDto();
			action.setAction(ActionsType.WRITE);
			log.debug("Sending response " + transaction);
			Msg2Tcp msg = action.getMsg();
			msg.setId(transaction.getId());
			msg.setMsg(transaction.getResponse());
			connection.setToRemoteMsg(action);
			transaction.setPosted(false);
			transaction.setState(TransactionStateNames.SENDING_RESPONSE);
		}

	}

}
