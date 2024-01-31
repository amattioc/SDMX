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
 * needsCredentials:    set this to true if the provider needs authentication
 * needsURLEncoding:    set this to true if the provider needs URL encoding
 * supportsCompression: set this to true if the provider supports stream compression
 * description:         a text description for the provider
 * sdmxVersion:         the version of the sdmx rest API (V2 or V3)
 *
 */

program addProvider
	version 13
	args name endpoint needsCredentials needsURLEncoding supportsCompression description
	javacall it.bancaditalia.oss.sdmx.client.StataClientHandler addProvider, args("`name'" "`endpoint'" "`needsCredentials'" "`needsURLEncoding'" "`supportsCompression'" "`description'"  "`sdmxVersion'")
end
