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

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

/**
 * @author Attilio Mattiocco
 *
 */
public class GetTimeSeries {

	public static void main(String[] args) throws IOException{
		if(args.length < 2 || args.length > 6 || args.length == 5){
			System.err.println("usage: GetTimeSeries <provider> <query> [start] [end] [username password]");
			System.exit(-1); // wrong number of arguments
		}
		else{
			String provider = args[0].toUpperCase();
			String query = args[1];
			String start = null;
			String end = null;
			
			if(args.length > 2){
				start = args[2];
			}
			if(args.length > 3){
				end = args[3];
			}
			if(args.length == 6){
				String user = args[4];
				String pw;
				if (args.length == 5)
					try (SafeLineReader reader = new SafeLineReader(new InputStreamReader(System.in, Charset.forName("UTF-8"))))
					{
						pw = reader.readLine();
					}
				else
					pw = args[5];	// arg 5
				try {
					SdmxClientHandler.setCredentials(provider, user, pw);
				} catch (SdmxException e) {
					System.err.println(e.toString());
					System.exit(-2); // exception setting credentials
				}
			}
			try {
				String result = SdmxClientHandler.dumpTimeSeries(provider, query, start, end);
				System.out.println(result);
			} catch (Exception e) {
				System.err.println(e.toString());
				System.exit(-3); // exception calling get method
			}
		}

	}

}



