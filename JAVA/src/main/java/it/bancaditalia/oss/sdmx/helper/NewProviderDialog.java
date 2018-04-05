package it.bancaditalia.oss.sdmx.helper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class NewProviderDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String CANCEL_ACTION_COMMAND = "Cancel";
	private static final String OK_ACTION_COMMAND = "OK";

	private int result = JOptionPane.CANCEL_OPTION;
	private String name = null;
	private String description = null;
	private String URL = null;
	
	public NewProviderDialog()
	{
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		setTitle("Add new SDMX 2.1 compliant provider");
		setBounds(100, 100, 395, 175);
		final Container mainPane = getContentPane();
		mainPane.setLayout(new BorderLayout());

		final JPanel contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		mainPane.add(contentPanel, BorderLayout.CENTER);
		
		final GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] {80, 0};
		gbl_contentPanel.columnWeights = new double[] {0, 1.0};
		gbl_contentPanel.rowWeights = new double[]{1, 1, 1};
		contentPanel.setLayout(gbl_contentPanel);
		
		final JLabel lblName = new JLabel("Provider name:");
		lblName.setDisplayedMnemonic(KeyEvent.VK_P);
		lblName.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblName.insets = new Insets(5, 5, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		contentPanel.add(lblName, gbc_lblName);
		
		final JTextField txtName = new JTextField();
		lblName.setLabelFor(txtName);
		GridBagConstraints gbc_txtName = new GridBagConstraints();
		gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtName.insets = new Insets(5, 5, 5, 5);
		gbc_txtName.gridx = 1;
		gbc_txtName.gridy = 0;
		contentPanel.add(txtName, gbc_txtName);
		
		final JLabel lblDescription = new JLabel("Description:");
		lblDescription.setHorizontalAlignment(SwingConstants.TRAILING);
		lblDescription.setDisplayedMnemonic(KeyEvent.VK_D);
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblDescription.insets = new Insets(5, 5, 5, 5);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 1;
		contentPanel.add(lblDescription, gbc_lblDescription);
		
		final JTextField txtDescription = new JTextField();
		lblDescription.setLabelFor(txtDescription);
		GridBagConstraints gbc_txtDescription = new GridBagConstraints();
		gbc_txtDescription.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtDescription.insets = new Insets(5, 5, 5, 5);
		gbc_txtDescription.gridx = 1;
		gbc_txtDescription.gridy = 1;
		contentPanel.add(txtDescription, gbc_txtDescription);

		final JLabel lblURL = new JLabel("Endpoint URL:");
		lblURL.setDisplayedMnemonic(KeyEvent.VK_U);
		lblURL.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblURL = new GridBagConstraints();
		gbc_lblURL.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblURL.insets = new Insets(5, 5, 5, 5);
		gbc_lblURL.gridx = 0;
		gbc_lblURL.gridy = 2;
		contentPanel.add(lblURL, gbc_lblURL);
		
		final JTextField txtURL = new JTextField();
		lblURL.setLabelFor(txtURL);
		GridBagConstraints gbc_txtURL = new GridBagConstraints();
		gbc_txtURL.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtURL.insets = new Insets(5, 5, 5, 5);
		gbc_txtURL.gridx = 1;
		gbc_txtURL.gridy = 2;
		contentPanel.add(txtURL, gbc_txtURL);
		
		JPanel buttonPane = new JPanel();
		buttonPane.setPreferredSize(new Dimension(10, 35));
		buttonPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		mainPane.add(buttonPane, BorderLayout.SOUTH);
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
		
		Component horizontalGlue_1 = Box.createHorizontalGlue();
		buttonPane.add(horizontalGlue_1);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		buttonPane.add(horizontalGlue);
		
		Component horizontalGlue_2 = Box.createHorizontalGlue();
		buttonPane.add(horizontalGlue_2);
		
		JPanel panel = new JPanel();
		buttonPane.add(panel);
		panel.setLayout(new GridLayout(0, 2, 10, 0));

		ActionListener buttonActionListener = new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				if (OK_ACTION_COMMAND.equals(actionEvent.getActionCommand()))
				{
					name = txtName.getText();
					description = txtDescription.getText();
					URL = txtURL.getText();
					result = JOptionPane.OK_OPTION;
					dispose();
				}
				else if (CANCEL_ACTION_COMMAND.equals(actionEvent.getActionCommand()))
				{
					dispose();
				}
			}
		};
		
		JButton okButton = new JButton("OK");
		okButton.setMnemonic(KeyEvent.VK_O);
		okButton.addActionListener(buttonActionListener);
		okButton.setActionCommand(OK_ACTION_COMMAND);
		panel.add(okButton);
		getRootPane().setDefaultButton(okButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setMnemonic(KeyEvent.VK_C);
		cancelButton.addActionListener(buttonActionListener);
		cancelButton.setActionCommand(CANCEL_ACTION_COMMAND);
		panel.add(cancelButton);
	}

	public int getResult()
	{
		setVisible(true);
		
		return result;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getURL() {
		return URL;
	}
}
