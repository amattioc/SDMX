package it.bancaditalia.oss.sdmx.client.custom;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxExceptionFactory;
import it.bancaditalia.oss.sdmx.exceptions.SdmxXmlContentException;
import it.bancaditalia.oss.sdmx.parser.v21.DataflowParser;
import it.bancaditalia.oss.sdmx.parser.v21.Sdmx21Queries;

public class FILE extends RestSdmxClient {
	
	public FILE(String name, URI endpoint) {
		super(name, endpoint, false, false, false);
	}
	
//	@Override
//	public Map<String, Dataflow> getDataflows() throws SdmxException {
//		Map<String, Dataflow> result = new HashMap<>();
//		File basedir;
//		try {
//			basedir = new File(endpoint.toURL().getFile());
//		} catch (MalformedURLException e) {
//			throw SdmxExceptionFactory.wrap(e);
//		}
//		String[] files = basedir.list(new FilenameFilter() {
//			
//			@Override
//			public boolean accept(File dir, String name) {
//				if(name.startsWith("dataflow_"))
//					return true;
//				else
//					return false;
//			}
//		});
//		for (int i = 0; i < files.length; i++) {
//			if(!files[i].isEmpty()){
//				int start = "dataflow_".length();
//				int end = files[i].length() - ".xml".length();
//				if(start < end && end > 0){
//					String dfId = files[i].substring(start, end);
//					Dataflow df = new Dataflow();
//					df.setId(dfId);
//					result.put(dfId, df);
//				}
//			}
//		}
//		
//		return result;
//	}
	
	@Override
	public Dataflow getDataflow(String dataflow, String agency, String version) throws SdmxException
	{
		Map<String, Dataflow> flows = getDataflows();
		Dataflow result = null;
		//System.err.println(flows);
		if (flows.size() >= 1)
			result  = flows.get(dataflow);
		else
			throw new SdmxXmlContentException("The query returned zero dataflows");
		return result;
	}

	@Override
	protected URL buildDataQuery(Dataflow dataflow, String resource, String startTime, String endTime, boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException
	{
		String id = (dataflow.getId() + "_" + resource);//.replaceAll("\\p{Punct}", "_");
		return ((Sdmx21Queries) new Sdmx21Queries(endpoint).addPath("data_" + id + ".xml")).buildSdmx21Query();
	}
	
	@Override
	protected URL buildDSDQuery(String dsd, String agency, String version, boolean full) throws SdmxException
	{
		return ((Sdmx21Queries) new Sdmx21Queries(endpoint).addPath("datastructure_" + dsd + ".xml")).buildSdmx21Query();
	}
	
	@Override
	protected URL buildFlowQuery(String dataflow, String agency, String version) throws SdmxException
	{
		return ((Sdmx21Queries) new Sdmx21Queries(endpoint).addPath("dataflow_all.xml")).buildSdmx21Query();
	}
	
	@Override
	protected URL buildCodelistQuery(String codeList, String agency, String version) throws SdmxException 
	{
		return ((Sdmx21Queries) new Sdmx21Queries(endpoint).addPath("codelist_" + codeList + ".xml")).buildSdmx21Query();
	}
	

}
