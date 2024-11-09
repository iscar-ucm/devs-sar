% Experimento:

close all;

EXP='Benchmark6';  % Escenario a dibujar
EXPSOL='ANNSIM2022_2_Acy_OP';

OP={'Op1NSGA2','Op2SPEA2'}';

% NOTA: se especifican las ejecuciones que se quieren pintar (comparar).
% Tambien se puede hacer un frente guardado de una ejecución (modificando
% SOL) pero mezclarlo todo no se ve nada.
%RUN={'01','02','03','04''05','06','07','08','09','10','11','12','13','14','15','16','17','18','19','20'}'; % Ejecuciones del algoritmo
%SOL={'00'}; % Solución guardada

RUN={'01','02'}'; % Ejecuciones del algoritmo
SOL={'00','01','02','03','04','05'}'; % Solución guardada (frente)

NoBelief=1; % Indica que no se lea ni pinte el Belief porque no se ve nada y ralentiza mucho al ser muchos datos en una figura.

especif='spec1';            % Especificación: 
                                % "spec1" (se evalua despues del vuelo)
                                % "spec2" (evalua mientras vuela)
                                % "." (Versión antigua)

tipo='Evaluator';   

freememory=1;               % Elimina las variables de lectura de los 
                            % archivos para liberar memoria                           
sep=filesep; % Separador de directorios del SO


% Importar datos del escenario:
%js=fileread(strcat(strcat('..',sep,'scenarios',sep,tipo,sep),EXP));
js=fileread(strcat(strcat('..',sep,'scenarios',sep,tipo,sep,EXP,sep),strcat(EXP,'.json')))
valjs=jsondecode(js);
if (freememory), clear js; end;

%zone:
xWidth=valjs.zone.xWidth;
yHeight=valjs.zone.yHeight;
areaBearing=valjs.zone.areaBearing;
xCells=valjs.zone.xCells;
yCells=valjs.zone.yCells;
%tramo=valjs.cntrlParams.subsequence;

Nruns=valjs.cntrlParams.numOfRuns;
algorithms=valjs.algorithms; 
% ----------------------------
[ops,c]=size(OP)
[runs,c]=size(RUN);
[sols,c]=size(SOL);

Runcol={'k','r','m','g','y','c','k-','r-','m-','g-','y-','c-','k--','r--','m--','g--','y--','c--','k:','r:','m:','g:','y:','c:','k-.','r-.','m-.','g-.','y-.','c-.'};
maxcol=length(Runcol); p=1;


figs=0;
ts=''; % Para el rótulo de ETD y PD conjunto

% DIBUJOS:

for Nops=1:ops,

for Nrun=1:runs,
for Ncol=1:sols,
str_path=strcat(especif,sep,tipo,sep,EXPSOL,sep,OP{Nops},sep,RUN{Nrun},sep,SOL{Ncol})

if (isfolder(str_path)), % Si no está la ejecución correspondiente no se pinta y se salta
    disp('dibujo');
%uavs:
[N_uav,c]=size(valjs.uavs);
%Leer CSVs:
for k=1:N_uav,
    str_uav=valjs.uavs(k).name;
    UAV=importUAV(strcat(str_path,sep,'uavs',sep,str_uav,sep,str_uav,'Path.csv'));
    U(k).dat(:,:)=table2array(UAV);
    if (freememory), clear UAV; end;
    
    initState(k)=valjs.uavs.initState;
    finalState(k)=valjs.uavs.finalState;
end


col=['k','m','r','y','g'];
figure(1+figs);
x=xWidth/xCells:xWidth/xCells:xWidth;
y=yHeight/yCells:yHeight/yCells:yHeight;
maxBel=0.15;

    
% De momento, sólo 1 target:
s=importSOL(strcat(str_path,sep,'targets',sep,'target1',sep,'target1Path.csv'));
sol=table2array(s);
if (freememory), clear s; end;
[N_bel,f]=size(sol);

if not(NoBelief),

for k=0:N_bel-1,
    esc=importTargetBelief(strcat(str_path,sep,'targets',sep,'target1',sep,'target1State',sprintf('%d',k),'.csv'));
    belief(k+1).M=table2array(esc(1:xCells,1:yCells));
    if (freememory), clear esc; end;
end

% Dibujar:
ms=mesh(x,y,belief(1).M);
maxBel=max(max(belief(1).M));
title('Initial Belief')

hold on;    
end;

numNFZ=length(valjs.nfzs);
cellWidth=xWidth/xCells;
cellHeight=yHeight/yCells;
Xnfz=[]; Ynfz=[]; Znfz=[];
%maxBel=0.15; 
for k=1:numNFZ,
    X=valjs.nfzs(k).xRow;
    Y=valjs.nfzs(k).yCol;
    Xnfz(:,k)= cellWidth*[X, X,   (X+1), (X+1)]';
    Ynfz(:,k)=cellHeight*[Y,(Y+1),(Y+1), Y]';
    Znfz(:,k)=maxBel*[1,1,1,1]';
end;
figure(1+figs), hold on
fill3(Xnfz,Ynfz,Znfz,'k');
tramo_plot=zeros(1,N_uav);

% Dibujo de cada belief:
if (0),   % NO UTILIZAR SI NoBelief=1
disp('Coloca la vista del plot y Pulsa para continuar');
pause;    
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

p=p+1; if (p>maxcol), p=1; end;

if (1),       
    if not(NoBelief),
        delete(ms); % Borro el Belief anterior para que no se llene la memoria
        ms=mesh(x,y,belief(N_bel-1).M);
        title(sprintf('Belief%d - t(%3.2f seg.)',N_bel-1,sol(N_bel,3)))
    end;
    fill3(Xnfz,Ynfz,Znfz,'k');    
    %fill3(Xnfz,Ynfz,Znfz,Runcol{p});

    paso=3;
for k=2:N_bel-1,        
    for uk=1:N_uav,
        f=find(U(uk).dat(:,end)<=sol(k+1,3)); %indice en el Tiempo del UAV del Belief (última columna)
        if ~isempty(f),
          ind=f(end);
          if U(uk).dat(ind,end)>tramo+tramo_plot(uk)*tramo,
              plot3(U(uk).dat(ind,1),U(uk).dat(ind,2),maxBel*ones(ind,1),strcat(col(uk),'*'))
              tramo_plot(uk)=tramo_plot(uk)+1;
          end
          %plot3(U(uk).dat(1:paso:ind,1),U(uk).dat(1:paso:ind,2),maxBel*ones(size([1:paso:ind]')),col(uk))
          plot3(U(uk).dat(1:paso:ind,1),U(uk).dat(1:paso:ind,2),maxBel*ones(size([1:paso:ind]')),Runcol{p})
          plot3(U(uk).dat(1,1),U(uk).dat(1,2),maxBel,'ro')
        end
    end
end
title(OP{Nops});
end

if (1), % Vista desde la vertical (plana)
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
end;

val=sol(end,3)/tramo;
val_tramos=[1:1:val-1]*tramo;
for k=1:length(val_tramos),
    pintatramos_etd(k)=sol(max(find(sol(1:end,3)<=val_tramos(k))),2);
    pintatramos_pd(k)=sol(max(find(sol(1:end,3)<=val_tramos(k))),1);
end
    

figure(2+figs);

plot(sol(2:paso:end,3),sol(2:paso:end,2),Runcol{p});
if ~isempty(val_tramos),
    hold on, plot(val_tramos,pintatramos_etd,'*')
    %hold off
end;
xlabel('time'); ylabel('etd'), title(OP{Nops});

figure(3+figs)
plot(sol(2:paso:end,3),sol(2:paso:end,1),Runcol{p});
if ~isempty(val_tramos),
    hold on, plot(val_tramos,pintatramos_pd,'*')
    %hold off
end;
xlabel('time'); ylabel('dp'),title(OP{Nops});

%Todas las Optimizaciones junta (ETD y PD):
figure(3*ops+1);

plot(sol(2:paso:end,3),sol(2:paso:end,2),col(Nops));
if ~isempty(val_tramos),
    hold on, plot(val_tramos,pintatramos_etd,'*')
    %hold off
end;



figure(3*ops+2)
plot(sol(2:paso:end,3),sol(2:paso:end,1),col(Nops));
if ~isempty(val_tramos),
    hold on, plot(val_tramos,pintatramos_pd,'*')
    %hold off
end;



end; % if str_path

%disp('OTRA');
%pause;

end; % for Ncol
end; % for Nrun

figs=figs+3;
aux=sprintf("%s(%s)",OP{Nops},col(Nops));
ts=strcat(ts,' - ',aux); 
figure(3*ops+1), xlabel('time'); ylabel('etd'), title(ts);
figure(3*ops+2), xlabel('time'); ylabel('dp'), title(ts);

end; % for Nops

% Guardar figuras:
if (0),
    cad='2_'; %String para distinguir el escenario
    figs=0;

for k=1:Nops,
    figure(1+figs)
    str=strcat(cad,'TrayOP',int2str(k),'.jpg')
    saveas(gcf,str);
    figure(2+figs)
    str=strcat(cad,'ETDOp',int2str(k),'.jpg')
    saveas(gcf,str);
    figure(3+figs)
    str=strcat(cad,'PdOP',int2str(k),'.jpg')
    saveas(gcf,str);
    figs=figs+3;
end;
figure(3*ops+1)
str=strcat(cad,'TotETD_OPs','.jpg')
saveas(gcf,str);
figure(3*ops+2)
str=strcat(cad,'TotPD_OPs','.jpg')
saveas(gcf,str);
end;
