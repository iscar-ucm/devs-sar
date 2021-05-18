% Pintar Modelo de Radar
    
   %Poner en TestRadar.java lo siguiente:
   % Escenario 1: altura 400 - celda 600 m
        % new SearchArea(searchAreaPos, 15000, 15000, 25, 25, 0.0);
        % UavState uavState = new UavState(1000, 1000, 400, 0, 0, 0, 0);
   % Escenario 2: altura 300 - celda 231 m
        % new SearchArea(searchAreaPos, 15000, 15000, 65, 65, 0.0);
        % UavState uavState = new UavState(1000, 1000, 300, 0, 0, 0, 0);
   % Escenario 3: altura 150 - celda 115 m
        % new SearchArea(searchAreaPos, 15000, 15000, 130, 130, 0.0);
        % UavState uavState = new UavState(1000, 1000, 150, 0, 0, 0, 0);
   % Prueba Radar: altura 1 - celda 1 m
        % new SearchArea(searchAreaPos, 15000, 15000, 25, 25, 0.0);
        % UavState uavState = new UavState(1000, 1000, 400, 0, 0, 0, 0);
  
   % Tamaño de celda (m), cuadrada DESCOMENTAR LA QUE SE QUIERA PROBAR:     
   %T_cel= 600; x=1000; y=1000; % Escenario 1
   %T_cel= 231; x=1000; y=1000; % Escenario 2
   %T_cel= 115; x=1000; y=1000; % Escenario 3
   T_cel=1; x=1; y=1;          % Prueba de Radar 
   
   T=readtable('matrix_radar.csv'); 
   [f,c]=size(T);
   Radar = table2array(T(:,1:c-1));
   
   % dibujo radar:
   figure,mesh(1:f,1:c-1,1-Radar)
   
   % dibujar una línea:
   % Posición del radar (m):
   x=1; y=1; 
   %x=1000; y=1000; 
   
   filaRadar=ceil(x/T_cel); columnaRadar=ceil(y/T_cel);
   celdas=[filaRadar:f];
   figure, plot(celdas,1-Radar(celdas,columnaRadar),celdas,1-Radar(celdas,columnaRadar),'o')
   title('Prob detección del radar en la celda')
   xlabel('Num. celda')
   
   figure, plot((celdas-filaRadar)*T_cel,1-Radar(celdas,columnaRadar),...
                (celdas-filaRadar)*T_cel,1-Radar(celdas,columnaRadar),'o')
   title('Prob detección del radar en distancia')
   xlabel('m')   
   