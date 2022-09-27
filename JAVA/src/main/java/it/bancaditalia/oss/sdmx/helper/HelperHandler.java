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
import static javax.swing.text.StyleConstants.Foreground;

import java.awt.Color;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

public class HelperHandler extends java.util.logging.Handler {
	private SimpleFormatter formatter = new SimpleFormatter();

    public HelperHandler(JTextPane loggingArea) {
		super();
		this.whereTo = loggingArea;
	}

	private JTextPane whereTo = null;

    @Override
    public void publish(final LogRecord record) {
    	
    	int currentLength = whereTo.getDocument().getLength();
    	try
		{
			String message = formatter.format(record);
			StyledDocument document = (StyledDocument) whereTo.getDocument();
			document.insertString(currentLength, message, null);
			MutableAttributeSet colorAttr = new SimpleAttributeSet();
			if (record.getLevel() == SEVERE || record.getLevel() == WARNING)
				colorAttr.addAttribute(Foreground, new Color(255, 0, 0));
			document.setCharacterAttributes(currentLength, message.length(), colorAttr, false);
			
	    	whereTo.setCaretPosition(whereTo.getDocument().getLength());
		}
		catch (BadLocationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
	}
}