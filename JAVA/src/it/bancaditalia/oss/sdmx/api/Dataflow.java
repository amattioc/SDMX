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

package it.bankitalia.reri.sia.sdmx.api;

/**
 * This is a Java container for dataflow information.
 * 
 * @author Attilio Mattiocco
 * 
 */
public class Dataflow {

	private String id;
	private String name; // the description
	private String agency;
	private String version;
	private DSDIdentifier dsdIdentifier;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return getFullIdentifier() + " ; " + name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAgency() {
		return agency;
	}

	public void setAgency(String agency) {
		this.agency = agency;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public DSDIdentifier getDsdIdentifier() {
		return dsdIdentifier;
	}

	public void setDsdIdentifier(DSDIdentifier dsdIdentifier) {
		this.dsdIdentifier = dsdIdentifier;
	}

	public String getFullIdentifier() {
		String dsd = id;
		if(agency!=null){
			dsd = agency + "," + dsd;
		}
		if(version!=null){
			dsd = dsd +  "," + version;
		}
		return dsd;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Dataflow [id=").append(getFullIdentifier())
			.append(",\n name=").append(name)
			.append(",\n dsd=").append(dsdIdentifier).append("]");
		return builder.toString();
	}
}
