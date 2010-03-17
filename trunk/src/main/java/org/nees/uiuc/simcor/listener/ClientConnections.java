package org.nees.uiuc.simcor.listener;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.Connection.ConnectionStatus;
import org.nees.uiuc.simcor.tcp.TcpActionsDto.ActionsType;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.BroadcastTransaction;
import org.nees.uiuc.simcor.transaction.Msg2Tcp;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;
import org.nees.uiuc.simcor.transaction.TriggerResponse;

public class ClientConnections {
	private final List<ClientIdWithConnection> clients = new ArrayList<ClientIdWithConnection>();
	private int msgTimeout = 3000;
	private final List<ClientIdWithConnection> newClients = new ArrayList<ClientIdWithConnection>();
	private final Logger log = Logger.getLogger(ClientConnections.class);
	private final List<ClientIdWithConnection> waitingForResponse = new ArrayList<ClientIdWithConnection>();

	public ClientConnections() {
	}

	public synchronized void addClient(ClientIdWithConnection client) {
		newClients.add(client);
	}

	public synchronized void assembleTriggerMessages(
			BroadcastTransaction transaction) {
		mergeClients(transaction);
		for (ClientIdWithConnection c : clients) {
			sendMsg(c.connection, transaction.getCommand(), transaction.getId());
		}
	}

	private TriggerResponse checkResponse(ClientIdWithConnection cid) {
		Connection client = cid.connection;
		if (client.getConnectionState().equals(ConnectionStatus.BUSY)) {
			return null;
		}
		TriggerResponse result = new TriggerResponse(client.getFromRemoteMsg()
				.getMsg());
		result.setRemoteId(new ClientId(cid));
		result.setError(client.getFromRemoteMsg().getError());
		return result;
	}

	private void closeClient(Connection client) {
		TcpActionsDto cmd = new TcpActionsDto();
		cmd.setAction(ActionsType.CLOSE);
		client.setToRemoteMsg(cmd);
	}

	public int getMsgTimeout() {
		return msgTimeout;
	}

	private void mergeClients(BroadcastTransaction transaction) {
		clients.addAll(newClients);
		String message = null;
		for (ClientId c : newClients) {
			if (message == null) {
				message = "";
			}
			message += c.system + " at " + c.remoteHost + " is connected.\n";
		}
		transaction.setBroadcastMsg(message);
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

	public void setupResponsesCheck(BroadcastTransaction transaction) {
		for (ClientIdWithConnection c : clients) {
			readMsg(c.connection);
		}
		waitingForResponse.addAll(clients);
		transaction.getResponses().clear();
	}

	public synchronized boolean waitForBroadcastFinished() {
		for (ClientIdWithConnection c : clients) {
			if (c.connection.getConnectionState() == ConnectionStatus.BUSY) {
				return false;
			}
		}
		return true;
	}

	public synchronized boolean waitForResponsesFinished(
			BroadcastTransaction transaction) {
		boolean result = true;
		String message = transaction.getResponseMsg();
		List<Integer> lostClientsIdx = new ArrayList<Integer>();
		List<Integer> responseReceivedIdx = new ArrayList<Integer>();
		for (ClientIdWithConnection c : waitingForResponse) {
			log.debug("Checking Client: " + c.system);
			TriggerResponse rsp = checkResponse(c);
			if (rsp == null) {
				result = false;
				continue;
			}
			if (rsp.getError().getType().equals(TcpErrorTypes.NONE) == false) {
				int idx = clients.indexOf(c);
				if (message == null) {
					message = "";
				}
				message += "Lost contact with " + c.system + " at "
						+ c.remoteHost + " because " + rsp.getError().getText()
						+ "\n";
				transaction.setResponseMsg(message);
				lostClientsIdx.add(new Integer(idx));
				closeClient(c.connection);
			} else {
				transaction.getResponses().add(rsp);				
			}
			int ridx = waitingForResponse.indexOf(c);
			log.debug("Response received from " + c.system);
			responseReceivedIdx.add(new Integer(ridx));
		}

		for (Integer i : lostClientsIdx) {
			int idx = i.intValue();
			clients.remove(idx);
		}
		for (Integer i : responseReceivedIdx) {
			int idx = i.intValue();
			waitingForResponse.remove(idx);
		}
		return result;
	}
}
