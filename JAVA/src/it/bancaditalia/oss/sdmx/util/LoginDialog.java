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

	public LoginDialog(Frame parentFrame, String target) {
		this(parentFrame, target, true);
	}
	
	public LoginDialog(Frame parentFrame, String target, boolean twoFields) {
		super(parentFrame, target, true);
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.fill = GridBagConstraints.HORIZONTAL;

		if(twoFields){
			labelUser = new JLabel("Username: ");
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.gridwidth = 1;
			panel.add(labelUser, constraints);
	
			user = new JTextField(20);
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

	public String getUsername() {
		return user.getText().trim();
	}

	public String getPassword() {
		return new String(pw.getPassword());
	}
	
	private JButton setLogin(){
		JButton loginButton = new JButton("Login");
		loginButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                dispose();
			}
		});
		return loginButton;
	}
	private JButton setCancel(){
		JButton cancelButton = new JButton("Cancel");
		
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		return cancelButton;
	}

}
