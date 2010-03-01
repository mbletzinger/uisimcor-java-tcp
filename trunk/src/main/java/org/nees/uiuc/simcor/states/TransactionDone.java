package org.nees.uiuc.simcor.states;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.logging.Archiving;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.ConnectionFactory;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class TransactionDone extends ExitState {
	private Logger log = Logger.getLogger(TransactionDone.class);
	public TransactionDone(ConnectionFactory cf,Archiving archive) {
		super(TransactionStateNames.TRANSACTION_DONE,cf,archive);
	}

	@Override
	public void execute(Transaction transaction, Connection connection) {
		if(archive.isArchivingEnabled()) {
			log.debug("Handling: " + transaction);
			archive.logTransaction(transaction);
		} else {
			log.debug("Skip archiving");
		}
		
		setStatus(transaction, new TcpError(), TransactionStateNames.READY);
	}

}
