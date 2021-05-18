%% 
caso=2; %2 o 3

for paso=2:pasos

    bcs=belief{1}{paso};
    bcb=belief{caso}{paso};
    bcs2cb=acumular(bcs,[factorx(caso),factory(caso)]);
    beldif=bcb-bcs2cb;
    if any(beldif(:)>1e-10)
        disp(sprintf('paso=%d, diferencia positiva, max=%f',paso,max(beldif(:))));
    end
    if any(-beldif(:)>1e-10)
        disp(sprintf('paso=%d, diferencia negativa, max=%f',paso,min(beldif(:))));
    end
    if any(abs(beldif(:))>1e-10)
       disp(sprintf('diferencia total: %f',sum(beldif(:))));
    end

    likea=sensor{1}{paso};
    likeb=sensor{caso}{paso};
    likea2b=acumular(sensor{1}{paso},[factorx(caso),factory(caso)])/factorx(caso)/factory(caso);
    likdif=likeb-likea2b;
    if any(abs(likdif(:))>1e-10)
        disp('ojo likelihoods')
        keyboard
    end
end
    