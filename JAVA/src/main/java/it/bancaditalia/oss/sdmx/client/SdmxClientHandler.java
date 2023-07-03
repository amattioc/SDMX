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

import java.io.File;
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
import it.bancaditalia.oss.sdmx.api.SDMXReference;
import it.bancaditalia.oss.sdmx.api.PortableDataSet;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.api.SdmxAttribute;
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

	public static boolean needsCredentials(String provider) throws SdmxException
	{
		return getClient(provider).needsCredentials();
	}

	public static void setCredentials(String provider, String user, String pw) throws SdmxException
	{
		getClient(provider, user, pw);
	}

	public static void setPreferredLanguage(String lang) throws SdmxException
	{
		Configuration.setLanguages(lang);
	}

	/**
	 * Adds a local provider 
	 * 
	 * @param provider a non-null, non-empty provider identification short name.
	 * @param endpoint a non-null existing directory where the SDMX files are stored
	 * @param description an optional natural language description of the provider
	 * 
	 * @throws SdmxException
	 */
	public static void addLocalProvider(String provider, String endpoint, String description) throws SdmxException
	{

		if (provider == null || provider.trim().isEmpty())
		{
			LOGGER.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (endpoint == null)
		{
			LOGGER.severe("The enpoint of the provider cannot be null");
			throw new SdmxInvalidParameterException("The endpoint of the provider cannot be null");
		}
		// get the URL from this file
		File epFile = new File(endpoint);
		if (!epFile.isDirectory())
		{
			LOGGER.severe("The enpoint of the provider must be an existing local directory");
			throw new SdmxInvalidParameterException(
					"The enpoint of the provider has to be an existing local directory");
		}

		SDMXClientFactory.addProvider(provider, epFile.toURI(), null, false, false, false, description, false);
	}

	/**
	 * Adds a local provider 
	 * 
	 * @param provider a non-null, non-empty provider identification short name.
	 * @param endpoint a non-null provider-defined endpoint url for queries
	 * @param needsCredentials true if the provider needs authentication
	 * @param needsURLEncoding true if the url must be encoded
	 * @param supportsCompression true if the provider supports message compression
	 * @param description an optional natural language description of the provider
	 * 
	 * @throws SdmxException
	 */
	public static void addProvider(String provider, String endpoint, boolean needsCredentials, boolean needsURLEncoding,
			boolean supportsCompression, String description) throws SdmxException
	{

		if (provider == null || provider.trim().isEmpty())
		{
			LOGGER.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (endpoint == null)
		{
			LOGGER.severe("The enpoint of the provider cannot be null");
			throw new SdmxInvalidParameterException("The endpoint of the provider cannot be null");
		}
		try
		{
			SDMXClientFactory.addProvider(provider, new URI(endpoint), null, needsCredentials, needsURLEncoding,
					supportsCompression, description, false);
		}
		catch (URISyntaxException e)
		{
			throw new SdmxInvalidParameterException(e.getMessage());
		}
	}

	/**
	 * Adds a local provider 
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
	public static void addProvider(String provider, String endpoint, boolean needsCredentials, boolean needsURLEncoding,
			boolean supportsCompression, String description, String sdmxVersion) throws SdmxException
	{

		if (provider == null || provider.trim().isEmpty())
		{
			LOGGER.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (endpoint == null)
		{
			LOGGER.severe("The enpoint of the provider cannot be null");
			throw new SdmxInvalidParameterException("The endpoint of the provider cannot be null");
		}
		try
		{
			SDMXClientFactory.addProvider(provider, new URI(endpoint), null, needsCredentials, needsURLEncoding,
					supportsCompression, description, false, sdmxVersion);
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
	public static SortedMap<String, Boolean> getProviders()
	{
		TreeMap<String, Boolean> result = new TreeMap<>();
		for (Entry<String, Provider> entry : SDMXClientFactory.getProviders().entrySet())
			result.put(entry.getKey(), entry.getValue().isNeedsCredentials());
		return result;
	}

	public static DataFlowStructure getDataFlowStructure(String provider, String dataflow) throws SdmxException
	{
		if (provider == null || provider.trim().isEmpty())
		{
			LOGGER.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (dataflow == null || dataflow.trim().isEmpty())
		{
			LOGGER.severe("The name of the dataflow cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
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
							dim.setCodeList(codes);
							result.setDimension(dim);
						}
					}
					
					for (SdmxAttribute attr: result.getAttributes())
					{
						Codelist cl = attr.getCodeList();
						if (cl != null && cl.isEmpty())
						{
							//for attributes we let it go even if we don't fine the codes
							attr.setCodeList(getClient(provider).getCodes(cl));
							result.setAttribute(attr);
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

	public static SDMXReference getDSDIdentifier(String providerName, String dataflow) throws SdmxException
	{
		if (providerName == null || providerName.trim().isEmpty())
		{
			LOGGER.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (dataflow == null || dataflow.trim().isEmpty())
		{
			LOGGER.severe("The name of the dataflow cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		Provider provider = getProvider(providerName);
		SDMXReference result = null;
		result = provider.getDSDIdentifier(dataflow);
		if (result == null)
		{
			LOGGER.finer("DSD identifier for dataflow " + dataflow + " not cached. Calling Provider.");
			result = provider.getFlow(dataflow);
			Dataflow df = getClient(providerName).getDataflow(result.getId(), result.getAgency(), result.getVersion());
			if (df != null)
			{
				provider.setFlow(df);
				result = df.getDsdIdentifier();
				if (result == null)
					throw new SdmxXmlContentException("Could not get DSD identifier for dataflow '" + dataflow
							+ "' in provider: '" + provider.getName() + "'");
			}
			else
				throw new SdmxXmlContentException(
						"Could not get dataflow '" + dataflow + "' in provider: '" + providerName + "'");
		}
		return result;
	}

	public static List<Dimension> getDimensions(String provider, String dataflow) throws SdmxException
	{
		if (provider == null || provider.trim().isEmpty())
		{
			LOGGER.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (dataflow == null || dataflow.trim().isEmpty())
		{
			LOGGER.severe("The name of the dataflow cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		return getDataFlowStructure(provider, dataflow).getDimensions();
	}

	public static Map<String, Map<String, String>> filterCodes(String provider, String dataflow, String filter) throws SdmxException
	{
		if (provider == null || provider.trim().isEmpty())
		{
			LOGGER.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (dataflow == null || dataflow.trim().isEmpty())
		{
			LOGGER.severe("The name of the dataflow cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
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
	
	public static Integer getSeriesCount(String provider, String dataflow, String filter) throws SdmxException
	{
		if (provider == null || provider.trim().isEmpty())
		{
			LOGGER.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (dataflow == null || dataflow.trim().isEmpty())
		{
			LOGGER.severe("The name of the dataflow cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		Dataflow df = getFlow(provider, dataflow);
		return getClient(provider).getAvailableTimeSeriesNumber(df, filter);
		 
	}
	
	public static Map<String, String> getCodes(String provider, String dataflow, String dimension) throws SdmxException
	{
		if (provider == null || provider.trim().isEmpty())
		{
			LOGGER.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (dataflow == null || dataflow.trim().isEmpty())
		{
			LOGGER.severe("The name of the dataflow cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (dimension == null || dimension.trim().isEmpty())
		{
			LOGGER.severe("The name of the dimension cannot be null");
			throw new SdmxInvalidParameterException("The name of the dimension cannot be null");
		}
		DataFlowStructure dsd = getDataFlowStructure(provider, dataflow);
		Dimension dim = dsd.getDimension(dimension);
		Codelist codes = null;
		if (dim != null)
		{
			codes = dim.getCodeList();
			if (codes != null && !codes.isEmpty())
				return codes;
			else
			{
				// this is a 2.1 provider
				LOGGER.finer("Codelist for " + provider + ", " + dataflow + ", " + dimension + " not cached.");
				codes = getClient(provider).getCodes(dsd.getDimension(dimension).getCodeList());
				if (codes != null)
					dim.setCodeList(codes);
				else
					throw new SdmxXmlContentException(
							"Could not get codes for '" + dataflow + "' in provider: '" + provider + "'");
			}
		}
		else
			throw new SdmxXmlContentException(
					"The dimension: '" + dimension + "' does not exist in dataflow: '" + dataflow + "'");

		return codes;
	}

	public static Dataflow getFlow(String provider, String dataflow) throws SdmxException
	{
		if (provider == null || provider.trim().isEmpty())
		{
			LOGGER.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (dataflow == null || dataflow.trim().isEmpty())
		{
			LOGGER.severe("The name of the dataflow cannot be null");
			throw new SdmxInvalidParameterException("The name of the dataflow cannot be null");
		}
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
		if (provider == null || provider.trim().isEmpty())
		{
			LOGGER.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}

		Map<String, Dataflow> flows = null;
		Provider p = getProvider(provider);
		flows = p.getFlows();
		if (flows == null || flows.size() == 0)
		{
			LOGGER.fine("Flows for " + provider + " not cached. Calling Provider.");
			flows = getClient(provider).getDataflows();
			if (flows != null && flows.size() != 0)
			{
				p.setFlows(flows);
				
			}
			else
				throw new SdmxXmlContentException("Could not get dataflows from provider: '" + provider + "'");
		}
		return filterFlows(flows, pattern);
	}

	public static PortableDataSet<Double> getTimeSeriesTable(String provider, String dataflow, String tsKey, String filter, 
			String startTime, String endTime, 
			boolean serieskeysonly, String updatedAfter, boolean includeHistory)
			throws SdmxException, DataStructureException
	{
		return new PortableDataSet<>(getTimeSeries(provider, dataflow, tsKey, filter, startTime, endTime, serieskeysonly, updatedAfter, includeHistory));
	}

	//shortcut for v2 API
	public static List<PortableTimeSeries<Double>> getTimeSeries(String provider, String tsKey, String startTime, String endTime) throws SdmxException
	{
		return getTimeSeries(provider, null, tsKey, null, startTime, endTime, false, null, false);
	}

	//full featured, valid for v2 and v3
	public static List<PortableTimeSeries<Double>> getTimeSeries(String provider, String dataflow, String tsKey, String filter, 
			String startTime, String endTime, 
			boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException
	{
		if (provider == null || provider.trim().isEmpty())
		{
			LOGGER.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if ((tsKey == null || tsKey.trim().isEmpty()) && (dataflow == null || dataflow.trim().isEmpty()))
		{
			LOGGER.severe("Either the ts key or the dataflow must have valid values");
			throw new SdmxInvalidParameterException("Either the ts key or the dataflow must have valid values");
		}

		List<PortableTimeSeries<Double>> result = new ArrayList<>(); //SDMX 2.0 did not provide a way to specify multiple series keys
		if(tsKey != null && !tsKey.isEmpty()){
			for (String keyId : tsKey.trim().split("\\s*;\\s*"))
				result.addAll(getSingleTimeSeries(provider, dataflow, keyId, filter, startTime, endTime, 
						serieskeysonly, updatedAfter, includeHistory));
		}
		else{
			result = getSingleTimeSeries(provider, dataflow, null, filter, startTime, endTime, 
					serieskeysonly, updatedAfter, includeHistory);
		}
		return (result);
	}

	private static List<PortableTimeSeries<Double>> getSingleTimeSeries(String provider, String dataflow, String tsKey, String filter, 
			String startTime, String endTime, 
			boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException
	{
		if (provider == null || provider.trim().isEmpty())
		{
			LOGGER.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if ((tsKey == null || tsKey.trim().isEmpty()) && (dataflow == null || dataflow.trim().isEmpty()))
		{
			LOGGER.severe("Either the ts key or the dataflow must have valid values");
			throw new SdmxInvalidParameterException("Either the ts key or the dataflow must have valid values");
		}
	
		List<PortableTimeSeries<Double>> result = null;
	
		if(dataflow == null || dataflow.isEmpty()){
			String[] tokens = extractFlowAndResource(tsKey);
			dataflow = tokens[0];
			tsKey = tokens[1];
		}
		
		Dataflow df = getFlow(provider, dataflow);
		DataFlowStructure dsd = getDataFlowStructure(provider, dataflow);
		result = getClient(provider).getTimeSeries(df, dsd, tsKey, filter, startTime, endTime, serieskeysonly, updatedAfter, includeHistory);
		if (result == null || result.size() == 0)
			throw new SdmxXmlContentException(
					"The query: key=" +tsKey + " and filter="+ filter + " did not match any time series on the provider for dataflow: " + dataflow);
		return result;
	}

	public static String getDataURL(String provider, String tsKey, String start, String end, boolean seriesKeysOnly,
			String updatedAfter, boolean includeHistory) throws SdmxException
	{
		if (provider == null || provider.trim().isEmpty())
		{
			LOGGER.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (tsKey == null || tsKey.trim().isEmpty())
		{
			LOGGER.severe("The tsKey cannot be null");
			throw new SdmxInvalidParameterException("The tsKey cannot be null");
		}

		String[] tokens = extractFlowAndResource(tsKey);
		String dataflow = tokens[0];
		String resource = tokens[1];
		Dataflow df = getFlow(provider, dataflow);

		String result = getClient(provider).buildDataURL(df, resource, start, end, seriesKeysOnly, updatedAfter,
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

	public static String dumpTimeSeries(String provider, String dataflow, String id, String filter, String startTime, String endTime)
			throws SdmxException, DataStructureException
	{
		if (provider == null || provider.trim().isEmpty())
		{
			LOGGER.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (id == null || id.trim().isEmpty())
		{
			LOGGER.severe("The id cannot be null");
			throw new SdmxInvalidParameterException("The id cannot be null");
		}
		if (!SDMXClientFactory.getProviders().containsKey(provider))
		{
			throw new SdmxInvalidParameterException("The provider : " + id + " does not exist.");
		}
		String result = "";
		if (!Configuration.isTable())
		{
			// Do it as a list of time series
			List<PortableTimeSeries<Double>> ts = getTimeSeries(provider, dataflow, id, filter, startTime, endTime, false, null, false);
			result = dumpTimeSeriesList(ts);
		}
		else
		{
			// do it as a table
			result = getTimeSeriesTable(provider, dataflow, id, filter, startTime, endTime, false, null, false).toString();
		}
		return result;
	}

	private static Provider getProvider(String providerName) throws SdmxException
	{
		if (providerName == null || providerName.trim().isEmpty())
		{
			LOGGER.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		Provider provider = SDMXClientFactory.getProviders().get(providerName);

		// TODO: move this check to SDMXClientFactory
		if (provider == null)
			throw new SdmxUnknownProviderException(providerName);

		return provider;
	}

	private static GenericSDMXClient getClient(String provider, String user, String password) throws SdmxException
	{
		final String sourceMethod = "getClient";
		LOGGER.entering(sourceClass, sourceMethod);
		if (provider == null || provider.trim().isEmpty())
		{
			LOGGER.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		GenericSDMXClient client = clients.get(provider);
		if (client == null)
		{
			LOGGER.finer("Client for " + provider + " does not exist. I will create it.");
			client = (GenericSDMXClient) SDMXClientFactory.createClient(provider);
			if (client.needsCredentials())
				handlePassword(client, user, password);

			clients.put(provider, client);
		}
		LOGGER.exiting(sourceClass, sourceMethod);
		return client;

	}

	private static GenericSDMXClient getClient(String provider) throws SdmxException
	{
		if (provider == null || provider.trim().isEmpty())
		{
			LOGGER.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}

		return getClient(provider, null, null);

	}

	private static void handlePassword(GenericSDMXClient client, String user, String pw) throws SdmxException
	{
		if (client == null)
		{
			LOGGER.severe("The client cannot be null");
			throw new SdmxInvalidParameterException("The client cannot be null");
		}
		if (client.needsCredentials())
		{
			if (user == null || pw == null)
			{
				final JFrame frame = new JFrame("Authentication");
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				LoginDialog loginDlg = new LoginDialog(frame, client.getName() + " Authentication");
				loginDlg.setVisible(true);
				client.setCredentials(loginDlg.getUsername(), loginDlg.getPassword());
				frame.dispose();
			}
			else
			{
				client.setCredentials(user, pw);
			}
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
			LOGGER.fine("Pattern:"+ pattern);
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
		else
			LOGGER.fine("No flows to filter");
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
