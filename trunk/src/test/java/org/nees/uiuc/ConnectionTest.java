package org.nees.uiuc.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.nees.uiuc.simcor.ConnectionPeer;
import org.nees.uiuc.simcor.UiSimCorTcp;
import org.nees.uiuc.simcor.factories.ConnectionFactory;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.ConnectionManager;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.TcpListenerDto;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.tcp.Connection.ConnectionStatus;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.DirectionType;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class ConnectionTest {

	private TcpParameters params = new TcpParameters();
	private final Logger log = Logger.getLogger(ConnectionTest.class);
	private ConnectionManager cm;
	private ConnectionFactory cf;
	private Connection connection;
	private List<TransactionStateNames> readyStates = new ArrayList<TransactionStateNames>();

	@Before
	public void setUp() throws Exception {
		readyStates.add(TransactionStateNames.TRANSACTION_DONE);
		readyStates.add(TransactionStateNames.ERRORS_EXIST);
		readyStates.add(TransactionStateNames.READY);
	}

	@Test
	public void testSenderConnection() throws Exception {
		log.info("========= Running testSenderConnection =========");
		params.setRemoteHost("127.0.0.1");
		params.setRemotePort(6444);
		params.setTcpTimeout(20000);
		cm = new ConnectionManager();
		cm.setParams(params);
		cm.openConnection();
		connection = cm.getConnection();
		while (connection.getConnectionState().equals(ConnectionStatus.BUSY)) {
			Thread.sleep(100);
		}
		TcpActionsDto rsp = connection.getFromRemoteMsg();
		log.info("Open result [" + rsp + "]");
		TcpErrorTypes err = rsp.getError().getType();
		assertEquals(TcpErrorTypes.IO_ERROR, err);
		assertEquals(ConnectionStatus.IN_ERROR, connection.getConnectionState());
	}

	@Test
	public void testListener() throws Exception {
		log.info("========= Running testListener =========");
		params.setLocalPort(6444);
		params.setTcpTimeout(20000);
		cf = new ConnectionFactory();
		cf.setParams(params);
		cf.startListener();
		int count = 0;
		connection = cf.checkForListenerConnection();
		TcpListenerDto dto = cf.getListener().getDto();
		log.info("Open result [" + dto + "]");

		while (connection == null
				&& dto.getError().getType().equals(TcpErrorTypes.NONE)
				&& count < 4) {
			log.info("Waiting for a connection");
			count++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.info("My sleep was interrupted.");
			}
			connection = cf.checkForListenerConnection();
			dto = cf.getListener().getDto();
			log.info("Open result [" + dto + "]");
		}

		if (count < 4) {
			log.info("Connection established");
		}

		while (cf.stopListener() == false) {
			Thread.sleep(1000);
		}
		TcpListenerDto rsp = cf.getListener().getDto();
		log.info("Close result [" + rsp + "]");
		assertEquals(TcpErrorTypes.NONE, rsp.getError().getType());
	}

	@Test
	public void testUiSimCorTcpSenderConnection() throws Exception {
		log.info("========= Running testUiSimCorTcpSenderConnection =========");
		params.setRemoteHost("127.0.0.1");
		params.setRemotePort(6454);
		params.setTcpTimeout(20000);
		UiSimCorTcp simcor = new ConnectionPeer(DirectionType.RECEIVE_COMMAND);
		simcor.startup(params);
		int cnt = 0;

		TransactionStateNames state = simcor.isReady();
		while (readyStates.contains(state) == false) {
			Thread.sleep(100);
				log.debug("Current state " + simcor.getTransaction());
			cnt++;
			state = simcor.isReady();
		}
		Transaction t = simcor.getTransaction();
		log.debug("Current transaction " + t);
		TcpErrorTypes err = t.getError().getType();
		assertEquals(TransactionStateNames.ERRORS_EXIST, t.getState());
		assertEquals(TcpErrorTypes.IO_ERROR, err);
		state = simcor.isReady();
		t = simcor.getTransaction();
		log.debug("Current transaction " + t);
		err = t.getError().getType();
		assertEquals(TcpErrorTypes.NONE, err);
		assertEquals(TransactionStateNames.READY, t.getState());
		log.debug("Shutting down");
		simcor.shutdown();
		cnt = 0;
		state = simcor.isReady();
		while (readyStates.contains(state) == false) {
			Thread.sleep(100);
			t = simcor.getTransaction();
			cnt++;
			state = simcor.isReady();
		}
		t = simcor.getTransaction();
		log.debug("Current transaction " + t);
		err = t.getError().getType();
		assertEquals(TcpErrorTypes.NONE, err);
		assertEquals(TransactionStateNames.TRANSACTION_DONE, t.getState());
	}

	@Test
	public void testUiSimCorTcpListenerConnection() throws Exception {
		log.info("========= Running testUiSimCorTcpListenerConnection =========");
		params.setLocalPort(6454);
		params.setTcpTimeout(20000);
		UiSimCorTcp simcor = new ConnectionPeer(DirectionType.RECEIVE_COMMAND);
		simcor.startup(params);
		int count = 0;
		TransactionStateNames state = simcor.isReady();
		while (readyStates.contains(state) == false && count < 4) {
			Thread.sleep(100);
			Transaction t = simcor.getTransaction();
			log.debug("Current transaction " + t + "count=" + count);
			count++;
			state = simcor.isReady();
		}
		if (count < 4) {
			log.info("Connection established");
		}


		assertEquals(TransactionStateNames.OPENING_CONNECTION, simcor
				.getTransaction().getState());

		simcor.shutdown();
		state = simcor.isReady();
		while (readyStates.contains(state) == false) {
			 Transaction t = simcor.getTransaction();
			log.debug("Current transaction " + t);
			Thread.sleep(1000);
			state = simcor.isReady();
		}

		 Transaction t = simcor.getTransaction();
		log.debug("Current transaction " + t);
		TcpErrorTypes err = t.getError().getType();
		assertEquals(TcpErrorTypes.NONE, err);
		assertEquals(TransactionStateNames.TRANSACTION_DONE, t.getState());
	}
}
