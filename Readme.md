# Resilience 4j Examples !

Resilience4J provides CircuitBreaker, BulkHead, Retries to Java 8 based Applications. Here are some of the examples that I tried. 

# Circuit Breaker
Circuit Breaker functionality allows you to handle either slow calls to an API (to avoid overwhelming the target system ) OR handle with Failures Downstream or **both**. 
Here is what you need to check in the code. 

 - ***Example 1 - Circuit Breaker with Slow calls***: This example shows how the downstream  API's slow response can trigger a circuit breaker to Open. Then under this example, the circuit does not auto close but it is done by an external trigger through an API call.
 
 > The CircuitBreakerGenerator.java has a CircuitBreakerRegistry with CircuitBreakerConfig set to trigger whenever 30% of all calls in a "sliding window" of size 5 (most recent calls to downstream API) are slow (response time > 5 secs). The Circuit Breaker kicks in after a "minimum number of calls". The Service is /serviceTooSlow and to reset the circuit breaker is /changeCircuitState

  - ***Example 2 - Service Call that regularly fails***: This example shows when downstream API produces frequent failures / exceptions causing circuit to open. 
  > The CircuitBreakerGenerator.java is injecting a bean for CircuitBreakerRegistry "*failedCallCktBreaker*" that configures the following parameters : Minimum number of calls to be made before cktbreaker kicks in, failure threshold in % of all calls (min calls described before too takes part in calculating stats),
  sliding window size and type, and **waitDuration** which automatically resets from OPEN to HALF OPEN State (looks like automaticTransitionFromOpenToHalfOpenEnabled does not have an effect). The service is /serviceFailure and /changeCircuitState is used to reset the Ckt Breaker.
  
# Rate Limiter
Rate Limiter presents an oppurtunity to throttle access to an API or a method and define the number of requests per time duration that are allowed to access that method/API. It can either timeout the waiting threads (above the rate limit) or allows the user to handle them (queue them).
A typical example would be if you want to limit the access an API to be called only 20 times/Minute. Any thing more will either be queued or rejected depending on your config.
*One Interesting Observation* is if your config says 20 request/sec, and underlying API takes more than a Second to respond, then RateLimiter would allow additional 20 to get access after a "sec", and then 20 after that. So if you are particular about how many threads are running inside the 
protected API/method, you have to use other methods with it OR define Timeouts on the API OR change configuration of Rate Limiter (which can be done dynamcially as well through methods available on RateLimiter)

 - ***Example 1 - RateLimiter that allows 20 request/5Seconds***: This example shows how the downstream  API limits the requests to 20 request/5 secnds. When run through JMEter with 50 threads, all 50 went into the Controller's method but only 20 returned, then the next 20 get in and finally the last 10 timed out as they were waiting a long time (per the config)
 
 > The RateLimiterGenerator.java has a RateLimitRegistry that allows 20 (limitForPeriod) requests in 5 secs (limitRefreshPeriod) and making any thread waiting more than 9 secs to timeout (timeoutDuration). 
    The Service /rateLimit/service calls the downstream service after decorating with RateLimiter. This is tested using Apache JMEter file (RateLimiter-TG) 

## BulkHeads

Bulk head essentially means separating failures. This can be done either by separating the consumer/client's requests to specific instances of a service OR at the client side by implementing separate thread pools for each service so that a slow/unresponsive/failed service does not overwhelm the client.
There are 2 types of Bulkheads available: 
 - Semaphore Based: Defines a behavior where max Concurrent Calls are allowed to a limit and you can reject any other threads that are waiting for attention more than a pre set time. 
 - ThreadPool Based: Fixed Threadpool with Core and Max Thread Pools and a Bounded Queue. Anything above Bounded Queue Size is immediately rejected by BulkheadFullException

Here is explanation of code samples:
- ***Example 1 - Semaphore based bulkhead that allows 10 concurrent calls and rejects anything waiting more than 10 seconds for attention***: 
This example shows that the downstream API (if slow ) can be controlled to be called only for a set number of threads by client without tying all the resources up with that call. Any further calls to get concurrent connection are rejected after a set wait period (otherwise they would keep on waiting , something that we may not want)
> The BulkHeadGenerator.java has a BulkheadRegistry that allows 10 (maxConcurrent) requests with a wait time for waiting theads before they are killed set as 10 seconds.  
    The Service /bulkhead/service calls the downstream service after decorating with Bulkhead. This is tested using Apache JMEter file (Bulkhead-TG) . I tested with 50 requests placed simultaneously to an API that had a 7 second delay that resulted in 14 requests to go through and all others failed.

- ***Example 2 - Threadpool  based bulkhead that fixes a threadpool of max and core threads with a bounded queue***: 
This example shows that the downstream API  can be controlled to be called only for a set number of threads by client without tying all the resources up with that call. Any further calls to get concurrent connection are rejected after the bounded queue is full, immediately.
> The BulkHeadGenerator.java has a ThreadPoolBulkheadRegistry that allows 5 core and 7 max threads with bounded queue set to 10 .  
    The Service /bulkhead/service (with param useTP=true) calls the downstream service after decorating with ThreadPoolBulkhead. This is tested using Apache JMEter file (Bulkhead-Threadpool-TG) .  I tested with 50 requests placed simultaneously to an API that had a 7 second delay that resulted in 17 requests to go through and all others failed.
