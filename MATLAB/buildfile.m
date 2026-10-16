% leave the function name as "build"
function plan = buildfile()

import matlab.buildtool.tasks.*
% Build the MatSDMX toolbox by including everything under ./files/

sdmxPath = fullfile("files", "sdmx");

plan = buildplan(localfunctions);

plan("check") = CodeIssuesTask(sdmxPath);    % Task for identifying code issues

plan("test") = TestTask('./test', ...
    TestResults = 'test-results/matlab-results.xml', ...
    SourceFiles = sdmxPath, ...
    CodeCoverageResults='test-results/coverage.xml');           % Task for running tests

plan("test").Actions = [@setup, plan("test").Actions, @cleanup];

plan("archive").Dependencies = ["check" "test"];

plan.DefaultTasks = ["check" "test"];

end

function setup(~)
if isMATLABReleaseOlderThan('R2026b')
    sdmxPath = fullfile("files", "sdmx");
    addpath(sdmxPath);
end
end

function cleanup(~)
if isMATLABReleaseOlderThan('R2026b')
    sdmxPath = fullfile("files", "sdmx");
    rmpath(sdmxPath);
end
end

function archiveTask(~, version)

% update Contents.m
f = fileread("Contents.inc");
f = regexprep(f, '<X.Y.Z>', version);
f = regexprep(f, '<dd-mm-yyyy>', string(datetime('today', Format = 'dd-MMM-yyyy')));
writelines(f, fullfile("files", "sdmx", "Contents.m"),"WriteMode","overwrite")

opts = matlab.addons.toolbox.ToolboxOptions('files', '50de8506-6d87-47ee-aa8a-2c7f2e56d761');

% Include everything inside ./sdmx (recursively)
opts.ToolboxMatlabPath    = fullfile('files', 'sdmx');
opts.MinimumMatlabRelease = 'R2024a';
opts.OutputFile           = 'MatSDMX.mltbx';
opts.ToolboxName          = 'MatSDMX';
opts.ToolboxVersion       = version;
opts.AuthorName           = 'Attilio Mattiocco';
opts.AuthorEmail          = 'attilio.mattiocco@bancaditalia.it';
opts.AuthorCompany        = 'Banca d''Italia';
opts.Summary              = 'Provides functions to retrieve data and metadata from providers that disseminate data by means of SDMX web services.';
opts.Description          = strjoin([
    "The SDMX Connectors project has been developed with the aim of covering the 'last mile' in SDMX implementations."
    ""
    "In particular, the focus of the project is to provide the end user a set of plugins that can be easily installed in the most popular data"
    "analysis tools (e.g. R, MATLAB, SAS, STATA, Excel, etc.) allowing a direct access to SDMX data from the tool."
    ""
    "Project site:"
    ""
    "https://github.com/amattioc/SDMX"
    ]);

matlab.addons.toolbox.packageToolbox(opts)
end
