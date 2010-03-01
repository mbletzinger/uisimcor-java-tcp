package org.nees.uiuc.simcor.states;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.TcpActionsDto.ActionsType;
import org.nees.uiuc.simcor.transaction.Msg2Tcp;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class TransmitCommandWaitForCommand extends TransactionState {
private final Logger log = Logger
		.getLogger(TransmitCommandWaitForCommand.class);
	public TransmitCommandWaitForCommand() {
		super(TransactionStateNames.WAIT_FOR_COMMAND);
	}

	@Override
	public void execute(Transaction transaction, Connection connection) {
		if (transaction.isPosted()) {
			TcpActionsDto action = new TcpActionsDto();
			action.setAction(ActionsType.WRITE);
			Msg2Tcp msg = action.getMsg();
			msg.setId(transaction.getId());
			msg.setMsg(transaction.getCommand());
			log.debug("Sending: " + transaction);
			connection.setToRemoteMsg(action);
			transaction.setPosted(false);
			transaction.setState(TransactionStateNames.SENDING_COMMAND);
		}

	}

}
