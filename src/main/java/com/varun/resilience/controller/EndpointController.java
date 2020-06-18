package com.varun.resilience.controller;

import com.varun.resilience.service.ServiceA;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@RestController
public class EndpointController {
    private AtomicInteger ai = new AtomicInteger();
    int i = 0;
    private  int count=0;

    @Resource
    private ServiceA serviceA;

    @Resource(name = "slowCallCktBreakder")
    private CircuitBreakerRegistry registry;

    @Resource(name="failedCallCktBreaker")
    private CircuitBreakerRegistry failureCktBreakerRegistry;



    @GetMapping(path = "/status")
    public ResponseEntity getStatus(){
        i++;
        System.out.println(i);
        ai.incrementAndGet();
        setCount();
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping(path = "/count")
    public ResponseEntity getCount(){
        String s = "{count:" + i + ",countActual: " + count + ", atomicCount: " + ai.get() + "}";
       return new ResponseEntity(s, HttpStatus.OK);
    }

    public synchronized void setCount(){
        count++;
    }

    @GetMapping(path="/serviceTooSlow")
    public ResponseEntity simulateServiceTooSlow(int delayInSecs,String  id)
    {
        CircuitBreaker circuitBreaker = this.registry.circuitBreaker("name");

        Supplier<String> ofService = ()->serviceA.simulateSlowService(delayInSecs);
        Supplier<String> decoratedService = Decorators.ofSupplier(ofService).withCircuitBreaker(circuitBreaker).decorate();
        String message = Try.ofSupplier(decoratedService).recover(throwable ->"Failure: " + throwable.getLocalizedMessage()).get();
        System.out.println(id + ":" + message);
        ResponseEntity responseBody = new ResponseEntity<Status>(new Status("Success", message), HttpStatus.OK);
        return  responseBody;
    }

    @GetMapping(path="/serviceFailure")
    public ResponseEntity simulateServiceFailure(String  id, boolean failInstance) throws Exception
    {
        CircuitBreaker circuitBreaker = this.failureCktBreakerRegistry.circuitBreaker("failedCktBreaker");

        Supplier<String> ofService = ()-> {
            try {
                 serviceA.simlateFailedService(failInstance);
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
            return "";
        };
        Supplier<String> decoratedService = Decorators.ofSupplier(ofService).withCircuitBreaker(circuitBreaker).decorate();
        String message = Try.ofSupplier(decoratedService).recover(throwable ->"Failure: " + throwable.getLocalizedMessage()).get();
        System.out.println(id + ":" + message);
        ResponseEntity responseBody = new ResponseEntity<Status>(new Status("Success", message), HttpStatus.OK);
        return  responseBody;
    }

    @PostMapping(path="/changeCircuitState")
    public ResponseEntity closeCircuit(@RequestBody CircuitInfo cktInfo)
    {
        CircuitBreaker circuitBreaker = registry.circuitBreaker(cktInfo.getCircuitName());
        if ("HalfOpen".equalsIgnoreCase(cktInfo.getCircuitState()))
            circuitBreaker.transitionToHalfOpenState();
        else if ("Closed".equalsIgnoreCase(cktInfo.getCircuitState()))
            circuitBreaker.transitionToClosedState();
        else if ("Open".equalsIgnoreCase(cktInfo.getCircuitState()))
            circuitBreaker.transitionToOpenState();
        ResponseEntity responseBody = new ResponseEntity<Status>(new Status("Success", "Success"), HttpStatus.OK);
        return  responseBody;
    }

    private static class Status{
        private String status;
        private String message;

        public Status(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    private static class CircuitInfo{
        private String circuitName;
        private String circuitState;

        public String getCircuitName() {
            return circuitName;
        }

        public void setCircuitName(String circuitName) {
            this.circuitName = circuitName;
        }

        public String getCircuitState() {
            return circuitState;
        }

        public void setCircuitState(String circuitState) {
            this.circuitState = circuitState;
        }

        public CircuitInfo(String circuitName, String circuitState) {
            this.circuitName = circuitName;
            this.circuitState = circuitState;
        }
    }
}
