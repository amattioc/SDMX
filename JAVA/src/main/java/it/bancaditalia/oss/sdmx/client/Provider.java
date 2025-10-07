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

import java.net.URI;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.SDMXReference;
import it.bancaditalia.oss.sdmx.api.SDMXVersion;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

/**
 * 
 * @author Valentino Pinna
 *
 */
public class Provider
{
	public static enum AuthenticationMethods {
		NONE, BASIC, BEARER
	}
	
	private final String name;
	private final URI endpoint;
	private final SDMXVersion sdmxVersion;
	private final boolean needsURLEncoding;
	private final boolean supportsCompression;
	private final boolean supportsAvailability;
	private String description;
	private AuthenticationMethods authMethod;
	private boolean full = false;

	// key: flow id (full) --> flow
	private Map<String, Dataflow> flows;
	// key: dsd id (full) --> structure
	private Map<String, DataFlowStructure> dsdNameToStructureCache = null;

	public Provider(String name, URI endpoint, AuthenticationMethods authMethod, boolean needsURLEncoding, boolean supportsCompression, boolean supportsAvailability, String description, SDMXVersion sdmxVersion) throws SdmxException
	{
		this.name = name;
		this.endpoint = endpoint;
		this.description = description;
		this.flows = new HashMap<>();
		this.dsdNameToStructureCache = new HashMap<>();
		this.authMethod = authMethod;
		this.needsURLEncoding = needsURLEncoding;
		this.supportsCompression = supportsCompression;
		this.supportsAvailability = supportsAvailability;
		this.sdmxVersion = sdmxVersion;
	}

	public String getName()
	{
		return name;
	}

	public URI getEndpoint()
	{
		return endpoint;
	}

	public void setFlows(Map<String, Dataflow> flows)
	{
		this.flows = flows;
	}

	public void setFlow(Dataflow flow)
	{
		/*
		 * BUG: flow is inserted using just the id, but this.flows is set up using the
		 * full identifier, this creates duplicates inside this.flows.
		 */
		// this.flows.put(flow.getId(), flow);
		this.flows.put(flow.getFullIdentifier(), flow);
	}

	public Dataflow getFlow(String dataflow)
	{
		if (flows.containsKey(dataflow))
			return flows.get(dataflow);

		// it could be because we got the simple flow id (e.g. from getTimSeries).
		// We try to handle it matching the id (if any) and returning the first
		// available agency and the latest version
		return flows.values().stream().filter(df -> df.getId().equals(dataflow)).max(Comparator.comparing(Dataflow::getVersion)).orElse(null);
	}

	public Map<String, Dataflow> getFlows()
	{
		return flows;
	}

	public SDMXReference getDSDIdentifier(String dataflow)
	{
		Dataflow df = getFlow(dataflow);
		return df != null ? df.getDsdIdentifier() : null;
	}

	public DataFlowStructure getDSD(String dsdID)
	{
		return dsdNameToStructureCache.get(dsdID);
	}

	public void setDSD(String dsdID, DataFlowStructure dsd)
	{
		this.dsdNameToStructureCache.put(dsdID, dsd);
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public AuthenticationMethods getAuthMethod()
	{
		return authMethod;
	}

	public void seAuthMethod(AuthenticationMethods authMethod)
	{
		this.authMethod = authMethod;
	}

	public void setFull(boolean full)
	{
		this.full = full;
	}

	public boolean isFull()
	{
		return full;
	}

	public boolean isNeedsURLEncoding()
	{
		return needsURLEncoding;
	}

	public boolean isSupportsCompression()
	{
		return supportsCompression;
	}

	public boolean isSupportsAvailability()
	{
		return supportsAvailability;
	}

	public SDMXVersion getSdmxVersion()
	{
		return sdmxVersion;
	}
}
