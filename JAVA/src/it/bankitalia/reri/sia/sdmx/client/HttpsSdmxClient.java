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
package it.bankitalia.reri.sia.sdmx.client;

import it.bankitalia.reri.sia.util.Configuration;

import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * @author Attilio Mattiocco
 *
 */
public class HttpsSdmxClient extends RestSdmxClient{

	public HttpsSdmxClient(String name, URL endpoint, String agency, boolean needsCredentials, boolean dotStat) throws NoSuchAlgorithmException, KeyManagementException {
		super(name, endpoint, agency, needsCredentials, dotStat);
		
		// check if we want to disable the certificate checks, 
		// elseway the certificates have to be installed in teh keystore
		if (Configuration.isSSLCertificatesDisabled()) {
			logger.fine("The SSL Certificate checks are disabled...");
			TrustManager[] alwaysTrust = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				public void checkClientTrusted(X509Certificate[] certs,
						String authType) {
				}
				public void checkServerTrusted(X509Certificate[] certs,
						String authType) {
				}
			} };

			SSLContext context = SSLContext.getInstance("SSL");
			context.init(null, alwaysTrust, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
			//we also want to avoid verification of the chains 
			HostnameVerifier alwaysValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
			HttpsURLConnection.setDefaultHostnameVerifier(alwaysValid);
		}
	}
	
}
