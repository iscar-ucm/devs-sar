% Extrae el número de algoritmos
N_alg = numel(OP);

% Itera sobre los algoritmos para encontrar el máximo número de objetivos Pareto
N_paretos = 0;
idx = 0;

% Itera sobre los algoritmos para encontrar el máximo número de objetivos de Pareto
for k = 1:N_alg
    if numel(OP(k).data.objectives.paretos) > N_paretos
        N_paretos = numel(OP(k).data.objectives.paretos);
        idx = k;
    end
end

% Obtén los nombres de los objetivos del algoritmo con el máximo número
pareto_names = OP(idx).data.objectives.paretos;

% Define una paleta de colores para los algoritmos
colorPalette = [
    0, 0.4470, 0.7410;      % Azul
    0.8500, 0.3250, 0.0980; % Naranja
    0.9290, 0.6940, 0.1250; % Amarillo
    0.4940, 0.1840, 0.5560; % Violeta
    0.4660, 0.6740, 0.1880; % Verde
    0.3010, 0.7450, 0.9330; % Cyan
];

% Define las leyendas de los algoritmos
legends = {'NSGA-II','','SPEA2','','NPSO','','MODE','',};

% Define el número de secuencias
%n_seq = [6,3,1];

% Itera sobre los objetivos de Pareto
for p = 1:N_paretos
    figure;

    % Itera sobre los algoritmos OP(k)
    for k = 1:N_alg
        % Obtiene el color para el OP(k) actual
        color = colorPalette(k, :);
        used_colors(k, :) = color; % Almacena el color utilizado

        % Define el estilo de línea y grosor
        line_style = '-';
        line_width = 2;

        % Encuentra el índice de la columna del Pareto actual en OP(k)
        paretoIdx = find(strcmp(pareto_names(p), OP(k).data.objectives.paretos));

        % Verifica si el Pareto no se encontró en el algoritmo actual
        if isempty(paretoIdx)
            % Pasa al siguiente Pareto
            continue;
        end

        % Inicializamos la estructura solStats dentro del bucle del algoritmo
        solStats = struct('time', [], 'values', []);

        % Itera sobre las ejecuciones runs(r) del algoritmo actual OP(k)
        for r = 1:numel(OP(k).runs)
            % Extrae los datos de iteraciones para la ejecución actual
            solData = OP(k).runs(r).iterationsFF;

            % Filtra los datos para la primera solución sol=1 del primer frente Pareto
            solDataFiltered = solData(solData(:, 3) == "1", :);

            % -------------- JALO:
            solDataArray=str2double(solDataFiltered);
            indSec=find(diff(solDataArray(:,1))>0); %punto en el que cambia de tramo
            if isempty(indSec) 
                indSec=length(solDataArray); % Un solo tramo
            else
                indSec(end+1)=length(solDataArray);
            end
            pdNoSec=(1-solDataArray(indSec,end)); % Prob de no detección al finalizar cada secuencia            
            % --------------

            % Convierte las columnas numéricas de solDataFiltered a matrices
            time = str2double(solDataFiltered(:, 4));
            values = str2double(solDataFiltered(:, 4 + paretoIdx));

            % Inicializar solStats.time y solStats.values si están vacíos
            if isempty(solStats.time)
                solStats.time = zeros(size(time));
                solStats.values = zeros(size(values));
                solStats.squared_values = zeros(size(values));
            end

            % Agregar los valores
            solStats.time = solStats.time + time;

            % -------- Corregir ETD: %JALO
            if strcmp(pareto_names{p},'etd')
            %if 0    % Si se comenta lo anterior y se descomenta esto hace
                     % el ETD sin compensar, como antes
                t_restante=OP(k).data.sequenceTime*(length(indSec)-1);
                ETDNoDetec=ones(indSec(1),1)*pdNoSec(1)*t_restante;
                for nn=2:length(indSec)
                   t_restante=OP(k).data.sequenceTime*(length(indSec)-nn); 
                   NoDetec=ones(indSec(nn)-indSec(nn-1),1)*pdNoSec(nn); 
                   ETDNoDetec=[ETDNoDetec; NoDetec*t_restante];                   
                end                
                solStats.values = solStats.values + (values+ETDNoDetec);
                solStats.squared_values = solStats.squared_values + (values+ETDNoDetec).^2;
            else
                % Lo de antes (si no es etd)
                solStats.values = solStats.values + values;
                solStats.squared_values = solStats.squared_values + values.^2;
            end        
            % ----------------

        end

        % Calcular la media de los valores para todas las ejecuciones
        mean_time = solStats.time / numel(OP(k).runs);
        mean_values = sum(solStats.values, 2) / numel(OP(k).runs);

        % Calcular la desviación estándar de los valores para todas las ejecuciones
        sum_squared_diff = solStats.squared_values - (solStats.values.^2) / numel(OP(k).runs);
        sum_squared_diff = sum(sum_squared_diff, 2); % Sumar las diferencias cuadradas por fila
        std_deviation_values = sqrt(sum_squared_diff / (numel(OP(k).runs) - 1));

        % Calcula los límites superior e inferior para el área sombreada
        upper_bound = mean_values + std_deviation_values;
        lower_bound = mean_values - std_deviation_values;

        % Grafica los resultados para cada OP(k) en la misma figura con el mismo color y estilo de línea
        plot(mean_time, mean_values, line_style, 'Color', color, 'LineWidth', line_width); % Dibuja la línea de la media con el estilo y grosor definidos

        hold on;        

        % Rellena el área entre los límites superior e inferior
        fill([mean_time; flipud(mean_time)], [upper_bound; flipud(lower_bound)], color, 'FaceAlpha', 0.2, 'EdgeAlpha', 0);

        hold on;

    end

    % Personaliza la gráfica
    %title(sprintf('Resultados de NSGA-2 por T_{DH}'));
    xlabel_text = 'Tiempo (s)';
    ylabel_text = upper(OP(1).data.objectives.paretos{p}); % Convierte la etiqueta a mayúsculas
    xlabel(xlabel_text, 'FontWeight', 'bold', 'FontSize', 24); % Establece el texto de xlabel en negrita y tamaño de fuente 20
    ylabel(ylabel_text, 'FontWeight', 'bold', 'FontSize', 24); % Establece el texto de ylabel en negrita y tamaño de fuente 20

    % Crea leyendas para cada OP(k) y asigna el color correspondiente
    legend(legends, 'Location', 'Best', 'FontSize', 24);

    % Establece el tamaño de la fuente de los tics de los ejes
    set(gca, 'FontSize', 24);    

    grid on;

end