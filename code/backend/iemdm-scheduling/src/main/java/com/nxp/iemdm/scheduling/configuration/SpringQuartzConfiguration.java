package com.nxp.iemdm.scheduling.configuration;

import com.nxp.iemdm.scheduling.listener.JobMailNotificationOnFailureListener;
import com.nxp.iemdm.scheduling.listener.JobUpdateListenerListener;
import com.nxp.iemdm.scheduling.listener.SysJobLoggingJobListener;
import com.nxp.iemdm.shared.intf.notification.NotificationModService;
import com.nxp.iemdm.shared.intf.operational.SysJobLogService;
import com.nxp.iemdm.shared.intf.operational.UpdateService;
import com.nxp.iemdm.shared.intf.operational.UserRoleService;
import javax.sql.DataSource;
import lombok.extern.java.Log;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

@Log
@Configuration
@PropertySource("classpath:iemdm-scheduling.properties")
public class SpringQuartzConfiguration {

  private static final int QUARTZ_SCHEDULER_START_UP_DELAY_SECONDS = 10;

  private final ApplicationContext applicationContext;
  private final NotificationModService notificationModService;
  private final SysJobLogService sysJobLogService;
  private final UserRoleService userRoleService;
  private final UpdateService updateService;

  public SpringQuartzConfiguration(
      ApplicationContext applicationContext,
      NotificationModService notificationModService,
      SysJobLogService sysJobLogService,
      UserRoleService userRoleService,
      UpdateService updateService) {
    this.applicationContext = applicationContext;
    this.notificationModService = notificationModService;
    this.sysJobLogService = sysJobLogService;
    this.userRoleService = userRoleService;
    this.updateService = updateService;
  }

  @Bean
  public Scheduler scheduler(SchedulerFactoryBean schedulerFactory) throws SchedulerException {
    Scheduler scheduler = schedulerFactory.getScheduler();
    scheduler
        .getListenerManager()
        .addJobListener(new SysJobLoggingJobListener(this.sysJobLogService));
    scheduler
        .getListenerManager()
        .addJobListener(new JobUpdateListenerListener(this.updateService));
    scheduler
        .getListenerManager()
        .addJobListener(
            new JobMailNotificationOnFailureListener(
                this.notificationModService, this.userRoleService));
    try {
      scheduler.start();
    } catch (Exception exception) {
      log.severe(String.format("Could not start Quartz scheduler: %s", exception));
    }

    return scheduler;
  }

  @Bean
  public SchedulerFactoryBean schedulerFactory(
      @Qualifier("QuartzDataSource") DataSource dataSource, SpringBeanJobFactory jobFactory) {
    log.info("=== Configuring Quartz Scheduler ===");
    log.info("DataSource: " + (dataSource != null ? "OK" : "NULL"));

    SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
    schedulerFactory.setAutoStartup(true);
    schedulerFactory.setJobFactory(jobFactory);
    schedulerFactory.setDataSource(dataSource);
    schedulerFactory.setStartupDelay(QUARTZ_SCHEDULER_START_UP_DELAY_SECONDS);
    schedulerFactory.setSchedulerName("IEMDM_SCHEDULER");

    // 使用 Spring 的 LocalDataSourceJobStore 而非 JobStoreTX
    // LocalDataSourceJobStore 可以直接使用 Spring 注入的 DataSource
    java.util.Properties quartzProperties = new java.util.Properties();
    quartzProperties.setProperty(
        "org.quartz.jobStore.class",
        "org.springframework.scheduling.quartz.LocalDataSourceJobStore");
    quartzProperties.setProperty(
        "org.quartz.jobStore.driverDelegateClass",
        "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
    quartzProperties.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");
    quartzProperties.setProperty("org.quartz.jobStore.isClustered", "false");
    schedulerFactory.setQuartzProperties(quartzProperties);

    log.info("Quartz Properties configured:");
    quartzProperties.forEach((key, value) -> log.info("  " + key + " = " + value));
    log.info("=== Quartz Scheduler Configuration Complete ===");

    return schedulerFactory;
  }

  // Configure the jobFactory with our 'custom' job factory
  @Bean
  public SpringBeanJobFactory jobFactory() {
    AutoWiringSpringBeanJobFactory jobFactory = new AutoWiringSpringBeanJobFactory();
    jobFactory.setApplicationContext(applicationContext);
    return jobFactory;
  }

  /*
  It might sound like this is another datasource, but it is just our standard datasource. Something to
  note, we could specify another datasource than our standard datasource e.g. another database
  just for the quartz scheduler to store jobs and triggers. Or, in the case that we have multiple
  schedulers, they could each have a different datasource (DB).
  */
  @Bean(name = "QuartzDataSource")
  public DataSource quartzDataSource(
      @Value("${app.datasource.url}") String url,
      @Value("${app.datasource.username}") String username,
      @Value("${app.datasource.password}") String password) {
    DataSourceBuilder<?> builder = DataSourceBuilder.create();

    builder.url(url);
    builder.username(username);
    builder.password(password);

    return builder.build();
  }
}
