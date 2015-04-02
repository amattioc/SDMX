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

import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

public class Provider {
	private String name;
	private String description;
	private URL endpoint;
	private boolean needsCredentials;
	private boolean needsURLEncoding;
	private boolean supportsCompression;
	private boolean full = false;

	// key: flow id (simple) --> flow
	private Map<String, Dataflow> flows; 
	// key: dsd id (full) --> structure
	private Hashtable<String, DataFlowStructure> dsdNameToStructureCache = null;

	public Provider(String name, URL endpoint, boolean needsCredentials, boolean needsURLEncoding, boolean supportsCompression, String description) {
		super();
		this.name = name;
		this.endpoint = endpoint;
		this.description = description;
		this.flows = new Hashtable<String, Dataflow>();
		this.dsdNameToStructureCache = new Hashtable<String, DataFlowStructure>();
		this.needsCredentials = needsCredentials;
		this.needsURLEncoding = needsURLEncoding;
		this.supportsCompression = supportsCompression;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public URL getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(URL endpoint) {
		this.endpoint = endpoint;
	}

	public void setFlows(Map<String, Dataflow> flows) {
		this.flows = flows;
	}

	public void setFlow(Dataflow flow) {
		this.flows.put(flow.getId(), flow);
	}

	public Map<String, Dataflow> getFlows() {
		return flows;
	}

	public DSDIdentifier getDSDIdentifier(String flow) {
		DSDIdentifier dsdid = null;
		Dataflow df = flows.get(flow);
		if(df != null){
			dsdid = df.getDsdIdentifier();
		}
		return dsdid;
	}

	public DataFlowStructure getDSD(String flow) {
		return dsdNameToStructureCache.get(flow);
	}

	public void setDSD(String flow, DataFlowStructure dsd) {
		this.dsdNameToStructureCache.put(flow, dsd);
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

}
