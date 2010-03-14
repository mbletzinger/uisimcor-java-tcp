package org.nees.uiuc.simcor.factories;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.transaction.Address;
import org.nees.uiuc.simcor.transaction.SimCorCompoundMsg;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;
import org.nees.uiuc.simcor.transaction.SimCorMsg.MsgType;
import org.nees.uiuc.simcor.transaction.SimpleTransaction.DirectionType;
import org.nees.uiuc.simcor.transaction.TransactionIdentity.StepTypes;

public class TransactionFactory {
	private DirectionType direction;

	private TransactionIdentity id  = new TransactionIdentity();

	private final Logger log = Logger.getLogger(TransactionFactory.class);
	
	private String mdl = "MDL-00-01";
	private String systemDescription;
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
	public SimCorMsg createSessionCommand(boolean isOpen) {
		return createCommand((isOpen ? "open-session" : "close-session"),mdl,null,systemDescription);
	}
	public SimCorMsg createSessionResponse(SimCorMsg cmd) {
		return createResponse(mdl,null, (systemDescription + " " + cmd.getCommand() + " done"), false);
	}
	public SimpleTransaction createTransaction(SimCorMsg msg) {
		SimpleTransaction result = new SimpleTransaction();
		if (direction == DirectionType.SEND_COMMAND) {
			result.setCommand(msg);
			result.setId(id);
		}
		result.setDirection(direction);
		result.setTimeout(transactionTimeout);
		log.debug("Created transaction: " + result);
		return result;
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
	public DirectionType getDirection() {
		return direction;
	}
	public TransactionIdentity getId() {
		return id;
	}
	
	public String getMdl() {
		return mdl;
	}

	public String getSystemDescription() {
		return systemDescription;
	}
	public void setDirection(DirectionType direction) {
		this.direction = direction;
	}

	public void setId(TransactionIdentity id) {
		this.id = id;
	}
	public void setMdl(String mdl) {
		this.mdl = mdl;
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
	public void setSystemDescription(String systemDescription) {
		this.systemDescription = systemDescription;
	}
	
}
