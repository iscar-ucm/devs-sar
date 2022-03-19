function [b0] = init_several_gaussians2(wcol,wrow,Mu,COV,scale,weights,display)
%creates a probability map with several gaussians
%wcol corresponde al eje x
%wrow corresponde al eje y
%Mu: array where each cells contains the median x and y of the gaussian
%COV: array with the covariance matrix of the gaussians
%weigth: vector with the weigths of each gaussian
% Sara

%% Example
% M=30;N=30;
% Mu{1}=[24,23];
% COV{1}=[1,0; 0 1]
%
% Mu{2}=[13,18];
% COV{2}=[1,0; 0 1]
% Mu{3}=[11,18];
% COV{3}=[1,0; 0 1]
% Mu{4}=[9,18];
% COV{4}=[1,0; 0 1]
% Mu{5}=[7,18];
% COV{5}=[1,0; 0 1]
%
% %
%
% Mu{6}=[15,5];
%  COV{6}=[1,0; 0 1]
% Mu{7}=[15,7];
%  COV{7}=[1,0; 0 1]
%  Mu{8}=[15,9];
%  COV{8}=[1,0; 0 1]
%
% weights=[5.5,1,1,1,1,0.3,0.3,0.4]
% [b0] = init_several_gaussians(M,N,Mu,COV,weights,1)
% save(fullfile(pwd,'Data\Scenarios\MAT','belief_Gador3'),'b0')

b0=0;
numGauss=numel(Mu);



[X, Y] = meshgrid(1:1:wcol, 1:1:wrow);
for g=1:numGauss
    gaussians{g}=zeros(size(X));
    COV{g}(1,1)=COV{g}(1,1)/scale(1)^2;
    COV{g}(2,2)=COV{g}(2,2)/scale(2)^2;
    COV{g}(2,1)=COV{g}(2,1)/prod(scale(2));
    COV{g}(1,2)=COV{g}(1,2)/prod(scale(2));
    invsigma=inv(COV{g});
    factor=1; %/(2*pi*sqrt(det(COV{g})));  %JALO
    Mu{g}=Mu{g}./scale;
    for i=1:size(X,1)*size(X,2)
        xvec=[X(i),Y(i)]; xdif=xvec-Mu{g};
        aux=xdif*invsigma*xdif';%vector fila*matriz*vector columna
        gaussians{g}(i)=factor*exp(-aux/2);%matrix
    end
    gaussians{g}=gaussians{g}/sum(sum(gaussians{g}));
    b0=b0+weights(g)*gaussians{g};
end
b0 = b0/sum(sum(b0));

if display
    figure(32);
    surf(b0);
    view(2);
end
end