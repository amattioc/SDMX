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

/*
 *	Execute addProvider call. Parameters:
 *	
 * name:                the name you want to set for the provider
 * endpoint:            the URL of the provider web service
 * needsCredentials:    it can be one of "1", "0", "BASIC", "BEARER", "NONE". Note that "1" means "BASIC" and "0" means "NONE" for backward compatibility
 * needsURLEncoding:    set this to true if the provider needs URL encoding
 * supportsCompression: set this to true if the provider supports stream compression
 * description:         a text description for the provider
 * sdmxVersion:         the version of the sdmx rest API (V2 or V3)
 * supportsAvailability: set this to true if the provider supports availability queries
 *
 */

program addProvider
	version 17
	args name endpoint needsCredentials needsURLEncoding supportsCompression description sdmxVersion supportsAvailability
	
	java, shared(BItools): SaddProvider("`name'", "`endpoint'", "`needsCredentials'", "1".equals("`needsURLEncoding'"), "1".equals("`supportsCompression'"), "1".equals("`supportsAvailability'"), "`description'", "V3".equals("`sdmxVersion'"));
end

quietly initSDMX

java, shared(BItools):
	import static it.bancaditalia.oss.sdmx.api.SDMXVersion.V2;
	import static it.bancaditalia.oss.sdmx.api.SDMXVersion.V3;
	
	import it.bancaditalia.oss.sdmx.util.Configuration;
	import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
	
	import it.bancaditalia.oss.sdmx.client.Provider.AuthenticationMethods;
	
	import java.util.logging.Logger;
	
	void SaddProvider(String name, String endpoint, String needsCredentials, boolean needsURLEncoding, boolean supportsCompression, boolean supportsAvailability, String description, boolean isV3)
	{
		Logger logger = Configuration.getSdmxLogger();
	
		if (name.isEmpty() || endpoint.isEmpty())
		{
			logger.info("The provider name and endpoint are required.");
			return;
		}
		AuthenticationMethods auth = null;
		if (needsCredentials.equals("1") || needsCredentials.equals("BASIC")){
			auth = AuthenticationMethods.BASIC;
		}
		else if (needsCredentials.equals("BEARER")) {
			auth = AuthenticationMethods.BEARER;
		}
		else{
			auth = AuthenticationMethods.NONE;
		}
		try
		{
			SdmxClientHandler.addProvider(name, endpoint, auth, needsURLEncoding, supportsCompression, supportsAvailability, description, isV3 ? V3 : V2);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
end
