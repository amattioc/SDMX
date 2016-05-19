function flows = getFlows(provider, pattern)
	% Get the list of data flows for this provider
	%
	% Usage: getFlows(provider, pattern)
	%
	% Arguments
	%
	% provider: the name of the SDMX data provider
	% pattern: a wildcarded pattern to search (e.g. 'Exchange*').
	%          If null all flows are returned
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
    
    if nargin == 0
        error(sprintf(['\nUsage: getFlows(provider, pattern)\n\n' ...
                    'Arguments\n\n' ...
                        'provider: the name of the SDMX data provider\n' ...
                        'pattern: a wildcarded pattern to search (e.g. "Exchange*").' ...
                        'If null all flows are returned.\n' ...
                    ]));            
        
    end
    if nargin < 2
		pattern = '';
    end
    %get flows
    try
        result = it.bancaditalia.oss.sdmx.client.SdmxClientHandler.getFlows(provider, pattern);
    catch mexp
        error(['SDMX getFlows() error:\n' mexp.message]);             
    end 
    
    %verify returned class type
    if (~ isa(result,'java.util.Map'))
        error('SDMX getFlows() returned class error.')
    end
    
    %create Map
	ids = cell(result.keySet.toArray);
	description = cell(result.values.toArray);
	flows = containers.Map(ids, description);
    
end