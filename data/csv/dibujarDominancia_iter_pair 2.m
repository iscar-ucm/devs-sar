% Factores pareto utilizados
factors = [1.0, 1000.0, 10.0, 1000.0, 1.0, 1.0];  % Tiempo, MYO, ETD, PD, NFZs, COL

% Número de algoritmos y ejecuciones
N_alg = numel(OP);
Nruns = numel(OP(1).runs);

% Inicializar estructura para guardar las soluciones por algoritmo
mediasPareto = cell(N_alg, 1);

% Calcular la media de los Pareto para cada algoritmo
for k = 1:N_alg
    
    % Inicializar acumuladores para las medias
    acumulador = [];

    % Iterar sobre las ejecuciones
    for r = 1:Nruns

        % Extraer las iteraciones de la ejecución actual
        solData = OP(k).runs(r).iterationsFF;

        % Filtrar los datos para reducirlos a la primera solución del
        % primer frente Pareto
        solDataFiltered = solData(solData(:, 3) == "1", :);

        % Acumular para cada secuencia e iteración los valores Pareto
        for i = 1:size(solDataFiltered, 1)

            % Extraer las columnas de interés (de la 4 a la última)
            fila = solDataFiltered(i, 4:end);

            % Convertir las columnas a números
            filaNumerica = str2double(fila);

            % Inicializar acumulador si es necesario
            if size(acumulador, 1) < i
                acumulador(i, :) = zeros(1, size(filaNumerica, 2)); % Inicializar nueva fila con ceros
            end

            % Actualizar acumulador
            acumulador(i, :) = acumulador(i, :) + filaNumerica;
        end
    end

    % Crear la estructura para guardar las soluciones con medias acumuladas
    solucionesMedia = cell(size(acumulador, 1), size(acumulador, 2));

    % Calcular la media acumulada para cada fila y asignarla a las columnas 4:end
    for i = 1:size(acumulador, 1)
        for j = 1:size(acumulador, 2)
            % Aplicar la corrección con el factor correspondiente
            factor = factors(j); % Seleccionar el factor según la columna
            solucionesMedia{i, j} = round((acumulador(i, j) / Nruns) * factor) / factor;
        end
    end

    % Almacenar las soluciones medias para este algoritmo
    mediasPareto{k} = solucionesMedia;
end

% Determinar el número mínimo de filas en las estructuras de mediasPareto
minRows = min(cellfun(@(x) size(x, 1), mediasPareto));

% Calcular los factores de escala para cada algoritmo
scaleFactors = zeros(N_alg, 1);
for k = 1:N_alg  
    % iteraciones totales (secuencias * iteraciones)
    numRows_k = size(mediasPareto{k}, 1);
    % factor de escala para comparar algoritmos con distinto número de
    % iteraciones
    scaleFactors(k) = numRows_k / minRows; 
end

% -------------------------------
% Cálculo de dominancia por parejas
% -------------------------------
% Se asume que el número de algoritmos es par.
N_pairs = floor(N_alg/2);
dominancia = zeros(N_alg, minRows); % Inicializar dominancia

for iter = 1:minRows
    for pair = 1:N_pairs
        k1 = 2*pair - 1;
        k2 = 2*pair;
        
        % Calcular las iteraciones equivalentes a comparar para cada algoritmo
        idx_k1 = round(iter * scaleFactors(k1));
        idx_k2 = round(iter * scaleFactors(k2));
        
        % Extraer los valores (omitiendo la columna del tiempo)
        values_k1 = cell2mat(mediasPareto{k1}(idx_k1, 2:end));
        values_k2 = cell2mat(mediasPareto{k2}(idx_k2, 2:end));
        
        % Obtener los nombres de los objetivos (paretos) de cada algoritmo
        paretos_k1 = OP(k1).data.objectives.paretos; 
        paretos_k2 = OP(k2).data.objectives.paretos;
        
        % Se consideran solo los objetivos comunes
        common_paretos = intersect(paretos_k1, paretos_k2, 'stable');
        
        % Inicializar flags para determinar la dominancia en el par
        k1_improves = false;
        k2_improves = false;
        k1_not_worse = true;
        k2_not_worse = true;
        
        % Comparar los objetivos comunes
        for p = 1:numel(common_paretos)
            idx_k1_pareto = find(strcmp(paretos_k1, common_paretos{p}));
            idx_k2_pareto = find(strcmp(paretos_k2, common_paretos{p}));
            
            value_k1 = values_k1(idx_k1_pareto);
            value_k2 = values_k2(idx_k2_pareto);
            
            % Para objetivos que se deben minimizar
            if ismember(common_paretos{p}, {'myo', 'etd', 'fuel', 'smooth'})
                if value_k1 < value_k2
                    k1_improves = true;
                elseif value_k1 > value_k2
                    k1_not_worse = false;
                end
                
                if value_k2 < value_k1
                    k2_improves = true;
                elseif value_k2 > value_k1
                    k2_not_worse = false;
                end
            % Para 'pd' se maximiza
            elseif strcmp(common_paretos{p}, 'pd')
                if value_k1 > value_k2
                    k1_improves = true;
                elseif value_k1 < value_k2
                    k1_not_worse = false;
                end
                
                if value_k2 > value_k1
                    k2_improves = true;
                elseif value_k2 < value_k1
                    k2_not_worse = false;
                end
            end
        end
        
        % Asignar la dominancia para la pareja
        if k1_improves && k1_not_worse
            dominancia(k1, iter) = 1;   % k1 domina a k2
            dominancia(k2, iter) = -1;  % k2 es dominado por k1
        elseif k2_improves && k2_not_worse
            dominancia(k1, iter) = -1;  % k1 es dominado por k2
            dominancia(k2, iter) = 1;   % k2 domina a k1
        else
            dominancia(k1, iter) = 0;
            dominancia(k2, iter) = 0;
        end
    end
end

algoritmoEtiquetas = {'NSGA-II-A', 'SPEA2-A', 'NSPSO-A', 'MODE-A', 'NSGA-II-B', 'SPEA2-B', 'NSPSO-B', 'MODE-B'};

%%EMPIEZO AQUI.
%Se supone que tienes que tener cargadas varias parejas: los metes en
%celdas. Como yo solo tengo un experimento, lo que hago es hacer parejas
%con ellas. 
dom{1}=dominancia(1:2,:);
algoritmoE{1} =algoritmoEtiquetas([1,5]);

dom{2}=dominancia([3,4],:);
algoritmoE{2} = algoritmoEtiquetas([2,6]);

dom{3}=dominancia([5,6],:);
algoritmoE{3} = algoritmoEtiquetas([3,7]);

dom{4}=dominancia([7,8],:);
algoritmoE{4} = algoritmoEtiquetas([4,8]);

%Aqui es donde empiezo a dibujar
figure;
cuantas=length(dom);

v=1-0.05-0.05*2-0.025*cuantas;
vs=v/cuantas;

for i=1:cuantas
%hAxis(i)=subplot(cuantas,1,i)
axes('Position',[0.18,1-(vs+0.025)*(i),0.77,vs])
imagesc(dom{i});

% Ajustar el colormap con gris claro
colormap([1 0 0; 0.8 0.8 0.8; 0 1 0]); % Rojo, Gris claro, Verde

% Ajustar límites y ticks de los ejes
ax = gca;
ax.XTickMode = 'auto'; % Dejar que MATLAB ajuste los ticks automáticamente
%xlabel('Iterations', 'FontSize', 20, 'FontWeight', 'bold'); % Cambiar texto del eje X
if i==cuantas
    xlabel('Generations', 'FontSize', 20, 'FontWeight', 'bold'); % Cambiar texto del eje X

else
    set(gca,'XTick',[])
end


% Ajustar etiquetas del eje Y
ylabel('');
title('');
yticks(1:2);
yticklabels(algoritmoE{i}); % Usar etiquetas personalizadas

% Ajustar fuente de los ticks

    set(gca, 'FontSize', 20, 'FontWeight', 'bold');


% Añadir rayas separadoras entre iteraciones (líneas verticales)
hold on;
for x = 0.5:minRows
    line([x x], [0.5 2 + 0.5], 'Color', [0.5 0.5 0.5], 'LineWidth', 0.5); % Gris oscuro
end


% Añadir rayas separadoras entre algoritmos (líneas horizontales)
for y = 1.5:2
    line([0.5 minRows + 0.5], [y y], 'Color', 'k', 'LineWidth', 1.5); % Línea negra gruesa
end

% Ajustar visualización
axis tight;
set(gca, 'FontSize', 20, 'FontWeight', 'bold', 'Layer', 'top');
hold off;
end
