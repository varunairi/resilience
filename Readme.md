# Resilience 4j Examples !

Resilience4J provides CircuitBreaker, BulkHead, Retries to Java 8 based Applications. Here are some of the examples that I tried. 

# Circuit Breaker
Circuit Breaker functionality allows you to handle either slow calls to an API (to avoid overwhelming the target system ) OR handle with Failures Downstream or **both**. 
Here is what you need to check in the code. 

 - ***Example 1 - Circuit Breaker with Slow calls***: This example shows how the downstream  API's slow response can trigger a circuit breaker to Open. Then under this example, the circuit does not auto close but it is done by an external trigger through an API call.
 
 > The Application.java has a CircuitBreakerRegistry with CircuitBreakerConfig set to trigger whenever 30% of all calls in a "sliding window" of size 5 (most recent calls to downstream API) are slow (response time > 5 secs). The Circuit Breaker kicks in after a "minimum number of calls". The Service is /serviceTooSlow and to reset the circuit breaker is /changeCircuitState

  - ***Example 2 - Service Call that regularly fails***: This example shows when downstream API produces frequent failures / exceptions causing circuit to open. 
  > The Application.java is injecting a bean for CircuitBreakerRegistry "*failedCallCktBreaker*" that configures the following parameters : Minimum number of calls to be made before cktbreaker kicks in, failure threshold in % of all calls (min calls described before too takes part in calculating stats),
  sliding window size and type, and **waitDuration** which automatically resets from OPEN to HALF OPEN State (looks like automaticTransitionFromOpenToHalfOpenEnabled does not have an effect). The service is /serviceFailure and /changeCircuitState is used to reset the Ckt Breaker.
  

StackEdit stores your files in your browser, which means all your files are automatically saved locally and are accessible **offline!**

## Create files and folders

The file explorer is accessible using the button in left corner of the navigation bar. You can create a new file by clicking the **New file** button in the file explorer. You can also create folders by clicking the **New folder** button.

