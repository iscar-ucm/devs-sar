% ============================
% 1. Cálculo de medias Pareto
% ============================
% Factores pareto utilizados
factors = [1.0, 1000.0, 10.0, 1000.0, 1.0, 1.0];  % Tiempo, MYO, ETD, PD, NFZs, COL

% Número de algoritmos y ejecuciones
N_alg = numel(OP);
Nruns = numel(OP(1).runs);

% Inicializar estructura para guardar las soluciones por algoritmo
mediasPareto = cell(N_alg, 1);

% Calcular la media de los Pareto para cada algoritmo
for k = 1:N_alg    
    acumulador = [];
    for r = 1:Nruns
        solData = OP(k).runs(r).iterationsFF;
        % Filtrar los datos: quedarnos con la primera solución del primer frente Pareto
        solDataFiltered = solData(solData(:, 3) == "1", :);
        for i = 1:size(solDataFiltered, 1)
            % Se extraen las columnas de interés (desde la 4 en adelante)
            fila = solDataFiltered(i, 4:end);
            % Convertir a numérico
            filaNumerica = str2double(fila);
            if size(acumulador, 1) < i
                acumulador(i, :) = zeros(1, numel(filaNumerica));
            end
            acumulador(i, :) = acumulador(i, :) + filaNumerica;
        end
    end
    % Calcular las medias para cada fila (dividiendo entre Nruns) y aplicar corrección
    solucionesMedia = cell(size(acumulador, 1), size(acumulador, 2));
    for i = 1:size(acumulador, 1)
        for j = 1:size(acumulador, 2)
            factor = factors(j);
            solucionesMedia{i, j} = round((acumulador(i, j) / Nruns) * factor) / factor;
        end
    end
    mediasPareto{k} = solucionesMedia;
end

% =====================================================
% 2. Comparación de algoritmos basándose en el tiempo
% =====================================================
% Se asume que en cada mediasPareto{k}, la primera columna es el tiempo (en s)
% y las siguientes columnas son los valores de los objetivos.
% Primero, determinamos el máximo tiempo común: el menor de los tiempos máximos alcanzados.
T_common = inf;
for k = 1:N_alg
    % Convertir la columna 1 a vector numérico (asumiendo que ya lo es)
    times_k = cell2mat(mediasPareto{k}(:, 1));
    T_common = min(T_common, max(times_k));
end

% Definir un vector de tiempos comunes. Aquí usamos como número de muestras
% el mínimo número de registros (filas) entre algoritmos, pero puedes cambiarlo.
N_common = min(cellfun(@(x) size(x, 1), mediasPareto));
commonTimes = linspace(0, T_common, N_common);

% Inicializar matriz de dominancia: filas = algoritmos, columnas = instantes de tiempo
dominancia = zeros(N_alg, N_common); %  0: sin relación, 1: domina, -1: es dominado

% Para cada instante de tiempo, se obtiene la última solución de cada algoritmo
% cuyo tiempo <= t, y se comparan los objetivos.
for i = 1:N_common
    t = commonTimes(i);
    % Para cada algoritmo, encontrar el índice de la solución en el tiempo t
    indices = zeros(N_alg, 1);
    for k = 1:N_alg
        times_k = cell2mat(mediasPareto{k}(:, 1));
        idx = find(times_k <= t, 1, 'last');
        if isempty(idx)
            idx = 1;  % Si no se encuentra, se usa la primera solución disponible
        end
        indices(k) = idx;
    end

    % Comparar cada par de algoritmos para el instante t
    relaciones = zeros(N_alg, N_alg); % 1: k1 domina a k2, -1: k2 domina a k1, 0: sin relación
    for k1 = 1:N_alg
        for k2 = k1+1:N_alg
            % Extraer valores de los objetivos (excluyendo la columna del tiempo)
            values_k1 = cell2mat(mediasPareto{k1}(indices(k1), 2:end));
            values_k2 = cell2mat(mediasPareto{k2}(indices(k2), 2:end));
            
            % Obtener nombres de los objetivos (paretos) de cada algoritmo
            paretos_k1 = OP(k1).data.objectives.paretos; 
            paretos_k2 = OP(k2).data.objectives.paretos;
            % Se consideran solo los objetivos comunes
            common_paretos = intersect(paretos_k1, paretos_k2, 'stable');
            
            % Variables para determinar la dominancia
            k1_improves = false;
            k2_improves = false;
            k1_not_worse = true;
            k2_not_worse = true;
            
            % Comparar los objetivos comunes
            for p = 1:numel(common_paretos)
                % Encontrar la posición del objetivo en cada vector
                idx_k1_pareto = find(strcmp(paretos_k1, common_paretos{p}));
                idx_k2_pareto = find(strcmp(paretos_k2, common_paretos{p}));
                
                value_k1 = values_k1(idx_k1_pareto);
                value_k2 = values_k2(idx_k2_pareto);
                
                % Se asume que para ciertos objetivos se minimiza y para 'pd' se maximiza
                if ismember(common_paretos{p}, {'myo', 'etd', 'fuel', 'smooth'}) % Minimizar
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
                elseif strcmp(common_paretos{p}, 'pd') % Maximizar
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
            
            % Determinar la relación de dominancia entre k1 y k2 en este instante
            if k1_improves && k1_not_worse
                relaciones(k1, k2) = 1;
                relaciones(k2, k1) = -1;
            elseif k2_improves && k2_not_worse
                relaciones(k1, k2) = -1;
                relaciones(k2, k1) = 1;
            end
        end
    end

    % Asignar el color según la dominancia: 1 (verde) si domina a todos, -1 (rojo) si es dominado, 0 (gris) en otro caso
    for k = 1:N_alg
        otros = setdiff(1:N_alg, k);
        if all(relaciones(k, otros) == 1)
            dominancia(k, i) = 1;
        elseif all(relaciones(k, otros) == -1)
            dominancia(k, i) = -1;
        else
            dominancia(k, i) = 0;
        end
    end
end

% =====================================================
% 3. Visualización de la dominancia en función del tiempo
% =====================================================
algoritmoEtiquetas = {'NSGA-II', 'SPEA2', 'NSPSO', 'MODE'};

figure;
imagesc(dominancia);
colormap([1 0 0; 0.8 0.8 0.8; 0 1 0]); % Rojo, Gris claro, Verde

% Para el eje X, se muestran algunos ticks basados en el tiempo
ax = gca;
% Definir ticks de tiempo de forma automática (o bien personalizarlos)
ax.XTickMode = 'auto';
xlabel('Time (s)', 'FontSize', 20, 'FontWeight', 'bold');

ylabel('');
title('');
yticks(1:N_alg);
yticklabels(algoritmoEtiquetas);
set(gca, 'FontSize', 20, 'FontWeight', 'bold');

hold on;
% Líneas verticales para separar los instantes de tiempo
for x = 0.5:size(dominancia,2)
    line([x x], [0.5 N_alg+0.5], 'Color', [0.5 0.5 0.5], 'LineWidth', 0.5);
end
% Líneas horizontales para separar los algoritmos
for y = 1.5:N_alg
    line([0.5 size(dominancia,2)+0.5], [y y], 'Color', 'k', 'LineWidth', 1.5);
end

axis tight;
set(gca, 'Layer', 'top');
hold off;
