function ITERdata = importITERFF(filename, objectives)
%IMPORTFILE Import data from a text file
%  ITERPATH = IMPORTFILE(FILENAME) reads data from text file FILENAME
%  for the default selection.  Returns the data as a table.
%
%  ITERPATH = IMPORTFILE(FILE, TYPE, DATALINES) reads data for the specified
%  row interval(s) of text file FILENAME. Specify DATALINES as a
%  positive scalar integer or a N-by-2 array of positive scalar integers
%  for dis-contiguous row intervals.
% TYPE='smoothess' reads a new variable smoothes, otherwise reads a standard
% profile.
%
%  Example:
%  iterPath = importfile("C:\Users\jalo\Documents\NetBeansProjects\xdevs-planner-master\data\csv\Optimizer\uavs\uav_1\uav_1Ctrl.csv", [2, Inf]);
%
%  See also READTABLE.
%

%% Input handling

% If dataLines is not specified, define defaults
dataLines = [2, Inf];

%% Setup the Import Options and import the data
N_paretos=numel(objectives.paretos);
N_constraints=numel(objectives.constraints);
numVars = 4 + N_paretos + N_constraints; 
opts = delimitedTextImportOptions("NumVariables", numVars);

% Specify range and delimiter
opts.DataLines = dataLines;
opts.Delimiter = ",";

% Specify column names and types
variableNamesOrder = ["seq","iteration","sol","time"];
variableNamesOrder = [variableNamesOrder, objectives.paretos(:)', objectives.constraints(:)'];
variableNamesOrder = variableNamesOrder(:)'; % Ensure it's a row vector
opts.VariableNames = variableNamesOrder;
opts.VariableTypes = ["string", "string", "string", "double", repmat("double", 1, N_paretos + N_constraints)];

% Specify file level properties
opts.ExtraColumnsRule = "ignore";
opts.EmptyLineRule = "read";

% Import the data
ITERdata = readtable(filename, opts);

end