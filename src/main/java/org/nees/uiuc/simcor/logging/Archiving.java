package org.nees.uiuc.simcor.logging;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.SimpleTransaction.DirectionType;

public class Archiving extends Thread {
	private boolean archivingEnabled = false;
	private BlockingQueue<SimpleTransaction> buffer = new ArrayBlockingQueue<SimpleTransaction>(100);
	private boolean exit = false;
	private String filename;
	private Logger log = Logger.getLogger(Archiving.class);

	public synchronized String getFilename() {
		return filename;
	}

	public synchronized boolean isArchivingEnabled() {
		return archivingEnabled;
	}

	public synchronized boolean isExit() {
		return exit;
	}
	
	public synchronized void logTransaction(SimpleTransaction t) {
		try {
			buffer.put(t);
		} catch (InterruptedException e) {
			log.debug("I was interrupted");
		}
	}

	@Override
	public void run() {
		List<SimpleTransaction> records = new ArrayList<SimpleTransaction>();
		if(archivingEnabled == false) {
			log.info("Archiving has not been enabled");
			return;
		}
		while (isExit() == false) {
			records.clear();
			buffer.drainTo(records);
			PrintWriter wf = null;
			try {
				FileWriter ff = new FileWriter(filename, true);
				wf = new PrintWriter(ff);
			} catch (FileNotFoundException e) {
				log.error("Should not have happened for [" + filename + "]");
				continue;
			} catch (IOException e) {
				log.error("Cannot write to [" + filename + "]");
				setExit(true);
				continue;
			}

			TransactionLogRecord tlr = new TransactionLogRecord();
			for (Iterator<SimpleTransaction> r = records.iterator(); r.hasNext();) {
				SimpleTransaction t = r.next();
				if (t instanceof ExitTransaction) {
					setExit(true);
					continue;
				}
				if(t.getDirection().equals(DirectionType.NONE)) {
					log.error("Transaction [" + t + "] is empty");
					continue;
				}
				wf.print(tlr.toString(t));
			}
			wf.close();
		}
		log.info("Archiving is ending");
	}

	public synchronized void setArchivingEnabled(boolean archivingEnabled) {
		this.archivingEnabled = archivingEnabled;
	}

	public synchronized void setExit(boolean exit) {
		this.exit = exit;
	}

	public synchronized void setFilename(String filename) {
		this.filename = filename;
	}


}
