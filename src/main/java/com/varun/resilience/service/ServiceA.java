package com.varun.resilience.service;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
public class ServiceA {
public String simulateSlowService(int delayInSecs){
    try {
        Thread.sleep(delayInSecs*1000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    return "Success";
}

public void simlateFailedService(boolean throwException)throws Exception{
    if(throwException)throw new Exception();

}

public String failRandomly(int di) {
   double dd= Math.random();
    if(dd>0.5d)
        throw new NumberFormatException();
    return "Success";
}


    public String failRandomlyAndGiveRandomResults(int di) {
        System.out.println(di + " " + new Date());
        double dd= Math.random();
        if(dd>0.66d)
            throw new NumberFormatException();
        else if (dd< 0.33d)
            return "GoForRetry";
        return "Success";
    }
}
