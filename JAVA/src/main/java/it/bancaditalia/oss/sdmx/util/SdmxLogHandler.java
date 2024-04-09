package it.bancaditalia.oss.sdmx.util;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class SdmxLogHandler extends Handler
{
	public static final String LOG_FORMAT = "%1$tFT%1$tT.%1$tL %2$-10s [%3$s] %4$s%5$s%n"; 

	private final Handler err = new ConsoleHandler();
	private final Handler out = new ConsoleHandler() { { setOutputStream(System.out); } };

	private static class DefaultFormatter extends Formatter
	{
		@Override
		public String format(LogRecord record)
		{
			String source;
			if (record.getSourceClassName() != null)
			{
				source = record.getSourceClassName();
				if (record.getSourceMethodName() != null)
					source += " " + record.getSourceMethodName();
			}
			else
				source = record.getLoggerName();

			String message = formatMessage(record);
			String throwable = "";
			if (record.getThrown() != null)
			{
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				pw.println();
				record.getThrown().printStackTrace(pw);
				pw.close();
				throwable = sw.toString();
			}
			
			return String.format(LOG_FORMAT, new Date(record.getMillis()), record.getLevel().getName(), source, message, throwable);
		}
	}
	
	public SdmxLogHandler()
	{
		super();
		setFormatter(new DefaultFormatter());
	}
	
	@Override
	public void close() throws SecurityException
	{
		err.close();
		out.close();
	}

	@Override
	public void flush()
	{
		err.flush();
		out.flush();
	}

	@Override
	public void publish(LogRecord logRecord)
	{
		if (WARNING == logRecord.getLevel() || SEVERE == logRecord.getLevel())
			err.publish(logRecord);
		else
			out.publish(logRecord);
	}

	@Override
	public void setLevel(Level logLevel) throws SecurityException
	{
		err.setLevel(logLevel);
		out.setLevel(logLevel);
		super.setLevel(logLevel);
	}

	@Override
	public void setFormatter(Formatter formatter) throws SecurityException
	{
		err.setFormatter(formatter);
		out.setFormatter(formatter);
		super.setFormatter(formatter);
	}

	@Override
	public void setErrorManager(ErrorManager errorManager)
	{
		err.setErrorManager(errorManager);
		out.setErrorManager(errorManager);
		super.setErrorManager(errorManager);
	}

	@Override
	public void setFilter(Filter filter) throws SecurityException
	{
		err.setFilter(filter);
		out.setFilter(filter);
		super.setFilter(filter);
	}	
}