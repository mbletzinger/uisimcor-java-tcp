package org.nees.uiuc.simcor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.ConnectionFactory;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.Connection.ConnectionState;
import org.nees.uiuc.simcor.tcp.TcpActionsDto.ActionsType;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.TransactionFactory;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class TriggerBroadcaster {
	private List<TriggerClient> clients = new ArrayList<TriggerClient>();
	private ConnectionFactory connectionFactory = new ConnectionFactory();
	private TransactionFactory transactionFactory = new TransactionFactory();
	private final Logger log = Logger.getLogger(TriggerBroadcaster.class);
	private List<TriggerClient> badClients = new ArrayList<TriggerClient>();
	public static List<TransactionStateNames> sendCommandStates = new ArrayList<TransactionStateNames>(); 
	static {
		sendCommandStates.add(TransactionStateNames.WAIT_FOR_COMMAND);
		sendCommandStates.add(TransactionStateNames.SENDING_COMMAND);
		sendCommandStates.add(TransactionStateNames.WAIT_FOR_RESPONSE);
		sendCommandStates.add(TransactionStateNames.RESPONSE_AVAILABLE);
		sendCommandStates.add(TransactionStateNames.TRANSACTION_DONE);

	}
	public void startBroadcast(SimCorMsg cmd) {
		collectClients();
		for(Iterator<TriggerClient> c = clients.iterator();c.hasNext();) {
			TriggerClient tc = c.next();
			Transaction t = transactionFactory.createTransaction(cmd);
			tc.startTransaction(t);
		}
	}
	
	public TransactionStateNames isDone() {
		TransactionStateNames result = TransactionStateNames.TRANSACTION_DONE;
		badClients.clear();
		for(Iterator<TriggerClient> c = clients.iterator(); c.hasNext();) {
			TriggerClient tc = c.next();
			TransactionStateNames r = tc.isReady();
			if(r == TransactionStateNames.ERRORS_EXIST) {
				badClients.add(tc);
				continue;
			}
			if(sendCommandStates.indexOf(r) < sendCommandStates.indexOf(result)) {
				result = r;
			}
		}
		
		return result;
	}
	
	public String [] getHostnames() {
		List<String> result = new ArrayList<String>();
		for(Iterator<TriggerClient> tc = clients.iterator(); tc.hasNext();) {
//			result.add(tc.next().getConnection().getRemoteHost());
		}
		return (String[]) result.toArray();
	}
	
	public List<TriggerClient> getClients() {
		return clients;
	}

	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public TransactionFactory getTransactionFactory() {
		return transactionFactory;
	}

	public List<TcpError> getErrorList() {
		List<TcpError> result = new ArrayList<TcpError>();
		for(Iterator<TriggerClient> c = badClients.iterator(); c.hasNext();) {
			result.add(c.next().getErrors());
		}
		return result;
	}

	public List<TriggerClient> getBadClients() {
		return badClients;
	}

	public void disconnect(boolean allClients) {
		List<TriggerClient> list = badClients;
		if(allClients) {
			list = clients;
		}
		for(Iterator<TriggerClient> t = list.iterator();t.hasNext();) {
			TriggerClient tc = t.next();
//			execute(tc.getConnection(), ActionsType.CLOSE);
//			execute(tc.getConnection(), ActionsType.EXIT);
			clients.remove(tc);
		}
	}
	private void collectClients() {
		List<Connection> cns = null;
		try {
			cns = connectionFactory.checkForListenerConnections();
		} catch (Exception e) {
			log.error("Connections collections failed because",e);
			return;
		}
		
		for(Iterator<Connection> cn = cns.iterator(); cn.hasNext();) {
			clients.add(new TriggerClient(cn.next()));
		}
	}
	private TcpErrorTypes execute(Connection connection, ActionsType a) {
		TcpActionsDto cmd = new TcpActionsDto();
		cmd.setAction(a);
		connection.setToRemoteMsg(cmd);
		while(connection.getConnectionState().equals(ConnectionState.BUSY)) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		TcpActionsDto rsp  = connection.getFromRemoteMsg();
		return rsp.getError().getType();
	}

}
