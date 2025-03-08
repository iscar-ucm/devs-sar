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

%% Paso 1: Definir tiempos de referencia (en segundos)
N_alg = numel(mediasPareto);
startTimes = zeros(N_alg,1);
finalTimes = zeros(N_alg,1);

for k = 1:N_alg
    tiempos_k = cell2mat(mediasPareto{k}(:,1));
    startTimes(k) = tiempos_k(1);
    finalTimes(k) = tiempos_k(end);
end

% Encontrar el mínimo tiempo positivo (mayor que 0)
t_min = min(startTimes(startTimes > 0));
t_max = max(finalTimes);

N_ref = 200;  % Número de puntos de referencia (ajusta según necesites)
t_ref = linspace(t_min, t_max, N_ref); % Empezar desde t_min en vez de 0

%% Paso 2: Inicializar la matriz de dominancia
% Códigos:
%   1  -> Dominante (verde)
%  -1  -> Dominado (rojo)
%   0  -> Neutral (gris)
%   2  -> Aún no iniciado (se pintará azul)
%  -2  -> Ya finalizado (se pintará amarillo)
dominancia = zeros(N_alg, N_ref);

%% Paso 3: Para cada tiempo de referencia, determinar el registro a usar y calcular dominancia
for t_idx = 1:N_ref
    currentTime = t_ref(t_idx);
    validIdx = NaN(N_alg,1);  % Índice del registro a usar para cada algoritmo
    estado = zeros(N_alg,1);  % 0: registro válido; 2: no iniciado; -2: finalizado
    
    for k = 1:N_alg
        t_k = cell2mat(mediasPareto{k}(:,1));
        if currentTime < t_k(1)
            estado(k) = 2;  % Aún no iniciado
        elseif currentTime > t_k(end)
            estado(k) = -2; % Ya finalizó
        else
            % Último registro cuyo tiempo sea <= currentTime
            idx = find(t_k <= currentTime, 1, 'last');
            if ~isempty(idx)
                validIdx(k) = idx;
                estado(k) = 0;  % Registro válido
            else
                estado(k) = 2;
            end
        end
    end
    
    % Calcular la matriz de relaciones
    relaciones = zeros(N_alg, N_alg);  
    for k1 = 1:N_alg
        for k2 = k1+1:N_alg
            % Si el algoritmo k2 no ha comenzado, k1 lo domina automáticamente
            if estado(k2) == 2
                relaciones(k1, k2) = 1;
                relaciones(k2, k1) = -1;
                continue;
            end
            
            % Determinar qué valores usar
            if estado(k1) == -2
                idx_k1 = size(mediasPareto{k1}, 1); % Último valor alcanzado
            elseif estado(k1) == 0
                idx_k1 = validIdx(k1);
            else
                continue;
            end
            
            if estado(k2) == -2
                idx_k2 = size(mediasPareto{k2}, 1); % Último valor alcanzado
            elseif estado(k2) == 0
                idx_k2 = validIdx(k2);
            else
                continue;
            end

            % Obtener valores de los registros correctos
            values_k1 = cell2mat(mediasPareto{k1}(idx_k1, 2:end));
            values_k2 = cell2mat(mediasPareto{k2}(idx_k2, 2:end));

            % Obtener los nombres de los paretos de ambos algoritmos
            paretos_k1 = OP(k1).data.objectives.paretos;
            paretos_k2 = OP(k2).data.objectives.paretos;
            common_paretos = intersect(paretos_k1, paretos_k2, 'stable');

            % Inicializar indicadores de dominancia
            k1_improves = false;
            k2_improves = false;
            k1_not_worse = true;
            k2_not_worse = true;

            % Comparar solo los objetivos paretos comunes
            for p = 1:numel(common_paretos)
                idx_k1_pareto = find(strcmp(paretos_k1, common_paretos{p}));
                idx_k2_pareto = find(strcmp(paretos_k2, common_paretos{p}));

                value_k1 = values_k1(idx_k1_pareto);
                value_k2 = values_k2(idx_k2_pareto);

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

            % Evaluar dominancia
            if k1_improves && k1_not_worse
                relaciones(k1, k2) = 1; % K1 domina a K2
                relaciones(k2, k1) = -1;
            elseif k2_improves && k2_not_worse
                relaciones(k1, k2) = -1;
                relaciones(k2, k1) = 1;
            end
        end
    end

    % Asignar dominancia
    for k = 1:N_alg
        if estado(k) ~= 0
            dominancia(k, t_idx) = estado(k);
        else
            otros = setdiff(1:N_alg, k);
            if isempty(otros)
                dominancia(k, t_idx) = 0;
            else
                if all(relaciones(k, otros) == 1)
                    dominancia(k, t_idx) = 1;
                elseif all(relaciones(k, otros) == -1)
                    dominancia(k, t_idx) = -1;
                else
                    dominancia(k, t_idx) = 0;
                end
            end
        end
    end
end


%% Paso 4: Remapear los códigos de dominancia a índices fijos para el colormap
% Mapeo:
%   -2 -> 1  (finalizado → Amarillo)
%   -1 -> 2  (dominado → Rojo)
%    0 -> 3  (neutral → Gris)
%    1 -> 4  (dominante → Verde)
%    2 -> 5  (no iniciado → Azul)
dominanciaIndices = zeros(size(dominancia));
for i = 1:numel(dominancia)
    switch dominancia(i)
        case -2, dominanciaIndices(i) = 1;
        case -1, dominanciaIndices(i) = 2;
        case 0,  dominanciaIndices(i) = 3;
        case 1,  dominanciaIndices(i) = 4;
        case 2,  dominanciaIndices(i) = 5;
        otherwise, dominanciaIndices(i) = 3;
    end
end

%% Paso 5: Graficar
algoritmoEtiquetas = {'NSGA-II', 'SPEA2', 'NSPSO', 'OMOPSO', 'MODE'};
figure;
% Usar t_ref (en segundos) como eje X y los algoritmos en el eje Y
imagesc(t_ref, 1:N_alg, dominanciaIndices);

% Definir el colormap de 5 colores:
%   Índice 1: Amarillo (finalizado)
%   Índice 2: Rojo (dominado)
%   Índice 3: Gris (neutral)
%   Índice 4: Verde (dominante)
%   Índice 5: Azul (no iniciado)
myColormap = [1 1 0;      % Amarillo
              1 0 0;      % Rojo
              0.8 0.8 0.8; % Gris
              0 1 0;      % Verde
              0 0 1];     % Azul
colormap(myColormap);
caxis([1 5]);  % Forzar la escala del colormap

% Configurar etiquetas y formato de la gráfica
xlabel('Tiempo (s)', 'FontSize', 20, 'FontWeight', 'bold');
yticks(1:N_alg);
yticklabels(algoritmoEtiquetas);
set(gca, 'FontSize', 20, 'FontWeight', 'bold');

% Dibujar separadores entre tiempos (líneas verticales)
hold on;
for x = t_ref(2:end) - diff(t_ref)/2
    line([x x], [0.5 N_alg + 0.5], 'Color', [0.5 0.5 0.5], 'LineWidth', 0.5);
end

% Dibujar separadores entre algoritmos (líneas horizontales)
for y = 1.5:N_alg
    line([t_ref(1) t_ref(end)], [y y], 'Color', 'k', 'LineWidth', 1.5);
end
hold off;

% Ajustar altura de la figura
v = get(gcf, 'Position');
v(end) = 250; % Cambiar altura
set(gcf, 'Position', v);
