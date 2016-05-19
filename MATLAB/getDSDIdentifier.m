function dsd = getDSDIdentifier(provider, dataflow)
	% Get the key family for this dataflow
	%
	% Usage: getDSDIdentifier(provider, dataflow)
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
    error(sprintf([ '\nUsage: getDSDIdentifier(provider, dataflow)\n\n' ...
                    'Arguments\n\n' ...
                    'provider: the name of the SDMX data provider\n' ...
                    'dataflow: the dataflow to be analyzed\n' ...
                    ]));
    end
    %try java code
    try
        dsd = char(it.bancaditalia.oss.sdmx.client.SdmxClientHandler.getDSDIdentifier(provider, dataflow));
    catch mexp
        error(['SDMX getDSDIdentifier() error:\n' mexp.message]);             
    end
	
end
