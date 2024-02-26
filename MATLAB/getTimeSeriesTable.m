function tt = getTimeSeriesTable(provider, id, startTime, endTime)  
	% Extract a list of time series based on an input pattern and return a
	% table object with the results
    %
    % Usage: getTimeSeriesTable(provider, id, startTime, endTime)
	%
	% Arguments
	%
	% provider: the name of the SDMX data provider
	% id:   the key of the time series (can contain wildcards, 
	%       e.g.    'EXR.M.USD.EUR.SP00.A' or 
	%               'EXR.M:*:EUR:SP00:A')
	%
	% startTime: the first observation time  (optional)
	% endTime:   the last observation time   (optional)
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
    
    %deal with arguments
    
    initClasspath;
    
    if nargin <2
        error('\nUsage: getTimeSeriesTable(provider, id, startTime, endTime)\n\n');
    end    
    if nargin < 4
        endTime = '';
    end    
    if nargin < 3
        startTime = '';
    end
       
    %try java code
    try
        result = it.bancaditalia.oss.sdmx.client.SdmxClientHandler.getTimeSeriesTable(provider, id, startTime, endTime, false, '', false); 
	catch mexp
        error('SDMX getTimeSeries() error:\n %s', mexp.message);            
    end
    
    %verify returned class type
    if (~ isa(result, 'it.bancaditalia.oss.sdmx.api.PortableDataSet')) 
        error('SDMX getTimeSeries() returned wrong class: %s', class(result))
    end
    
    %convert
	tt = convertTable(result, false);
    
end

