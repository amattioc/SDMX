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
package it.bancaditalia.oss.sdmx.util;

import static it.bancaditalia.oss.sdmx.client.Provider.AuthenticationMethods.BASIC;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.SortedMap;

import it.bancaditalia.oss.sdmx.client.Provider.AuthenticationMethods;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;

/**
 * @author Attilio Mattiocco
 *
 */
public class GetProviders
{

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		try
		{
			StringBuffer buf = new StringBuffer();
			SortedMap<String, AuthenticationMethods> providers = SdmxClientHandler.getProviders();
			boolean first = true;
			for (Entry<String, AuthenticationMethods> provider : providers.entrySet())
			{
				if (!first)
					buf.append(",");
				buf.append(provider.getKey() + "{" + (provider.getValue() == BASIC ? "1" : "0") + "}");
				first = false;
			}
			System.out.println(buf);

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
