package com.myseotoolbox.crawler.spider.ratelimiter;

import java.util.Optional;

public interface RateLimiter {

    UnlimitedRateLimiter UNLIMITED_RATE_LIMITER = new UnlimitedRateLimiter();

    <T> Optional<T> process(Task<T> task);

    class UnlimitedRateLimiter implements RateLimiter {
        private UnlimitedRateLimiter() {}

        @Override
        public <T> Optional<T> process(Task<T> task) {
            return Optional.ofNullable(task.execute());
        }
    }

    interface Task<T> {
        T execute();
    }
}