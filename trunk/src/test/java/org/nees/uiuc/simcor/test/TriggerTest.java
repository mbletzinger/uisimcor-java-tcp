package org.nees.uiuc.simcor.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nees.uiuc.simcor.ConnectionPeer;
import org.nees.uiuc.simcor.factories.ConnectionFactory;
import org.nees.uiuc.simcor.factories.TransactionFactory;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.tcp.Connection.ConnectionStatus;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.test.simcor.TriggerBroadcaster;
import org.nees.uiuc.simcor.test.simcor.tcp.TcpListenerDto;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;
import org.nees.uiuc.simcor.transaction.Transaction.DirectionType;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;
public class TriggerTest {
	private final Logger log = Logger.getLogger(TriggerTest.class);
	private TcpParameters params = new TcpParameters();
	private Responder responder;
	private TriggerBroadcaster broadcaster;
	TransactionMsgs data = new TransactionMsgs();

	@Before
	public void setUp() throws Exception {
		responder = new Responder();
		data.setUp();
		responder.setData(data);
	}
 
	@After
	public void shutdown() {
		responder.connected = false;
		responder.getSimcor().getConnectionManager().closeConnection();
		broadcaster.getConnectionFactory().closeConnection();
	}

	@Test
	public void testSendTriggers() throws Exception {
		params.setLocalPort(6342);
		params.setTcpTimeout(2000);
		broadcaster = new TriggerBroadcaster();
		Connection connection = null;
		ConnectionFactory cf = broadcaster.getConnectionFactory();
		try {
			connection = cf.getConnection();
		} catch (Exception e1) {
			log.error("Get connection failed because", e1);
		}
		TcpListenerDto dto = cf.getListener().getDto();
		log.info("Open result [" + dto + "]");

		TransactionFactory tf = broadcaster.getTransactionFactory();
		checkResponder();
		for(Iterator<Transaction> t = data.cmdList.iterator(); t.hasNext();) {
			Transaction trans = t.next();
			log.debug("Original command " + trans);
			TransactionIdentity id = trans.getId();
			tf.setId(id);
			trans = tf.createTransaction(trans.getCommand());
			if (id != null) {
				trans.getId().setTransId(id.toString());
			}
			log.debug("Sending command " + trans);
//			broadcaster.startTransaction(trans);
//			TransactionStateNames state = broadcaster.isReady();
//			while(state != TransactionStateNames.RESPONSE_AVAILABLE  && state != TransactionStateNames.ERRORS_EXIST) {
//				try {
//					checkResponder();
//					Thread.sleep(200);
//					state = broadcaster.isReady();
//					log.debug("Send command state is " + state);
//				} catch (InterruptedException e) {
//					log.info("My sleep was interrupted.");
//				}
//			}
//			Transaction transaction = broadcaster.pickupTransaction();
//			log.debug("Received response " + broadcaster.getTransaction());
//			if(transaction.getError().getType() != TcpErrorTypes.NONE) {
//				log.error("Transaction error " + transaction.getError());
//			}
//			assertEquals(TcpErrorTypes.NONE, transaction.getError().getType());
//			while(state != TransactionStateNames.TRANSACTION_DONE && state != TransactionStateNames.ERRORS_EXIST) {
//				try {
//					checkResponder();
//					log.debug("Getting response state is " + state);
//					Thread.sleep(200);
//					state = broadcaster.isReady();
//				} catch (InterruptedException e) {
//					log.info("My sleep was interrupted.");
//				}
//			}
//			transaction = broadcaster.getTransaction();
//			if(transaction.getError().getType() != TcpErrorTypes.NONE) {
//				log.error("Transaction error " + transaction.getError());
//			}
//			assertEquals(TcpErrorTypes.NONE, transaction.getError().getType());
		}
		while (cf.closeConnection() == false) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.info("Sleep interrupted");
			}
		}
	}
	private void checkResponder() {
		if(responder.isAlive() == false) {
			fail();
		}
	}
	private void sendTriggers() {
		
	}
	private void startTriggerListener() {
		
	}
	private void stopTriggerListener(TriggerListener l) {
		
	}
	private void cleanExit() {
		
	}
}
