package it.bancaditalia.oss.sdmx.helper;

import java.util.function.Consumer;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class DoFilterListener implements DocumentListener
{
	private final JTextField textField;
	private final Consumer<String> onChange;

	public DoFilterListener(JTextField textField, Consumer<String> onChange)
	{
		this.textField = textField;
		this.onChange = onChange;
	}

	@Override
	public void insertUpdate(DocumentEvent e)
	{
		filter();
	}

	@Override
	public void removeUpdate(DocumentEvent e)
	{
		filter();
	}

	@Override
	public void changedUpdate(DocumentEvent e)
	{
		filter();
	}
	
	public void filter()
	{
		String text = textField.getText();
		if (text != null)
			onChange.accept(text);
	}
}
