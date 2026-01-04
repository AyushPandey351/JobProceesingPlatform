package com.example.jobplatform.worker;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobWorker {

    private final JobProcessor jobProcessor;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String QUEUE_NAME = "job_queue";
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    @PostConstruct
    public void startWorkers() {
        for (int i = 0; i < 2; i++) {
            executorService.submit(this::processLoop);
        }
    }

    private void processLoop() {
        log.info("Worker started listening on {}", QUEUE_NAME);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Object rawId = redisTemplate.opsForList().rightPop(QUEUE_NAME, 5, TimeUnit.SECONDS);
                if (rawId != null) {
                    log.info("Popped job ID: '{}'", rawId);
                    UUID jobId = UUID.fromString(rawId.toString().replace("\"", ""));
                    jobProcessor.processJob(jobId);
                }
            } catch (Exception e) {
                log.error("Error in worker loop", e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
