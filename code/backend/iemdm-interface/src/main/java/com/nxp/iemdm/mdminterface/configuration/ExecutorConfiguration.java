package com.nxp.iemdm.mdminterface.configuration;

import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@PropertySource("classpath:ie-mdm-interface.properties")
@PropertySource(
    value = "classpath:ie-mdm-interface.override.properties",
    ignoreResourceNotFound = true)
public class ExecutorConfiguration {
  @Bean
  public Executor interfaceTaskExecutor(
      @Value("${com.nxp.iemdm.interface.task.executor.core.pool.size:10}") int maxPoolSize,
      @Value("${com.nxp.iemdm.interface.task.executor.keep.alive.seconds:30}") int keepAliveSeconds,
      @Value("${com.nxp.iemdm.interface.task.executor.thread.name.prefix:intf_thd-}")
          String threadNamePrefix) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(maxPoolSize);
    executor.setAllowCoreThreadTimeOut(true);
    executor.setKeepAliveSeconds(keepAliveSeconds);
    executor.setThreadNamePrefix(threadNamePrefix);
    executor.initialize();
    return executor;
  }
}
