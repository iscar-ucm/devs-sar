function b=acumular(b0,factor)

if factor(1)==1 & factor(2)==1
    b=b0;
    return
end

[wx,wy]=size(b0);
wx_new=wx/factor(1);
wy_new=wy/factor(2);
for i=1:wx_new
    for j=1:wy_new
        inicioi=(i-1)*factor(1);
        inicioj=(j-1)*factor(2);
        aux=0;
        for ii=1:factor(1)
            for jj=1:factor(2)
                aux=aux+b0(ii+inicioi,jj+inicioj);
            end
        end
        b(i,j)=aux;
    end
end