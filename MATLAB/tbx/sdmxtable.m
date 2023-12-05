function tstable = sdmxtable(tslist, meta)  
	% Convert a list of time series into a MATLAB table. This function is
	% available only for MATLAB 2013B and later.
    %
    % Usage: sdmxtable(tslist, meta)
	%
	% Arguments
	%
	% tslist: the list of time series to be converted (as retrieved with
	% the getTimeSeries command
	% meta:   set this to true if you want metadata to be processed
	%
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
    
    %deal with version
    if verLessThan('matlab', '8.2.0')
        error('This function is not supported in MATLAB versions lower than 2013B.');
    end 
    
    %deal with arguments
    if nargin <1
        error([ 'Usage: sdmxtable(tslist, meta)\n ' ...
                    'Arguments\n ' ...
                    'tslist: the list of time series\n ' ...
                    'meta:   true to process metadata ']);
    end    
    if nargin < 2
        meta = false;
    end    
    
    tsNumber = length(tslist);
    %check tslist
    if tsNumber == 0 || ~iscell(tslist)
        error('The list of time series in input is empty or malformed');
    end
    
    tableList = cell(1, tsNumber);
    
    % retieve all attribute names in all time series
    allVariables = containers.Map;
    if meta == true
        % add metadata entries too
        for i = 1:tsNumber
            if( isa(tslist{i}, 'timeseries'))
                keys = tslist{i}.UserData.keys;
                for j = 1:length(keys)
                    allVariables(keys{j}) = 1;
                end
            end
        end
    end
    
    for i = 1:tsNumber
        if( ~isa(tslist{i}, 'timeseries') )
            fprintf('Element number %d in list is not a time series. Skipping', i); 
            continue;
        end
        nobs = length(tslist{i}.Data);
        varNames = cell({'ID', 'TIME_PERIOD', 'OBS_VALUE'});
        varValues = cell({cellstr(repmat(tslist{i}.Name, [nobs 1])), ...
                            tslist{i}.getabstime, ...
                            tslist{i}.Data});
        
        % now handle metadata
        if meta == true
            keys = allVariables.keys;
            for j = 1:length(keys)
                key = keys{j};
                try
                    value = tslist{i}.UserData(key);
                catch exception
                    % attribute not present, set empty
                    value = '';
                end
                
                % check if this is a ts level attribute. If so, repeat it
                % for every observation
                if ~iscell(value)
                    if isempty(value)
                        % workaround...
                        value = cellstr(repmat({''}, [nobs 1]));
                    else
                        value = cellstr(repmat(value, [nobs 1]));
                    end
                end
                varNames{3 + j} = key;
                varValues{3 + j} = value;

                % now add the variable to the global list
                allVariables(key) = 1;
            end
        end
        tmpTable = table(varValues{:});
        tmpTable.Properties.VariableNames = varNames;
        tableList{i} = tmpTable;
    end
    
    % now create the global table
    tstable = cell2table(cell(0, length(varNames)));
    tstable.Properties.VariableNames = varNames;
    for i = 1:length(tableList)
        tstable = [tstable; tableList{i}];
    end
end 

