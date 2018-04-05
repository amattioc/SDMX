package it.bancaditalia.oss.sdmx.util;

import java.io.IOException;
import java.io.Reader;

public class SafeLineReader extends Reader {

	Reader reader;

	final char buffer[] = new char[8192];
	int bufferPos = 0;
	int bufferLen = 0;

	public SafeLineReader(Reader reader) {
		this.reader = reader;
	}

	@Override
	public synchronized int read(char[] cbuf, int off, int len) throws IOException
	{
		if (len >= bufferLen)
		{
			System.arraycopy(buffer, bufferPos, cbuf, off, bufferLen);
			bufferPos = 0;
			bufferLen = 0;
			if (len == bufferLen)
				return bufferLen;
			else
				return bufferLen + reader.read(cbuf, off + bufferLen, len - bufferLen);
		}
		else
		{
			System.arraycopy(buffer, bufferPos, cbuf, off, len);
			bufferPos += len;
			bufferLen -= len;
			return len;
		}
	}
	
	/**
	 * Reads a line eventually truncating it at the end of the buffer.
	 * 
	 * @return The line read. It cannot exceed the allocated buffer size.
	 * @throws IOException if an error occurs while reading.
	 */
	public synchronized String readLine() throws IOException
	{
		StringBuilder builder = new StringBuilder(8192);
		boolean eolFound = false;
		int start = bufferPos;
		
		for (; !eolFound && bufferLen > 0; bufferPos++, bufferLen--)
			if (buffer[bufferPos] == '\n' || buffer[bufferPos] == '\r')
				eolFound = true;
		
		builder.append(buffer, start, bufferPos - start);
		
		if (!eolFound)
		{
			// truncate any line at 8192 chars
			int allowLen = 8192 - builder.length();
			bufferLen = reader.read(buffer, bufferPos = 0, allowLen);

			for (; !eolFound && bufferLen > 0; bufferPos++, bufferLen--)
				if (buffer[bufferPos] == '\n' || buffer[bufferPos] == '\r')
					eolFound = true;
			
			builder.append(buffer, start, bufferPos - start);
		}
		
		return builder.toString();
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}
}
