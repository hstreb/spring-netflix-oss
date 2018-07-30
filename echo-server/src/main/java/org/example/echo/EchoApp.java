package org.example.echo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SpringBootApplication
@RestController
public class EchoApp {

    private final static Logger LOGGER = LoggerFactory.getLogger(EchoApp.class);

    private Map<Long, Long> calls = Collections.synchronizedMap(new HashMap<>());

    @RequestMapping("/echo")
    public Echo echo(@RequestParam("id") Long id, @RequestParam("duration") Long duration,
                     @RequestParam("retries") Long retries) {
        try {
            MDC.put("CALL_ID", id.toString());
            LOGGER.info("GET /echo?id={},duration={},retries={}", id, duration, retries);
            Long current = Optional.ofNullable(calls.get(id))
                    .orElse(0L);
            calls.put(id, current + 1);
            LOGGER.info("current: {}, retries: {}", current, retries);
            Long waiting = current == retries ? duration : duration * 2;
            LOGGER.info(calls.toString());
            LOGGER.info("Waiting for {} ms", waiting);
            Thread.sleep(waiting);
            return new Echo(id, Optional.ofNullable(calls.get(id)).orElse(1L));
        } catch (InterruptedException e) {
            LOGGER.error("Error when call echo", e);
            return new Echo();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(EchoApp.class, args);
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
