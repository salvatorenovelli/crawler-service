package com.myseotoolbox.crawler.utils;

import io.vavr.CheckedRunnable;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class FunctionalExceptionUtils {

    public static void runOrLogWarning(CheckedRunnable task, String msg) {
        Try.run(task).orElseRun(throwable -> log.warn(msg, throwable));
    }

}
