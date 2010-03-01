package org.nees.uiuc.simcor.states;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.logging.Archiving;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.ConnectionFactory;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class ErrorsExist extends ExitState {
	private Logger log = Logger.getLogger(ErrorsExist.class);
	public ErrorsExist(ConnectionFactory cf, Archiving archive) {
		super(TransactionStateNames.ERRORS_EXIST,cf,archive);
	}

	@Override
	public void execute(Transaction transaction, Connection connection) {
		if(transaction.getError().getType().equals(TcpErrorTypes.NONE)) {
			transaction.setError(cf.getSavedError());
		}
		if(archive.isArchivingEnabled()) {
			log.debug("Handling: " + transaction);
			archive.logTransaction(transaction);
		}
		cf.clearError();
		setStatus(transaction, new TcpError(), TransactionStateNames.READY);
	}
}
