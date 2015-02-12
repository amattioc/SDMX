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



public class ILOClientTest {
	public static void main(String[] args) throws SdmxException{
		System.err.println(SdmxClientHandler.getFlows("ILO", null));
//		System.err.println(SdmxClientHandler.getDSDIdentifier("ILO", "DF_YI_ALL_EMP_TEMP_SEX_AGE_NB"));
//		System.err.println(SdmxClientHandler.getDataFlowStructure("ILO", "DF_YI_ALL_EMP_TEMP_SEX_AGE_NB"));
//		System.err.println(SdmxClientHandler.getDimensions("ILO", "DF_YI_ALL_EMP_TEMP_SEX_AGE_NB"));
//		System.err.println(SdmxClientHandler.getCodes("ILO", "DF_YI_ALL_EMP_TEMP_SEX_AGE_NB", "COUNTRY"));
//		System.err.println(SdmxClientHandler.getTimeSeries("ILO", "DF_YI_ALL_EMP_TEMP_SEX_AGE_NB/YI.MEX.A.463.EMP_TEMP_NB.SEX_F.AGE_10YRBANDS_TOTAL", null, null));
//		System.err.println(SdmxClientHandler.getDimensions("ILO", "DF_YI_ALL_EMP_TEMP_SEX_AGE_NB"));
	}
}
