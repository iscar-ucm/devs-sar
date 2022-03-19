% Prueba actualización de belief en celdas de diferente tamaño
clear all,close all

% Muestreo en posiciones fijas de la celda respecto al UAV.

nm_To_m=1852; % metros
ft_To_m=0.3048; %metros
knts_To_ms=0.5144; % m/s

% Escenario
scenario=4; %1,2 o 3

Tamano_celdas=750;
pasos=40; 40; 10;
avance=200; %Tamano_celdas;


switch(scenario)
   case 1, %Scenario 1, ideal
        scaleX=Tamano_celdas;
        scaleY=Tamano_celdas;
        dx=scaleX/4; dy=scaleY/4; %Aqui pares
        celdasX=4;%80;
        celdasY=1;
        Lx=celdasX*scaleX;
        Ly=celdasY*scaleY;
        %Pos inicial
        centroX=0;Lx/scaleX/2;centroY=0;Ly/scaleY/2;
        PosIni=[centroX*scaleX+scaleX/2, centroY*scaleY+scaleY/2, 3000*ft_To_m];
        factorx=[1,4];
        factory=[1,1];
        wx=celdasX,wy=celdasY;
        %b0=ones(wx,wy)/(wx*wy); %Uniforme
        b0=[0.25 0.25 0.25 0.25]';
        Range=Tamano_celdas/2;  % m
        %NOTA: Según la función, sale 0.8 a los 1000 m.
        Pd=1;
        fun_likelihood=@(px,py,pz)fun_ideal(px,py,pz,Pd,Range);
        
    case 2, %Scenario 2, alcance 2 celdas hacia adelante
        scaleX=Tamano_celdas;
        scaleY=Tamano_celdas;
        dx=scaleX/4; dy=scaleY/4; %Aqui pares
        celdasX=4;%80;
        celdasY=1;
        Lx=celdasX*scaleX;
        Ly=celdasY*scaleY;
        %Pos inicial
        centroX=0;Lx/scaleX/2;centroY=0;Ly/scaleY/2;
        PosIni=[centroX*scaleX+scaleX/2, centroY*scaleY+scaleY/2, 3000*ft_To_m];
        factorx=[1,4];
        factory=[1,1];
        wx=celdasX,wy=celdasY;
        %b0=ones(wx,wy)/(wx*wy); %Uniforme
        b0=[0.25 0.25 0.25 0.25]';
        Range=Tamano_celdas/2;     750+750/2;  % m: celda y media       
        bx=[-Tamano_celdas/2,Tamano_celdas/2];
        by=[-Tamano_celdas/2,Tamano_celdas/2];
        Pd=1;
        fun_likelihood=@(px,py,pz)fun_ideal2(px,py,pz,Pd,bx,by);
        
   case 3, %Radar, grande o pequeño
        scaleX=Tamano_celdas;
        scaleY=Tamano_celdas;
        dx=scaleX/4; dy=scaleY/4; %Aqui pares
        celdasX=80;%80; %cambiar para radar grande
        celdasY=celdasX;
        Lx=celdasX*scaleX;
        Ly=celdasX*scaleY;
        %Pos inicial
        centroX=Lx/scaleX/2;
        centroY=Ly/scaleY/2;
        PosIni=[centroX*scaleX+scaleX/2, centroY*scaleY+scaleY/2, 3000*ft_To_m];
        factorx=[1,2,4];
        factory=[1,2,4];
        wx=celdasX,wy=celdasY;
        if 0
        b0=ones(wx,wy)/(wx*wy); %Uniforme
        else
        alcance=2;
        mu{1}=[centroX+2,centroY]*scaleX;
        cov{1}=[alcance^2,0;0,alcance^2]*scaleX*scaleY;
        mu{2}=[centroX+10,centroY]*scaleY;
        cov{2}=[alcance^2,0;0,alcance^2]*scaleX*scaleY;
        b0=init_several_gaussians2(celdasX,celdasY,mu,cov,[scaleX,scaleY],[0.5,0.5],1)
        b0=b0';
        end
        
        Range=Tamano_celdas/2; %2000;  % m
        % Medida del radar:
        Pstd=0.9; % Probabilidad de detección a distancia dstd
        dstd=1000; % distancia (m)
        %NOTA: Según la función, sale 0.8 a los 1000 m.
        fun_likelihood=@(px,py,pz)fun_Radar(px,py,pz,Pstd,dstd);
   
        
    case 4, %Scenario 4, con gaussianas
        scaleX=Tamano_celdas;
        scaleY=Tamano_celdas;
        dx=scaleX/4; dy=scaleY/4; %Aqui pares
        celdasX=40;%80;
        celdasY=40;
        Lx=celdasX*scaleX;
        Ly=celdasY*scaleY;
        %Pos inicial
        centroX=2;centroY=Ly/scaleY/2;
        PosIni=[centroX*scaleX+scaleX/2, centroY*scaleY+scaleY/2, 3000*ft_To_m];
        factorx=[1,2,4];
        factory=[1,2,4];
        wx=celdasX,wy=celdasY;
        if 0
        b0=ones(wx,wy)/(wx*wy); %Uniforme
        else
        mu{1}=[2,centroY]*scaleX;
        alcance=0.5
        cov{1}=[alcance^2,0;0,alcance^2]*scaleX*scaleY;
        mu{2}=[4,centroY]*scaleY;
        cov{2}=[alcance^2,0;0,alcance^2]*scaleX*scaleY;
        b0=init_several_gaussians2(celdasX,celdasY,mu,cov,[scaleX,scaleY],[0.5,0.5],1)
        b0=b0';
        end
        Range=2000;  % m
        %NOTA: Según la función, sale 0.8 a los 1000 m.
        Pd=1;
        fun_likelihood=@(px,py,pz)fun_ideal(px,py,pz,Pd,Range);
end


for index=1:length(factorx),  % Para registrar las diferencias entre tamaños de celdas
    wx=celdasX/factorx(index),wy=celdasY/factory(index);
    scaleX=Lx/wx; scaleY=Ly/wy;
    
    % Posición del UAV [x, y, h] (m):
    Pos=PosIni;
    
    % Belief inicial:
    %b=init_several_gaussians(wx,wy,[3,3],[1 0;0 1],1,0);
    b=acumular(b0,[factorx(index),factory(index)]);
    
    % Pintar Belief inicial:
    figure(1+10*index)
    if ~isvector(b)
    surf(b);
    end
    %Voy a ver los valores que tiene
    Pnd{index}=[];
    Pnd{index}(1)=sum(b(:));
    sensor{index}{1}=[];
    belief{index}{1}=b;
    % -------------------------
    
    
    % -------------------------------------------------------------
    % Puntos por celda (m):
    Nx=scaleX/dx; Ny=scaleY/dy;
    puntos_celda=Nx*Ny;
   
    
    % Posiciones de los puntos de medida:
    posx=-ceil(Range/dx)*dx+dx/2:dx:ceil(Range/dx)*dx-dx/2; %Mejor asi, para que esté centrado en cero
    posy=-ceil(Range/dy)*dy+dy/2:dy:ceil(Range/dy)*dy-dy/2;
    ptosX=length(posx);
    ptosY=length(posy);
    
    %-------------------------
    
    % Se precalcula el valor del radar (siempre va a ser el mismo):
    for px=1:ptosX,
        for py=1:ptosY,
            p_radar(px,py) = fun_likelihood(posx(px),posy(py),Pos(3)); 
        end;
    end;
    
    if (1),
        figure(1)
        surf(p_radar)
        title('Probabilidad del radar')
        s=sprintf('Medidas cada %3.1f m',dx); xlabel(s);
        s=sprintf('Medidas cada %3.1f m',dy); ylabel(s);
        %pause
    end;
    
    for mov=1:pasos % Movimientos del UAV, cambiar el contador final para movimiento
        
        Recorrido(:,mov)=Pos;
        
        % ---------------------------------------------------------------
        % Medidas en la posición del UAV (circular):
        baux=zeros(wx,wy); % Para acumular el belief de los distintos puntos
        ptosCelda=zeros(wx,wy); % Para medir los ptos que caen en cada celda
        
        figure(2+index*10)
        plot(Pos(1),Pos(2),'*k'); %Pinto la posición del UAV
        hold on
        for px=1:ptosX,
            for py=1:ptosY,
                x=Pos(1)+posx(px);
                y=Pos(2)+posy(py);
                plot(x,y,'o')
                posCelda=ceil([x/scaleX, y/scaleY]);
                if ~((posCelda(1)<1) || (posCelda(2)<1) || (posCelda(1)>wx) || (posCelda(2)>wy)), % Si no está fuera
                    baux(posCelda(1),posCelda(2))= baux(posCelda(1),posCelda(2))+p_radar(px,py);
                    ptosCelda(posCelda(1),posCelda(2))= ptosCelda(posCelda(1),posCelda(2))+1; %No hace falta programar, es para comprobación
                end;
            end
        end
        
        baux=baux/puntos_celda; % divido por los puntos de la celda (tamaño de celda)
        
        % --------- dibujar ----------------------
        if ~isvector(baux)
        figure(3+index*10);
        surf(baux)
        view(2);
        title('Pd del radar en cada celda');
        end
        
        i=0:scaleX:(wx)*scaleX;
        j=0:scaleY:(wy)*scaleY;
        [I,J]=meshgrid(i,j);
        figure(2+index*10)
        plot(I,J,I',J','linewidth', 0.3,'color', [1 0 0])
        title('Puntos de medida en el mapa (celdas dibujadas)');
        hold off
        
        if mov==1
            keva=1;
        end
        % --------------------------------------------
        
        % Actualizar el Belief (las ecuaciones de siempre, baux es la probabilidad
        % de detección de cada celda en el instante de medida y b el belief acumulado):
        
        b=b.*(1-baux);
        Pnd{index}(end+1)=sum(b(:));
        sensor{index}{end+1}=baux;
        belief{index}{end+1}=b;
        
        
        % Se mueve el UAV y siguiente medida:
        
        % Lo muevo en horizontal, para probar (no implemento
        % modelo de vuelo del UAV).
        
        % Supongo que la velocidad crucero es de 80 kts= 41.15 m/s => 40 m/s
        % Estimo que se tiene una medida del radar (ya con el análisis inteligente
        % de la imagen) cada 5 s.
        
        % Por tanto, se desplaza 40 * 5 = 200 m en cada medida
        
        Pos(1)=Pos(1)+avance; %200 %Vamos a ponerlo en multiplo, para que se note menos
        Pos(2)=Pos(2);
        
        % Pinto el belief:
        
        if ~isvector(b)
        figure(1+index*10)
        surf(b);
        title('Belief');
        end
        %pause(0.1)
    end;
    
    guardar_eva{index}=[scaleX,scaleY];
    st{index}=sprintf('Celda %f x %f',scaleX,scaleY);
    
end; % Tamaños de celdas


figure(2)
for i=1:length(factorx)
    hold on;plot(1-Pnd{i},'o-');  
end
legend(st{:})


analizo
