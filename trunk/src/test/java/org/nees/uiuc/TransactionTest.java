package org.nees.uiuc.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nees.uiuc.simcor.ConnectionPeer;
import org.nees.uiuc.simcor.UiSimCorTcp;
import org.nees.uiuc.simcor.factories.TransactionFactory;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;
import org.nees.uiuc.simcor.transaction.SimCorMsg.MsgType;
import org.nees.uiuc.simcor.transaction.Transaction.DirectionType;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;
public class TransactionTest {
	private final Logger log = Logger.getLogger(TransactionTest.class);
	private TcpParameters params = new TcpParameters();
	private Responder responder;
	private UiSimCorTcp simcor;
	private List<TransactionStateNames> readyStates = new ArrayList<TransactionStateNames>();
	TransactionMsgs data = new TransactionMsgs();

	@Before
	public void setUp() throws Exception {
		responder = new Responder();
		data.setUp();
		responder.setData(data);
		readyStates.add(TransactionStateNames.TRANSACTION_DONE);
		readyStates.add(TransactionStateNames.ERRORS_EXIST);
		readyStates.add(TransactionStateNames.READY);
	}
 
	@After
	public void shutdown() {
		responder.connected = false;
		responder.getSimcor().getConnectionManager().closeConnection();
		simcor.getConnectionManager().closeConnection();
	}

	@Test
	public void testSendTransactions() throws Exception {
		responder.start();
		checkResponder();
		// wait for the responder to start
		Thread.sleep(2000);
		params.setRemoteHost("127.0.0.1");
		params.setRemotePort(6445);
		params.setTcpTimeout(20000);
		String home = System.getProperty("user.dir");
		String fs = System.getProperty("file.separator");
		simcor = new ConnectionPeer(DirectionType.SEND_COMMAND);
		simcor.setArchiveFilename(home + fs + "archive.txt");
		simcor.startup(params);
		while(simcor.isReady().equals(TransactionStateNames.OPENING_CONNECTION)) {
			Thread.sleep(200);
		}
		TcpActionsDto dto = simcor.getConnectionManager().getConnection().getFromRemoteMsg();
		log.info("Open result [" + dto + "]");
		assertEquals(TcpErrorTypes.NONE, dto.getError().getType());
		assertEquals(TransactionStateNames.READY,simcor.isReady());
		
		TransactionFactory tf = simcor.getTransactionFactory();
		checkResponder();
		for(Iterator<Transaction> t = data.cmdList.iterator(); t.hasNext();) {
			Transaction transO = t.next();
			log.debug("Original command " + transO);
			TransactionIdentity id = transO.getId();
			tf.setId(id);
			Transaction trans = tf.createTransaction(transO.getCommand());
			if (id != null) {
				trans.getId().setTransId(id.getTransId());
			}
			log.debug("Sending command " + trans.getCommand());
			simcor.startTransaction(trans);
			TransactionStateNames state = simcor.isReady();
			while(state != TransactionStateNames.RESPONSE_AVAILABLE  && state != TransactionStateNames.ERRORS_EXIST) {
				try {
					checkResponder();
					Thread.sleep(200);
					state = simcor.isReady();
					log.debug("Send command state is " + state);
				} catch (InterruptedException e) {
					log.info("My sleep was interrupted.");
				}
			}
			log.debug("Pick up response state " + state);
			Transaction transaction = simcor.pickupTransaction();
			log.debug("Received response " + transaction.getResponse());
			if(transaction.getError().getType() != TcpErrorTypes.NONE) {
				log.error("Transaction error " + transaction.getError());
			}
			assertNotNull(transaction.getResponse().getContent());
			assertEquals(transaction.getResponse().getType(), MsgType.OK_RESPONSE);
			assertEquals(TcpErrorTypes.NONE, transaction.getError().getType());
			state = simcor.isReady();
			while (readyStates.contains(state) == false) {
				try {
					checkResponder();
					log.debug("Getting response state is " + state);
					Thread.sleep(200);
					state = simcor.isReady();
					log.debug("Transaction completing state is " + state);					
				} catch (InterruptedException e) {
					log.info("My sleep was interrupted.");
				}
			}
			transaction = simcor.getTransaction();
			if(transaction.getError().getType() != TcpErrorTypes.NONE) {
				log.error("Transaction error " + transaction.getError());
			}
			assertEquals(TcpErrorTypes.NONE, transaction.getError().getType());
			state = simcor.isReady();
			log.debug("Reseting state is " + state);					
		}
		simcor.shutdown();
		TransactionStateNames state = simcor.isReady();
		while (readyStates.contains(state) == false) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.info("Sleep interrupted");
			}
			state = simcor.isReady();
			log.debug("Transaction completing state is " + state);					
		}
		assertNull(simcor.getConnectionManager().getConnection());
	}
	private void checkResponder() {
		if(responder.isAlive() == false) {
			fail();
		}
	}
}
