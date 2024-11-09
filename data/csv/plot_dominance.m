function plot_dominance(win,labels, name)
    nk = size(win,2);
    n_compare = size(win,1);
%     vector = ones(1,nk);
%     vector(win==1) = 1;
%     vector(win==2) = 2;  
    win = win(end:-1:1,:);
    labels = labels(end:-1:1);
    vector = [win; zeros(1,nk)];
    vector = [vector,ones(n_compare+1,1)*2];
    
    figure('Name',name);
    pcolor(vector);
    hold on;
   
    % Equal -white, 1 dominates 2 - red, 2 dominates 1 - blue
    color = [1 1 1; 0 1 0; 1 0 0];
    colormap(color);
    set(gca,'XTick',[1.5,nk+.5]);    
    set(gca,'XTickLabel',[1,nk]);
    set(gca,'YTick',1.5:n_compare+0.5);
    set(gca,'YTickLabel',labels);
    set(gca,'FontSize',14);
    set(gca,'FontWeight','demi');

%     ylabel('Algorithms');
    xlabel('(k)');
     axis tight;
end
