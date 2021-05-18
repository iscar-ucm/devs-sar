% Dibujar el Escenario Cargado:
 
Paso_a_Paso=0;
pinta_marcas=0;

col=['k','m','r','y','g'];
figure(1);
ms=mesh(x,y,belief(1).M);
%ms=surf(belief(1).M);
%view(2)
xlabel('x(m)');
ylabel('y(m)');
title('Initial Belief')

if (1),
    % Dibujo del Belief en determinados instantes:
    t_belief=[2000, 5000, 7200];
    
    for k=1:length(t_belief),        
        figure(30+k)
        ind=max(find(sol(:,3)<=t_belief(k)));
        if ~isempty(ind),
            ms2=mesh(x,y,belief(ind).M);
            title(sprintf('Belief - t(%3.2f seg.)',sol(ind,3)))
        end
    end
    
disp('Continuar???');
pause
end;

figure(1)
hold on;    

numNFZ=length(valjs.nfzs);
cellWidth=xWidth/xCells;
cellHeight=yHeight/yCells;
Xnfz=[]; Ynfz=[]; Znfz=[];

maxBel=max(max(belief(1).M));
%maxBel=0.01;  %Altura fija para todos los UAVs
maxBel=0.001;

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
if (Paso_a_Paso),
    
for k=2:N_bel-1,
    set(ms,'Visible','off');
    ms=mesh(x,y,belief(k).M);
    %ms=surf(belief(k).M);
    fill3(Xnfz,Ynfz,Znfz,'k');
    title(sprintf('Belief%d - t(%3.2f seg.)',k,sol(k+1,3)))
    for uk=1:N_uav,
        f=find(U(uk).dat(:,end)<=sol(k+1,3)); %indice en el Tiempo del UAV del Belief (última columna)
        if ~isempty(f),
          ind=f(end);
          if U(uk).dat(ind,end)>tramo+tramo_plot(uk)*tramo,
              if pinta_marcas,
                 plot3(U(uk).dat(ind,1),U(uk).dat(ind,2),maxBel*ones(ind,1),strcat(col(uk),'*'))
              end
              tramo_plot(uk)=tramo_plot(uk)+1;
          end
          plot3(U(uk).dat(1:ind,1),U(uk).dat(1:ind,2),maxBel*ones(ind,1),col(uk))
          plot3(U(uk).dat(1,1),U(uk).dat(1,2),maxBel,'ro')
        end
    end
        
    for uk=1:N_uav,
        
        f=find(U(uk).dat(:,end)<=sol(k+1,3)); %indice en el Tiempo del UAV del Belief (última columna)
        if ~isempty(f),
          ind=f(end);
          if U(uk).dat(ind,end)>tramo+tramo_plot(uk)*tramo,
              plot3(U(uk).dat(ind,1),U(uk).dat(ind,2),maxBel*ones(ind,1),strcat(col(uk),'*'))
              tramo_plot(uk)=tramo_plot(uk)+1;
          end
          plot3(U(uk).dat(1:ind,1),U(uk).dat(1:ind,2),maxBel*ones(ind,1),col(uk))
          
        end
    end
    pause(0.2);
end
else

% Dibujo del recorrido del UAV y SOLO el último belief
    set(ms,'Visible','off');
    ms=mesh(x,y,belief(N_bel-1).M);
    %ms=surf(belief(N_bel-1).M);
    fill3(Xnfz,Ynfz,Znfz,'k');
    %title(sprintf('Belief%d - t(%3.2f seg.)',N_bel-1,sol(N_bel,3)))
    title(sprintf('Belief - t(%3.2f seg.)',sol(N_bel,3)))
    
    pinta_tramos=[];
    for uk=1:N_uav,
        pinta_tramos=[];
        for t=tramo:tramo:valjs.uavs(uk).finalState.time,
            ind_t=max(find(U(uk).dat(:,end)<=t));
            pinta_tramos=[pinta_tramos ind_t];
        end
        plot3(U(uk).dat(:,1),U(uk).dat(:,2),maxBel*ones(length(U(uk).dat(:,1)),1),col(uk))
        plot3(U(uk).dat(1,1),U(uk).dat(1,2),maxBel,'ro')
        
        if pinta_marcas,
            plot3(U(uk).dat(pinta_tramos,1),U(uk).dat(pinta_tramos,2),maxBel*ones(length(pinta_tramos),1),strcat(col(uk),'*'))
                
        for kj=1:length(UAV_Ctrl(uk).dat(:,end)),
            ind_ctrl=max(find(U(uk).dat(:,end)<=UAV_Ctrl(uk).dat(kj,end)));
            if ~isempty(ind_ctrl),
                 plot3(U(uk).dat(ind_ctrl,1),U(uk).dat(ind_ctrl,2),maxBel,strcat(col(uk),'o'))
            end
        end
        
        end;
        
    end
    view(2)
end

fg=gca;
set(fg,'XTick',0:cellWidth*10:xWidth);
set(fg,'YTick',0:cellHeight*10:yHeight);
set(fg,'GridColor','k')
set(fg,'GridAlpha',0.25)
set(fg,'Layer','top');
set(fg,'XMinorGrid','on');
set(fg,'YMinorGrid','on');
xlabel('x (m)'); %  -- grid (cell*10 m)');
ylabel('y (m)'); %  -- grid (cell*10 m)');
view([-40 35]);

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
