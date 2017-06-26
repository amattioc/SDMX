function addLocalProvider(name, directory, description)
	% Add a new local provider to the internal registry. The provider has to be 
    % fully compliant with the SDMX 2.1 specifications. The provider will
    % give access to local SDMX-ML 2.1 files 
    %
    % Usage: addProvider('ECB_LOCAL_TEST', '/local/dir', 'Test local provider')
	%
	% Arguments
	%
	% name:                the name you want to set for the provider
	% endpoint:            the directory containing the files
	% description:         a text description for the provider
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
        error(sprintf(['\nUsage: addProvider(name, directory, description)\n\n' ...
            'Arguments\n\n' ...
            'name: the name of the provider\n' ...
            'directory:  the directory where the files are stored\n' ...
            'description:   a brief text description of the provider\n' ...      
        ]));
    end    
    if nargin < 3
        description = '';
    end    
       
    %try java code
    try
        it.bancaditalia.oss.sdmx.client.SdmxClientHandler.addLocalProvider(name, directory, description); 
	catch mexp
        error(sprintf('SDMX addProvider() error:\n %s', mexp.message));            
    end
        
end

