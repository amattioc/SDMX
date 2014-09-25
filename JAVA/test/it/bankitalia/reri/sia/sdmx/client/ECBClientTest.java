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
package it.bankitalia.reri.sia.sdmx.client;

import it.bankitalia.reri.sia.util.SdmxException;



public class ECBClientTest {
	public static void main(String[] args) throws SdmxException{
		System.err.println(SdmxClientHandler.getFlows("ECB", "EXR*"));
//		System.err.println(SdmxClientHandler.getDSDIdentifier("ECB", "EX"));
		
//		System.err.println(SdmxClientHandler.getInstance().getDataFlowStructure("ECB", "EXR"));
//		System.err.pridntln(SdmxClientHandler.getDimensions("ECB", "EX"));
//		System.err.println(SdmxClientHandler.getTimeSeries("ECB", "EXR.A.USD.EUR.SP00.A", null, null));
		//System.err.println(SdmxClientHandler.getCodes("ECB", "EXR", "FREQ"));
		
	}
}
