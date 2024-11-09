function RUNSdata = importRUNS(filename, objectives)
%IMPORTFILE Import data from a text file
%  RUNSPATH = IMPORTFILE(FILENAME) reads data from text file FILENAME
%  for the default selection.  Returns the data as a table.
%
%  RUNSPATH = IMPORTFILE(FILE, DATALINES) reads data for the specified
%  row interval(s) of text file FILENAME. Specify DATALINES as a
%  positive scalar integer or a N-by-2 array of positive scalar integers
%  for dis-contiguous row intervals.
%
%  Example:
%  iterPath = importfile("C:\Users\jalo\Documents\NetBeansProjects\xdevs-planner-master\data\csv\Optimizer\Scenario\runs.csv", [2, Inf]);
%
%  See also READTABLE.
%

%% Input handling
dataLines = [2, Inf];

%% Setup the Import Options and import the data
N_paretos=numel(objectives.paretos);
N_constraints=numel(objectives.constraints);
numVars = 3 + N_paretos + N_constraints; 
opts = delimitedTextImportOptions("NumVariables", numVars);

% Specify range and delimiter
opts.DataLines = dataLines;
opts.Delimiter = ",";

% Specify column names and types
variableNamesOrder = ["run", "sol", "time"];
variableNamesOrder = [variableNamesOrder, objectives.paretos(:)', objectives.constraints(:)'];
variableNamesOrder = variableNamesOrder(:)'; % Ensure it's a row vector
opts.VariableNames = variableNamesOrder;
opts.VariableTypes = ["string", "string", "double", repmat("double", 1, N_paretos + N_constraints)];

% Specify file level properties
opts.ExtraColumnsRule = "ignore";
opts.EmptyLineRule = "read";

% Import the data
RUNSdata = readtable(filename, opts);

end