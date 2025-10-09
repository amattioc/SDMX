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

import static it.bancaditalia.oss.sdmx.client.Provider.AuthenticationMethods.BASIC;
import static it.bancaditalia.oss.sdmx.client.Provider.AuthenticationMethods.BEARER;
import static it.bancaditalia.oss.sdmx.util.Utils.checkString;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import it.bancaditalia.oss.sdmx.api.Codelist;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import it.bancaditalia.oss.sdmx.api.PortableDataSet;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.api.SDMXReference;
import it.bancaditalia.oss.sdmx.api.SDMXVersion;
import it.bancaditalia.oss.sdmx.api.SdmxAttribute;
import it.bancaditalia.oss.sdmx.client.Provider.AuthenticationMethods;
import it.bancaditalia.oss.sdmx.client.custom.RestSdmx20Client;
import it.bancaditalia.oss.sdmx.exceptions.DataStructureException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxUnknownProviderException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxXmlContentException;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.LoginDialog;

/**
 * <p>
 * Java class for optimizing interactions with the SdmxClients in non Java environment. It provides a sort fo 'session',
 * storing the clients that are created and reusing them. It also provides caching of all key families retrieved.
 * 
 * @author Attilio Mattiocco
 *
 */
public class SdmxClientHandler
{
	protected static final Logger					LOGGER			= Configuration.getSdmxLogger();
	private static final String						sourceClass		= SdmxClientHandler.class.getSimpleName();

	// key: provider name --> client
	private static Map<String, GenericSDMXClient>	clients			= new HashMap<>();

	public static AuthenticationMethods getAuthMethod(String provider) throws SdmxException
	{
		return getClient(provider).getAuthMethod();
	}

	public static void setCredentials(String provider, String user, String pw) throws SdmxException
	{
		getClient(provider, new BasicCredentials(user, pw));
	}
	
	public static void setToken(String provider, String token) throws SdmxException
	{
		getClient(provider, new BearerCredentials(token));
	}

	public static void setPreferredLanguage(String lang) throws SdmxException
	{
		Configuration.setLanguages(lang);
	}

	/**
	 * Adds a provider 
	 * 
	 * @param provider a non-null, non-empty provider identification short name.
	 * @param endpoint a non-null provider-defined endpoint url for queries
	 * @param needsCredentials true if the provider needs authentication
	 * @param needsURLEncoding true if the url must be encoded
	 * @param supportsCompression true if the provider supports message compression
	 * @param description an optional natural language description of the provider
	 * @param sdmxVersion the major version of the SDMX standard of this provider (SDMX_V2 or SDMX_V3)
	 * 
	 * @throws SdmxException
	 */
	public static void addProvider(String provider, String endpoint, AuthenticationMethods authMethod, boolean needsURLEncoding,
			boolean supportsCompression, boolean supportsAvailability, String description, SDMXVersion sdmxVersion) throws SdmxException
	{
		checkString(provider, "The name of the provider cannot be null");
		checkString(endpoint, "The URL of the provider cannot be null");

		try
		{
			SDMXClientFactory.addProvider(provider, new URI(endpoint), authMethod, needsURLEncoding, supportsCompression, supportsAvailability, description, sdmxVersion);
		}
		catch (URISyntaxException e)
		{
			throw new SdmxInvalidParameterException(e.getMessage());
		}
	}

	/**
	 * Get the list of all available SDMX Providers
	 * 
	 * @return A map where the keys are the provider names and the values the needsCredentials values
	 */
	public static SortedMap<String, AuthenticationMethods> getProviders()
	{
		TreeMap<String, AuthenticationMethods> result = new TreeMap<>();
		for (Entry<String, Provider> entry : SDMXClientFactory.getProviders().entrySet())
			result.put(entry.getKey(), entry.getValue().getAuthMethod());
		return result;
	}

	public static DataFlowStructure getDataFlowStructure(String provider, String dataflow) throws SdmxException
	{
		checkString(provider, "The name of the provider cannot be null");
		checkString(dataflow, "The name of the dataflow cannot be null");

		DataFlowStructure result = null;
		SDMXReference keyF = getDSDIdentifier(provider, dataflow);
		String fullkeyFamilyKey = keyF.getFullIdentifier();
		Provider p = getProvider(provider);
		result = p.getDSD(fullkeyFamilyKey);
		if (result == null)
		{
			LOGGER.finer("DSD for " + keyF.getFullIdentifier() + " not cached. Calling Provider.");
			result = getClient(provider).getDataFlowStructure(keyF, true);
			
			if (result != null)
			{
				if (!(getClient(provider) instanceof RestSdmx20Client))
				{
					// workaround only for V2.1+ : some providers do not set in the dsd response all the referenced codelists
					// and this is a problem, especially for dimensions.
					// we try to fill it with a direct codelist call
					for (Dimension dim: result.getDimensions())
					{
						Codelist cl = dim.getCodeList();
						if (cl != null && cl.isEmpty())
						{
							// we do not allow uncoded dimensions
							Codelist codes = getClient(provider).getCodes(cl);
							if(codes == null || codes.isEmpty()){
								throw new SdmxXmlContentException(
										"Could not find codelist  for '" + cl + "' in provider: '" + provider + "'");

							}
							cl.importFrom(codes);
						}
					}
					
					for (SdmxAttribute attr: result.getAttributes())
					{
						Codelist cl = attr.getCodeList();
						if (cl != null && cl.isEmpty())
						{
							//for attributes we let it go even if we don't fine the codes
							Codelist codes = getClient(provider).getCodes(cl);
							cl.importFrom(codes);
						}
					}
				}
				p.setDSD(fullkeyFamilyKey, result);
			}
			else
				throw new SdmxXmlContentException(
						"Could not find dataflow structure for '" + dataflow + "' in provider: '" + provider + "'");
		}

		return result;
	}

	public static SDMXReference getDSDIdentifier(String provider, String dataflow) throws SdmxException
	{
		checkString(provider, "The name of the provider cannot be null");
		checkString(dataflow, "The name of the dataflow cannot be null");
		
		Provider prov = getProvider(provider);
		SDMXReference result = null;
		result = prov.getDSDIdentifier(dataflow);
		if (result == null)
		{
			LOGGER.finer("DSD identifier for dataflow " + dataflow + " not cached. Calling Provider.");
			Dataflow df = getClient(provider).getDataflow(dataflow, null, null);
			if (df != null)
			{
				prov.setFlow(df);
				result = df.getDsdIdentifier();
				if (result == null)
					throw new SdmxXmlContentException("Could not get DSD identifier for dataflow '" + dataflow
							+ "' in provider: '" + prov.getName() + "'");
			}
			else
				throw new SdmxXmlContentException(
						"Could not get dataflow '" + dataflow + "' in provider: '" + provider + "'");
		}
		return result;
	}

	public static List<Dimension> getDimensions(String provider, String dataflow) throws SdmxException
	{
		return getDataFlowStructure(provider, dataflow).getDimensions();
	}

	public static Map<String, Map<String, String>> filterCodes(String provider, String dataflow, String filter) throws SdmxException
	{
		checkString(provider, "The name of the provider cannot be null");
		checkString(dataflow, "The name of the dataflow cannot be null");
		
		Map<String, Map<String, String>> codes = new LinkedHashMap<>();
		Dataflow df = getFlow(provider, dataflow);
		DataFlowStructure dsd = getDataFlowStructure(provider, dataflow);
		Map<String, List<String>> availableCodes = getClient(provider).getAvailableCubeRegion(df, filter, "available");
		if(availableCodes.size() == dsd.getDimensions().size()){
			for (Iterator<Dimension> iterator = dsd.getDimensions().iterator(); iterator.hasNext();) {
				Dimension dim = (Dimension) iterator.next();
				Map<String, String> dimCodes = getCodes(provider, dataflow, dim.getId())
						.entrySet()
		                .stream()
		                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));;
				dimCodes.keySet().retainAll(availableCodes.get(dim.getId()));
				codes.put(dim.getId(), dimCodes);
			}
			return codes;
		}
		else{
			throw new SdmxInvalidParameterException("The filter returned and empty cube region");
		}
		
	}
	
	public static Map<String, Integer> getSeriesCount(String provider, String dataflow, String filter) throws SdmxException
	{
		Dataflow df = getFlow(provider, dataflow);
		return getClient(provider).getAvailableTimeSeriesNumber(df, filter);
	}
	
	public static Map<String, String> getCodes(String provider, String dataflow, String dimension) throws SdmxException
	{
		checkString(provider, "The name of the provider cannot be null");
		checkString(dataflow, "The name of the dataflow cannot be null");
		checkString(dimension, "The name of the dimension cannot be null");
		
		DataFlowStructure dsd = getDataFlowStructure(provider, dataflow);
		Dimension dim = dsd.getDimension(dimension);
		if (dim != null)
		{
			Codelist cl = dim.getCodeList();
			if (!cl.isEmpty())
				return cl;
			else
			{
				// this is a 2.1 provider
				LOGGER.finer("Codelist for " + provider + ", " + dataflow + ", " + dimension + " not cached.");
				Codelist codes = getClient(provider).getCodes(dsd.getDimension(dimension).getCodeList());
				if (codes != null)
					cl.importFrom(codes);
				else
					throw new SdmxXmlContentException(
							"Could not get codes for '" + dataflow + "' in provider: '" + provider + "'");
				
				return cl;
			}
		}
		else
			throw new SdmxXmlContentException(
					"The dimension: '" + dimension + "' does not exist in dataflow: '" + dataflow + "'");
	}

	public static Dataflow getFlow(String provider, String dataflow) throws SdmxException
	{
		checkString(provider, "The name of the provider cannot be null");
		checkString(dataflow, "The name of the dataflow cannot be null");
		
		Provider p = getProvider(provider);
		Dataflow flow = p.getFlow(dataflow);
		if (flow == null)
		{
			LOGGER.fine("Dataflow " + dataflow + " not cached. Calling Provider.");
			//we get the latest version and all agencies. Hopefully we have only one
			flow = getClient(provider).getDataflow(dataflow, null, null);
			if (flow != null)
				p.setFlow(flow);
			else
				throw new SdmxXmlContentException(
						"Could not get dataflow '" + dataflow + "' in provider: '" + provider + "'");
		}
		return flow;
	}

	public static Map<String, String> getFlows(String provider, String pattern) throws SdmxException
	{
		Map<String, Dataflow> flows = getFlowObjects(provider, pattern);
		Map<String, String> result = new HashMap<>();
		for (Entry<String, Dataflow> entry : flows.entrySet())
			result.put(entry.getKey(), entry.getValue().getDescription());
		return result;
	}

	public static Map<String, Dataflow> getFlowObjects(String provider, String pattern) throws SdmxException
	{
		checkString(provider, "The name of the provider cannot be null");

		Map<String, Dataflow> flows = null;
		Provider p = getProvider(provider);
		flows = p.getFlows();
		if (flows == null || flows.size() == 0 || !p.isFull())
		{
			LOGGER.fine("Flows for " + provider + " not cached. Calling Provider.");
			flows = getClient(provider).getDataflows();
			if (flows != null && flows.size() != 0)
			{
				p.setFlows(flows);
				p.setFull(true);
			}
			else
				throw new SdmxXmlContentException("Could not get dataflows from provider: '" + provider + "'");
		}
		return filterFlows(flows, pattern);
	}

	public static PortableDataSet<Double> getTimeSeriesTable(String provider, String tsKey, 
			String startTime, String endTime, 
			boolean serieskeysonly, String updatedAfter, boolean includeHistory)
			throws SdmxException, DataStructureException
	{
		return new PortableDataSet<>(getTimeSeries(provider, tsKey, startTime, endTime, serieskeysonly, updatedAfter, includeHistory));
	}

	public static PortableDataSet<Double> getTimeSeriesTable2(String provider, String dataflow, String tsKey, String filter, 
			String startTime, String endTime, 
			String attributes, String measures, String updatedAfter, boolean includeHistory)
			throws SdmxException, DataStructureException
	{
		
		PortableDataSet<Double> ds = new PortableDataSet<>(getTimeSeries2(provider, dataflow, tsKey, filter, startTime, endTime, attributes, measures, updatedAfter, includeHistory));
		ds.setDataflow(getFlow(provider, dataflow).getFullIdentifier());
		return(ds);
	}

	//valid for sdmx v2 
	public static List<PortableTimeSeries<Double>> getTimeSeries(String provider, String tsKey,
			String startTime, String endTime, 
			boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException
	{

		List<PortableTimeSeries<Double>> result = new ArrayList<>(); //SDMX 2.0 did not provide a way to specify multiple series keys
		for (String keyId : tsKey.trim().split("\\s*;\\s*"))
			result.addAll(getSingleTimeSeries(provider, keyId, startTime, endTime, 
					serieskeysonly, updatedAfter, includeHistory));
		return (result);
	}

	//valid for sdmx v3
	public static List<PortableTimeSeries<Double>> getTimeSeries2(String provider, String dataflow, String tsKey, String filter, 
			String startTime, String endTime, 
			String attributes, String measures, String updatedAfter, boolean includeHistory) throws SdmxException
	{
		Dataflow df = getFlow(provider, dataflow);
		DataFlowStructure dsd = getDataFlowStructure(provider, dataflow);
		
		List<PortableTimeSeries<Double>> result = getClient(provider).getTimeSeries(df, dsd, tsKey, filter, startTime, endTime, 
																					attributes, measures, updatedAfter, includeHistory);
		if (result == null || result.size() == 0)
			throw new SdmxXmlContentException(
					"The query: key=" + tsKey + " and filter=" + filter + " did not match any time series on the provider for dataflow: " + dataflow);
		return (result);
	}

	private static List<PortableTimeSeries<Double>> getSingleTimeSeries(String provider, String tsKey, 
			String startTime, String endTime, 
			boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException
	{
		checkString(provider, "The name of the provider cannot be null");
		checkString(tsKey, "The ts key must have valid values");
	
		List<PortableTimeSeries<Double>> result = null;
	
		String[] tokens = extractFlowAndResource(tsKey);
		String dataflow = tokens[0];
		tsKey = tokens[1];
		// workaround for 2.1 specs defect 
		if(tsKey.equals(".."))
			tsKey = "ALL";
		
		Dataflow df = getFlow(provider, dataflow);
		DataFlowStructure dsd = getDataFlowStructure(provider, dataflow);
		result = getClient(provider).getTimeSeries(df, dsd, tsKey, startTime, endTime, serieskeysonly, updatedAfter, includeHistory);
		if (result == null || result.size() == 0)
			throw new SdmxXmlContentException(
					"The query: key=" + tsKey + " did not match any time series on the provider for dataflow: " + dataflow);
		return result;
	}

	public static String getDataURL(String provider, String dataflow, String tsKey, String start, String end, boolean seriesKeysOnly,
			String updatedAfter, boolean includeHistory) throws SdmxException
	{
		checkString(provider, "The name of the provider cannot be null");
		checkString(dataflow, "The name of the dataflow cannot be null");
		checkString(tsKey, "The ts key must have valid values");

		Dataflow df = getFlow(provider, dataflow);
		String result = getClient(provider).buildDataURL(df, tsKey, start, end, seriesKeysOnly, updatedAfter,
				includeHistory);
		return (result);
	}

	public static String dumpTimeSeriesList(List<PortableTimeSeries<Double>> ts)
	{
		StringBuffer result = new StringBuffer("");
		int maxSize = 0;
		boolean first = true;
		for (PortableTimeSeries<?> series : ts)
		{
			if (!first)
				result.append(";");
			first = false;
			result.append(";").append(series.getName());
			int size = series.size();
			if (size > maxSize)
				maxSize = size;
			if (Configuration.isReverse())
			{
				// reverse the time series for user friendliness
				series.reverse();
			}
		}
		result.append("\n");
		for (int i = 0; i < maxSize; i++)
		{
			for (int j = 0; j < ts.size(); j++)
			{
				if (i < ts.get(j).size())
				{
					result.append(ts.get(j).get(i).getTimeslot()).append(";");
					result.append(ts.get(j).get(i).getValue());
				}
				else
				{
					result.append(";");
				}
				if (j + 1 < ts.size())
				{
					result.append(";");
				}
			}
			result.append("\n");
		}
		return result.toString();
	}

	public static String dumpTimeSeries(String provider, String tsKey, String startTime, String endTime)
			throws SdmxException, DataStructureException
	{
		checkString(provider, "The name of the provider cannot be null");
		checkString(tsKey, "The ts key must have valid values");

		if (!SDMXClientFactory.getProviders().containsKey(provider))
			throw new SdmxInvalidParameterException("The provider : " + provider + " does not exist.");
		
		String result = "";
		if (!Configuration.isTable())
		{
			// Do it as a list of time series
			List<PortableTimeSeries<Double>> ts = getTimeSeries(provider, tsKey, startTime, endTime, false, null, false);
			result = dumpTimeSeriesList(ts);
		}
		else
		{
			// do it as a table
			result = getTimeSeriesTable(provider, tsKey, startTime, endTime, false, null, false).toString();
		}
		return result;
	}

	private static Provider getProvider(String provider) throws SdmxException
	{
		checkString(provider, "The name of the provider cannot be null");

		Provider prov = SDMXClientFactory.getProviders().get(provider);
		if (prov == null)
			throw new SdmxUnknownProviderException(provider);

		return prov;
	}

	private static GenericSDMXClient getClient(String provider, Credentials creds) throws SdmxException
	{
		checkString(provider, "The name of the provider cannot be null");

		final String sourceMethod = "getClient";
		LOGGER.entering(sourceClass, sourceMethod);

		GenericSDMXClient client = clients.get(provider);
		if (client == null)
		{
			LOGGER.finer("Client for " + provider + " does not exist. I will create it.");
			client = (GenericSDMXClient) SDMXClientFactory.createClient(provider);
			handleAuthentication(client, creds);
			clients.put(provider, client);
		}
		LOGGER.exiting(sourceClass, sourceMethod);
		return client;
	}
	
	private static GenericSDMXClient getClient(String provider) throws SdmxException
	{
		return getClient(provider, null);
	}
	
	private static void handleAuthentication(GenericSDMXClient client, Credentials creds) throws SdmxException
	{
		if (client == null)
		{
			LOGGER.severe("The client cannot be null");
			throw new SdmxInvalidParameterException("The client cannot be null");
		}
		
		if (creds == null && client.getAuthMethod() != AuthenticationMethods.NONE){
			if (client.getAuthMethod() == BASIC) {
				final JFrame frame = new JFrame("Authentication");
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				LoginDialog loginDlg = new LoginDialog(frame, client.getName() + " Authentication");
				loginDlg.setVisible(true);
				client.setCredentials(new BasicCredentials(loginDlg.getUsername(), loginDlg.getPassword()));
				frame.dispose();
			}
			else if (client.getAuthMethod() == BEARER)
			{
				final JFrame frame = new JFrame("Authentication");
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				LoginDialog loginDlg = new LoginDialog(frame, client.getName() + " Authentication", false);
				loginDlg.setVisible(true);
				client.setCredentials(new BearerCredentials(loginDlg.getPassword()));
				frame.dispose();
			}
			else{
				throw new SdmxInvalidParameterException("The client does not need authentication");
			}
		}
		else
		{
			client.setCredentials(creds);
		}
	}

	private static Map<String, Dataflow> filterFlows(Map<String, Dataflow> flows, String pattern)
	{
		final String sourceMethod = "filterFlows";
		LOGGER.entering(sourceClass, sourceMethod);
		Map<String, Dataflow> result = new HashMap<>();
		if (flows != null && flows.size() > 0)
		{
			if (pattern != null && !pattern.trim().isEmpty())
				pattern = pattern.replaceAll("\\*", ".*").replaceAll("\\?", ".");

			for (Entry<String, Dataflow> entry : flows.entrySet())
			{
				String trueName;
				if (entry.getKey().split(",").length > 2)
					trueName = entry.getKey().split(",")[1];
				else
					trueName = entry.getKey();
				String description = entry.getValue().getDescription();
				if (pattern != null && !pattern.trim().isEmpty())
					if (trueName.matches(pattern) || description.matches(pattern))
						result.put(entry.getKey(), entry.getValue());
					else
						;
				else
					result.put(entry.getKey(), entry.getValue());
			}
		}

		LOGGER.exiting(sourceClass, sourceMethod);
		return result;
	}

	private static String[] translateLegacyTSQuery(String tsKey)
	{
		String[] newKey = new String[2];
		String delims = "[.]";
		String[] tokens = tsKey.split(delims, 2);
		if (tokens.length == 2)
		{
			newKey[0] = tokens[0];
			String resource = tokens[1];
			resource = resource.replace("*", "");
			resource = resource.replace('|', '+');
			newKey[1] = resource;
		}
		else
		{
			LOGGER.severe("Error in query string format: '" + tsKey + "'. Could not get dataflow id.");
		}
		return newKey;
	}

	public static String[] extractFlowAndResource(String tsKey) throws SdmxException
	{
		tsKey = tsKey.trim();
		String delims = "[ /]";
		String[] tokens = tsKey.split(delims, 2);
		if (tokens.length != 2)
		{
			// legacy mode: flow.tskey
			tokens = translateLegacyTSQuery(tsKey);
			if (tokens.length != 2)
				throw new SdmxXmlContentException("Malformed time series key: " + tsKey);
		}
		return tokens;
	}

}
