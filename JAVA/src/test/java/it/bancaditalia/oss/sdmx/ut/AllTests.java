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
package it.bancaditalia.oss.sdmx.ut;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import it.bancaditalia.oss.sdmx.util.LanguagePriorityListTest;
import it.bancaditalia.oss.sdmx.util.RestQueryBuilderTest;

@RunWith(Suite.class)
@Suite.SuiteClasses( {	SdmxInterfaceTest.class,
						DimensionsAndCodesTest.class,
						DSDIdentifierTest.class,
						ISTATTest.class, 
						ECBTest.class, 
						OECDTest.class, 
						ESTATTest.class, 
						ILOTest.class, 
						IMF2Test.class, 
						IMFSDMXCentralTest.class, 
						InegiTest.class, 
						WBTest.class,
						INSEETest.class,
//						NBBTest.class,
						UNDATATest.class,
						WITSTest.class,
						ABSTest.class,
						UISTest.class,
						ProxyTest.class,
//						FileTest.class,
						LanguagePriorityListTest.class,
						RestQueryBuilderTest.class
						})
public class AllTests {
}

