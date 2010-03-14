package org.nees.uiuc.simcor.states;

import org.nees.uiuc.simcor.listener.ClientConnections;
import org.nees.uiuc.simcor.listener.ListenerStateMachine;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.Transaction;

public class StateActionsProcessorWithCc extends StateActionsProcessorWithLsm {

	private ClientConnections cc;
	public StateActionsProcessorWithCc(ListenerStateMachine lsm) {
		super(lsm);
		this.cc = lsm.getCc();
	}
	public void assembleTriggerCommands(Transaction transaction, TransactionStateNames next, boolean isClose) {
		if(isClose) {
			SimCorMsg msg = tf.createSessionCommand(false);
			transaction.setCommand(msg);
		}
		cc.assembleTriggerMessages(transaction.getCommand(), transaction.getId());
		setStatus(transaction, transaction.getError(), next);
	}
	public void broadcastCommands(Transaction transaction, TransactionStateNames next) {
		TransactionStateNames state = transaction.getState();
		if(cc.waitForBroadcastFinished()) {
			state = next;
		}
		setStatus(transaction, transaction.getError(), state);
	}
	public void setupTriggerResponses(Transaction transaction, TransactionStateNames next) {
		cc.setupResponsesCheck();
		setStatus(transaction, transaction.getError(), next);		
		
	}
	public void waitForTriggerResponse(Transaction transaction, TransactionStateNames next) {
		TransactionStateNames state = transaction.getState();
		if(cc.waitForResponsesFinished()) {
			state = next;
		}
		setStatus(transaction, transaction.getError(), state);
		
	}
	public void closeTriggerConnections(Transaction transaction, TransactionStateNames next) {
		
	}

}
