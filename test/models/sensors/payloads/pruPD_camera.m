% Prueba de la camara:

SizeTarget= sqrt(5 * 2); % Bote salvavidas: 5 x 2 m

% camera:       Camera parameters: 
camera.pixelSize=[]; % -> Size of the element of the sensor matrix (m)
camera.dimImage=[1080,1920] %  -> [DimV,DimH], (pixels x pixels)
camera.FoV=[0.58, 6.7]; %       -> field of View [FOV_V,FOV_H] (grad)
camera.f=[];        % -> (m) focal length    

Vdetec=2.03;
Vrecog=8.1;
Vident=16.2;

%V50=Vdetec;           %Cycles of TTP metric (or target size) (point to 50% of probability)

V50=Vrecog;

%V50=V50+2; % Aumento para empeorar el radar por la dificultad en el mar

% Distancia directa:

R=100:10:6000;
pD=[]; Vr=[];
for j=1:length(R),
    [pD(j),Vr(j)]=detectionProb(SizeTarget,camera,V50,R(j));
end;

figure(10)
plot(R,pD)
xlabel('metres'); ylabel('Probability');
title('Pd Camera');

% distancia desde el UAV:

h= 3048; 
V=40;H=0;

[DPr1,Vt]=detectionProb(SizeTarget,camera,Vdetec,h,V,H)



% -------------------- RADAR -------------

Pstd=0.9;
dstd=1000;


% En una línea (dirección x) a partir de la posición del Radar:
Pos=[100, 100, 0];
wx=100;
wy=100;
scale=60; % Tamaño en metros de cada celda.
Pos_celda=ceil(Pos(1:2)/scale);
celdas=[Pos_celda(1):wx];

% En una línea (dirección x) a partir de la posición del Radar:
p0 = sensor_radar(Pos,wx,wy,scale,Pstd, dstd); % Prob de no detección
P0_radar=reshape(1-p0, [wx wy]);

% A altura h=914 m:
Pos=[100, 100, 914];
p = sensor_radar(Pos,wx,wy,scale,Pstd, dstd); % Prob de no detección
P_radar=reshape(1-p, [wx wy]);

figure(11);
plot((celdas*scale)-Pos(1),P0_radar(Pos_celda(1):wx,Pos_celda(2)),(celdas*scale)-Pos(1),P_radar(Pos_celda(1):wx,Pos_celda(2)))
title('Prob detección del radar en distancia')
xlabel('m')

% Probabilidad de los dos sensores juntos:

figure(12)
plot(R,pD,(celdas*scale)-Pos(1),P_radar(Pos_celda(1):wx,Pos_celda(2)))
xlabel('metres'); ylabel('Probability');
title('Detection Probability');
legend('Camera', 'Radar')