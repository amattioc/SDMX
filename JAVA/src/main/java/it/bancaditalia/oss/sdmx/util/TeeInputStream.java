package it.bancaditalia.oss.sdmx.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TeeInputStream extends InputStream
{
	private final InputStream source;
	private final OutputStream teed;

	public TeeInputStream(InputStream source, File tee) throws FileNotFoundException
	{
		this.source = source;
		this.teed = new FileOutputStream(tee);
	}
	
	@Override
	public void close() throws IOException
	{
		source.close();
	}

	@Override
	public int read() throws IOException
	{
		int c = source.read();
		if (c >= 0)
			teed.write(c);
		if (c == '\n')
			teed.flush();
		return c;
	}
}
