package org.nees.uiuc.simcor.test.util;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class TriggerConnectionsClient {
	private final StateActionsProcessor sap = new StateActionsProcessor();
	private final Logger log = Logger.getLogger(TriggerConnectionsClient.class);

	public TriggerConnectionsClient(TcpParameters params,
			String systemDescription) {
		sap.setParams(params);
		sap.getTf().setSystemDescription(systemDescription);
	}

	public void connect() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		TransactionStateNames prevState = TransactionStateNames.READY;
		SimpleTransaction transaction = sap.getTf().createSimpleTransaction(
				null);
		sap.openConnection(transaction);
		while (transaction.getState().equals(
				TransactionStateNames.CHECK_OPEN_CONNECTION)) {
			sap.checkOpenConnection(transaction,
					TransactionStateNames.SETUP_READ_COMMAND);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			if (transaction.getState().equals(prevState) == false) {
				log.debug("During opening connection: " + transaction);
				prevState = transaction.getState();
			}
		}
		log.debug("After trigger connect: " + sap.getCm().checkForErrors());
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		sap.setUpRead(transaction, true,
				TransactionStateNames.WAIT_FOR_OPEN_COMMAND);
		while (transaction.getState().equals(
				TransactionStateNames.WAIT_FOR_OPEN_COMMAND)) {
			sap.waitForRead(transaction, true,
					TransactionStateNames.ASSEMBLE_OPEN_RESPONSE);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			if (transaction.getState().equals(prevState) == false) {
				log.debug("During wait for command: " + transaction);
				prevState = transaction.getState();
			}
		}

		sap.assembleSessionMessage(transaction, true, false,
				TransactionStateNames.SENDING_RESPONSE);
		log.debug("After assemble response: " + transaction);
		while (transaction.equals(TransactionStateNames.SENDING_RESPONSE)) {
			sap
					.waitForSend(transaction,
							TransactionStateNames.TRANSACTION_DONE);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			if (transaction.getState().equals(prevState) == false) {
				log.debug("During wait for response send: " + transaction);
				prevState = transaction.getState();
			}
		}
		log.debug("After response sent: " + transaction);
	}

	public TcpError getError() {
		return sap.getCm().checkForErrors();
	}

	public void checkForMessages() {
		SimpleTransaction transaction = sap.getTf().createSimpleTransaction(
				new SimCorMsg());
		sap
				.setUpRead(transaction, true,
						TransactionStateNames.WAIT_FOR_COMMAND);
		log.debug("After read command setup: " + sap.getCm().checkForErrors());
		while (transaction.getState().equals(
				TransactionStateNames.WAIT_FOR_COMMAND)) {
			sap.waitForRead(transaction, true,
					TransactionStateNames.COMMAND_AVAILABLE);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
		}
		log.debug("After read command: " + sap.getCm().checkForErrors());
		if (transaction.getError().getType().equals(TcpErrorTypes.NONE) == false) {
			log.error("Transaction error: " + transaction);
			return;
		}
		SimCorMsg rsp = sap.getTf().createResponse("MDL-00-01", null,
				sap.getTf().getSystemDescription(), false);
		transaction.setResponse(rsp);
		sap.setUpWrite(transaction, false,
				TransactionStateNames.SENDING_RESPONSE);
		log.debug("After setup write: " + sap.getCm().checkForErrors());
		while (transaction.getState().equals(
				TransactionStateNames.SENDING_RESPONSE)) {
			sap.waitForRead(transaction, true,
					TransactionStateNames.TRANSACTION_DONE);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
		}
		log.debug("After write response: " + sap.getCm().checkForErrors());
	}

	public void closeConnection() {
		SimpleTransaction transaction = sap.getTf().createSimpleTransaction(
				new SimCorMsg());
		sap.closingConnection(transaction,
				TransactionStateNames.TRANSACTION_DONE);
		log.debug("After close connection: " + sap.getCm().checkForErrors());

	}
}
