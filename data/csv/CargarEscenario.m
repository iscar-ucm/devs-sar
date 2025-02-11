% Experimento:

clearvars; close all;

EXP = 'annsim25_s2'; %'ScenarioEVPADS3Dinamic_baseCamMOV';    % Escenario a dibujar
especif = 'spec1';                % Especificación: 
                                % "spec1" (se evalua despues del vuelo)
                                % "spec2" (evalua mientras vuela)
                                % "." (Versión antigua)
tipo = 'Evaluator';   
%tipo = 'Optimizer';

freememory = true;               % Elimina las variables de lectura de los 
                                % archivos para liberar memoria                           
sep = filesep; % Separador de directorios del SO

% Importar datos del escenario:
scenario_path = fullfile('..', 'scenarios', tipo, EXP);
js = fileread(fullfile(scenario_path, [EXP, '.json']));
valjs = jsondecode(js);
if freememory, clear js; end;

% Zone:
xWidth = valjs.zone.xWidth;
yHeight = valjs.zone.yHeight;
areaBearing = valjs.zone.areaBearing;
xCells = valjs.zone.xCells;
yCells = valjs.zone.yCells;
%tramo = 225;

% UAVs:
[N_uav, c] = size(valjs.uavs);

% Leer CSVs y cargar datos UAVs:
U = struct('dat', []);  % Inicializar estructura de datos para UAVs
UAV_Ctrl = struct('dat', []);  % Inicializar estructura de datos para UAV_Ctrl

for k = 1:N_uav
    str_uav = valjs.uavs(k).name;
    UAV = importUAV(fullfile(especif, tipo, EXP, 'uavs', str_uav, [str_uav, 'Path.csv']));
    U(k).dat = table2array(UAV);
    if freememory, clear UAV; end;
    
    CTRL = importCTRL(fullfile(especif, tipo, EXP, 'uavs', str_uav, [str_uav, 'Cntrl.csv']));
    UAV_Ctrl(k).dat = table2array(CTRL);
    if freememory, clear CTRL; end;
    
    initState(k) = valjs.uavs.initState;
    finalState(k) = valjs.uavs.finalState;
end

% Importar datos del target:
target_path = fullfile(especif, tipo, EXP, 'targets', 'target1');
s = importSOL(fullfile(target_path, 'target1Path.csv'));
sol = table2array(s);
if freememory, clear s; end;

[N_bel, ~] = size(sol);

x = xWidth / xCells: xWidth / xCells: xWidth;
y = yHeight / yCells: yHeight / yCells: yHeight;

belief = struct('M', zeros(xCells, yCells, N_bel));

for k = 1:N_bel
    esc = importTargetBelief(fullfile(target_path, ['target1State', num2str(k - 1), '.csv']));
    belief(k).M = table2array(esc(1:xCells, 1:yCells));
end