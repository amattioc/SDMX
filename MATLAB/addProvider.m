function addProvider(name, endpoint, needsCredentials, needsURLEncoding, supportsCompression, description, sdmxVersion)
	% Add a new provider to the internal registry. The provider has to be 
    % fully compliant with the SDMX 2.1 specifications
    %
    % Usage: addProvider('ECB_TEST', 'http://sdw-wsrest.ecb.europa.eu/service', false, false, false, 'Sample ECB provider')
	%
	% Arguments
	%
	% name:                the name you want to set for the provider
	% endpoint:            the URL of the provider web service
	% needsCredentials:    set this to true if the provider needs authentication
	% needsURLEncoding:    set this to true if the provider needs URL encoding
    % supportsCompression: set this to true if the provider supports stream compression
    % description:         a text description for the provider
    % sdmxVersion:         the sdmx rest api version of the provider (V2 or V3)
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
        error(sprintf(['\nUsage: addProvider(name, endpoint, needsCredentials, needsURLEncoding, supportsCompression, description)\n\n' ...
            'Arguments\n\n' ...
            'name: the name of the provider\n' ...
            'endpoint:  the URL where the provider resides\n' ...
            'needsCredentials:   set this to TRUE if the user needs to authenticate to query the provider\n' ...
            'needsURLEncoding:   set this to TRUE if the provider does not handle character "+" in URLs\n' ...      
            'supportsCompression:   set this to TRUE if the provider is able to handle compression\n' ...      
            'description:   a brief text description of the provider\n' ...      
            'sdmxVersion:  the sdmx rest api version of the provider (V2 or V3)\n' ...      
        ]));
    end    
    if nargin < 7
        sdmxVersion = it.bancaditalia.oss.sdmx.api.SDMXVersion.V2;
    else
        if(strcmp(sdmxVersion, 'V2'))
            sdmxVersion = it.bancaditalia.oss.sdmx.api.SDMXVersion.V2;
        else
            sdmxVersion = it.bancaditalia.oss.sdmx.api.SDMXVersion.V3;
        end
    end    
    if nargin < 6
        description = '';
    end    
    if nargin < 5
        supportsCompression = false;
    end
    if nargin < 4
        needsURLEncoding = false;
    end
    if nargin < 3
        needsCredentials = false;
    end
       
    %try java code
    try
        it.bancaditalia.oss.sdmx.client.SdmxClientHandler.addProvider(name, endpoint, needsCredentials, needsURLEncoding, supportsCompression, description, sdmxVersion); 
	catch mexp
        error('SDMX addProvider() error:\n %s', mexp.message);            
    end
        
end

