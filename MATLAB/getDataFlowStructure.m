function dfs = getDataFlowStructure(provider, dataflow)
	% Get the data flow structure
	%
	% Usage: getDataFlowStructure(provider, dataflow)
	%
	% Arguments
	%
	% provider: the name of the SDMX data provider
	% dataflow: the dataflow to be analyzed   
	%
	% #############################################################################################
	% Copyright 2010,2014 Bank Of Italy
	%
	% Licensed under the EUPL, Version 1.1 or - as soon they
	% will be approved by the European Commission - subsequent
	% versions of the EUPL (the "Licence");
	% You may not use this work except in compliance with the
	% Licence.
	% You may obtain a copy of the Licence at:
	%
	%
	% http://ec.europa.eu/idabc/eupl
	%
	% Unless required by applicable law or agreed to in
	% writing, software distributed under the Licence is
	% distributed on an "AS IS" basis,
	% WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
	% express or implied.
	% See the Licence for the specific language governing
	% permissions and limitations under the Licence.
	% 

    initClasspath;
    
    if nargin <2
        error(sprintf([ '\nUsage: getDataFlowStructure(provider, dataflow)\n\n' ...
                    'Arguments\n\n' ...
                    'provider: the name of the SDMX data provider\n' ...
                    'dataflow: the dataflow to be analyzed\n' ...
                    ]));
    end
    %try java code
    try
        jdfs = it.bancaditalia.oss.sdmx.client.SdmxClientHandler.getDataFlowStructure(provider, dataflow);
    catch mexp
        error(['SDMX getDataFlowStructure() error:\n' mexp.message]);             
    end
	
    dfs = struct('id', char(jdfs.getId()), ...
                 'agency', char(jdfs.getAgency()), ...
                 'version', char(jdfs.getVersion()), ...
                 'name', char(jdfs.getName()), ...
                 'timeDimension', char(jdfs.getTimeDimension()), ...
                 'primaryMeasure', char(jdfs.getMeasure()), ...
                 'dimensions', mapDimensions(jdfs.getDimensions()), ...
                 'attributes', mapAttributes(jdfs.getAttributes()));
end

function mds = mapDimensions(jds)
    mds = cell(jds.size(), 1);
    for di = 1:length(mds)
        mds{di} = mapMetaElement(jds.get(di - 1));
    end
    mds = cell2mat(mds);
end

function md = mapMetaElement(jd)
    md = struct('id', char(jd.getId()), ...
                'name', char(jd.getName()), ...
                'codelist', mapCodeList(jd.getCodeList()));
end

function mcl = mapCodeList(jcl)
    if isempty(jcl)
        mcl = containers.Map();
    else
        mcl = containers.Map( ...
            cell(jcl.keySet().toArray()), ...
            cell(jcl.values().toArray()));
    end
end

function mas = mapAttributes(jas)
    mas = cell(jas.size(), 1);
    for di = 1:length(mas)
        mas{di} = mapMetaElement(jas.get(di - 1));
    end
    mas = cell2mat(mas);
end
