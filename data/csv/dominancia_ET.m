function [win,razon]=dominancia_ET(base,other,pvalue)

for k=1:length(base)
    %
    %        ind=sum(xAlgorithm{al}<=xAlgorithm{base}(k));%indice del  mayor xAlgorithm{al} que es menor o igual al tiempo 
    %        % de xAlgorithm{base}(k)
    %        if ind==0
    %            win(al,k)=-1;%no hay ninguna solucion del algoritmo al para un tiempo menor 
    %            %o igual al base en la iteraccion k  (se pondra color
    %            %negro)
    %            razon(al,k)=NaN;%=1
    %        else
    %yAlgorithm{base}{k};%vector de NR elementos (ET de la iteraccion k del algoritmo base)
    %                       vectorother=yAlgorithm{al}{ind}; 
                
    %Almacemanos tiempo?
    vectorbase=base(:,k);
    vectorother=other(:,k); 
                
     %           % todas las iteraciones tiene NR menos las ultimas, si en las ultimas  hay demasiadas pocas soluciones las quitamos
     %           if length(vectorother)<2 | length(vectorbase)<NR/2
     %win(al,k)=-2%no hay suficientes datos para comparar
     %              break
     %           end
               
     [win(k),razon(k)]=hypothesis_test(vectorbase,vectorother,pvalue);
               
end

