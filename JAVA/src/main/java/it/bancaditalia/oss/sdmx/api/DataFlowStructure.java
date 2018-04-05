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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a Java container for a DSD. It contains the basic information for a DataFlow Structure Definition (name,
 * dimensions, codes), and a set of api to retrieve them.
 * 
 * @author Attilio Mattiocco
 *
 */
public class DataFlowStructure
{

	private String								id;
	private String								name;
	private String								agency;
	private String								version;
	private String								timeDimension;
	private String								primaryMeasure;

	// this is an ordered list (id, codelist). the position is got from the DSD
	// key: dimension id --> dimension
	private final Map<String, Dimension>		dimensions	= new HashMap<>();
	private final Map<String, SdmxAttribute>	attributes	= new HashMap<>();

	/**
	 * @return This dataflow structure id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @param id
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * @param name This dataflow structure name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return This dataflow structure name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return This dataflow structure agency
	 */
	public String getAgency()
	{
		return agency;
	}

	/**
	 * @param agency This dataflow structure agency
	 */
	public void setAgency(String agency)
	{
		this.agency = agency;
	}

	/**
	 * @return This dataflow structure version
	 */
	public String getVersion()
	{
		return version;
	}

	/**
	 * @param version This dataflow structure version
	 */
	public void setVersion(String version)
	{
		this.version = version;
	}

	/**
	 * @return A list of dimensions in this dataflow structure
	 */
	public List<Dimension> getDimensions()
	{
		// order by dsd
		Dimension[] result = new Dimension[dimensions.size()];
		for (Dimension dimension : dimensions.values())
			result[dimension.getPosition() - 1] = dimension;

		return Arrays.asList(result);
	}

	/**
	 * @return A list of attributes in this dataflow structure
	 */
	public List<SdmxAttribute> getAttributes()
	{
		return new ArrayList<>(attributes.values());
	}

	/**
	 * Puts a dimension into this structure.
	 * 
	 * @param dim The dimension which is to put.
	 */
	public void setDimension(Dimension dim)
	{
		this.dimensions.put(dim.getId(), dim);
	}

	/**
	 * Gets a dimension from this {@link DataFlowStructure} if it exists.
	 * 
	 * @param dimensionId A name of a dimension contained in this dataflow structure.
	 * @return The dimension object with given name in this dataflow structure, or null.
	 */
	public Dimension getDimension(String dimensionId)
	{
		return this.dimensions.get(dimensionId);
	}

	/**
	 * Sets the metadata and value of an attribute to this {@link DataFlowStructure}.
	 * 
	 * @param attr The attribute to set.
	 */
	public void setAttribute(SdmxAttribute attr)
	{
		this.attributes.put(attr.getId(), attr);
	}

	/**
	 * @param attrName The name of an attribute.
	 * @return The metadata associated with given attribute, or null.
	 */
	public SdmxAttribute getAttribute(String attrName)
	{
		return this.attributes.get(attrName);
	}

	/**
	 * @param timeDimension
	 */
	public void setTimeDimension(String timeDimension)
	{
		this.timeDimension = timeDimension;
	}

	/**
	 * Checks if a dimension is defined in this dataflow structure.
	 * 
	 * @param candidate A dimension identifier
	 * @return True if the dimension is defined in this dataflow structure
	 */
	public boolean isDimension(String candidate)
	{
		return dimensions.containsKey(candidate);
	}

	/**
	 * Returns the ordered position of a dimension in this dataflow structure
	 * 
	 * @param candidate A dimension identifier
	 * @return the ordered position of given dimension
	 * @throws NullPointerException if given dimension is not defined in this dataflow structure.
	 */
	public int getDimensionPosition(String candidate)
	{
		return dimensions.get(candidate).getPosition();
	}

	/**
	 * @return The identifier of the time dimension.
	 */
	public String getTimeDimension()
	{
		return timeDimension;
	}

	/**
	 * @return The full identifier of this dataflow structure in the form "agency/id/version".
	 */
	public String getFullIdentifier()
	{
		String dsd = id;
		if (agency != null)
		{
			dsd = agency + "/" + dsd;
		}
		if (version != null)
		{
			dsd = dsd + "/" + version;
		}
		return dsd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("DSD [id=").append(getFullIdentifier()).append(", name=").append(name).append(", dimensions=\n")
				.append(dimensions).append(", attributes=\n").append(attributes).append("]");
		return builder.toString();
	}

	/**
	 * @return The primary measure of this dataflow structure.
	 */
	public String getMeasure()
	{
		return primaryMeasure;
	}

	/**
	 * Sets the primary measure of this dataflow structure.
	 * 
	 * @param primaryMeasure The primary measure of this dataflow structure to set.
	 */
	public void setMeasure(String primaryMeasure)
	{
		this.primaryMeasure = primaryMeasure;
	}
}
