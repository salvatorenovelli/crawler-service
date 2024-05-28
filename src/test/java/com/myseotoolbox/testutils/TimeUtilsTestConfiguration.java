package com.myseotoolbox.testutils;

import com.myseotoolbox.utils.TimeUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TimeUtilsTestConfiguration {
    @Bean
    @Primary
    public TimeUtils getTimeUtils() {
        return new TestTimeUtils();
    }
}
