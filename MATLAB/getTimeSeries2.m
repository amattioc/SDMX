function list = getTimeSeries2(provider, dataflow, key, filter, startTime, endTime, attributes, measures)  
	% Extract a table of time series based on an input pattern
    %
    % Usage: getTimeSeries(provider, id, startTime, endTime)
	%
	% Arguments
	%
	% provider: the name of the SDMX data provider
	% dataflow: the name of the dataflow
	% key:   the key of the time series according to sdmx rest v2
    % (optional)
    % (can contain wildcards, 
	%       e.g.    'M.USD.EUR.SP00.A' or 
	%               'M.*.EUR.SP00.A')
	%
    % filter: a filter on dimensions, attributes of measures (optional)
	% startTime: the first observation time  (optional)
	% endTime:   the last observation time   (optional)
	% attributes:   the attributes to be returned (optional, 'all' default, 'none' for no attributes)
    % measures:   the measures to be returned (optional, 'all' default, 'none' for no measures)
    %
	% #############################################################################################
	% Copyright 2024,2024 Bank Of Italy
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
        error('\nUsage: getTimeSeries2(provider, dataflow, key, filter, startTime, endTime, attributes, measures)\n\n');
    end    
    if nargin < 8
        measures = 'all';
    end    
    if nargin < 7
        attributes = 'all';
    end
    if nargin < 6
        endTime = '';
    end
    if nargin < 5
        startTime = '';
    end
    if nargin < 4
        filter = 'all';
    end
    if nargin < 3
        key = 'all';
    end
    %try java code
    try
        result = it.bancaditalia.oss.sdmx.client.SdmxClientHandler.getTimeSeries2(provider, dataflow, key, filter, startTime, endTime, attributes, measures, '', false); 
	catch mexp
        error('SDMX getTimeSeries2() error:\n %s', mexp.message);      
    end
    
    %verify returned class type
    if (~ isa(result, 'java.util.List')) 
        error('SDMX getTimeSeries() returned class error.')
    end
    
    %convert
	list = convert(result);

end

