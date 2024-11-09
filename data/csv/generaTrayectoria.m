% Generar trayectoria
Tray='Absolute'; %'Absolute'; %'Rate'
Mode='Espiral'; %'Espiral'; %'zig-zag'

% ---------------------
% INITIALIZATION:

% UAV Origin:
% Radar:
P0x=0; %m
P0y=0; %m
V= 40; % Velocidad cte (m/s)
t0=0;

% Camara:
P0x=50000; %m
P0y=60000; %m
V= 45; % Velocidad cte (m/s)
t0=1000;

H0= 941; % No hace falta porque me voy a mover a altura constante

Theta0= 0; % Orientación inicial (grados)
ThetaF= 45;% Orientación final (grados)



% ----- Trayectories --------------
if strcmp(Mode,'zig-zag'),
    % Origin of zig-zag:
    % Origen:
        P(1)=5000;
        P(2)=15000; 
    % Origen para la cámara:
    %    P(1)=40000;
    %    P(2)=40000;    
    % Leg space (large size)
    % Para solo radar:
        SS=34000;  % 20000;
    % Para radar y cámara:
        SS=18000;
    %Direction of the first change
    signo=-1;       
    %Initial Length (first straight Leg distance size)
    d=2000;  %2000;       %1500;  
    %Number of legs in the pattern
    hm=30;    
    %Orientantion of the pattern
    angle=95;      % 90;    
end;
if strcmp(Mode,'Espiral'),
    % Origin of espiral:
    % Origen en el centro:
        P(1)=25000;
        P(2)=25000;
    % Origen para la cámara:
        P(1)=45000;
        P(2)=40000;    
    % Leg space (large size)
    SS=2000;    %3000; % m      
    %Direction of the first change
    signo=-1;       
    %Initial Length (first straight Leg distance size)
    d=400;      %500;      1500;  %//Espiral:    
    %Number of legs in the pattern
    hm=24;      % 24;      
    %Orientantion of the pattern
    angle=45;
end
% ---------------------

% GENERATE TRAYECTORY:
% ----------------------------------------------------------------------
if strcmp(Tray,'Absolute'),
  
if strcmp(Mode,'zig-zag'),   

%The fist element is the long leg
Route.dist=SS;
Route.angle=angle;    
%For each element in the leg
for i=1:hm-1    
    %The second element is the shortest
    Route.dist(end+1)=d;
    angle=(angle+signo*90);
    if abs(angle) > 180,
          if angle >0,
               angle= angle - 360;
          else
               angle= angle + 360;
          end
    end
    Route.angle(end+1)=angle;
    
    %The third element is the long leg
    Route.dist(end+1)=SS;       
    %Angle of short part of the leg
    angle=(angle+signo*90);
    if abs(angle) > 180,
          if angle >0,
               angle= angle - 360;
          else
               angle= angle + 360;
          end
    end
    Route.angle(end+1)=angle;
    
    %Change direction of the next short turn
    signo=-signo;
    
end 


% Ir recto al inicio del zig-zag:
T=sqrt((P(1)-P0x)^2+(P(2)-P0y)^2)/V;
angle= atan2(P(2)-P0y,P(1)-P0x)*180/pi
    if abs(angle) > 180,
          if angle >0,
               angle= angle - 360;
          else
               angle= angle + 360;
          end
    end

control=[0.0, angle, 0.0, t0];
zip=[];
T=T+t0;
for k=1:hm,    
    angle=Route.angle(k);
    zip=[zip;   0.0, angle, 0.0, T]
    T=T+Route.dist(k)/V;        
end;
zip=[zip;   0.0, ThetaF, 0.0, T];

control=[control;zip];

end;


% Espiral:
if strcmp(Mode,'Espiral'),

Route.dist=0;
Route.angle=0;
%For each element in the leg
for i=1:hm-1
    %The fist element is the long leg
    Route.dist(end+1)=d+(i-1)*SS;
                
	%Angle of short part of the leg
	angle=angle+signo*90;
    if abs(angle) > 180,
          if angle >0,
               angle= angle - 360;
          else
               angle= angle + 360;
          end
    end
    Route.angle(end+1)=angle;
            
    %The second element is the shortest
    Route.dist(end+1)=i*SS;
                   
    %Obtain new angle of the next leg (A(i) formula, I don´t have GR_RAD, use turn 90º)
    angle=angle+signo*90;
    if abs(angle) > 180,
          if angle >0,
               angle= angle - 360;
          else
               angle= angle + 360;
          end
    end
    Route.angle(end+1)=angle;                    
    
end
%Add the final distances and angles of this section of the trajectory
Route.dist(end+1)=d+(i-1)*SS;
    angle=angle+signo*90;
    if abs(angle) > 180,
          if angle >0,
               angle= angle - 360;
          else
               angle= angle + 360;
          end
    end
Route.angle(end+1)=angle;   

% Ir recto al centro de la espiral:
T=sqrt((P(1)-P0x)^2+(P(2)-P0y)^2)/V;
angle= atan2(P(2)-P0y,P(1)-P0x)*180/pi;
    if abs(angle) > 180,
          if angle >0,
               angle= angle - 360;
          else
               angle= angle + 360;
          end
    end

control=[0.0, angle, 0.0, t0];
espiral=[];
T=T+t0;
for k=2:hm,    
    angle=Route.angle(k);
    espiral=[espiral;   0.0, angle, 0.0, T];
    T=T+Route.dist(k)/V;        
end;
espiral=[espiral;   0.0, ThetaF, 0.0, T];

control=[control;espiral];

end;

end; % ABSOLUTE

% ========================================================================
if strcmp(Tray,'Rate'),
    
    if (0),
    Dgiro=[22918, 11462, 7639.4, 5729.6, 4583.8, 3819.7, 3274, 2865, 2546.5, 2291.7, 2083.9, 1915.7, 1527.8, 1145.7, 763.68, 572.96, 457.93];
    rate=[0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1, 1.2, 1.5, 2.0, 3.0, 4.0, 5.0];
       
    %myfittype2 = fittype('a*exp(-b*sqrt(x))',...
    %myfittype2 = fittype('a.*exp(-b.*x.^(1/3))',...
    myfittype2 = fittype('a.*exp(-b.*x.^(1/3))+c',...
    'dependent',{'y'},'independent',{'x'},...
    'coefficients',{'a','b','c'});

    [curve1,gof1] = fit(rate',Dgiro',myfittype2);
      
    x=0.05:0.05:5.3; 
    figure, plot(rate,Dgiro,'o')
    hold on
    plot(curve1,'m')
    
    %estim=curve1.a*exp(-curve1.b*sqrt(rate));
    %estim=curve1.a*exp(-curve1.b*rate.^(1/3));
    estim=curve1.a*exp(-curve1.b*rate.^(1/3))+curve1.c;
    [rate',Dgiro',estim']
    
end

% Trayectoria en espiral:
if strcmp(Mode,'Espiral'),
%llegar a la zona central del belief:

% Entrada en el escenario:
recta=40*600
control= [  0.0,0.1,0.0,0.0
            0.0,0.0,0.0,600.0];
T=1800;
% Espiral:
brazos=5;
numDgiro= 16; sentido=-1; % -1 gira en sentido agujas del reloj
paso=2;
separa=400; % metros
TR=separa/V;
espiral=[]

for k=1:brazos,
    D=Dgiro(numDgiro);    
    espiral=[espiral;   0.0, rate(numDgiro), 0.0, T];
    T=T+3/4*D*pi/V;  % 3/4 de vuelta        
    %espiral=[espiral;   0.0, 0.0, 0.0, T];
    %T=T+(D/2)/V;  % avanzo recto el radio
    numDgiro=numDgiro+sentido*paso;
end;
espiral=[espiral;   0.0, ThetaF, 0.0, T];

control=[control;
        espiral];
end;


end; % RATE

% --------- ARCHIVO DE CONTROL ---------------

[f,c]=size(control);
disp('CELEVATION,CHEADING,CSPEED,TIME');
for k=1:f,
    s=sprintf('%2.1f,%2.1f,%2.1f,%2.1f',control(k,1),control(k,2),control(k,3),control(k,4));
    disp(s);
end;

%T=array2table(control,'VariableNames',{'CELEVATION','CHEADING','CSPEED','TIME'});


    




