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
package it.bankitalia.reri.sia.util;

import it.bankitalia.reri.sia.sdmx.client.SdmxClientHandler;

import java.io.IOException;

/**
 * @author Attilio Mattiocco
 *
 */
public class GetTimeSeries {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws SdmxException 
	 */
	public static void main(String[] args) throws IOException, SdmxException {
		if(args.length < 2 || args.length > 6 || args.length == 5){
			System.out.println("usage: GetTimeSeries <provider> <query> [start] [end] [username password]");
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
			if(args.length > 4){
				String user = args[4];
				String pw = args[5];
				SdmxClientHandler.setCredentials(provider, user, pw);
			}
			System.out.println(SdmxClientHandler.dumpTimeSeries(provider, query, start, end));
		}

	}

}



