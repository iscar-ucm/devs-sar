function OP=FunCargarOptimizacion(EXP,smooth),
% Función para cargar un Escenario
% 
% OP=FunCargarOptimizacion(EXP,smooth);
% 
%   EXP: string del Escenario (nombre del directorio/json del Escenario)
%   smooth: 'smoothess' Indica si se ha incluido la evaluación de la suavidad de la
%   trayectoria en el archivo iteration.cvs (si no se incluye, poner smooth='') 
%
%   OP: Array de estructuras con la información del Escenario. Campos:
%     data:  son los datos generales del algoritmo que se está ejecutando. Tiene los campos que vienen en el Excel del Escenario:          
%            type: por ejemplo 'nsga2'
%            selection: [1×1 struct]
%            crossover: [1×1 struct]
%            mutation: [2×1 double]
%            nf: 1
%            ns: 50
%            exchange: [1×1 struct]
%     runsumary: es un array de string y corresponde con el archivo runs.csv y contiene las soluciones que se han guardado del 
%                       frente de cada una de las ejecuciones (es para verificar que soluciones se usan, no está procesado en campos más simples).
%     runs: es un array con cada una de las ejecuciones del algoritmo. Tiene los siguientes campos:
%            iterations: Son el frente pareto de cada iteración de la ejecución correspondiente. 
%                           Es una matriz con los siguientes columnas: "seq","iteration","sol","dp","etd","heurist","nfzs","collisions" 
%            sol: Son las soluciones guardadas al final del algoritmo (el último frente, como máximo el número de soluciones que se ha indicado en la variable nf).
% 
%   Ayuda: 
%       Para saber el número de algoritmos diferentes ejecutados en el Escenario (N_alg): [f,N_alg]=size(OP);
%       Para saber el número de ejecuciones diferentes realizadas por el optimizador de cada algoritmo (Nruns): [f,Nruns]=size(OP(1).runs);
%       Para saber cuántas soluciones diferentes se guardan en cada ejecución del algoritmo (k, por ejemplo k=2): OP(2).Nsols. Es un array con el número de soluciones de cada ejecución (tamaño Nruns).



%EXP='ANNSIM2022_Acy_Inc'; %'ScenarioEVPADS3Dinamic_baseCamMOV';    % Escenario a dibujar
%smooth='smoothess'; % Si no se ha grabado el smooth de la trayectoria poner '';


especif='spec1';                % Especificación: 
                                % "spec1" (se evalua despues del vuelo)
                                % "spec2" (evalua mientras vuela)
                                % "." (Versión antigua)
%tipo='Evaluator';   
tipo='Optimizer';

freememory=1;               % Elimina las variables de lectura de los 
                            % archivos para liberar memoria                           
sep=filesep; % Separador de directorios del SO


% Importar datos del escenario:
js=fileread(strcat(strcat('..',sep,'scenarios',sep,tipo,sep,EXP,sep),strcat(EXP,'.json')));
valjs=jsondecode(js);
if (freememory), clear js; end;

%zone:
xWidth=valjs.zone.xWidth;
yHeight=valjs.zone.yHeight;
areaBearing=valjs.zone.areaBearing;
xCells=valjs.zone.xCells;
yCells=valjs.zone.yCells;
tramo=valjs.cntrlParams.subsequence;
Nruns=valjs.cntrlParams.numOfRuns;
algorithms=valjs.algorithms;     
% ----------------------------
% algorithms:
[N_alg,c]=size(algorithms);
[N_uav,c]=size(valjs.uavs);
for k=1:N_alg,
    OP(k).data=algorithms(k);
    OP(k).ctrlParams=valjs.cntrlParams;
    str_alg=strcat('Op',int2str(k),upper(valjs.algorithms(k).type));
    str_path=strcat(especif,sep,tipo,sep,EXP,sep,str_alg);
    
    RUNS=importRUNS(strcat(str_path,sep,'runs.csv'));
    OP(k).runsumary(:,:)=table2array(RUNS);
    if (freememory), clear ITER; end;    
    Nr=str2num(join(OP(k).runsumary(:,1)') );
    
    for r=1:Nruns,     
        if (r<10), str_0='0'; 
        else,      str_0=''; end; 
        str_path_run=strcat(str_path,sep,strcat(str_0,int2str(r)));
        
        ITER=importITER(strcat(str_path_run,sep,'iterations.csv'),smooth);
        OP(k).runs(r).iterations(:,:)=table2array(ITER);
        if (freememory), clear ITER; end;
        
        cont_sols=sum(Nr==r);
        OP(k).Nsols(r)=cont_sols;
        for s=0:cont_sols-1,        
            if (s<10), str_0='0'; 
            else,      str_0=''; end; 
            str_path_sol=strcat(str_path_run,sep,strcat(str_0,int2str(s)) );
            for j=1:N_uav,
                str_uav=valjs.uavs(j).name;
                CTRL=importCTRL(strcat(str_path_sol,sep,...
                    'uavs',sep,str_uav,sep,str_uav,'Cntrl.csv'));    
                OP(k).runs(r).sol(s+1).UAV_Ctrl(j).data(:,:)=table2array(CTRL);
                if (freememory), clear CTRL; end;
            end;    
        end;
    end;    
end;

