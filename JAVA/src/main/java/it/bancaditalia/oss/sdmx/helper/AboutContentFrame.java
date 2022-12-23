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

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import it.bancaditalia.oss.sdmx.util.Configuration;

/**
 * @author Attilio Mattiocco
 *
 */
public class AboutContentFrame extends JDialog
{
	private static final long	serialVersionUID	= 1L;
	protected static Logger		logger				= Configuration.getSdmxLogger();

	/**
	 * 
	 */
	public AboutContentFrame()
	{
		String buildID = "NOT FOUND";
		URL manifestURL = AboutContentFrame.class.getResource("/META-INF/MANIFEST.MF");
		if (manifestURL != null)
			try (InputStream e = manifestURL.openStream())
			{
				Manifest manifest = new Manifest(manifestURL.openStream());
				String build = manifest.getMainAttributes().getValue("BUILD");
				if (build != null && !build.isEmpty())
					buildID = build;
			}
			catch (IOException e)
			{
				logger.severe("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
				logger.log(Level.FINER, "", e);
			}

		StringBuffer buf = new StringBuffer();
		buf.append("<div style='font-family: sans-serif;'>");
		buf.append("<h1 style='text-align:center; font-weight: bold'>SDMX Connectors Project</h1>");
		buf.append("<h2 style='text-align:center'>Project information and code available on GitHub:</h2>");
		buf.append(
				"<p style='text-align:center; font-family: monospace; font-weight: bold'><a href='https://github.com/amattioc/SDMX'><b>amattioc/SDMX</b></a></p>");
		buf.append("<p style='margin: 50px 0 30px 0; font-size: 90%'>Copyright 2010-2023 Bank Of Italy</p>");
		buf.append("<div style='font-style: italic'>");
		buf.append(
				"<p><a href='http://eur-lex.europa.eu/legal-content/EN/TXT/?uri=uriserv:OJ.L_.2017.128.01.0059.01.ENG'>");
		buf.append("Licensed under the EUPL, Version 1.2</a> or subsequent version (the \"Licence\");</p>");
		buf.append("<p>You may not use this work except in compliance with the Licence.</p>");
		buf.append("<p>The Work is provided under the Licence on an as-is<br />");
		buf.append("basis and without warranties of any kind concerning the<br />");
		buf.append("Work, including without limitation merchantability, fitness<br />");
		buf.append("for a particular purpose, absence of defects or errors,<br />");
		buf.append("accuracy, non-infringement of intellectual property rights<br />");
		buf.append("other than copyright as stated in the Licence.</p>");
		buf.append("<p>See the Licence for the specific language governing<br />");
		buf.append("permissions and limitations under the Licence.</p></div>");
		buf.append("<p style='font-size: 80%'>Build ID: <b>" + buildID + "</b></p>");
		buf.append("</div>");

		JEditorPane textPane = new JEditorPane("text/html", "");
		textPane.setEditable(false);
		textPane.setContentType("text/html");
		textPane.setText(buf.toString());
		textPane.setBackground(UIManager.getColor("Panel.background"));
		textPane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e)
			{
				try
				{
					Desktop.getDesktop().browse(e.getURL().toURI());
				}
				catch (IOException e1)
				{
					// ignore
				}
				catch (URISyntaxException e1)
				{
					// impossible
				}
			}
		});

		ActionListener closeListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				dispatchEvent(new WindowEvent(AboutContentFrame.this, WindowEvent.WINDOW_CLOSING));
			}
		};

		JButton close = new JButton("Close");
		close.addActionListener(closeListener);

		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(close);

		JPanel content = new JPanel();
		content.setLayout(new BorderLayout());
		content.setBorder(new EmptyBorder(10, 10, 10, 10));
		content.add(textPane, BorderLayout.CENTER);
		content.add(box, BorderLayout.SOUTH);

		this.setSize(800, 600);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setResizable(false);
		this.setLocationRelativeTo(this.getContentPane());
		this.setTitle("SDMX Connectors Information");
		this.add(content);

		JRootPane root = getRootPane();
		root.setDefaultButton(close);
		root.registerKeyboardAction(closeListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		this.setVisible(true);
	}
}
