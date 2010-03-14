package org.nees.uiuc.simcor.transaction;

import java.util.HashSet;
import java.util.Set;

public class BroadcastTransaction extends Transaction {
	private final Set<SimCorMsg> responses = new HashSet<SimCorMsg>();
	public void addResponse(SimCorMsg msg) {
		responses.add(msg);
	}
	public void clearResponses() {
		responses.clear();
	}
	public Set<SimCorMsg> getResponses() {
		return responses;
	}
}
