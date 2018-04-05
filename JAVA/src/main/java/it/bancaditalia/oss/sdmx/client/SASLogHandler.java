package it.bancaditalia.oss.sdmx.client;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class SASLogHandler extends Handler
{
	private final SimpleFormatter formatter = new SimpleFormatter();
	private static final Queue<String> logRecords = new ConcurrentLinkedQueue<>(); 

	@Override
	public void close() throws SecurityException
	{
		// No-op
	}

	@Override
	public void flush()
	{
		// No-op
	}

	@Override
	public void publish(LogRecord record)
	{
		if (record.getLevel() == Level.WARNING)
			logRecords.add("WARNING: " + formatter.formatMessage(record));
		else if (record.getLevel() == Level.SEVERE)
			logRecords.add("ERROR: " + formatter.formatMessage(record));
		else if (record.getLevel() == Level.INFO)
			logRecords.add("NOTE: " + formatter.formatMessage(record));
		else
			logRecords.add(formatter.formatMessage(record));
	}

	public String getLogItem()
	{
		String logMessage = logRecords.poll();
		return logMessage == null ? "" : logMessage;
	}
}