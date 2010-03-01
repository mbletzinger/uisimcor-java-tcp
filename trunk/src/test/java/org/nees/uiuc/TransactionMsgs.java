package org.nees.uiuc.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.nees.uiuc.simcor.transaction.Address;
import org.nees.uiuc.simcor.transaction.SimCorCompoundMsg;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;
import org.nees.uiuc.simcor.transaction.SimCorMsg.MsgType;

public class TransactionMsgs {
	public List<Transaction> cmdList;
	public HashMap<String, Transaction> transactions;
	public Transaction openTransaction;
	public Transaction closeTransaction;
	public Transaction triggerTransaction;

	public TransactionMsgs() {
		this.cmdList = new ArrayList<Transaction>();
		this.transactions = new HashMap<String,Transaction>();

	}

	public void setUp() throws Exception {

		Transaction transaction = new Transaction();
		SimCorMsg msg = new SimCorMsg();
		msg.setCommand("open-session");
		msg.setContent("dummySession");
		SimCorMsg resp = new SimCorMsg();
		resp.setContent("Open Session Suceeded");
		resp.setType(MsgType.OK_RESPONSE);
		transaction.setCommand(msg);
		transaction.setResponse(resp);
		transactions.put(msg.toString(),transaction);
		cmdList.add(transaction);
		openTransaction = transaction;
	
		transaction = new Transaction();
		msg = new SimCorMsg();
		msg.setCommand("set-parameter");
		msg.setContent("dummySetParam	nstep	0");
	
		resp = new SimCorMsg();
		resp.setContent("Command ignored. Carry on.");
		resp.setType(MsgType.OK_RESPONSE);
		transaction.setCommand(msg);
		transaction.setResponse(resp);
		transactions.put(msg.toString(),transaction);
		cmdList.add(transaction);
	
		transaction = new Transaction();
		msg = new SimCorMsg();
		TransactionIdentity id = new TransactionIdentity();
		msg.setCommand("get-control-point");
		id.setTransId("dummy");
		msg.setAddress(new Address("MDL-00-01:LBCB2"));
	
		resp = new SimCorMsg();
		resp.setAddress(new Address("MDL-00-01:LBCB2"));
		resp.setContent("x	displacement	5.036049E-1" +
				"	y	displacement	1.557691E-4" +
				"	z	displacement	-7.850649E-4" + 
				"	x	rotation	3.829964E-5" +
				"	y	rotation	-2.747683E-5" +
				"	z	rotation	1.195688E-3" +
				"	x	force	6.296413E+0" +
				"	y	force	1.451685E-1" +
				"	z	force	-1.252275E+0" +
				"	x	moment	7.296673E-1" +
				"	y	moment	-2.214440E+0" +
				"	z	moment	3.522242E-2");
		resp.setType(MsgType.OK_RESPONSE);
		transaction.setId(id);
		transaction.setCommand(msg);
		transaction.setResponse(resp);
		transactions.put(msg.toString(),transaction);
		cmdList.add(transaction);
	
		transaction = new Transaction();
		id = new TransactionIdentity();
		msg = new SimCorMsg();
		msg.setCommand("get-control-point");
		id.setTransId("dummy");
		msg.setAddress(new Address("MDL-00-01:ExternalSensors"));
	
		resp = new SimCorMsg();
		resp.setAddress(new Address("MDL-00-01:ExternalSensors"));
		resp.setContent("Ext.Long.LBCB2	external	2.422813E-1	" +
				"Ext.Tranv.TopLBCB2	external	2.422813E-1	" + 
				"Ext.Tranv.Bot.LBCB2	external	2.422813E-1	" + 
				"Ext.Long.LBCB1	external	2.422813E-1	" + 
				"Ext.Tranv.LeftLBCB1	external	2.422813E-1	" + 
				"Ext.Tranv.RightLBCB1	external	2.422813E-1");
		resp.setType(MsgType.OK_RESPONSE);
		transaction.setId(id);
		transaction.setCommand(msg);
		transaction.setResponse(resp);
		transactions.put(msg.toString(),transaction);
		cmdList.add(transaction);
	
		transaction = new Transaction();
		id = new TransactionIdentity();
		msg = new SimCorMsg();
		msg.setCommand("get-control-point");
		id.setTransId("dummy");
		msg.setAddress(new Address("MDL-00-01:ExternalSensors"));
	
		resp = new SimCorMsg();
		resp.setAddress(new Address("MDL-00-01:ExternalSensors"));
		resp.setContent("	1_LBCB1_x	external	-7.565553E-1"
				+ "	2_LBCB1_z_right	external	0.000000E+0"
				+ "	3_LBCB1_z_left	external	-8.081407E-1"
				+ "	4_LBCB2_z_right	external	0.000000E+0"
				+ "	5_LBCB2_z_left	external	7.518438E-2"
				+ "	6_LBCB2_x	external	0.00]000E+0");
		resp.setType(MsgType.OK_RESPONSE);
		transaction.setId(id);
		transaction.setCommand(msg);
		transaction.setResponse(resp);
		transactions.put(msg.toString(),transaction);
		cmdList.add(transaction);

		transaction = new Transaction();
		id = new TransactionIdentity();
		SimCorCompoundMsg cmsg = new SimCorCompoundMsg();
		cmsg.setCommand("propose");
		id.createTransId();
		cmsg.setContent(new Address("MDL-00-01:LBCB1"),"x	displacement	0.5	y	displacement	0.0");
		cmsg.setContent(new Address("MDL-00-01:LBCB2"),"z	displacement	0.5	y	rotation	0.002");
		id.setStep(100);
		id.setSubStep(23);
	
		resp = new SimCorMsg();
		resp.setContent("propose accepted");
		resp.setType(MsgType.OK_RESPONSE);
		transaction.setId(id);
		transaction.setCommand(cmsg);
		transaction.setResponse(resp);
		transactions.put(cmsg.toString(),transaction);
		cmdList.add(transaction);
	
		transaction = new Transaction();
		id = new TransactionIdentity();
		msg = new SimCorMsg();
		msg.setCommand("execute");
		id.createTransId();
		id.setTransId("trans200912317925.320");
		id.setStep(100);
		id.setSubStep(23);
	
		resp = new SimCorMsg();
		resp.setContent("execute done");
		resp.setType(MsgType.OK_RESPONSE);
		transaction.setId(id);
		transaction.setCommand(msg);
		transaction.setResponse(resp);
		transactions.put(msg.toString(),transaction);
		cmdList.add(transaction);
	
		transaction = new Transaction();
		id = new TransactionIdentity();
		msg = new SimCorMsg();
		msg.setCommand("trigger");
		id.createTransId();
		id.setTransId("trans200912317925.320");
		id.setStep(100);
		id.setSubStep(23);
	
		resp = new SimCorMsg();
		resp.setContent("trigger received");
		resp.setType(MsgType.OK_RESPONSE);
		transaction.setId(id);
		transaction.setCommand(msg);
		transaction.setResponse(resp);
		transactions.put(msg.toString(),transaction);
		cmdList.add(transaction);
		triggerTransaction = transaction;
	
		transaction = new Transaction();
		cmsg = new SimCorCompoundMsg();
		cmsg.setCommand("propose");
		id = new TransactionIdentity();
		id.setTransId("trans20080206155057.44");
		cmsg.setContent(new Address("MDL-00-01"),"x	displacement	1.0000000000e-003	y	displacement	2.0000000000e-003	z	rotation	3.0000000000e-003");
		cmsg.setContent(new Address("MDL-00-02"),"x	displacement	4.0000000000e-003	y	displacement	5.0000000000e-003	z	rotation	6.0000000000e-003");
		cmsg.setContent(new Address("MDL-00-03"),"x	displacement	7.0000000000e-003	y	displacement	8.0000000000e-003	z	rotation	9.0000000000e-003");
	
		SimCorCompoundMsg cresp = new SimCorCompoundMsg();
		cresp.setContent(new Address("MDL-00-01"),"x	displacement	1.0000000000e-003	y	displacement	2.0000000000e-003	z	rotation	3.0000000000e-003");
		cresp.setContent(new Address("MDL-00-02"),"x	displacement	4.0000000000e-003	y	displacement	5.0000000000e-003	z	rotation	6.0000000000e-003");
		cresp.setContent(new Address("MDL-00-03"),"x	displacement	7.0000000000e-003	y	displacement	8.0000000000e-003	z	rotation	9.0000000000e-003");
		cresp.setType(MsgType.OK_RESPONSE);
		transaction.setId(id);
		transaction.setCommand(cmsg);
		transaction.setResponse(cresp);
		transactions.put(cmsg.toString(),transaction);
		cmdList.add(transaction);
	
		transaction = new Transaction();
		msg = new SimCorMsg();
		msg.setCommand("close-session");
		msg.setContent("dummy");
	
		resp = new SimCorMsg();
		resp.setContent("Close accepted");
		resp.setType(MsgType.OK_RESPONSE);
		transaction.setId(null);
		transaction.setCommand(msg);
		transaction.setResponse(resp);
		transactions.put(msg.toString(),transaction);
		cmdList.add(transaction);
		closeTransaction = transaction;
	}
}