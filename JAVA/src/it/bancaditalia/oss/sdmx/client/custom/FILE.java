package it.bancaditalia.oss.sdmx.client.custom;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxExceptionFactory;
import it.bancaditalia.oss.sdmx.parser.v21.Sdmx21Queries;
import it.bancaditalia.oss.sdmx.util.RestQueryBuilder;

public class FILE extends RestSdmxClient {
	
	public FILE(String name, URI endpoint) {
		super(name, endpoint, false, false, false);
	}
	
	@Override
	public Map<String, Dataflow> getDataflows() throws SdmxException {
		Map<String, Dataflow> result = new HashMap<String, Dataflow>();
		File basedir;
		try {
			basedir = new File(endpoint.toURL().getFile());
		} catch (MalformedURLException e) {
			throw SdmxExceptionFactory.wrap(e);
		}
		String[] files = basedir.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				if(name.startsWith("dataflow_"))
					return true;
				else
					return false;
			}
		});
		for (int i = 0; i < files.length; i++) {
			if(!files[i].isEmpty()){
				int start = "dataflow_".length();
				int end = files[i].length() - ".xml".length();
				if(start < end && end > 0){
					String dfId = files[i].substring(start, end);
					Dataflow df = new Dataflow();
					df.setId(dfId);
					result.put(dfId, df);
				}
			}
		}
		
		return result;
	}

	@Override
	protected URL buildDataQuery(Dataflow dataflow, String resource, String startTime, String endTime, boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException
	{
		String id = (dataflow.getFullIdentifier()+ "_" + resource).replaceAll("\\p{Punct}", "_");
		return ((Sdmx21Queries) new Sdmx21Queries(endpoint).addPath("data_" + id + ".xml")).buildSdmx21Query();
	}
	
	@Override
	protected URL buildDSDQuery(String dsd, String agency, String version, boolean full) throws SdmxException
	{
		return ((Sdmx21Queries) new Sdmx21Queries(endpoint).addPath("datastructure_" + agency + "_" + dsd + "_" + version.replaceAll("\\p{Punct}", "_") + ".xml")).buildSdmx21Query();
	}
	
	@Override
	protected URL buildFlowQuery(String dataflow, String agency, String version) throws SdmxException
	{
		return ((Sdmx21Queries) new Sdmx21Queries(endpoint).addPath("dataflow_" + agency + "_" + dataflow + "_" + version.replaceAll("\\p{Punct}", "_") + ".xml")).buildSdmx21Query();
	}
	
	@Override
	protected URL buildCodelistQuery(String codeList, String agency, String version) throws SdmxException 
	{
		return ((Sdmx21Queries) new Sdmx21Queries(endpoint).addPath("codelist_" + agency + "_" + codeList + "_" + version.replaceAll("\\p{Punct}", "_") + ".xml")).buildSdmx21Query();
	}
	

}
