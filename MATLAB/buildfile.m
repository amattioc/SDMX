function plan = buildfile

% Create a plan from task functions
plan = buildplan(localfunctions);

% Make the "archive" task the default task in the plan
plan.DefaultTasks = "archive";

% Make the "archive" task dependent on the "check" and "test" tasks
plan("archive").Dependencies = ["check" "test"];
end

function checkTask(~)
% Identify code issues
issues = codeIssues;
assert(isempty(issues.Issues),formattedDisplayText( ...
    issues.Issues(:,["Location" "Severity" "Description"])))
end

function testTask(~)

% Run unit tests
import matlab.unittest.TestRunner;
import matlab.unittest.TestSuite;
import matlab.unittest.plugins.TestReportPlugin;
import matlab.unittest.plugins.CodeCoveragePlugin
import matlab.unittest.plugins.codecoverage.CoverageReport
import matlab.unittest.plugins.codecoverage.CoverageResult

suite = TestSuite.fromProject(currentProject);

runner = TestRunner.withTextOutput;
htmlFolder = 'tests/reports/results';
plugin = TestReportPlugin.producingHTML(htmlFolder);
runner.addPlugin(plugin);

sourceCodeFolder = "tbx";
reportFolder = "tests/reports/coverageReport";
reportFormat = CoverageReport(reportFolder);
format = CoverageResult;
plugin = CodeCoveragePlugin.forFolder(sourceCodeFolder,"Producing",[reportFormat,format], ...
    IncludingSubfolders = true);
runner.addPlugin(plugin)

results = runner.run(suite);

assertSuccess(results);

end

function archiveTask(~)
description = sprintf('The SDMX Connectors project has been developed with the aim of covering the ''last mile'' in SDMX implementations.\n\nIn particular, the focus of the project is to provide the end user a set of plugins that can be easily installed in the most popular data analysis tools (e.g. R, MATLAB, SAS, STATA, Excel, etc.) allowing a direct access to SDMX data from the tool.\nProject site: \n\nhttps://github.com/amattioc/SDMX');
% Create ZIP file
opts = matlab.addons.toolbox.ToolboxOptions('tbx', "50de8506-6d87-47ee-aa8a-2c7f2e56d761", ...
    ToolboxName    = 'MatSDMX', ...
    ToolboxVersion = '4.0.0', ...
    AuthorName     = 'Attilio Mattiocco', ...
    Summary        = 'Provides functions to retrieve data and metadata from providers that disseminate data by means of SDMX web services.', ...
    Description    = description, ...
    ToolboxJavaPath = "tbx/lib/SDMX.jar", ...
    ToolboxGettingStartedGuide = 'tbx/doc/GettingStarted.mlx', ...
    OutputFile = 'releases/SDMX.mltbx', ...
    MinimumMatlabRelease = 'R2023a');
matlab.addons.toolbox.packageToolbox(opts)
end