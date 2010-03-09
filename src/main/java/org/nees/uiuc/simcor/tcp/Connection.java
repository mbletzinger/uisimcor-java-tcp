package org.nees.uiuc.simcor.tcp;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.TcpActionsDto.ActionsType;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;

public class Connection extends Thread {
	public enum ConnectionStatus {
		BUSY, CLOSED, IN_ERROR, READY
	};

	private TcpActions actions = new TcpActions();
	private ConnectionStatus connectionState = ConnectionStatus.CLOSED;
	private TcpActionsDto fromRemoteMsg = new TcpActionsDto();
	private final Logger log = Logger.getLogger(Connection.class);
	private String remoteHost;
	private boolean running = false;
	private int msgTimeout = 3000;

	public synchronized int getMsgTimeout() {
		return msgTimeout;
	}

	public synchronized void setMsgTimeout(int msgTimeout) {
		this.msgTimeout = msgTimeout;
	}

	private TcpActionsDto toRemoteMsg = new TcpActionsDto();

	public Connection() {
		super();

	}

	public Connection(TcpLinkDto link) {
		super();
		actions.setLink(link);
		remoteHost = link.getRemoteHost();
	}

	public synchronized ConnectionStatus getConnectionState() {
		return connectionState;
	}

	public synchronized TcpActionsDto getFromRemoteMsg() {
		log.debug("Returning outMsg[" + fromRemoteMsg + "]");
		return new TcpActionsDto(fromRemoteMsg);
	}

	public synchronized TcpLinkDto getLink() {
		return actions.getLink();
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public synchronized TcpActionsDto getToRemoteMsg() {
		if (getConnectionState() == ConnectionStatus.READY) {
			try {
				log.debug("I'm WAAAAIITING");
				wait();
			} catch (InterruptedException e1) {
			}
		}
		// log.debug("Returning outMsg[" + toRemoteMsg + "]");
		TcpActionsDto result = new TcpActionsDto(toRemoteMsg);
		toRemoteMsg.setAction(ActionsType.NONE);
		return result;
	}

	public boolean isRunning() {
		return running;
	}

	@Override
	public void run() {
		TcpActionsDto outM = getToRemoteMsg();
		// CONNECT, CLOSE, EXIT,READ,WRITE,IDLE
		log.debug("Connection is running");
		ActionsType act = outM.getAction();
		running = true;
		boolean noRemoteMsgNeeded = false;
		while (act != ActionsType.EXIT) {
			TcpActionsDto inM = new TcpActionsDto();
			log.debug("Executing action " + act.toString());
			if (act.equals(ActionsType.CONNECT)) {
				inM = actions.connect();
				inM.setAction(ActionsType.NONE);
				setFromRemoteMsg(inM, act);
				if (getConnectionState().equals(ConnectionStatus.IN_ERROR)) {
					log.debug("Attempting to EXIT");
					noRemoteMsgNeeded = true;
					break;
				}
			}
			if (act.equals(ActionsType.CLOSE)) {
				inM = actions.closeConnection();
				inM.setAction(ActionsType.NONE);
				setFromRemoteMsg(inM, act);
				log.debug("Attempting to EXIT");
				// noRemoteMsgNeeded = true;
				break;
			}
			if (act.equals(ActionsType.READ)) {
				inM = actions.readMessage(getMsgTimeout());
				inM.setAction(ActionsType.NONE);
				inM.timestamp();
				setFromRemoteMsg(inM, act);
			}

			if (act.equals(ActionsType.WRITE)) {
				inM = actions.sendMessage(outM);
				inM.setAction(ActionsType.NONE);
				inM.timestamp();
				setFromRemoteMsg(inM, act);
			}
			if (act.equals(ActionsType.NONE)) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			outM = getToRemoteMsg();
			act = outM.getAction();
		}
		if (noRemoteMsgNeeded == false) {
			setFromRemoteMsg(new TcpActionsDto(), ActionsType.EXIT);
			setConnectionState(ConnectionStatus.CLOSED);
		}
		running = false;
		log.info("Goodbye");
	}

	public synchronized void setConnectionState(ConnectionStatus busy) {
		log.debug("I am " + busy);
		this.connectionState = busy;
	}

	public synchronized void setFromRemoteMsg(TcpActionsDto inMsg,
			ActionsType act) {
		log.debug("Setting inMsg[" + inMsg + "]");
		this.fromRemoteMsg = inMsg;
		if (inMsg.getError().getType() != TcpErrorTypes.NONE) {
			fromRemoteMsg.getError().setRemoteHost(remoteHost);
			setConnectionState(ConnectionStatus.IN_ERROR);
		} else {
			if (act.equals(ActionsType.CLOSE)) {
				setConnectionState(ConnectionStatus.CLOSED);
			} else {
				setConnectionState(ConnectionStatus.READY);
			}
		}
		log.debug("Connection set to " + getConnectionState());
	}

	public void setLink(TcpLinkDto link) throws Exception {
		if (running) {
			Exception e = new Exception("Link is already connected");
			log.error(e);
			throw e;
		}
		actions.setLink(link);
	}

	public void setParams(TcpParameters params) throws Exception {
		if (running) {
			Exception e = new Exception("Link is already connected");
			log.error(e);
			throw e;
		}
		actions.setParameters(params);
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	public synchronized void setToRemoteMsg(TcpActionsDto outMsg) {
		log.debug("Setting outMsg[" + outMsg + "]");
		this.toRemoteMsg = new TcpActionsDto(outMsg);
		notifyAll();
		if (running) {
			setConnectionState(ConnectionStatus.BUSY);
			log.debug("Connection set to " + getConnectionState());
		} else {
			log.debug("Connection is not running");
			setFromRemoteMsg(outMsg, ActionsType.EXIT); // Clear out previous
														// fromMsg
		}
	}

}
