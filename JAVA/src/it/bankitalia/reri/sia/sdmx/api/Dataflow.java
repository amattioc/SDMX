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
	private String dsd;
	private String dsdAgency;
	private String dsdVersion;

	
	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getName() {
		return name;
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


	public String getDsd() {
		return dsd;
	}


	public void setDsd(String dsd) {
		this.dsd = dsd;
	}


	public String getDsdAgency() {
		return dsdAgency;
	}


	public void setDsdAgency(String dsdAgency) {
		this.dsdAgency = dsdAgency;
	}


	public String getDsdVersion() {
		return dsdVersion;
	}


	public void setDsdVersion(String dsdVersion) {
		this.dsdVersion = dsdVersion;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Dataflow [id=").append(id)
				.append(",\n name=").append(name)
				.append(",\n agency=").append(agency)
				.append(",\n version=").append(version)
				.append(",\n dsd=").append(dsd)
				.append(",\n dsdAgency=").append(dsdAgency)
				.append(",\n dsdVersion=").append(dsdVersion)
				.append("]");
		return builder.toString();
	}
	
	

}
