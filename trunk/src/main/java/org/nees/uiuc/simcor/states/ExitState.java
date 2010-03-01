package org.nees.uiuc.simcor.states;

import org.nees.uiuc.simcor.logging.Archiving;
import org.nees.uiuc.simcor.tcp.ConnectionFactory;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public abstract class ExitState extends ConnectionState {

	protected Archiving archive;

	public ExitState(TransactionStateNames state, ConnectionFactory cf,  Archiving archive) {
		super(state,cf);
		this.archive = archive;
	}

	public Archiving getArchive() {
		return archive;
	}

	public void setArchive(Archiving archive) {
		this.archive = archive;
	}

}