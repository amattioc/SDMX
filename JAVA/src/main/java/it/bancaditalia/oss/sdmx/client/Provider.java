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
package it.bancaditalia.oss.sdmx.client;

import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.net.URI;
import java.security.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Valentino Pinna
 *
 */
public class Provider {
	private String name;
	private String description;
	private URI endpoint;
	private String sdmxVersion;
	private boolean needsCredentials;
	private boolean needsURLEncoding;
	private boolean supportsCompression;
	private boolean full = false;
	private boolean isCustom = false;

	// key: flow id (full) --> flow
	private Map<String, Dataflow> flows; 
	// key: dsd id (full) --> structure
	private Map<String, DataFlowStructure> dsdNameToStructureCache = null;
	private SSLSocketFactory sslSocketFactory;

	public Provider(String name, URI endpoint, KeyStore trustStore, boolean needsCredentials, 
			boolean needsURLEncoding, boolean supportsCompression, String description, 
			boolean isCustom, String sdmxVersion) throws SdmxException {
		this.name = name;
		this.endpoint = endpoint;
		this.description = description;
		this.flows = new HashMap<>();
		this.dsdNameToStructureCache = new HashMap<>();
		this.needsCredentials = needsCredentials;
		this.needsURLEncoding = needsURLEncoding;
		this.supportsCompression = supportsCompression;
		this.isCustom = isCustom;
		this.sdmxVersion = sdmxVersion;
		
	    try {
			if (trustStore != null)
			{
			    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			    tmf.init(trustStore);
			    
			    SSLContext context = SSLContext.getInstance("TLS");
			    context.init(null, tmf.getTrustManagers(), new SecureRandom());
			    
			    this.sslSocketFactory = context.getSocketFactory();
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public URI getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(URI endpoint) {
		this.endpoint = endpoint;
	}

	public void setFlows(Map<String, Dataflow> flows) {
		this.flows = flows;
	}

	public void setFlow(Dataflow flow) {
		/* BUG: flow is inserted using just the id, but this.flows is set up using the
		*  full identifier, this creates duplicates inside this.flows. */
		//this.flows.put(flow.getId(), flow);
		this.flows.put(flow.getFullIdentifier(), flow);
	}


	public Dataflow getFlow(String dataflow) {
		if (flows.containsKey(dataflow)) {
			return flows.get(dataflow);
		} else {
			// it could be because we got the simple flow id (e.g. from getTimSeries).
			// We try to handle it matching the id (if any) and returning the first available agency and the latest version
			return flows.values()
					.stream()
					.filter(df -> df.getId().equals(dataflow))
					.max(Comparator.comparing(Dataflow::getVersion))
					.orElse(null);
		}
	}

	public Map<String, Dataflow> getFlows() {
		return flows;
	}

	public DSDIdentifier getDSDIdentifier(String dataflow) {
		DSDIdentifier dsdid = null;
		Dataflow df = getFlow(dataflow);
		if(df != null){
			dsdid = df.getDsdIdentifier();
		}
		return dsdid;
	}

	public DataFlowStructure getDSD(String dsdID) {
		return dsdNameToStructureCache.get(dsdID);
	}

	public void setDSD(String dsdID, DataFlowStructure dsd) {
		this.dsdNameToStructureCache.put(dsdID, dsd);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isNeedsCredentials() {
		return needsCredentials;
	}

	public void setNeedsCredentials(boolean needsCredentials) {
		this.needsCredentials = needsCredentials;
	}

	public void setFull(boolean full) {
		this.full = full;
	}

	public boolean isFull() {
		return full;
	}

	public boolean isNeedsURLEncoding() {
		return needsURLEncoding;
	}

	public void setNeedsURLEncoding(boolean needsURLEncoding) {
		this.needsURLEncoding = needsURLEncoding;
	}

	public boolean isSupportsCompression() {
		return supportsCompression;
	}

	public void setSupportsCompression(boolean supportsCompression) {
		this.supportsCompression = supportsCompression;
	}

	public void setCustom(boolean isCustom) {
		this.isCustom = isCustom;
	}

	public boolean isCustom() {
		return isCustom;
	}

	public SSLSocketFactory getSSLSocketFactory() {
		return sslSocketFactory;
	}

	public String getSdmxVersion() {
		return sdmxVersion;
	}

	public void setSdmxVersion(String sdmxVersion) {
		this.sdmxVersion = sdmxVersion;
	}

}
