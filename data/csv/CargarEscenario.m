% Experimento:

clear all; close all;

EXP='ScenarioCloud_1UAVradar'; %'ScenarioEVPADS3Dinamic_baseCamMOV';    % Escenario a dibujar
especif='spec1';                % Especificación: 
                                % "spec1" (se evalua despues del vuelo)
                                % "spec2" (evalua mientras vuela)
                                % "." (Versión antigua)
tipo='Evaluator';   
%tipo='Optimizator';

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
% ----------------------------
%uavs:
[N_uav,c]=size(valjs.uavs);
%Leer CSVs:
for k=1:N_uav,
    str_uav=valjs.uavs(k).name;
    UAV=importUAV(strcat(especif,sep,tipo,sep,EXP,sep,'uavs',sep,str_uav,sep,str_uav,'Path.csv'));
    U(k).dat(:,:)=table2array(UAV);
    if (freememory), clear UAV; end;
    
    CTRL=importCTRL(strcat(especif,sep,tipo,sep,EXP,sep,'uavs',sep,str_uav,sep,str_uav,'Cntrl.csv'));
    UAV_Ctrl(k).dat(:,:)=table2array(CTRL);
    if (freememory), clear CTRL; end;
    
    
    initState(k)=valjs.uavs.initState;
    finalState(k)=valjs.uavs.finalState;
end

% De momento, sólo 1 target:
s=importSOL(strcat(especif,sep,tipo,sep,EXP,sep,'targets',sep,'target1',sep,'target1Path.csv'));
sol=table2array(s);
if (freememory), clear s; end;

[N_bel,f]=size(sol);

x=xWidth/xCells:xWidth/xCells:xWidth;
y=yHeight/yCells:yHeight/yCells:yHeight;

for k=0:N_bel-1,
    esc=importTargetBelief(strcat(especif,sep,tipo,sep,EXP,sep,'targets',sep,'target1',sep,'target1State',sprintf('%d',k),'.csv'));
    belief(k+1).M=table2array(esc(1:xCells,1:yCells));
end
