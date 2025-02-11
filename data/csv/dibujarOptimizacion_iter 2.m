% Extrae el número de algoritmos
N_alg = numel(OP);

% Itera sobre los algoritmos para encontrar el máximo número de objetivos Pareto
N_paretos = 0;
idx = 0;
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
    0.8500, 0.3250, 0.0980;   % Naranja
    0.9290, 0.6940, 0.1250;   % Amarillo
    0.4940, 0.1840, 0.5560;   % Violeta
    0.4660, 0.6740, 0.1880;   % Verde
    0.3010, 0.7450, 0.9330;   % Cyan
];

% Define las leyendas de los algoritmos
legends = {'NSGA-II','','SPEA2','','NPSO','','MODE','',};

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
        if isempty(paretoIdx)
            continue;
        end

        % Inicializamos la estructura solStats (no usamos tiempo, sino iteraciones)
        solStats = struct('values', [], 'squared_values', []);
        
        % Itera sobre las ejecuciones runs(r) del algoritmo actual OP(k)
        for r = 1:numel(OP(k).runs)
            % Extrae los datos de iteraciones para la ejecución actual
            solData = OP(k).runs(r).iterationsFF;
            % Filtra los datos para la primera solución (sol=1) del primer frente Pareto
            solDataFiltered = solData(solData(:, 3) == "1", :);
            
            % ----- CORRECCIÓN ETD (igual que antes) -----
            solDataArray = str2double(solDataFiltered);
            indSec = find(diff(solDataArray(:,1)) > 0); % Punto en el que cambia de tramo
            if isempty(indSec)
                indSec = length(solDataArray); % Un solo tramo
            else
                indSec(end+1) = length(solDataArray);
            end
            pdNoSec = (1 - solDataArray(indSec, end)); % Prob. de no detección al finalizar cada secuencia            
            % --------------------------------------------
            
            % Extrae los valores: 
            % Se asume que en solDataFiltered, la columna 4 es Tiempo y las siguientes corresponden a los objetivos.
            % Como ahora usaremos el número de iteración, nos interesa la columna (4+paretoIdx) para el valor del objetivo.
            values = str2double(solDataFiltered(:, 4 + paretoIdx));
            
            % Corregir ETD si el objetivo es 'etd'
            if strcmpi(pareto_names{p}, 'etd')
                t_restante = OP(k).data.sequenceTime * (length(indSec) - 1);
                ETDNoDetec = ones(indSec(1),1) * pdNoSec(1) * t_restante;
                for nn = 2:length(indSec)
                    t_restante = OP(k).data.sequenceTime * (length(indSec) - nn);
                    NoDetec = ones(indSec(nn) - indSec(nn-1), 1) * pdNoSec(nn);
                    ETDNoDetec = [ETDNoDetec; NoDetec * t_restante];
                end                
                values = values + ETDNoDetec;
            end        
            
            % Inicializar solStats.values y solStats.squared_values en la primera ejecución
            if isempty(solStats.values)
                solStats.values = zeros(size(values));
                solStats.squared_values = zeros(size(values));
            end
            
            % Acumular los valores (por iteración)
            solStats.values = solStats.values + values;
            solStats.squared_values = solStats.squared_values + values.^2;
        end

        % Calcular la media de los valores para todas las ejecuciones
        mean_values = solStats.values / numel(OP(k).runs);
        % Calcular la desviación estándar de los valores para todas las ejecuciones
        sum_squared_diff = solStats.squared_values - (solStats.values.^2) / numel(OP(k).runs);
        sum_squared_diff = sum(sum_squared_diff, 2); % Sumar las diferencias cuadradas por fila
        std_deviation_values = sqrt(sum_squared_diff / (numel(OP(k).runs) - 1));

        % Límites superior e inferior para el área sombreada
        upper_bound = mean_values + std_deviation_values;
        lower_bound = mean_values - std_deviation_values;

        % Usar el número de iteración como eje x
        iter = (1:size(mean_values, 1))';
        
        % Graficar la línea de la media y el área sombreada
        plot(iter, mean_values, line_style, 'Color', color, 'LineWidth', line_width);
        hold on;
        fill([iter; flipud(iter)], [upper_bound; flipud(lower_bound)], color, 'FaceAlpha', 0.2, 'EdgeAlpha', 0);
        hold on;
    end

    % Personaliza la gráfica: ahora el eje x es "Iteration" y no "Tiempo (s)"
    xlabel('Generations', 'FontWeight', 'bold', 'FontSize', 24);
    ylabel(upper(OP(1).data.objectives.paretos{p}), 'FontWeight', 'bold', 'FontSize', 24);
    legend(legends, 'Location', 'Best', 'FontSize', 24);
    set(gca, 'FontSize', 24);
    v=get(gcf,'Position')
    v(end)=350; %cambiamos la altura;
    set(gcf,'Position',v);    
    grid on;
end
