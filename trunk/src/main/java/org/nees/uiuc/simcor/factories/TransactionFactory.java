package org.nees.uiuc.simcor.transaction;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.transaction.SimCorMsg.MsgType;
import org.nees.uiuc.simcor.transaction.Transaction.DirectionType;
import org.nees.uiuc.simcor.transaction.TransactionIdentity.StepTypes;

public class TransactionFactory {
	private DirectionType direction;

	private TransactionIdentity id  = new TransactionIdentity();

	private final Logger log = Logger.getLogger(TransactionFactory.class);
	
	private int transactionTimeout = 3000;

	public SimCorMsg createCommand(String cmd, String mdl, String cps, String cnt) {
		SimCorMsg result;
		result = new SimCorMsg();
		Address a = new Address(mdl);
		if (cps != null) {
			a.setSuffix(cps);
		}
		result.setAddress(a);
		result.setCommand(cmd);
		result.setContent(cnt);
		result.setType(MsgType.COMMAND);
		log.debug("Created " + result);
		return result;
	}
	public SimCorCompoundMsg createCompoundCommand(String cmd, String[] mdl,
			String[] cps, String[] cnt) {
		SimCorCompoundMsg result = new SimCorCompoundMsg();
		result.setCommand(cmd);
		result.setType(MsgType.COMMAND);
		for (int i = 0; i < mdl.length; i++) {
			Address a = new Address(mdl[i]);
			if (cps != null && cps[i] != null) {
				a.setSuffix(cps[i]);
			}
			if (cnt != null && cnt[i] != null) {
				result.setContent(a, cnt[i]);
			}
		}
		log.debug("Created " + result);
		return result;
	}
	public SimCorMsg createResponse(String mdl, String cps, String cnt, boolean notOk) {
		SimCorMsg result = new SimCorMsg();
		Address a = new Address(mdl);
		if (cps != null) {
			a.setSuffix(cps);
		}
		result.setAddress(a);
		result.setContent(cnt);
		result.setType(notOk ? MsgType.NOT_OK_RESPONSE : MsgType.OK_RESPONSE);
		log.debug("Created " + result);
		return result;
	}
	public SimCorCompoundMsg createCompoundResponse(String[] mdl, String[] cps,
			String[] cnt, boolean notOk) {
		SimCorCompoundMsg result = new SimCorCompoundMsg();
		result.setType(notOk ? MsgType.NOT_OK_RESPONSE : MsgType.OK_RESPONSE);
		for (int i = 0; i < mdl.length; i++) {
			Address a = new Address(mdl[i]);
			if (cps != null && cps[i] != null) {
				a.setSuffix(cps[i]);
			}
			if (cnt != null && cnt[i] != null) {
			result.setContent(a, cnt[i]);
			}
		}
		log.debug("Created " + result);
		return result;
	}
	
	public Transaction createTransaction(SimCorMsg msg) {
		Transaction result = new Transaction();
		if (direction == DirectionType.SEND_COMMAND) {
			result.setCommand(msg);
			result.setId(id);
		}
		result.setDirection(direction);
		result.setTimeout(transactionTimeout);
		log.debug("Created transaction: " + result);
		return result;
	}

	public synchronized int getTransactionTimeout() {
		return transactionTimeout;
	}
	public synchronized void setTransactionTimeout(int transactionTimeout) {
		this.transactionTimeout = transactionTimeout;
	}
	public void setId(TransactionIdentity id) {
		this.id = id;
	}
	public DirectionType getDirection() {
		return direction;
	}

	public TransactionIdentity getId() {
		return id;
	}
	public void setDirection(DirectionType direction) {
		this.direction = direction;
	}

	public void setStep(int s,StepTypes type) {
		if(type.equals(StepTypes.SUBSTEP)) {
			id.setSubStep(s);
		}else if(type.equals(StepTypes.CORRECTIONSTEP)){
			id.setCorrectionStep(s);				
			} else {
			id.setStep(s);
		}
	}
	public TransactionIdentity createTransactionId(int step, int subStep, int correctionStep) {
		TransactionIdentity result = new TransactionIdentity();
		if(step > 0) {
			result.setStep(step);
		}
		if(subStep > 0) {
			result.setSubStep(subStep);
		}
		if(correctionStep > 0) {
			result.setCorrectionStep(correctionStep);
		}
		result.createTransId();
		return result;
	}
	
}