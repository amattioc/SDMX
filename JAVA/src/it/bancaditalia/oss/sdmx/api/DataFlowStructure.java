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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is a Java container for a DSD. It contains the basic information for a 
 * DataFlow Structure Definition (name, dimensions, codes), and a set of api to retrieve them.
 * 
 * @author Attilio Mattiocco
 *
 */
public class DataFlowStructure {
	
	private String id;
	private String name;
	private String agency;
	private String version;
	private String timeDimension;
	
	// this is an ordered list (id, codelist). the position is got from the DSD 
	// key: dimension id --> dimension
	private Map<String, Dimension> dimensions;

	private Map<String, SdmxAttribute> attributes;

	public DataFlowStructure() {
		super();
		// key: dimension id --> dimension
		dimensions = new HashMap<String, Dimension>();
		attributes = new HashMap<String, SdmxAttribute>();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
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

	public List<Dimension> getDimensions() {
		Dimension[] result = null;
		Collection<Dimension> dims = dimensions.values();
		//order by dsd
		result = new Dimension[dims.size()];
		for (Iterator<Dimension> iterator = dims.iterator(); iterator.hasNext();) {
			Dimension dimension = (Dimension) iterator.next();
			result[dimension.getPosition()-1] = dimension;
		}
	
		return Arrays.asList(result);
	}

	public List<SdmxAttribute> getAttributes() {
		return new ArrayList<SdmxAttribute>(attributes.values());
	}

	public void setDimension(Dimension dim) {
		this.dimensions.put(dim.getId(), dim);
	}

	public Dimension getDimension(String dimensionName) {
		return this.dimensions.get(dimensionName);
	}

	public void setAttribute(SdmxAttribute attr) {
		this.attributes.put(attr.getId(), attr);
	}

	public SdmxAttribute getAttribute(String attrName) {
		return this.attributes.get(attrName);
	}

	public void setTimeDimension(String timeDimension) {
		this.timeDimension = timeDimension;
	}

	public boolean isDimension(String candidate) {
		return dimensions.containsKey(candidate);
	}
	
	public int getDimensionPosition(String candidate) {
		return dimensions.get(candidate).getPosition();
	}
	
	public String getTimeDimension() {
		return timeDimension;
	}

	public String getFullIdentifier() {
		String dsd = id;
		if(agency!=null){
			dsd = agency + "/" + dsd;
		}
		if(version!=null){
			dsd = dsd +  "/" + version;
		}
		return dsd;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DSD [id=").append(getFullIdentifier())
			.append(", name=").append(name)
			.append(", dimensions=\n").append(dimensions)
			.append(", attributes=\n").append(attributes).append("]");
		return builder.toString();
	}

	public String getMeasure() {
		return "OBS_VALUE";
	}

}
