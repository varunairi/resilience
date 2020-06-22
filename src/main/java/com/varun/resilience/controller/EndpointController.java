package com.varun.resilience.controller;

import com.varun.resilience.beanfarm.BulkHeadGenerator;
import com.varun.resilience.service.ServiceA;
import com.varun.resilience.vo.CircuitInfo;
import com.varun.resilience.vo.Status;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
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
        else if ("Reset".equalsIgnoreCase(cktInfo.getCircuitState()))
            circuitBreaker.reset();
        ResponseEntity responseBody = new ResponseEntity<Status>(new Status("Success", "Success"), HttpStatus.OK);
        return  responseBody;
    }


    @Resource
    private RateLimiterRegistry rateLimitReg;
    @GetMapping(path="/rateLimit/service")
    public ResponseEntity simulateServiceFailure(String  id, int delayInSecs) throws Exception
    {
        RateLimiter rateLimiter = this.rateLimitReg.rateLimiter("rateLimiter1");
        Supplier<String> decoratedService = Decorators.ofSupplier(()->this.serviceA.simulateSlowService(delayInSecs))
                .withRateLimiter(rateLimiter).decorate();

        String message = Try.ofSupplier(decoratedService).recover(throwable ->"Failure: " + throwable.getLocalizedMessage()).get();
        if(message.startsWith("Failure")) {
            System.out.println(id+ " Seeing Failure:" + message);
            return ResponseEntity.status(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED).build();
        }
        ResponseEntity responseBody = new ResponseEntity<Status>(new Status("Success", message), HttpStatus.OK);
        return  responseBody;
    }

    @Resource
    private BulkheadRegistry bhRegistry;
    @Resource
    private ThreadPoolBulkheadRegistry tpBhRegistry;
    @GetMapping(path="/bulkhead/service")
    public ResponseEntity getServiceForBulkHead(@RequestParam  int id, @RequestParam  int delayInSecs, @RequestParam (required = false) boolean useTP) throws ExecutionException, InterruptedException {
        Bulkhead bh = this.bhRegistry.bulkhead("bh");
        ThreadPoolBulkhead tpBh = this.tpBhRegistry.bulkhead("tpbh");
        String message ;
        if(useTP){
            System.out.println(useTP);
            Integer isFailure = new Integer(0);
            Supplier<CompletionStage<String>> supplier = tpBh.decorateSupplier(()->this.serviceA.simulateSlowService(delayInSecs));
            //this throws BulkHeadFullException when queue is full.
            CompletionStage<String> compmessage = Try.ofSupplier(supplier).onFailure(throwable-> System.out.println("Failure ")).get();

            message = compmessage.toCompletableFuture().get();
            if (message.startsWith("Failure")){
                System.out.println(id + message);
                return  new ResponseEntity<Status>(new Status("failure", message), HttpStatus.EXPECTATION_FAILED);
            }
        }
        else{
            Supplier<String> supplier =
                    Decorators.ofSupplier(()-> this.serviceA.simulateSlowService(delayInSecs)).withBulkhead(bh).decorate();
            message = Try.ofSupplier(supplier).recover(throwable -> "Failure ").get();
            if (message.startsWith("Failure")){
                System.out.println(id + message);
                return  new ResponseEntity<Status>(new Status("failure", message), HttpStatus.EXPECTATION_FAILED);
            }
        }
        return  new ResponseEntity<Status>(new Status("succes", message), HttpStatus.OK);
    }
}
