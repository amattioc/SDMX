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
package it.bancaditalia.oss.sdmx.client.custom;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JFrame;

import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.LoginDialog;

/**
 * @author Attilio Mattiocco
 *
 */
public class UIS extends RestSdmxClient{
	
	private String apiKey = null;
	
	public UIS() throws URISyntaxException {
		super("UIS", new URI("https://api.uis.unesco.org/sdmx"), false, false, true);
		apiKey = Configuration.getUISApiKey();
		if(apiKey == null || apiKey.isEmpty()){
			final JFrame frame = new JFrame("Authentication");
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			LoginDialog loginDlg = new LoginDialog(frame, "UIS API KEY", false);
            loginDlg.setVisible(true);
            apiKey = loginDlg.getPassword();
		}
        
	}
	
	@Override
	protected void handleHttpHeaders(HttpURLConnection conn, String acceptHeader) {
		super.handleHttpHeaders(conn, acceptHeader);
		conn.setRequestProperty("Ocp-Apim-Subscription-key", apiKey);
	}
}
