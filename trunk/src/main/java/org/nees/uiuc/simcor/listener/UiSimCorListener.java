package org.nees.uiuc.simcor.listener;

import java.util.HashMap;
import java.util.Map;

import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;

public class UiSimCorListener extends Thread {
	private final ClientConnections cc;
	private final boolean isP2P;
	protected Map<TransactionStateNames, TransactionState> machine = new HashMap<TransactionStateNames, TransactionState>();
	public UiSimCorListener(ClientConnections cc, boolean isP2P) {
		super();
		this.cc = cc;
		this.isP2P = isP2P;
	}
	private ClientId oneClient = null;
	private boolean isRunning;
	public synchronized ClientId getOneClient() {
		return oneClient;
	}
	public synchronized ClientId pickupOneClient() {
		ClientId result =  oneClient;
		oneClient = null;
		return result;
	}
	public synchronized void setOneClient(ClientId oneClient) {
		this.oneClient = oneClient;
	}
	public synchronized boolean isRunning() {
		return isRunning;
	}
	public synchronized void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	@Override
	public void run() {
		setRunning(true);
		while(isRunning()) {
			
		}
	}

}
