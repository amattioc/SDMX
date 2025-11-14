function setProviderCredentials(provider, user, pw)  
	% Set (or reset) the provider credentials.
    %
    % Usage: setProviderCredentials(provider, user, pw)
    %
    % provider STRING - provider name
    % user STRING - username
    % pw STRING - password
    %
    % setProviderCredentials('provider', 'my_user', ''my_password)
    % setProviderCredentials('provider', '', '') 
	%
	% #############################################################################################
	% Copyright 2025,2025 Bank Of Italy
	%
    
    initClasspath;    
    
    if nargin <3
        error('\nUsage: setProviderCredentials(provider, user, pw)\n');    
    end

    try
      it.bancaditalia.oss.sdmx.client.SdmxClientHandler.setCredentials(provider, user, pw);
    catch mexp
      error('Error setting credentials:\n %s', mexp.message);      
    end   
end
