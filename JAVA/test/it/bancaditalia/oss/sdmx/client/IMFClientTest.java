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
import it.bancaditalia.oss.sdmx.util.SdmxException;



public class IMFClientTest {
	public static void main(String[] args) throws SdmxException{
		System.err.println(SdmxClientHandler.getDSDIdentifier("IMF", "PGI"));
		System.err.println(SdmxClientHandler.getDimensions("IMF", "PGI"));
		System.err.println(SdmxClientHandler.getDataFlowStructure("IMF", "PGI"));
		System.err.println(SdmxClientHandler.getCodes("IMF", "PGI", "REF_AREA"));
		System.err.println(SdmxClientHandler.getTimeSeries("IMF", "PGI.US....", "1980", "2010"));

	}
}
