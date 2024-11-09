% Ejemplo de uso para leer las optimizaciones 
% realizadas en diferentes Escenarios con FunCargarOptimización:

clear all, close all

Escenarios= {'ANNSIM2022_Acy','ANNSIM2022_Cyc'}%,'ANNSIM2022_Acy_Inc_20Sol'}
smooth=''; %'smoothess'


for k=1:length(Escenarios),
    ESC{k}.OP=FunCargarOptimizacion(Escenarios{k},smooth);
    ESC{k}.escenario=Escenarios{k};

    disp(sprintf('Escenario: %s',ESC{k}.escenario));
    ESC{k}.OP

end;
