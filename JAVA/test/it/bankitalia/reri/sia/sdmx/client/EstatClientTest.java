/* Copyright 2010,2014 Bank Of Italy
*
* Licensed under the EUPL, Version 1.1 or – as soon they
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
package it.bankitalia.reri.sia.sdmx.client;

import it.bankitalia.reri.sia.util.SdmxException;



public class EstatClientTest {
	public static void main(String[] args) throws SdmxException{
//			client = SDMXClientFactory.createClient("ESTAT", null, null);
////			List<PortableTimeSeries> result = client.getTimeSeries("EXR/A.USD.EUR.SP00.A", null, null);
////			Map<String, String> result = client.getDataflows("DS-008573");
////			System.err.println(result);
			//System.err.println(SdmxClientHandler.getCodes("EUROSTAT", "aact_ali01", "FREQ"));
//			//System.err.println(client.getDSDIdentifier("DS-008573")[0]);
			//System.err.println(SdmxClientHandler.getDataFlowStructure("EUROSTAT", "DS-008573"));
			System.err.println(SdmxClientHandler.getTimeSeries("EUROSTAT","prc_hicp_midx/..CP00.EU+DE+FR", null, null));
		//System.err.println(SdmxClientHandler.getCodes("EUROSTAT", "aact_ali01", "FREQ"));
		
	}
}
