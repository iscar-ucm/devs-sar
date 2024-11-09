Paso_a_Paso = 0;
pinta_marcas = 0;

% Escenarios a pintar, modificar según necesidad.
scenarioNames = {
    'RESC-RAD2-T2', 'RESB-RAD2-T2', 'RESA-RAD2-T2'};

% Define una paleta de colores para los escenarios
% Agregar más colores si es necesario
col = {'k', 'm', 'y', 'r', 'g', 'b', 'c', [0.9, 0.9, 0.9], [1, 0.5, 0], [0.5, 0, 0.5], [0, 0.5, 0.5], [0.5, 0.5, 0]};

figure(20);
hold on;

figure(30);
hold on;

for scenarioIndex = 1:numel(scenariosData)
    
    valjs = scenariosData(scenarioIndex).valjs;    
    zone = scenariosData(scenarioIndex).zone;
    tramo = scenariosData(scenarioIndex).tramo;
    U = scenariosData(scenarioIndex).U;
    UAV_Ctrl = scenariosData(scenarioIndex).UAV_Ctrl;
    sol = scenariosData(scenarioIndex).sol;
    belief = scenariosData(scenarioIndex).belief;
    
    [N_uav, c] = size(valjs.uavs);
    [N_bel, ~] = size(sol);
    x = zone.xWidth / zone.xCells: zone.xWidth / zone.xCells: zone.xWidth;
    y = zone.yHeight / zone.yCells: zone.yHeight / zone.yCells: zone.yHeight;    

    figure(scenarioIndex);
    ms = mesh(x, y, belief(1).M);
    xlabel('x (m)');
    ylabel('y (m)');
    zlabel('p(\tau^t)');
    set(gca, 'FontSize', 20);

    %if (1)
    %    t_belief = [2000, 3600, 7200];
    %
    %    for k = 1:length(t_belief)
    %        figure(scenarioIndex + k);
    %        ind = max(find(sol(:, 3) <= t_belief(k)));
    %        if ~isempty(ind)
    %            ms2 = mesh(x, y, belief(ind).M);
    %            xlabel('x (m)');
    %            ylabel('y (m)');
    %            zlabel('p(\tau^t)');
    %            set(gca, 'FontSize', 20);
    %        end
    %    end
    %
    %    disp('Continuar???');
    %    pause;
    %end;

    figure(scenarioIndex);
    hold on;

    numNFZ = length(valjs.nfzs);
    cellWidth = zone.xWidth / zone.xCells;
    cellHeight = zone.yHeight / zone.yCells;
    Xnfz = []; Ynfz = []; Znfz = [];
    maxBel = max(max(max(cat(3, belief.M))));
    
    for k = 1:numNFZ
        X = valjs.nfzs(k).xRow;
        Y = valjs.nfzs(k).yCol;
        Xnfz(:, k) = cellWidth * [X, X, (X + 1), (X + 1)]';
        Ynfz(:, k) = cellHeight * [Y, (Y + 1), (Y + 1), Y]';
        Znfz(:, k) = maxBel * [1, 1, 1, 1]';
    end
    fill3(Xnfz, Ynfz, Znfz, 'k');

    disp('Coloca la vista del plot y Pulsa para continuar');
    pause;

    tramo_plot = zeros(1, N_uav);

    if (Paso_a_Paso)
        for k = 2:N_bel-1
            set(ms, 'Visible', 'off');
            ms = mesh(x, y, belief(k).M);
            fill3(Xnfz, Ynfz, Znfz, 'k');
            title(sprintf('Belief%d - t(%3.2f seg.)', k, sol(k+1, 3)));
            xlabel('x(m)');
            ylabel('y(m)');
            zlabel('p(\tau^t)');
            set(gca, 'FontSize', 20);
            for uk = 1:N_uav
                f = find(U(uk).dat(:, end) <= sol(k+1, 3));
                if ~isempty(f)
                    ind = f(end);
                    if U(uk).dat(ind, end) > tramo + tramo_plot(uk) * tramo
                        if pinta_marcas
                            plot3(U(uk).dat(ind, 1), U(uk).dat(ind, 2), maxBel * ones(ind, 1), strcat(col{uk}, '*'));
                        end
                        tramo_plot(uk) = tramo_plot(uk) + 1;
                    end
                    plot3(U(uk).dat(1:ind, 1), U(uk).dat(1:ind, 2), maxBel * ones(ind, 1), col{uk});
                    plot3(U(uk).dat(1, 1), U(uk).dat(1, 2), maxBel, 'ro');
                end
            end
            pause(0.2);
        end
    else
        set(ms, 'Visible', 'off');
        ms = mesh(x, y, belief(N_bel-1).M);
        fill3(Xnfz, Ynfz, Znfz, 'k');
        xlabel('x(m)');
        ylabel('y(m)');
        zlabel('p(\tau^t)');
        set(gca, 'FontSize', 20);
        pinta_tramos = [];
        for uk = 1:N_uav
            pinta_tramos = [];
            for t = tramo:tramo:valjs.uavs(uk).finalState.time
                ind_t = max(find(U(uk).dat(:, end) <= t));
                pinta_tramos = [pinta_tramos ind_t];
            end
            plot3(U(uk).dat(:, 1), U(uk).dat(:, 2), maxBel * ones(length(U(uk).dat(:, 1)), 1), 'color', col{uk});
            plot3(U(uk).dat(1, 1), U(uk).dat(1, 2), maxBel, 'ro');
            if pinta_marcas
                plot3(U(uk).dat(pinta_tramos, 1), U(uk).dat(pinta_tramos, 2), maxBel * ones(length(pinta_tramos), 1), strcat(col{uk}, '*'));
                for kj = 1:length(UAV_Ctrl(uk).dat(:, end))
                    ind_ctrl = max(find(U(uk).dat(:, end) <= UAV_Ctrl(uk).dat(kj, end)));
                    if ~isempty(ind_ctrl)
                        plot3(U(uk).dat(ind_ctrl, 1), U(uk).dat(ind_ctrl, 2), maxBel, strcat(col{uk}, 'o'));
                    end
                end
            end
        end
        view(2);
    end

    fg = gca;
    xticks(linspace(0, zone.xWidth, 7)); % Ajustar manualmente valor final con el nº de ticks
    yticks(linspace(0, zone.yHeight, 7)); % Ajustar manualmente valor final con el nº de ticks 
    set(fg, 'GridColor', 'k');
    set(fg, 'GridAlpha', 0.25);
    set(fg, 'Layer', 'top');
    set(fg, 'XMinorGrid', 'on');
    set(fg, 'YMinorGrid', 'on');
    xlabel('x (m)');
    ylabel('y (m)');
    set(gca, 'FontSize', 20);
    view([-40 35]);

    val = sol(end, 3) / tramo;
    val_tramos = [1:1:val-1] * tramo;
    pintatramos_etd = zeros(1, length(val_tramos));
    pintatramos_pd = zeros(1, length(val_tramos));

    for k = 1:length(val_tramos)
        pintatramos_etd(k) = sol(max(find(sol(1:end, 3) <= val_tramos(k))), 2);
        pintatramos_pd(k) = sol(max(find(sol(1:end, 3) <= val_tramos(k))), 1);
    end

    % Actualizar la figura 20 y 30 con los datos de este escenario
    figure(20);
    plot(sol(2:end, 3), sol(2:end, 2), 'color', col{scenarioIndex}, 'LineWidth', 2);
    if ~isempty(val_tramos)
        hold on;
        plot(val_tramos, pintatramos_etd, '*', 'color', col{scenarioIndex}, 'LineWidth', 2);
        hold off;
    end

    figure(30);
    plot(sol(2:end, 3), sol(2:end, 1), 'color', col{scenarioIndex}, 'LineWidth', 2);
    if ~isempty(val_tramos)
        hold on;
        plot(val_tramos, pintatramos_pd, '*', 'color', col{scenarioIndex}, 'LineWidth', 2);
        hold off;
    end
end

% Configuraciones finales para las figuras 20 y 30
figure(20);
xlabel('time');
ylabel('etd');
set(gca, 'FontSize', 20);
legend(scenarioNames, 'Location', 'northwest', 'FontSize', 10);

figure(30);
xlabel('time');
ylabel('dp');
set(gca, 'FontSize', 20);
legend(scenarioNames, 'Location', 'northwest', 'FontSize', 10);
