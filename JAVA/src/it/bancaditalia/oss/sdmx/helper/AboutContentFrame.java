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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Manifest;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JTextArea;

/**
 * @author Attilio Mattiocco
 *
 */
public class AboutContentFrame extends JFrame{
	private static final long serialVersionUID = 1L;
	
	public AboutContentFrame(){
		setSize(800, 600);
		this.setLocationRelativeTo(this.getContentPane());
	
		JEditorPane commandPane = new JEditorPane("text/html", "");
		JTextArea text = new JTextArea();
		text.setEditable(false);
		StringBuffer buf = new StringBuffer();
		buf.append(	"<H1>SDMX Connectors Project.</H1>");
		
		String buildID = "NOT FOUND";
		try {
			Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
			while (resources.hasMoreElements()) {
				URL tmp = resources.nextElement();
				if(tmp.getPath().contains("SDMX.jar")){
					Manifest manifest = new Manifest(tmp.openStream());
					String build = manifest.getMainAttributes().getValue("BUILD");
					if(build != null && ! build.isEmpty()){
						buildID = build;
					}
				}
			}
		} catch (IOException E) {
			// handle
		}

		buf.append(	"Build ID: <b>" + buildID + "</b><br><br>");
		buf.append(	"Project Information and Code : <b>https://github.com/amattioc/SDMX</b> <br><br>");
		buf.append(	"<i>Copyright 2010,2014 Bank Of Italy<br>");
		buf.append(	"Licensed under the EUPL, Version 1.1 or - as soon they<br>");
		buf.append(	"will be approved by the European Commission - subsequent<br>");
		buf.append(	"versions of the EUPL (the \"Licence\");<br>");
		buf.append(	"You may not use this work except in compliance with the Licence.<br>");
		buf.append(	"You may obtain a copy of the Licence at:<br>");
		buf.append(	"http://ec.europa.eu/idabc/eupl<br>");
		buf.append(	"Unless required by applicable law or agreed to in<br>");
		buf.append(	"writing, software distributed under the Licence is<br>");
		buf.append(	"distributed on an \"AS IS\" basis,<br>");
		buf.append(	"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either<br>");
		buf.append(	"express or implied.<br>");
		buf.append(	"See the Licence for the specific language governing<br>");
		buf.append(	"permissions and limitations under the Licence.</i>");

		text.setText(buf.toString());
		commandPane.setText(buf.toString());
		add(commandPane);
	}
}
