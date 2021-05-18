%% 
caso_basic=2; %1 o 2
pasos=2:3

for paso=pasos
    
    b_basic=belief{caso_basic}{paso};
    b_next=belief{caso_basic+1}{paso};
    
    b_basic_1=belief{caso_basic}{paso-1};
    b_next_1=belief{caso_basic+1}{paso-1};
    
    nolike_basic=1-sensor{caso_basic}{paso};
    nolike_next=1-sensor{caso_basic+1}{paso};
    
    b0_basic=belief{caso_basic}{1};
    b0_next=belief{caso_basic+1}{1};
    
    factores=[factorx(caso_basic+1)/factorx(caso_basic),factory(caso_basic)];
    
    %Podriamos comprobar si son parte de los otros. 
    [i_basic,j_basic]=find(abs(b_basic-b0_basic)>1e-6);
    
    [i_next ,j_next ]=find(abs(b_next-b0_next)>1e-6);
    
    i_next=unique(i_next);
    j_next=unique(j_next);
   
    i_next=[i_next(1):i_next(end)];
    j_next=[j_next(1):j_next(end)];
    
    b_next_sub=b_next(i_next,j_next);
    
    
    i1=(i_next(1)-1)*factores(1)+1;
    j1=(j_next(1)-1)*factores(2)+1;
    i2=i_next(end)*factores(1)
    j2=j_next(end)*factores(2);
    
    i_basicFromNext=i1:i2;
    j_basicFromNext=j1:j2;
    
    b_basic_1=b_basic_1(i_basicFromNext,j_basicFromNext) 
    nolike_basic=nolike_basic(i_basicFromNext,j_basicFromNext)  
    b_basic=b_basic(i_basicFromNext,j_basicFromNext)
    
 
    b_next_1=b_next_1(i_next,j_next)  
    nolike_next=nolike_next(i_next,j_next)
    b_next=b_next(i_next,j_next)   
    
   
  
    %Comprimido
    b_comp_1=acumular(b_basic_1,factores)
    nolike_comp=acumular(nolike_basic,factores)/prod(factores)
    b_comp=acumular(b_basic,factores)
       
    
    pause;
end
    