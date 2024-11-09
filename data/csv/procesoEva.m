%clear all;close all

colores=[1 0 0;0 1 0;0 0 1;0 1 1]
labels={'Acy-1','Acy-2'};%,'Cyc-1','Cyc-2'};


Escenarios= {'ANNSIM2022_2_Acy'};%,'ANNSIM2022_Cyc'}%,'ANNSIM2022_Acy_Inc_20Sol'}
smooth='smoothess'

clear OP;

for k=1:length(Escenarios),
    ESC{k}.OP=FunCargarOptimizacion(Escenarios{k},smooth);
    ESC{k}.escenario=Escenarios{k};

    disp(sprintf('Escenario: %s',ESC{k}.escenario));
    ESC{k}.OP
    hmESC=length(ESC{k}.OP);
    for i=1:hmESC
        if ~exist('OP')
            OP=ESC{k}.OP(i);
        else
            OP(end+1)=ESC{k}.OP(i);
        end
    end
end;

% elgir antes qué optimizadores quieres pintar:

hmOP=length(OP)



for k=1:hmOP
    tiempo_tramo=OP(k).ctrlParams.subsequence;
    hmrun(k)=length(OP(k).runs)
    ConsTramo=[];
    PdTramo=[];
    ETTramo=[];
    for r=1:hmrun(k)
        first=1;
        iterations=OP(k).runs(r).iterations;
        hm_tramos=iterations(end,1);
        Cons=[];
        Pd=[];
        ET=[];
        for t=1:hm_tramos
            %En las tres primeras columnas; 
            %seq (tramo)    iteration    sol 
            iter_tramo=iterations(iterations(:,1)==t,2:end);
            hm_iter=iter_tramo(end,1);
            for i=1:hm_iter    
                %dp  etd heurist smoothess nfzs collisions
                trozo=iter_tramo(iter_tramo(:,1)==i,3:end);
                frente{k}{r}{t}{i}=trozo;
                Cons(end+1)=sum(sum(trozo(:,5:6)));
                Pd(end+1)=max(trozo(:,1));
                [ET(end+1),i_last]=min(trozo(:,2)); 
                Pd_bestET=trozo(i_last,1);
            end
            ET(first:end)=ET(first:end)+(1-Pd_bestET)*(hm_tramos-t)*tiempo_tramo;
            first=length(ET)+1;
        end
        ConsTramo=[ConsTramo;Cons];
        PdTramo=[PdTramo;Pd];
        ETTramo=[ETTramo;ET];
    end
    Resumen.Cons=ConsTramo
    Resumen.Pd.all=PdTramo;
    Resumen.Pd.mean=mean(PdTramo);
    Resumen.Pd.std=std(PdTramo);
    Resumen.ET.all=ETTramo;
    Resumen.ET.mean=mean(ETTramo)
    Resumen.ET.std=std(ETTramo)
   
   
    Resumenes(k)=Resumen;
end

%% Representar ET
figure
for k=1:hmOP
   hold on
   hall=shadedErrorBar2(1:length(Resumenes(k).ET.mean),Resumenes(k).ET.mean,Resumenes(k).ET.std,1,{'Color',colores(k,:)},1)
   hmain(k)=hall.mainLine;
end
xlabel('Iteration')
ylabel('ET (corregido)');
legend(hmain,labels{:})
set(gca,'FontSize',16);


%% Representar Pd
figure
for k=1:hmOP
   hold on
   hall=shadedErrorBar2(1:length(Resumenes(k).Pd.mean),Resumenes(k).Pd.mean,Resumenes(k).Pd.std,1,{'Color',colores(k,:)},1)
   hmain(k)=hall.mainLine;
end
xlabel('Iteration')
ylabel('ET (sin corregir)');
legend(hmain,labels{:})
set(gca,'FontSize',16);

%%
alg_base=2; %indicar con cual se comparan los demás
for k=1:hmOP
    dominancia(k,:)=dominancia_ET(Resumenes(alg_base).ET.all,Resumenes(k).ET.all,0.05);
end
plot_dominance(dominancia(:,1:1:end),labels,'Dominancia-ET');

%Por tramo ahora.
%600

%alg_base=1;
for k=1:hmOP    dominancia(k,:)=dominancia_ET(-Resumenes(alg_base).Pd.all,-Resumenes(k).Pd.all,0.05);
end
plot_dominance(dominancia(:,1:1:end),labels,'Dominancia-Pd');