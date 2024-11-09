clearvars; close all;

% Escenarios a cargar, modificar según necesidad.
scenarioNames = {
    'resc_rad2_t2', 'resb_rad2_t2', 'resa_rad2_t2'};

especif = 'spec1';
tipo = 'Evaluator';
freememory = true;
sep = filesep;

scenariosData = struct('valjs', {}, 'zone', {}, 'U', {}, 'UAV_Ctrl', {}, 'tramo', {}, 'sol', {}, 'belief', {});

for scenarioIndex = 1:numel(scenarioNames)
    
    % Escenario a importar
    EXP = scenarioNames{scenarioIndex};

    % Importar datos del archivo json y decodificarlo:
    scenario_path = fullfile('..', 'scenarios', tipo, EXP);
    js = fileread(fullfile(scenario_path, [EXP, '.json']));
    valjs = jsondecode(js);
    if freememory, clear js; end;    
    
    % Almacenar datos de la zona:
    zone = struct('xWidth', 'yHeight', 'areaBearing', 'xCells', 'yCells', 'tramo');
    zone.xWidth = valjs.zone.xWidth;
    zone.yHeight = valjs.zone.yHeight;
    zone.areaBearing = valjs.zone.areaBearing;
    zone.xCells = valjs.zone.xCells;
    zone.yCells = valjs.zone.yCells;
    
    % Almacenar longitud del tramo:    
    tramo = valjs.cntrlParams.subsequence;
    
    % UAVs:
    [N_uav, c] = size(valjs.uavs);
    
    % Leer CSVs y cargar datos UAVs:
    U = struct('dat', []);  
    UAV_Ctrl = struct('dat', []);  
    
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

    belief = struct('M', zeros(zone.xCells, zone.yCells, N_bel));

    for k = 1:N_bel
        esc = importTargetBelief(fullfile(target_path, ['target1State', num2str(k - 1), '.csv']));
        belief(k).M = table2array(esc(1:zone.xCells, 1:zone.yCells));
    end
    
    scenariosData(scenarioIndex).valjs = valjs;    
    scenariosData(scenarioIndex).zone = zone;
    scenariosData(scenarioIndex).tramo = tramo;    
    scenariosData(scenarioIndex).U = U;
    scenariosData(scenarioIndex).UAV_Ctrl = UAV_Ctrl;
    scenariosData(scenarioIndex).sol = sol;
    scenariosData(scenarioIndex).belief = belief;
end
