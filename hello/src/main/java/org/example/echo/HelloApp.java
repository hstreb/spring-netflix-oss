package org.example.echo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableFeignClients
public class HelloApp {

    private final static Logger LOGGER = LoggerFactory.getLogger(HelloApp.class);

    @Autowired
    private EchoClient echoClient;

    @RequestMapping("/hello")
    public Echo echo(@RequestParam("id") Long id,
                     @RequestParam("duration") Long duration,
                     @RequestParam("retries") Long retries) {
        MDC.put("CALL_ID", id.toString());
        LOGGER.info("GET /hello?id={},duration={},retries={}", id, duration, retries);
        return echoClient.echo(id, duration, retries);
    }

    public static void main(String[] args) {
        SpringApplication.run(HelloApp.class, args);
    }

}

@FeignClient(name = "echo-server", fallback = EchoFallback.class)
interface EchoClient {
    @RequestMapping("/echo")
    Echo echo(@RequestParam("id") Long id, @RequestParam("duration") Long duration,
              @RequestParam("retries") Long retries);
}

@Component
class EchoFallback implements EchoClient {
    @Override
    public Echo echo(Long id, Long duration, Long retries) {
        return new Echo(id, 0L);
    }
}

class Echo {
    private Long id;
    private Long calls;

    public Echo() {
    }

    public Echo(Long id, Long calls) {
        this.id = id;
        this.calls = calls;
    }

    public Long getId() {
        return id;
    }

    public Long getCalls() {
        return calls;
    }
}
