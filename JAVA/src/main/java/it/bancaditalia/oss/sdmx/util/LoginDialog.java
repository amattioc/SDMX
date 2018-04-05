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
package it.bancaditalia.oss.sdmx.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

/**
 * @author Attilio Mattiocco
 *
 */
public class LoginDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTextField user;
	private JPasswordField pw;
	private JLabel labelUser;
	private JLabel labelPw;
	private JButton loginButton;
	private JButton cancelButton;

	/**
	 * Starts a login dialog asking for username and password.
	 * 
	 * @param parentFrame The frame if this dialog must be modal.
	 * @param title The title of this dialog.
	 */
	public LoginDialog(Frame parentFrame, String title) {
		this(parentFrame, title, null, true);
	}
	
	public LoginDialog(Frame parentFrame, String title, String username) {
		this(parentFrame, title, username, true);
	}
	
	/**
	 * Starts a login dialog asking for one or two authentication tokens.
	 * 
	 * @param parentFrame The frame if this dialog must be modal.
	 * @param title The title of this dialog.
	 * @param twoFields if this dialog must ask one or two tokens.
	 */
	public LoginDialog(Frame parentFrame, String title, boolean twoFields) {
		this(parentFrame, title, null, twoFields);
	}
	
	public LoginDialog(Frame parentFrame, String title, String username, boolean twoFields) {
		super(parentFrame, title, true);
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.fill = GridBagConstraints.HORIZONTAL;

		if(twoFields){
			labelUser = new JLabel("Username: ");
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.gridwidth = 1;
			panel.add(labelUser, constraints);
	
			user = new JTextField(username, 20);
			constraints.gridx = 1;
			constraints.gridy = 0;
			constraints.gridwidth = 2;
			panel.add(user, constraints);
		}
		
		labelPw = new JLabel(twoFields ? "Password: " : "Key:");
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		panel.add(labelPw, constraints);

		pw = new JPasswordField(20);
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		panel.add(pw, constraints);
		panel.setBorder(new LineBorder(Color.GRAY));

		loginButton = setLogin();
		cancelButton = setCancel();
		
		JPanel bp = new JPanel();
		bp.add(loginButton);
		bp.add(cancelButton);
		getContentPane().add(panel, BorderLayout.CENTER);
		getContentPane().add(bp, BorderLayout.PAGE_END);
		pack();
		setResizable(false);
		setLocationRelativeTo(parentFrame);
		
		getRootPane().setDefaultButton(loginButton);
	}

	/**
	 * @return the username entered by the user.
	 */
	public String getUsername() {
		return user.getText().trim();
	}

	/**
	 * @return the password entered by the user.
	 */
	public String getPassword() {
		return new String(pw.getPassword());
	}
	
	private JButton setLogin(){
		JButton loginButton = new JButton("Login");
		loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
                dispose();
			}
		});
		return loginButton;
	}
	private JButton setCancel(){
		JButton cancelButton = new JButton("Cancel");
		
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		return cancelButton;
	}

}