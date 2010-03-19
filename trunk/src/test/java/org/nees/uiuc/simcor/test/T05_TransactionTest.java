package org.nees.uiuc.simcor.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nees.uiuc.simcor.UiSimCorTcp;
import org.nees.uiuc.simcor.UiSimCorTcp.ConnectType;
import org.nees.uiuc.simcor.factories.TransactionFactory;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.test.util.TransactionMsgs;
import org.nees.uiuc.simcor.test.util.TransactionResponder;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;
import org.nees.uiuc.simcor.transaction.SimCorMsg.MsgType;
import org.nees.uiuc.simcor.transaction.Transaction.DirectionType;

public class T05_TransactionTest {
	TransactionMsgs data = new TransactionMsgs();
	private final Logger log = Logger.getLogger(T05_TransactionTest.class);
	private TcpParameters params = new TcpParameters();
	private List<TransactionStateNames> readyStates = new ArrayList<TransactionStateNames>();
	private TransactionResponder responder;
	private UiSimCorTcp simcor;

	private void checkResponder() {
		if (responder.isAlive() == false) {
			fail();
		}
	}

	@Before
	public void setUp() throws Exception {
		responder = new TransactionResponder();
		data.setUp();
		responder.setData(data);
	}

	@After
	public void teardown()  throws Exception {
		responder.connected = false;
		responder.getSimcor().shutdown();
		simcor.shutdown();
		TransactionStateNames state = simcor.isReady();
		while (state.equals(TransactionStateNames.READY) == false) {
			Thread.sleep(1000);
			state = simcor.isReady();
			log.debug("Shutdown state " + state);
			checkResponder();
		}
		while(responder.isAlive()) {
			Thread.sleep(1000);
			log.debug("Waiting for the responder to die");
		}
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
		simcor = new UiSimCorTcp(ConnectType.P2P_SEND_COMMAND, "MDL-00-00");
		simcor.setArchiveFilename(home + fs + "archive.txt");
		simcor.startup(params);
		TransactionStateNames state = simcor.isReady();
		while (state.equals(TransactionStateNames.READY) == false) {
			Thread.sleep(200);
			state = simcor.isReady();
			log.debug("Connection state " + state);
			checkResponder();
		}
		assertEquals(TcpErrorTypes.NONE, simcor.getErrors().getType());
		assertEquals(TransactionStateNames.READY, state);

		TransactionFactory tf = simcor.getSap().getTf();
		checkResponder();
		for (Iterator<SimpleTransaction> t = data.cmdList.iterator(); t
				.hasNext();) {
			Transaction transO = t.next();
			log.debug("Original command " + transO);
			TransactionIdentity id = transO.getId();
			log.debug("Sending command " + transO.getCommand());
			simcor.startTransaction(transO.getCommand(),id,2000);
			state = simcor.isReady();
			while (state != TransactionStateNames.RESPONSE_AVAILABLE) {
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
			SimpleTransaction transaction = simcor.pickupTransaction();
			log.debug("Received response " + transaction.getResponse());
			if (transaction.getError().getType() != TcpErrorTypes.NONE) {
				log.error("Transaction error " + transaction.getError());
			}
			assertNotNull(transaction.getResponse().getContent());
			assertEquals(transaction.getResponse().getType(),
					MsgType.OK_RESPONSE);
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
			transaction = (SimpleTransaction) simcor.getTransaction();
			if (transaction.getError().getType() != TcpErrorTypes.NONE) {
				log.error("Transaction error " + transaction.getError());
			}
			assertEquals(TcpErrorTypes.NONE, transaction.getError().getType());
			state = simcor.isReady();
			log.debug("Reseting state is " + state);
		}
		simcor.shutdown();
		state = simcor.isReady();
		while (readyStates.contains(state) == false) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.info("Sleep interrupted");
			}
			state = simcor.isReady();
			log.debug("Transaction completing state is " + state);
			checkResponder();
		}
		assertNull(simcor.getSap().getCm().getConnection());
	}
}
