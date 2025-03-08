% Experimento:
clear all; close all;

EXP = 'graficas';  % Escenario a dibujar
especif = 'spec1'; % Especificación: 
                  % "spec1" (se evalúa después del vuelo)
                  % "spec2" (evalúa mientras vuela)
                  % "." (Versión antigua)
tipo = 'Optimizer';

freememory = 1;  % Elimina las variables de lectura de los archivos para liberar memoria
sep = filesep;   % Separador de directorios del SO

% Importar datos del escenario:
js = fileread(strcat(strcat('..', sep, 'scenarios', sep, tipo, sep, EXP, sep), strcat(EXP, '.json')));
valjs = jsondecode(js);
if freememory, clear js; end

% Datos de zona:
xWidth = valjs.zone.xWidth;
yHeight = valjs.zone.yHeight;
areaBearing = valjs.zone.areaBearing;
xCells = valjs.zone.xCells;
yCells = valjs.zone.yCells;
Nruns = valjs.cntrlParams.numOfRuns;
algorithms = valjs.algorithms;

% Determinar el número de algoritmos
N_alg = numel(algorithms);
% Número de UAVs
N_uav = numel(valjs.uavs);

for k = 1:N_alg
    % Acceder al algoritmo actual, ya sea celda o estructura
    if iscell(algorithms)
        alg = algorithms{k};
    else
        alg = algorithms(k);
    end
    
    OP(k).data = alg;
    OP(k).ctrlParams = valjs.cntrlParams;
    
    % Construir el nombre del algoritmo de forma segura
    if isfield(alg, 'type')
        str_alg = strcat('Op', int2str(k), upper(alg.type));
    else
        str_alg = strcat('Op', int2str(k), 'UNKNOWN');
    end
    disp(str_alg);
    
    str_path = strcat(especif, sep, tipo, sep, 'resultadosCh4', sep, EXP, sep, str_alg);
    
    % Importar resultados de runs
    RUNSFF = importRUNS(strcat(str_path, sep, 'runs.csv'), OP(k).data.objectives);
    OP(k).runsFF(:,:) = table2array(RUNSFF);
    if freememory, clear ITER; end
    
    Nr = str2num(join(OP(k).runsFF(:,1)'));
    
    for r = 1:Nruns
        if r < 10
            str_0 = '0';
        else
            str_0 = '';
        end
        str_path_run = strcat(str_path, sep, strcat(str_0, int2str(r)));
        
        ITERFF = importITER(strcat(str_path_run, sep, 'iterations.csv'), OP(k).data.objectives);
        OP(k).runs(r).iterationsFF(:,:) = table2array(ITERFF);
        if freememory, clear ITERFF; end
        
        cont_sols = sum(Nr == r);
        OP(k).Nsols(r) = cont_sols;
        for s = 1:cont_sols
            if s < 10
                str_0 = '0';
            else
                str_0 = '';
            end
            str_path_sol = strcat(str_path_run, sep, strcat(str_0, int2str(s)));
            for j = 1:N_uav
                str_uav = valjs.uavs(j).name;
                CTRL = importCTRL(strcat(str_path_sol, sep, 'uavs', sep, str_uav, sep, strcat(str_uav, 'Cntrl.csv')));
                OP(k).runs(r).sol(s+1).UAV_Ctrl(j).data(:,:) = table2array(CTRL);
                if freememory, clear CTRL; end
            end
        end
    end
end