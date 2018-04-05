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

import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;

import java.io.IOException;

/**
 * @author Attilio Mattiocco
 *
 */
public class SDMX {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if(args.length < 3){
			System.out.println("usage: SDMX <provider> <method> <query>");
		}
		else{
			String provider = args[0].toUpperCase();
			String method = args[1];
			String query = args[2];
			//SdmxClientHandler handler = SdmxClientHandler.getInstance();
			
			try {
				if("getDimensions".equalsIgnoreCase(method)){
					System.out.println(SdmxClientHandler.getDimensions(provider, query));			
				}
				else if("getFlows".equalsIgnoreCase(method)){
					System.out.println(SdmxClientHandler.getFlows(provider, query));
				}
				else if("getTimeSeries".equalsIgnoreCase(method)){
					System.out.println("Method removed. Use GetTimeSeries command.");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
