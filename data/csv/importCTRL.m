function CTRLdata = importCTRL(filename, dataLines)
%IMPORTFILE Import data from a text file
%  CTRL1PATH = IMPORTFILE(FILENAME) reads data from text file FILENAME
%  for the default selection.  Returns the data as a table.
%
%  CTRL1PATH = IMPORTFILE(FILE, DATALINES) reads data for the specified
%  row interval(s) of text file FILENAME. Specify DATALINES as a
%  positive scalar integer or a N-by-2 array of positive scalar integers
%  for dis-contiguous row intervals.
%
%  Example:
%  ctrl1Path = importfile("C:\Users\jalo\Documents\NetBeansProjects\xdevs-planner-master\data\csv\Scenario1Gen.json\uavs\uav_1\uav_1Ctrl.csv", [2, Inf]);
%
%  See also READTABLE.
%
% Auto-generated by MATLAB on 05-Oct-2020 18:39:38

%% Input handling

% If dataLines is not specified, define defaults
if nargin < 2
    dataLines = [2, Inf];
end

%% Setup the Import Options and import the data
if (1),
opts = delimitedTextImportOptions("NumVariables", 4);

% Specify range and delimiter
opts.DataLines = dataLines;
opts.Delimiter = ",";

% Specify column names and types
opts.VariableNames = ["CELEVATION","CHEADING","CSPEED","TIME"];
opts.VariableTypes = ["double", "double", "double", "double"];

% Specify file level properties
opts.ExtraColumnsRule = "ignore";
opts.EmptyLineRule = "read";

% Import the data
CTRLdata = readtable(filename, opts);

else 
% Import the data
CTRLdata = readtable(filename);
    
end; % Para importar independientemente del número de columnas


end