% Variables de control
mostrarLeyendas = true; % Control de aparición de las leyendas
mostrarNumerosSecuencia = true; % Control de aparición de los números de secuencia

% Leyendas
leyenda1 = '$ETD=415s$';
leyenda2 = '$P_d=0.95$';
%leyenda3 = '$WCT=455s\pm3$';
% Posiciones de las leyendas en el eje Y (ajustar según sea necesario)
y_pos = 1.05;  % Posición inicial
delta_y = 0.08; % Espacio entre las leyendas


col = ['m', 'y', 'y', 'g', 'r']; % Colores de los UAV
maxBel = 0.001;
drawControlPoints = false; % Variable para controlar si se pintan los instantes de control

% Definición de t_belief (modificar según sea necesario)
%t_belief = [sol(1, 3), sol(end, 3)]; % sin secuencias
%t_belief = [sol(1, 3), sol(end, 3)]; % sin secuencias
%t_belief = [sol(1, 3), 900, 1800, 2700, 3600, 4500, 5400, 6300, sol(end, 3)]; % op_s1 900
%t_belief = [sol(1, 3), 1800, 3600, 5400, sol(end, 3)]; % op_s1 1800
%t_belief = [sol(1, 3), 500, 1000, 1500, 2000, 2500, 3000, 3500, sol(end, 3)]; % op_s2_s3 500
%t_belief = [sol(1, 3), 1000, 2000, 3000, sol(end, 3)]; % op_s2_s3 1000
t_belief = [sol(1, 3), 225, 450, 675, sol(end, 3)]; % op_s4 225
%t_belief = [sol(1, 3), 450, sol(end, 3)]; % op_s4 450
%t_belief = [sol(1, 3), 250, 500, 750, 1000, 1250, sol(end, 3)];%op_s5_250
%t_belief = [sol(1, 3), 500, 1000, sol(end, 3)]; %op_s5_500
%t_belief = [sol(1, 3), 225, 450, 675, 900, 1125, 1350, 1575, sol(end, 3)]; %op_s6_225
%t_belief = [sol(1, 3), 450, 900, 1350, sol(end, 3)]; %op_s6_450

% Configuración de zonas de exclusión
numNFZ = length(valjs.nfzs);
cellWidth = xWidth / xCells;
cellHeight = yHeight / yCells;
Xnfz = []; Ynfz = []; Znfz = [];
for k = 1:numNFZ
    X = valjs.nfzs(k).xRow;
    Y = valjs.nfzs(k).yCol;
    Xnfz(:, k) = cellWidth * [X, X, (X + 1), (X + 1)]';
    Ynfz(:, k) = cellHeight * [Y, (Y + 1), (Y + 1), Y]';
    Znfz(:, k) = maxBel * [1, 1, 1, 1]';  % Ajustar la altura de las NFZs a la de las trayectorias
end

% Plotear figuras adicionales
figure(100);
plot(sol(2:end, 3), sol(2:end, 1));
xlabel('time');
ylabel('dp');
set(gca, 'FontSize', 20);

figure(101);
plot(sol(2:end, 3), sol(2:end, 2));
xlabel('time');
ylabel('etd');
set(gca, 'FontSize', 20);

% Recorrer tiempos de belief y plotear figuras
for k = 1:length(t_belief)
    t_belief_k = t_belief(k);
    [~, ind] = min(abs(sol(:, 3) - t_belief_k));
    if ~isempty(ind)
        % Crear y configurar una nueva figura
        figure(k);  % Utilizar k como número de figura
        ms = mesh(x, y, belief(ind).M);
        
        % Configurar ejes y establecer ticks
        fg = gca;
        %xlim([0, xWidth]); % Establecer límites en el eje X
        %ylim([0, yHeight]); % Establecer límites en el eje Y        
        xticks(linspace(0, xWidth, 4));
        yticks(linspace(0, yHeight, 3));
        set(fg, 'GridColor', 'k');
        set(fg, 'GridAlpha', 0.25);
        set(fg, 'Layer', 'top');
        set(fg, 'XMinorGrid', 'on');
        set(fg, 'YMinorGrid', 'on');
        xlabel('x (m)');
        ylabel('y (m)');
        zlabel('p(\tau^t)');
        set(gca, 'FontSize', 20);   
        view([-35 40]);        
        hold on;
        
        % Plotear trayectorias de UAVs en esta figura de belief
        for uk = 1:N_uav
            f = find(U(uk).dat(:, end) <= sol(ind, 3));
            if ~isempty(f)
                ind_uav = f(end);
                line(U(uk).dat(1:ind_uav, 1), U(uk).dat(1:ind_uav, 2), maxBel * ones(ind_uav, 1), ...
                    'LineWidth', 1.5, 'Color', col(uk));
                plot3(U(uk).dat(1, 1), U(uk).dat(1, 2), maxBel, 'ro', 'MarkerSize', 7, 'MarkerEdgeColor', col(uk), 'MarkerFaceColor', col(uk));
                
                if drawControlPoints
                    ctrl_indices = find(UAV_Ctrl(uk).dat(:, end) <= t_belief_k);
                    for kj = 1:length(ctrl_indices)
                        ind_ctrl = max(find(U(uk).dat(:, end) <= UAV_Ctrl(uk).dat(ctrl_indices(kj), end)));
                        if ~isempty(ind_ctrl)
                            plot3(U(uk).dat(ind_ctrl, 1), U(uk).dat(ind_ctrl, 2), maxBel, ...
                                'Marker', '*', 'MarkerEdgeColor', col(uk), 'LineWidth', 1);
                        end
                    end
                end
            end
        end
        
        % Añadir indicadores de secuencia
        for j = 1:k-1
            for uk = 1:N_uav
                [~, ind_start] = min(abs(U(uk).dat(:, end) - t_belief(j)));
                plot3(U(uk).dat(ind_start, 1), U(uk).dat(ind_start, 2), maxBel, 'go', 'MarkerSize', 7, 'MarkerEdgeColor', col(uk), 'MarkerFaceColor', col(uk));
                
                % Mostrar números de secuencia si mostrarNumerosSecuencia es true
                if mostrarNumerosSecuencia
                    text(U(uk).dat(ind_start, 1), U(uk).dat(ind_start, 2), maxBel, [num2str(j)], ...
                         'HorizontalAlignment', 'left', 'VerticalAlignment', 'bottom', 'FontSize', 12, 'FontWeight', 'bold', 'Color', col(uk));
                end

                [~, ind_end] = min(abs(U(uk).dat(:, end) - t_belief(j+1)));
                plot3(U(uk).dat(ind_end, 1), U(uk).dat(ind_end, 2), maxBel, 'go', 'MarkerSize', 7, 'MarkerEdgeColor', col(uk), 'MarkerFaceColor', col(uk));
                
                % Mostrar números de secuencia si mostrarNumerosSecuencia es true
                if mostrarNumerosSecuencia
                    text(U(uk).dat(ind_end, 1), U(uk).dat(ind_end, 2), maxBel, [num2str(j+1)], ...
                         'HorizontalAlignment', 'left', 'VerticalAlignment', 'bottom', 'FontSize', 12, 'FontWeight', 'bold', 'Color', col(uk));
                end
            end
        end
        
        % Pintar las NFZs a la misma altura que las trayectorias
        fill3(Xnfz, Ynfz, Znfz, 'k');     

        % Añadir leyendas si mostrarLeyendas es true
        if mostrarLeyendas
            % Alinear todas las leyendas a la izquierda y ajustar espaciado
            text(0.05, y_pos, leyenda1, 'Units', 'normalized', 'HorizontalAlignment', 'left', ...
                'VerticalAlignment', 'top', 'Interpreter', 'latex', 'FontSize', 20, 'FontWeight', 'bold');
            text(0.05, y_pos - delta_y, leyenda2, 'Units', 'normalized', 'HorizontalAlignment', 'left', ...
                'VerticalAlignment', 'top', 'Interpreter', 'latex', 'FontSize', 20, 'FontWeight', 'bold');
            %text(0.05, y_pos - 2 * delta_y, leyenda3, 'Units', 'normalized', 'HorizontalAlignment', 'left', ...
            %    'VerticalAlignment', 'top', 'Interpreter', 'latex', 'FontSize', 20, 'FontWeight', 'bold');
        end
        
        hold off;
    end
end