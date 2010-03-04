package org.nees.uiuc.simcor.test;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.factories.ConnectionFactory;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.tcp.Connection.ConnectionStatus;
import org.nees.uiuc.simcor.tcp.TcpActionsDto.ActionsType;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.test.simcor.tcp.TcpListenerDto;
import org.nees.uiuc.simcor.transaction.Msg2Tcp;
import org.nees.uiuc.simcor.transaction.SimCorMsg;

public class MultiMessageSender {

	private TcpParameters params = new TcpParameters();
	private final Logger log = Logger.getLogger(MultiMessageSender.class);
	private ConnectionFactory cf;
	private Connection connection;
	private int count = 0;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MultiMessageSender mms = new MultiMessageSender();
		try {
			mms.openRemoteConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mms.sendMessage("start");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < 50; i++) {
			mms.sendMessage("send");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		mms.sendMessage("end");
		mms.closeRemoteConnection();

	}
	public TcpErrorTypes openRemoteConnection() throws Exception {
		params.setRemoteHost("cee-neessmom2.cee.illinois.edu");
		params.setRemotePort(6342);
		params.setTcpTimeout(20000);
		cf = new ConnectionFactory();
		cf.setParams(params);
		cf.startListener();
		connection = cf.getConnection();
		while(connection == null) {
			Thread.sleep(100);
			connection = cf.getConnection();
		}
		TcpListenerDto rsp  = cf.getListener().getDto();
		log.info("Open result [" + rsp + "]");
		return execute(ActionsType.CONNECT,null);
	}
	public TcpErrorTypes closeRemoteConnection() {
		cf.closingConnection(connection);
		TcpActionsDto rsp  = connection.getFromRemoteMsg();
		log.info("Close result [" + rsp + "]");
		return rsp.getError().getType();		
	}
	public TcpErrorTypes sendMessage(String c) {
		SimCorMsg msg = new SimCorMsg();
		msg.setCommand(c);
		msg.setContent("ABC" + count++);
		return execute(ActionsType.WRITE, msg);
	}
	private TcpErrorTypes execute(ActionsType a, SimCorMsg m) {
		TcpActionsDto cmd = new TcpActionsDto();
		cmd.setAction(a);
		if (m != null) {
			Msg2Tcp m2t = new Msg2Tcp();
			m2t.setMsg(m);
			cmd.setMsg(m2t);
		}
		connection.setToRemoteMsg(cmd);
		while(connection.getConnectionState().equals(ConnectionStatus.BUSY)) {
			try {
				log.debug("Waiting to complete action " + a);
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		TcpActionsDto rsp  = connection.getFromRemoteMsg();
		log.info(a + " result [" + rsp + "]");
		return rsp.getError().getType();
	}

}
