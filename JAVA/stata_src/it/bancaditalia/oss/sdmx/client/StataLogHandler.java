package it.bancaditalia.oss.sdmx.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import com.stata.sfi.SFIToolkit;

public class StataLogHandler extends Handler
{
	private SimpleFormatter formatter = new SimpleFormatter();

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
		if (record.getLevel().intValue() < Level.WARNING.intValue())
			SFIToolkit.errorln(formatter.formatMessage(record));
		else
			SFIToolkit.displayln(formatter.formatMessage(record));
	}

	public PrintStream getPrintStream()
	{
		return new PrintStream(new OutputStream()
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
				
				@Override
				public void write(int b) throws IOException
				{
					baos.write(b);
				}
				
				@Override
				public void flush() throws IOException
				{
					String s = new String(baos.toByteArray());
					baos.reset();
					SFIToolkit.display(s);
				}
			});
	}

	public PrintStream getErrorStream()
	{
		return new PrintStream(new OutputStream()
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
				
				@Override
				public void write(int b) throws IOException
				{
					baos.write(b);
				}
				
				@Override
				public void flush() throws IOException
				{
					String s = new String(baos.toByteArray());
					baos.reset();
					SFIToolkit.error(s);
				}
			});
	}
}