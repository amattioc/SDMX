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

import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;



public class ABSClientTest {
	public static void main(String[] args) throws SdmxException{
		System.err.println(SdmxClientHandler.getFlows("ABS", null));
		System.err.println(SdmxClientHandler.getDSDIdentifier("ABS", "ABS_C16_T04_LGA"));
		System.err.println(SdmxClientHandler.getDimensions("ABS", "ABS_C16_T04_LGA"));
		System.err.println(SdmxClientHandler.getDataFlowStructure("ABS", "ABS_C16_T04_LGA"));
		System.err.println(SdmxClientHandler.getCodes("ABS", "ABS_C16_T04_LGA", "SEX_ABS"));
		System.err.println(SdmxClientHandler.getTimeSeries("ABS", "ABS_C16_T04_LGA/1.TT.1+2...", "2016", "2016"));

		System.err.println(SdmxClientHandler.getFlows("ABS2", null));
		System.err.println(SdmxClientHandler.getDSDIdentifier("ABS2", "ABS_C16_T04_SA"));
		System.err.println(SdmxClientHandler.getDimensions("ABS2", "ABS_C16_T04_SA"));
		System.err.println(SdmxClientHandler.getDataFlowStructure("ABS2", "ABS_C16_T04_SA"));
		System.err.println(SdmxClientHandler.getCodes("ABS2", "ABS_C16_T04_SA", "SEX_ABS"));
		System.err.println(SdmxClientHandler.getTimeSeries("ABS2", "ABS_C16_T04_SA/1.TT.1+2...", "2016", "2016"));
	}
}
