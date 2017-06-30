package it.bancaditalia.oss.sdmx.client.custom;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

public class FILE extends RestSdmxClient {
	
	public FILE(String name, URL endpoint) {
		super(name, endpoint, false, false, false);
	}
	
	@Override
	public Map<String, Dataflow> getDataflows() throws SdmxException {
		Map<String, Dataflow> result = new HashMap<String, Dataflow>();
		File basedir = new File(endpoint.getFile());
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
	protected String buildDataQuery(Dataflow dataflow, String resource, String startTime, String endTime, boolean serieskeysonly, String updatedAfter, boolean includeHistory)
	{
		String id = (dataflow.getFullIdentifier()+ "_" + resource).replaceAll("\\p{Punct}", "_");
		return endpoint.toString() + "data_" + id + ".xml";
	}
	
	@Override
	protected String buildDSDQuery(String dsd, String agency, String version, boolean full) throws SdmxException
	{
		return endpoint.toString() + "datastructure_" + agency + "_" + dsd + "_" + version.replaceAll("\\p{Punct}", "_") + ".xml";
	}
	
	@Override
	protected String buildFlowQuery(String dataflow, String agency, String version) throws SdmxException
	{
		return endpoint.toString() + "dataflow_" + agency + "_" + dataflow + "_" + version.replaceAll("\\p{Punct}", "_") + ".xml";
	}
	
	@Override
	protected String buildCodelistQuery(String codeList, String agency, String version) throws SdmxException 
	{
		return endpoint.toString() + "codelist_" + agency + "_" + codeList + "_" + version.replaceAll("\\p{Punct}", "_") + ".xml";
	}
	

}
