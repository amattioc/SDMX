/* Copyright 2010,2014 Bank Of Italy
*
* Licensed under the EUPL, Version 1.1 or – as soon they
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

import it.bankitalia.reri.sia.sdmx.api.DSDIdentifier;
import it.bankitalia.reri.sia.sdmx.api.DataFlowStructure;

import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

public class Provider {
	private String name;
	private String agency;
	private URL endpoint;
	private boolean needsCredentials;
	
	// key: provider name --> provider class
	private Map<String,String> flows;
	// key: agency/flow/version --> dsd identifier
	private Hashtable<String, DSDIdentifier> flowToDSDCache = null;
	// key: agency/dsd/version --> structure
	private Hashtable<String, DataFlowStructure> dsdNameToStructureCache = null;

	public Provider(String name, String agency, URL endpoint, boolean needsCredentials) {
		super();
		this.name = name;
		this.agency = agency;
		this.endpoint = endpoint;
		
		this.flows = new Hashtable<String,String>();
		this.flowToDSDCache = new Hashtable<String, DSDIdentifier>();
		this.dsdNameToStructureCache = new Hashtable<String, DataFlowStructure>();
		this.needsCredentials = needsCredentials;
	}
	public String getAgency() {
		return agency;
	}
	public void setAgency(String agency) {
		this.agency = agency;
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
	public void setFlows(Map<String,String> flows) {
		this.flows = flows;
	}
	public Map<String,String> getFlows() {
		return flows;
	}
	public DSDIdentifier getDSDIdentifier(String flow) {
		return flowToDSDCache.get(flow);
	}
	public void setDSDIdentifier(String flow, DSDIdentifier dsd) {
		this.flowToDSDCache.put(flow, dsd);
	}
	public DataFlowStructure getDSD(String flow) {
		return dsdNameToStructureCache.get(flow);
	}
	public void setDSD(String flow, DataFlowStructure dsd) {
		this.dsdNameToStructureCache.put(flow, dsd);
	}
	public boolean isNeedsCredentials() {
		return needsCredentials;
	}
	public void setNeedsCredentials(boolean needsCredentials) {
		this.needsCredentials = needsCredentials;
	}
	
}
