package com.nxp.iemdm.scheduling.configuration;

import org.quartz.Job;
import org.quartz.SchedulerContext;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/*
 This class makes the bridge between Quartz and Spring, by enabling Quartz to do dependency injection,
 which is normally done by Spring.
*/
public final class AutoWiringSpringBeanJobFactory extends SpringBeanJobFactory
    implements ApplicationContextAware {

  private ApplicationContext applicationContext;
  private SchedulerContext schedulerContext;

  @Override
  public void setApplicationContext(final ApplicationContext context) {
    this.applicationContext = context;
  }

  @Override
  protected Object createJobInstance(final TriggerFiredBundle bundle) {
    Job job = applicationContext.getBean(bundle.getJobDetail().getJobClass());
    BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(job);
    MutablePropertyValues pvs = new MutablePropertyValues();
    pvs.addPropertyValues(bundle.getJobDetail().getJobDataMap());
    pvs.addPropertyValues(bundle.getTrigger().getJobDataMap());
    if (this.schedulerContext != null) {
      pvs.addPropertyValues(this.schedulerContext);
    }
    bw.setPropertyValues(pvs, true);
    return job;
  }

  @Override
  public void setSchedulerContext(SchedulerContext schedulerContext) {
    this.schedulerContext = schedulerContext;
    super.setSchedulerContext(schedulerContext);
  }
}
