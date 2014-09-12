package it.bankitalia.reri.sia.sdmx.helper;

import java.util.logging.LogRecord;

import javax.swing.JTextArea;

public class HelperHandler extends java.util.logging.Handler {

    public HelperHandler(JTextArea whereTo) {
		super();
		this.whereTo = whereTo;
	}

	private JTextArea whereTo = null;

    @Override
    public void publish(final LogRecord record) {
    	whereTo.append(" " + record.getMessage()+"\n");
    }

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws SecurityException {
		// TODO Auto-generated method stub
		
	}
}