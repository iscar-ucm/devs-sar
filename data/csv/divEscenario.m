% Experimento:

EXP='ScenarioComplex2_3_art1_static.json';  % Escenario a dibujar
especif='spec2';            % Especificación: 
                                % "spec1" (se evalua despues del vuelo)
                                % "spec2" (evalua mientras vuela)
                                % "." (Versión antigua)

freememory=1;               % Elimina las variables de lectura de los 
                            % archivos para liberar memoria                           
sep=filesep; % Separador de directorios del SO


% Importar datos del escenario:
js=fileread(strcat(strcat('..',sep,'scenarios',sep,'Optimizator',sep),EXP));
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
    UAV=importUAV(strcat(especif,sep,EXP,sep,'uavs',sep,str_uav,sep,str_uav,'Path.csv'));
    U(k).dat(:,:)=table2array(UAV);
    if (freememory), clear UAV; end;
    
    initState(k)=valjs.uavs.initState;
    finalState(k)=valjs.uavs.finalState;
end

% De momento, sólo 1 target:
s=importSOL(strcat(especif,sep,EXP,sep,'targets',sep,'target1',sep,'target1Path.csv'));
sol=table2array(s);
if (freememory), clear s; end;

[N_bel,f]=size(sol);

x=xWidth/xCells:xWidth/xCells:xWidth;
y=yHeight/yCells:yHeight/yCells:yHeight;

for k=0:N_bel-1,
    esc=importTargetBelief(strcat(especif,sep,EXP,sep,'targets',sep,'target1',sep,'target1State',sprintf('%d',k),'.csv'));
    belief(k+1).M=table2array(esc(1:xCells,1:yCells));
end

% Dibujar:
col=['k','m','r','y','g'];
figure(1);
ms=mesh(x,y,belief(1).M);
title('Initial Belief')

hold on;    

numNFZ=length(valjs.nfzs);
cellWidth=xWidth/xCells;
cellHeight=yHeight/yCells;
Xnfz=[]; Ynfz=[]; Znfz=[];
maxBel=max(max(belief(1).M));
%maxBel=0.15; 
for k=1:numNFZ,
    X=valjs.nfzs(k).xRow;
    Y=valjs.nfzs(k).yCol;
    Xnfz(:,k)= cellWidth*[X, X,   (X+1), (X+1)]';
    Ynfz(:,k)=cellHeight*[Y,(Y+1),(Y+1), Y]';
    Znfz(:,k)=maxBel*[1,1,1,1]';
end;
fill3(Xnfz,Ynfz,Znfz,'k');

disp('Coloca la vista del plot y Pulsa para continuar');
pause;

tramo_plot=zeros(1,N_uav);

% Dibujo de cada belief:
if (0), 
for k=2:N_bel-1,
    set(ms,'Visible','off');
    ms=mesh(x,y,belief(k).M);
    fill3(Xnfz,Ynfz,Znfz,'k');
    title(sprintf('Belief%d - t(%3.2f seg.)',k,sol(k+1,3)))
        
    for uk=1:N_uav,
        f=find(U(uk).dat(:,end)<=sol(k+1,3)); %indice en el Tiempo del UAV del Belief (última columna)
        if ~isempty(f),
          ind=f(end);
          if U(uk).dat(ind,end)>tramo+tramo_plot(uk)*tramo,
              plot3(U(uk).dat(ind,1),U(uk).dat(ind,2),maxBel*ones(ind,1),strcat(col(uk),'*'))
              tramo_plot(uk)=tramo_plot(uk)+1;
          end
          plot3(U(uk).dat(1:ind,1),U(uk).dat(1:ind,2),maxBel*ones(ind,1),col(uk))
          plot3(U(uk).dat(1,1),U(uk).dat(1,2),maxBel,'ro')
        end
    end
    pause(0.1);
end
end
% Dibujo del recorrido del UAV y SOLO el último belif
if (1),
    set(ms,'Visible','off');
    ms=mesh(x,y,belief(N_bel-1).M);
    fill3(Xnfz,Ynfz,Znfz,'k');
    title(sprintf('Belief%d - t(%3.2f seg.)',N_bel-1,sol(N_bel,3)))
for k=2:N_bel-1,        
    for uk=1:N_uav,
        f=find(U(uk).dat(:,end)<=sol(k+1,3)); %indice en el Tiempo del UAV del Belief (última columna)
        if ~isempty(f),
          ind=f(end);
          if U(uk).dat(ind,end)>tramo+tramo_plot(uk)*tramo,
              plot3(U(uk).dat(ind,1),U(uk).dat(ind,2),maxBel*ones(ind,1),strcat(col(uk),'*'))
              tramo_plot(uk)=tramo_plot(uk)+1;
          end
          plot3(U(uk).dat(1:ind,1),U(uk).dat(1:ind,2),maxBel*ones(ind,1),col(uk))
          plot3(U(uk).dat(1,1),U(uk).dat(1,2),maxBel,'ro')
        end
    end
end
end


fg=gca;
set(fg,'XTick',0:cellWidth*10:xWidth);
set(fg,'YTick',0:cellHeight*10:yHeight);
set(fg,'GridColor','k')
set(fg,'GridAlpha',0.25)
set(fg,'Layer','top');
set(fg,'XMinorGrid','on');
set(fg,'YMinorGrid','on');
xlabel('x (m)  -- grid (cell*10 m)');
ylabel('y (m)  -- grid (cell*10 m)');
view([0,90]);

val=sol(end,3)/tramo;
val_tramos=[1:1:val-1]*tramo;
for k=1:length(val_tramos),
    pintatramos_etd(k)=sol(max(find(sol(1:end,3)<=val_tramos(k))),2);
    pintatramos_pd(k)=sol(max(find(sol(1:end,3)<=val_tramos(k))),1);
end
    

figure(2);
plot(sol(2:end,3),sol(2:end,2));
if ~isempty(val_tramos),
    hold on, plot(val_tramos,pintatramos_etd,'*'), hold off
end;
xlabel('time'); ylabel('etd')

figure(3)
plot(sol(2:end,3),sol(2:end,1));
if ~isempty(val_tramos),
    hold on, plot(val_tramos,pintatramos_pd,'*'), hold off
end;
xlabel('time'); ylabel('dp')





