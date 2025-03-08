% Factores Pareto en un struct
factors = struct('myo', 1000.0, ...
                 'etd', 10.0, ...
                 'pd', 1000.0, ...
                 'fuel', 0.001, ...
                 'smooth', 0.001)

% Número de algoritmos y ejecuciones
N_alg = numel(OP);
Nruns = numel(OP(1).runs);

% Inicializar estructura para guardar las soluciones por algoritmo
mediasPareto = cell(N_alg, 1);

% Calcular la media de los Pareto para cada algoritmo
for k = 1:N_alg
    
    % Datos de paretos para el algoritmo actual
    N_paretos = numel(OP(k).data.objectives.paretos);
    pareto_names = OP(k).data.objectives.paretos;

    % Inicializar acumuladores para las medias
    acumulador = [];

    % Iterar sobre las ejecuciones
    for r = 1:Nruns

        % Extraer las iteraciones de la ejecución actual
        solData = OP(k).runs(r).iterationsFF;

        % Filtrar los datos para reducirlos a la primera solución del
        % primer frente Pareto
        solDataFiltered = solData(solData(:, 3) == "1", :);

        % Calcular el número de secuencias totales posibles
        totalSecuencias = size(solDataFiltered, 1) / OP(k).ctrlParams.stopCriteria.iterations;

        % Acumular para cada secuencia e iteración los valores Pareto
        for i = 1:size(solDataFiltered, 1)

            % Extraer las columnas de interés (de la 4 hasta el último pareto)
            fila = solDataFiltered(i, 4:4+N_paretos);

            % Convertir las columnas a números
            filaNumerica = str2double(fila);

            % Inicializar acumulador si es necesario
            if size(acumulador, 1) < i
                acumulador(i, :) = zeros(1, size(filaNumerica, 2)); % Inicializar nueva fila con ceros
            end

            % Actualizar el acumulador para el tiempo
            acumulador(i, 1) = acumulador(i, 1) + filaNumerica(1);

            % itera para cada uno de los paretos
            for p = 1:N_paretos
                if strcmp(pareto_names{p},'etd')
                    % -------- Corregir ETD
                    % Calcular el tiempo restante basado en la secuencia actual
                    secuenciaActual = ceil(i / OP(k).ctrlParams.stopCriteria.iterations);
                    t_restante = OP(k).data.sequenceTime * (totalSecuencias - secuenciaActual);
                    % Aplicar la corrección
                    acumulador(i, p+1) = acumulador(i, p+1) + (filaNumerica(p+1) + t_restante);
                else
                    % Lo de antes (si no es etd)
                    acumulador(i, p+1) = acumulador(i, p+1) + filaNumerica(p+1);
                end
            end
        end
    end

    % Crear la estructura para guardar las soluciones con medias acumuladas
    solucionesMedia = cell(size(acumulador, 1), size(acumulador, 2));

    % Calcular la media acumulada para cada fila y asignarla a las columnas 4:end
    for i = 1:size(acumulador, 1)
        for j = 1:size(acumulador, 2)
            if j == 1
                % La primera columna es el tiempo, no aplicar factor
                solucionesMedia{i, j} = round((acumulador(i, j) / Nruns) * 1.0) / 1.0;
            else
                % Obtener el nombre del pareto correspondiente (desplazado por 1)
                pareto_name = pareto_names{j-1}; % j-1 porque acumulador incluye tiempo

                % Buscar el factor correspondiente al pareto actual
                if isfield(factors, pareto_name)
                    factor = factors.(pareto_name);
                else
                    factor = 1.0; % Si no está en la lista, aplicar factor 1 (sin cambio)
                end

                % Aplicar la corrección con el factor correspondiente
                solucionesMedia{i, j} = round((acumulador(i, j) / Nruns) * factor) / factor;
            end
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

% Inicializar matriz de dominancia con el tamaño adecuado
dominancia = zeros(N_alg, minRows); % 0 = gris, 1 = verde, -1 = rojo

% Evaluar dominancia usando las medias
for iter = 1:minRows
    % Inicializar una matriz para registrar la relación de dominancia entre algoritmos
    relaciones = zeros(N_alg, N_alg); % 1 = k1 domina k2, -1 = k2 domina k1, 0 = sin relación

    % Comparar cada algoritmo contra todos los demás (sin repeticiones)
    for k1 = 1:N_alg
        for k2 = k1+1:N_alg % k2 comienza en k1+1 para evitar duplicados
            % Calcular las iteraciones equivalentes a comparar
            idx_k1 = iter * scaleFactors(k1);
            idx_k2 = iter * scaleFactors(k2);

            % Obtener valores de la fila correspondiente (omitiendo el tiempo columna 1)
            values_k1 = cell2mat(mediasPareto{k1}(idx_k1, 2:end));
            values_k2 = cell2mat(mediasPareto{k2}(idx_k2, 2:end));

            % Obtener los nombres de los paretos de ambos algoritmos
            paretos_k1 = OP(k1).data.objectives.paretos; 
            paretos_k2 = OP(k2).data.objectives.paretos;

            % Encontrar los objetivos paretos comunes entre k1 y k2
            common_paretos = intersect(paretos_k1, paretos_k2, 'stable');

            % Inicializar variables para verificar dominancia
            k1_improves = false;
            k2_improves = false;
            k1_not_worse = true;
            k2_not_worse = true;

            % Comparar solo los objetivos paretos comunes
            for p = 1:numel(common_paretos)
                % Obtener los índices de los objetivos paretos en cada algoritmo
                idx_k1_pareto = find(strcmp(paretos_k1, common_paretos{p}));
                idx_k2_pareto = find(strcmp(paretos_k2, common_paretos{p}));

                % Comparar los valores correspondientes de los objetivos comunes
                value_k1 = values_k1(idx_k1_pareto); % columna 1 es el tiempo
                value_k2 = values_k2(idx_k2_pareto); % columna 1 es el tiempo

                % Evaluar dominancia considerando los tipos de Pareto (minimizar/maximizar)
                if ismember(common_paretos{p}, {'myo', 'etd', 'fuel', 'smooth'}) % Minimizar
                    if value_k1 < value_k2
                        k1_improves = true; % K1 mejora en este objetivo
                    elseif value_k1 > value_k2
                        k1_not_worse = false; % K1 es peor en este objetivo
                    end

                    if value_k2 < value_k1
                        k2_improves = true; % K2 mejora en este objetivo
                    elseif value_k2 > value_k1
                        k2_not_worse = false; % K2 es peor en este objetivo
                    end
                elseif strcmp(common_paretos{p}, 'pd') % Maximizar
                    if value_k1 > value_k2
                        k1_improves = true; % K1 mejora en este objetivo
                    elseif value_k1 < value_k2
                        k1_not_worse = false; % K1 es peor en este objetivo
                    end

                    if value_k2 > value_k1
                        k2_improves = true; % K2 mejora en este objetivo
                    elseif value_k2 < value_k1
                        k2_not_worse = false; % K2 es peor en este objetivo
                    end
                end 
            end

            % Evaluar la relación de dominancia entre k1 y k2
            if k1_improves && k1_not_worse
                relaciones(k1, k2) = 1; % K1 domina a K2
                relaciones(k2, k1) = -1; % K2 es dominado por K1
            elseif k2_improves && k2_not_worse
                relaciones(k1, k2) = -1; % K1 es dominado por K2
                relaciones(k2, k1) = 1; % K2 domina a K1
            end
        end
    end

    % Determinar los colores para cada algoritmo en esta iteración
    for k1 = 1:N_alg
        % Ignorar la diagonal al evaluar dominancia
        sin_diagonal = setdiff(1:N_alg, k1);

        if all(relaciones(k1, sin_diagonal) == 1) % Domina a todos los demás
            dominancia(k1, iter) = 1; % Verde
        elseif all(relaciones(k1, sin_diagonal) == -1) % Es dominado por todos los demás
            dominancia(k1, iter) = -1; % Rojo
        else
            dominancia(k1, iter) = 0; % Gris
        end
    end
end

algoritmoEtiquetas = {'NSGA-II-225', 'NSGA-II-450', 'NSGA-II-900'};

% Gráfica con los resultados
figure;
imagesc(dominancia);

% Ajustar el colormap con gris claro
colormap([1 0 0; 0.8 0.8 0.8; 0 1 0]); % Rojo, Gris claro, Verde

% Ajustar límites y ticks de los ejes
ax = gca;
ax.XTickMode = 'auto'; % Dejar que MATLAB ajuste los ticks automáticamente
xlabel('Generations', 'FontSize', 20, 'FontWeight', 'bold'); % Cambiar texto del eje X

% Ajustar etiquetas del eje Y
ylabel('');
title('');
yticks(1:N_alg);
yticklabels(algoritmoEtiquetas); % Usar etiquetas personalizadas

% Ajustar fuente de los ticks
set(gca, 'FontSize', 20, 'FontWeight', 'bold');

% Añadir rayas separadoras entre iteraciones (líneas verticales)
hold on;
for x = 0.5:minRows
    line([x x], [0.5 N_alg + 0.5], 'Color', [0.5 0.5 0.5], 'LineWidth', 0.5); % Gris oscuro
end

% Añadir rayas separadoras entre algoritmos (líneas horizontales)
for y = 1.5:N_alg
    line([0.5 minRows + 0.5], [y y], 'Color', 'k', 'LineWidth', 1.5); % Línea negra gruesa
end

% Ajustar visualización
axis tight;
set(gca, 'FontSize', 20, 'FontWeight', 'bold', 'Layer', 'top');
hold off;

%Las tres lineas que he añadido para ajustar las gráfica en altura. El
%valor de 200 o el que quieras .... hasta que ajuste
v=get(gcf,'Position')
v(end)=250; %cambiamos la altura;
set(gcf,'Position',v);
