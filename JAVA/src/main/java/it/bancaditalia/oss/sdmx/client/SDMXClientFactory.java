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

import static it.bancaditalia.oss.sdmx.api.SDMXVersion.V2;
import static it.bancaditalia.oss.sdmx.client.Provider.AuthenticationMethods.BASIC;
import static it.bancaditalia.oss.sdmx.client.Provider.AuthenticationMethods.BEARER;
import static it.bancaditalia.oss.sdmx.client.Provider.AuthenticationMethods.NONE;

import java.lang.reflect.InvocationTargetException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import it.bancaditalia.oss.sdmx.api.SDMXVersion;
import it.bancaditalia.oss.sdmx.client.Provider.AuthenticationMethods;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxUnknownProviderException;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.SdmxProxySelector;

/**
 * <p>
 * Java Factory class for creating the Sdmx Clients.
 *
 * @author Attilio Mattiocco
 *
 */
public class SDMXClientFactory
{


	// read the configuration file
	static
	{
		providers = new TreeMap<>();
		logger = Configuration.getSdmxLogger();
		try
		{
			initExternalProviders();
		}
		catch (SdmxException e)
		{
			e.printStackTrace();
		}
	}

	private static final String sourceClass = SDMXClientFactory.class.getSimpleName();
	protected static Logger logger;
	private static NavigableMap<String, Provider> providers;

	/**
	 * Initialize the sdmx providers from the configuration file
	 * 
	 * @throws SdmxException
	 */
	private static void initExternalProviders() throws SdmxException
	{
		// external providers set in the configuration file
		String external = Configuration.getExternalProviders();
		if (external != null && !external.isEmpty())
		{
			String[] ids = external.trim().split("\\s*,\\s*");
			for (int i = 0; i < ids.length; i++)
				addExternalProvider(ids[i]);
		}
	}

	/**
	 * General method for creating an SdmxClient.
	 *
	 * @param name                The name of the provider to create.
	 * @param endpoint            the {@link URI} of the provider to create.
	 * @param needsCredentials    true if the provider needs authentication.
	 * @param needsURLEncoding    true if the provider needs the URL to be encoded.
	 * @param supportsCompression true if the provider supports HTTP compression
	 *                            features.
	 * @param description         The description of the provider
	 * 
	 * @throws SdmxException if there is an error creating the provider.
	 */
	public static void addProvider(String name, URI endpoint, AuthenticationMethods authMethod, boolean needsURLEncoding, boolean supportsCompression, boolean supportsAvailability, String description) throws SdmxException
	{
		addProvider(name, endpoint, authMethod, needsURLEncoding, supportsCompression, supportsAvailability, description, V2);
	}

	/**
	 * General method for creating an SdmxClient.
	 *
	 * @param name                The name of the provider to create.
	 * @param endpoint            the {@link URI} of the provider to create.
	 * @param needsCredentials    true if the provider needs authentication.
	 * @param needsURLEncoding    true if the provider needs the URL to be encoded.
	 * @param supportsCompression true if the provider supports HTTP compression
	 *                            features.
	 * @param description         The description of the provider
	 * @param sdmxVersion         the major version of the SDMX standard of this
	 *                            provider (SDMX_V2 or SDMX_V3)
	 * 
	 * @throws SdmxException if there is an error creating the provider.
	 */
	public static void addProvider(String name, URI endpoint, AuthenticationMethods authMethod, boolean needsURLEncoding, boolean supportsCompression, boolean supportsAvailability, String description, SDMXVersion sdmxVersion) throws SdmxException
	{
		Provider p = new Provider(name, endpoint, authMethod, needsURLEncoding, supportsCompression, supportsAvailability, description, sdmxVersion);
		providers.put(name, p);
	}

	/**
	 * Add a external provider and check whether the default values need to be
	 * overwritten with values defined in the configuration file.
	 * 
	 * @throws SdmxException
	 */
	private static void addExternalProvider(final String id) throws SdmxException
	{
		try
		{
			final String providerName = Configuration.getProperty("providers." + id + ".name", id);
			final String providerEndpoint = Configuration.getProperty("providers." + id + ".endpoint", (String) null);
			if (providerEndpoint != null && !providerEndpoint.isEmpty())
			{
				URI providerURL = new URI(providerEndpoint);
				AuthenticationMethods providerAuthMethod;
				switch (Configuration.getProperty("providers." + id + ".needsCredentials", "false").toLowerCase()) {
					case "true": case "basic": providerAuthMethod = BASIC; break;
					case "bearer": case "token": providerAuthMethod = BEARER; break;
					default: providerAuthMethod = NONE; break;
				}
					
				boolean providerNeedsURLEncoding = Configuration.getProperty("providers." + id + ".needsURLEncoding", false);
				boolean providerSupportsCompression = Configuration.getProperty("providers." + id + ".supportsCompression", false);
				boolean providerSupportsAvailability = Configuration.getProperty("providers." + id + ".supportsAvailability", false);
				String providerDescription = Configuration.getProperty("providers." + id + ".description", id);
				SDMXVersion providerSdmxVersion = Configuration.getProperty("providers." + id + ".sdmxversion", V2);
				addProvider(providerName, providerURL, providerAuthMethod, providerNeedsURLEncoding, providerSupportsCompression, providerSupportsAvailability, providerDescription, providerSdmxVersion);
			}
			else
			{
				logger.warning("No URL has been configured for the external provider: '" + id + "'. It will be skipped.");
				return;
			}
		}
		catch (URISyntaxException e)
		{
			logger.log(Level.SEVERE, "Exception. Class: {0} .Message: {1}", new Object[] { e.getClass().getName(), e.getMessage() });
			logger.log(Level.FINER, "", e);
		}
	}

	/**
	 * General method for creating an SdmxClient.
	 *
	 * @param providerName A non-null provider identification short name.
	 * @return The client.
	 * @throws SdmxException if there is an error creating the client.
	 */
	public static GenericSDMXClient createClient(String providerName) throws SdmxException
	{
		final String sourceMethod = "createClient";

		logger.entering(sourceClass, sourceMethod);
		logger.fine("Create an SDMX client for '" + providerName + "'");
		GenericSDMXClient client = null;
		Provider provider = providers.get(providerName);
		if (provider == null)
			throw new SdmxInvalidParameterException("The provider '" + providerName + "' is not available in this configuration.");

		try
		{
			// Try to find a custom provider if an appropriate class exists 
			Class<? extends GenericSDMXClient> clazz = Class.forName("it.bancaditalia.oss.sdmx.client.custom." + providerName).asSubclass(GenericSDMXClient.class);
			client = clazz.getConstructor(Provider.class).newInstance(provider);
		}
		catch (ClassNotFoundException e)
		{
			// No custom provider found, instantiate the generic SDMX client
			if (provider.getEndpoint().getScheme().toLowerCase().startsWith("http"))
				switch (provider.getSdmxVersion())
				{
					case V2: client = new RestSdmx21Client(provider); break;
					case V3: client = new RestSdmx30Client(provider); break;
					default: throw new SdmxInvalidParameterException("Unsupported SDMX REST API version: " + provider.getSdmxVersion());
				}
			else
				throw new SdmxInvalidParameterException("The protocol '" + provider.getEndpoint().getScheme() + "' is not supported.");
		}
		catch (NoSuchMethodException | IllegalAccessException e)
		{
			logger.severe("The provider implementation it.bancaditalia.oss.sdmx.client.custom." + providerName + " does not define a default constructor.");
			throw new SdmxUnknownProviderException(providerName, e);
		}
		catch (InstantiationException | InvocationTargetException e)
		{
			logger.severe("Could not instantiate provider implementation it.bancaditalia.oss.sdmx.client.custom." + providerName);
			throw new SdmxUnknownProviderException(providerName, e);
		}
		
		String hostname = provider.getEndpoint().getHost();

		// now set default proxy if necessary
		ProxySelector ps = ProxySelector.getDefault();
		if (hostname != null && ps != null && ps instanceof SdmxProxySelector)
			((SdmxProxySelector) ps).addToDefaultProxy(hostname);

		logger.exiting(sourceClass, sourceMethod);
		return client;
	}

	/**
	 * Get the list of all available SDMX Providers
	 * 
	 * @return A map of providers with keys as names and {@link Provider} instances
	 *         as values.
	 */
	public static NavigableMap<String, Provider> getProviders()
	{
		return providers;
	}
}
