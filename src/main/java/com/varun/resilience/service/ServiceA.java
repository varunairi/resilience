package com.varun.resilience.service;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

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
}
