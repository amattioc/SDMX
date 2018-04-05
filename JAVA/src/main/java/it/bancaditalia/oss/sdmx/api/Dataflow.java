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

package it.bancaditalia.oss.sdmx.api;

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

	/**
	 * @return This dataflow id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return The dataflow description.
	 */
	public String getDescription() {
		return name;
	}

	/**
	 * @return The dataflow extended name in the form "agency/id/version ; description". 
	 */
	public String getName() {
		return getFullIdentifier() + " ; " + name;
	}

	/**
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return The dataflow agency
	 */
	public String getAgency() {
		return agency;
	}

	/**
	 * @param agency
	 */
	public void setAgency(String agency) {
		this.agency = agency;
	}

	/**
	 * @return The dataflow version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return The DSDIdentifier for this dataflow 
	 */
	public DSDIdentifier getDsdIdentifier() {
		return dsdIdentifier;
	}

	/**
	 * @param dsdIdentifier
	 */
	public void setDsdIdentifier(DSDIdentifier dsdIdentifier) {
		this.dsdIdentifier = dsdIdentifier;
	}

	/**
	 * @return The full identifier of this dataflow in the form "agency/id/version".
	 */
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Dataflow [id=").append(getFullIdentifier())
			.append(",\n name=").append(name)
			.append(",\n dsd=").append(dsdIdentifier).append("]");
		return builder.toString();
	}
}
