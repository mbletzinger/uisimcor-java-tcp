package org.nees.uiuc.simcor.listener;

import java.util.ArrayList;
import java.util.List;

import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.Connection.ConnectionStatus;
import org.nees.uiuc.simcor.tcp.TcpActionsDto.ActionsType;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.Msg2Tcp;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;

public class ClientConnections {
	private final ArrayList<ClientId> clients = new ArrayList<ClientId>();
	private String message;
	private int msgTimeout = 3000;
	private final ArrayList<ClientId> newClients = new ArrayList<ClientId>();
	public ClientConnections() {
	}

	public synchronized void addClient(ClientId client) {
		newClients.add(client);
	}

	public synchronized void assembleTriggerMessages(SimCorMsg msg, TransactionIdentity id) {
		mergeClients();
		for (ClientId c : clients) {
			sendMsg(c.connection, msg, id);
		}
	}

	private  TcpError checkResponse(Connection client) {
		if (client.getConnectionState().equals(ConnectionStatus.BUSY)) {
			return null;
		}
		return client.getFromRemoteMsg().getError();
	}
	
	private void closeClient(Connection client) {
		TcpActionsDto cmd = new TcpActionsDto();
		cmd.setAction(ActionsType.CLOSE);
		client.setToRemoteMsg(cmd);
	}

	public String getMessage() {
		return message;
	}
	public int getMsgTimeout() {
		return msgTimeout;
	}

	private void mergeClients() {
		clients.addAll(newClients);
		message = "";
		for (ClientId c : newClients) {
			message = message + c.system + " at " + c.remoteHost
					+ " is connected.\n";
		}
		newClients.clear();
	}

	private void readMsg(Connection c) {
		TcpActionsDto action = new TcpActionsDto();
		action.setAction(ActionsType.READ);
		c.setMsgTimeout(msgTimeout);
		c.setToRemoteMsg(action);
	}

	private void sendMsg(Connection client, SimCorMsg msg,
			TransactionIdentity id) {
		client.setMsgTimeout(msgTimeout);
		TcpActionsDto action = new TcpActionsDto();
		action.setAction(ActionsType.WRITE);
		Msg2Tcp m2t = action.getMsg();
		m2t.setId(id);
		m2t.setMsg(msg);
		client.setToRemoteMsg(action);
	}

	public void setMsgTimeout(int msgTimeout) {
		this.msgTimeout = msgTimeout;
	}

	public void setupResponsesCheck() {
		for (ClientId c : clients) {
			readMsg(c.connection);
		}		
	}

	public synchronized boolean waitForBroadcastFinished() {
		for (ClientId c : clients) {
			if (c.connection.getConnectionState() == ConnectionStatus.BUSY) {
				return false;
			}
		}	
		return true;
	}
	public synchronized boolean waitForResponsesFinished() {
		boolean result = true;
		message = "";
		List<Integer> lostClientsIdx = new ArrayList<Integer>();
		for (ClientId c : clients) {
			TcpError er = checkResponse(c.connection);
			if (er == null) {
				result = false;
				continue;
			}
			if (er.getType().equals(TcpErrorTypes.NONE) == false) {
				int idx = clients.indexOf(c);
				message = message + "Lost contact with " + c.system + " at "
						+ c.remoteHost + " because " + er.getText() + "\n";
				lostClientsIdx.add(new Integer(idx));
				closeClient(c.connection);
			}
		}
		for(Integer i : lostClientsIdx) {
			int idx = i.intValue();
			clients.remove(idx);
		}
		return result;
	}
}
