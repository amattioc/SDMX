function plan = buildfile
import matlab.buildtool.tasks.CodeIssuesTask
import matlab.buildtool.tasks.TestTask

% Create a plan from task functions
plan = buildplan(localfunctions);

% Add the "check" task to identify code issues
plan("check") = CodeIssuesTask;

% Add the "test" task to run tests
plan("test") = TestTask;

% Make the "archive" task the default task in the plan
plan.DefaultTasks = "archive";

% Make the "archive" task dependent on the "check" and "test" tasks
plan("archive").Dependencies = ["check" "test"];
end

function archiveTask(~)
% Create ZIP file
filename = "source_" + ...
    string(datetime("now",Format="yyyyMMdd'T'HHmmss"));
zip(filename,"*")
end