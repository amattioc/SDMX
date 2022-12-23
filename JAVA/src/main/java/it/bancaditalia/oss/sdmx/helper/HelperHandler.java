/* Copyright 2010,2014 Bank Of Italy
*
* Licensed under the EUPL, Version 1.1 or - as soon they
* will be approved by the European Commission - subsequent
* versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the
* Licence.
* You may obtain a copy of the Licence at:
*
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in
* writing, software distributed under the Licence is
* distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
* express or implied.
* See the Licence for the specific language governing
* permissions and limitations under the Licence.
*/
package it.bancaditalia.oss.sdmx.helper;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class HelperHandler extends Handler
{
	public static final String LOG_FORMAT = "⏰ %1$s [%2$s:%3$s] %4$s%5$s%n"; 
	
	private static final Formatter FORMATTER = new Formatter()
	{
		@Override
		public String format(LogRecord record)
		{
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

	        String scn = record.getSourceClassName();
	        int dots = scn.length() - scn.replace(".", "").length() - 1;
	        for (int i = 0; i < dots; i++)
	        	scn = scn.replaceFirst("([A-Za-z_])[^.]+\\.", "$1.");
			return String.format(LOG_FORMAT, record.getLevel().getName(), scn, 
	        		record.getSourceMethodName(), message, throwable);
		 }
	};

	private final JTextPane loggingArea;
	private final Deque<Position> positions = new LinkedList<>();
	
    public HelperHandler(JTextPane loggingArea) 
    {
		this.loggingArea = loggingArea;
	}

    @Override
    public synchronized void publish(final LogRecord record)
    {
    	int currentLength = loggingArea.getDocument().getLength();
		String message = FORMATTER.format(record);
		StyledDocument document = (StyledDocument) loggingArea.getDocument();
		
    	try
		{
			document.insertString(currentLength, message, null);
			MutableAttributeSet msgAttrs = new SimpleAttributeSet();
			if (record.getLevel() == SEVERE || record.getLevel() == WARNING)
				StyleConstants.setForeground(msgAttrs, new Color(255, 0, 0));
			document.setCharacterAttributes(currentLength, message.length(), msgAttrs, false);

			msgAttrs.addAttribute("TIME", new Date());
			document.setCharacterAttributes(currentLength, 1, msgAttrs, true);

			positions.add(document.createPosition(document.getEndPosition().getOffset()));

			if (message.contains("http://") || message.contains("https://"))
			{
				int start = message.indexOf("http://");
				if (start == -1)
					start = message.indexOf("https://");
				int end = message.substring(start).split("\\s", 2)[0].length() + start;
				msgAttrs = new SimpleAttributeSet();
				msgAttrs.addAttribute("URL", message.substring(start, end));
				StyleConstants.setUnderline(msgAttrs, true);
				StyleConstants.setForeground(msgAttrs, Color.BLUE);
				document.setCharacterAttributes(currentLength + start, end - start, msgAttrs, true);
			}

			while (positions.size() > 0 && document.getLength() > 50000)
				document.remove(0, positions.remove().getOffset());
	    	loggingArea.setCaretPosition(loggingArea.getDocument().getLength());
		}
		catch (BadLocationException e)
		{
			// Ignore
		}
    }

	@Override
	public void flush() 
	{
		// Ignore
	}

	@Override
	public void close()  
	{
		// Ignore
	}
}