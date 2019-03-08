package com.myseotoolbox.crawler.testutils;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Thread builder that doesn't really return a thread but makes the current thread execute the {@link Runnable} command
 */

@Slf4j
public class CurrentThreadTestExecutorService extends AbstractExecutorService {

    @Override
    public void execute(Runnable command) {
        try {
            command.run();
        } catch (Exception e) {
            //Swallow leaked exception for consistency with executor service -.-
            log.error("Exception in run: ", e);
        }
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public List<Runnable> shutdownNow() {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public boolean isShutdown() {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public boolean isTerminated() {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("Not implemented!");
    }
}