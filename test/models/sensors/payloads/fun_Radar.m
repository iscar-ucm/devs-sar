function val = fun_Radar(px,py,h,Pstd,dstd),

%Pstd=0.85; dstd=190; h=250;

%scale
%Pstd=0.9; dstd=250;

%Pfa=1e-6;
Pfa=1e-4;
TNR=-log(Pfa);

if(0)
i=-px:px;
j=-py:py;
[I,J]=meshgrid(i,j);
r = I.^2 + J.^2;
else
r = px.^2 + py.^2;
d = sqrt(h^2 + r);
end;

%d = sqrt(h^2 + r*scale^2);

%a=-(TNR/log(Pstd)+1)*dstd^4;
a=-(TNR/log(Pstd)+60)*dstd^4;
%a=-(TNR/log(Pstd)+80)*dstd^4;
SNR=a./d.^4;
val = (1+2*SNR*TNR./(2+SNR).^2).*exp(-2*TNR./(SNR+2));


