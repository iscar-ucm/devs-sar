a=xlsread('Spec1Profile.xlsx')
b=xlsread('Spec2Profile.xlsx')
%bar([a(1,1);b(1,1)])
a(:,3)


times_a=a(:,2:2:end)  %Los tiempos
times_b=b(:,2:2:end)  %Los tiempos

etiquetas={'SIM','FS','EV','EF','TC','TMM','UM1/EUM1','UM2/EUM2'}; %Elige los elementos que quieres representar



%Elige las filas del fichero que quieres representar
index_a=[1,3,20,21,23,24,4,9]; %Escribe en que fila de la matriz esta cada elemento que tienes que representar
index_b=[1,15,3,4,13,14,5,8];
times_a=times_a(index_a,:);  %He escogido el global, el de FS y el EV 
times_b=times_b(index_b,:);   %He escogido el global, el de FS y el EV 


times_a=sum(times_a,2); %Sumo todos los tiempos (para no diferenciar entre lambdas, deltas, ....)
times_b=sum(times_b,2); %Sumo todos los tiempos

%Voy a crear una escala especial en el eje y. 
v=[0,1,10,100,1000,10000];
a_new=zeros(size(times_a));
b_new=zeros(size(times_b));

for i=1:length(v)-1;
    u=find(v(i)<times_a & times_a<=v(i+1));
    l=times_a(u)/v(i+1);
    a_new(u)=(i-1)+l;
    u=find(v(i)<times_b & times_b<=v(i+1));
    l=times_b(u)/v(i+1);
    b_new(u)=(i-1)+l;
end    
figure;bar([times_a,times_b]); %La original, sin reescalar
set(gca,'FontSize',22)
legend('SPEC1','SPEC2');
ylabel ('WCT (s)');

figure;bar([a_new,b_new]);
yticks([0:0.25:5]);
yticklabels({'0',' ',' ',' ','1E-3',' ',' ',' ','1E-2',' ',' ',' ','1E-1',' ',' ',' ','1',' ',' ',' ','1E1'});
xticklabels(etiquetas)
set(gca,'FontSize',22)
legend('SPEC1','SPEC2'); %Comprobar que no es al reves, porque no se como van los ficheros
ylabel ('WCT (s)');

NOCS_a=a(1,1);
NOCS_b=b(1,1);
b=bar([NOCS_a;NOCS_b])
ylabel ('NOCS (#)');
b.CData(2,:) = [0.85,0.33,0.10];
set(gca,'FontSize',22)
xticklabels({'SPEC1','SPEC2'});

%%%%%%%%%%

