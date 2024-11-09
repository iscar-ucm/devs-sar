% Median hypothesis test with 0.05 significance
function [win,razon] = hypothesis_test(r1,r2,pvalue)

    win=0;
    razon=0;
    v1 = r1;
    v2 = r2;
    [p,H]=ranksum(v1,v2);
    if p<0.05
        r1m=median(v1);
        r2m=median(v2);
        if r1m<r2m            
            win=1;
            razon=r2m/r1m;
          elseif r2m<r1m   %sara, añadido elseif en vez  de else (pueden ser iguales )                   
            win=2;
            razon=-r1m/r2m;
        end
    end
    


end