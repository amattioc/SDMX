package it.bancaditalia.oss.sdmx.api;

/**
 * This is a Java container for identifiable artifacts.
 * 
 * @author Valentino Pinna
 * 
 */
public class SDMXReference
{
	private final String id;
	private final String agency;
	private final String version;

	public SDMXReference(SDMXReference other)
	{
		this.id = other != null ? other.id : null;
		this.agency = other != null ? other.agency : null;
		this.version = other != null ? other.version : null;
	}
	
	public SDMXReference(String id, String agency, String version)
	{
		this.id = id;
		this.agency = agency;
		this.version = version;
	}

	public String getId()
	{
		return id;
	}

	public String getAgency()
	{
		return agency;
	}

	public String getVersion()
	{
		return version;
	}

	/**
	 * @return The full identifier of this dataflow in the form "agency/id/version".
	 */
	public String getFullIdentifier()
	{
		return getFullIdWithSep('/');
	}

	protected String getFullIdWithSep(char sep)
	{
		String fullId = id;
		if (agency != null)
			fullId = agency + sep + fullId;
		if (version != null)
			fullId = fullId + sep + version;
		return fullId;
	}
	
	@Override
	public String toString()
	{
		return getFullIdentifier();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((agency == null) ? 0 : agency.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SDMXReference other = (SDMXReference) obj;
		if (agency == null)
		{
			if (other.agency != null)
				return false;
		}
		else if (!agency.equals(other.agency))
			return false;
		if (id == null)
		{
			if (other.id != null)
				return false;
		}
		else if (!id.equals(other.id))
			return false;
		if (version == null)
		{
			if (other.version != null)
				return false;
		}
		else if (!version.equals(other.version))
			return false;
		return true;
	}
}
