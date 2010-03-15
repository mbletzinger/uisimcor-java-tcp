package org.nees.uiuc.simcor.states;

import org.nees.uiuc.simcor.listener.ClientConnections;
import org.nees.uiuc.simcor.listener.ListenerStateMachine;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.BroadcastTransaction;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.DirectionType;

public class StateActionsProcessorWithCc extends StateActionsProcessorWithLsm {

	private ClientConnections cc;
	public StateActionsProcessorWithCc(ListenerStateMachine lsm) {
		super(lsm);
		this.cc = lsm.getCc();
		lsm.getSap().setTf(tf);
		tf.setDirection(DirectionType.SEND_COMMAND);
		lsm.getSap().setCm(cm);
	}
	public void assembleTriggerCommands(BroadcastTransaction transaction, TransactionStateNames next, boolean isClose) {
		if(isClose) {
			SimCorMsg msg = tf.createSessionCommand(false);
			transaction.setCommand(msg);
		}
		cc.assembleTriggerMessages(transaction);
		setStatus((Transaction)transaction, new TcpError(), next);
	}
	public void broadcastCommands(BroadcastTransaction transaction, TransactionStateNames next) {
		TransactionStateNames state = transaction.getState();
		if(cc.waitForBroadcastFinished()) {
			state = next;
		}
		TcpError err = new TcpError();
		if(transaction.getBroadcastMsg() != null) {
			err.setType(TcpErrorTypes.BROADCAST_CLIENTS_ADDED);
		}
		setStatus((Transaction)transaction, err, state, state);
	}
	public void setupTriggerResponses(Transaction transaction, TransactionStateNames next) {
		cc.setupResponsesCheck();
		setStatus(transaction, new TcpError(), next);		
		
	}
	public void waitForTriggerResponse(BroadcastTransaction transaction, TransactionStateNames next) {
		TransactionStateNames state = transaction.getState();
		if(cc.waitForResponsesFinished(transaction)) {
			state = next;
		}
		TcpError err = new TcpError();
		if(transaction.getResponseMsg() != null) {
			err.setType(TcpErrorTypes.BROADCAST_CLIENTS_LOST);
		}
		setStatus(transaction, err, state,state);
		
	}
	public void closeTriggerConnections(Transaction transaction, TransactionStateNames next) {
		
	}

}
