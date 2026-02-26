package com.nxp.iemdm.shared.aop;

import com.nxp.iemdm.model.logging.SysJobLog;
import com.nxp.iemdm.shared.intf.operational.SysJobLogService;
import java.time.Instant;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MethodJobLogger {

  private static final String LOG_METHOD = "LOG_METHOD";
  private final SysJobLogService sysJobLogService;

  public MethodJobLogger(SysJobLogService sysJobLogService) {
    this.sysJobLogService = sysJobLogService;
  }

  @Pointcut("@annotation(com.nxp.iemdm.shared.aop.annotations.MethodJobLog)")
  public void loggableMethodsForSysJobLog() {
    // ignore
  }

  @Around("loggableMethodsForSysJobLog()")
  public Object loggableMethods(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
    long millis = System.currentTimeMillis();
    Object returnValue;
    Signature signature = proceedingJoinPoint.getSignature();
    String className = signature.getDeclaringType().getName();
    String methodName = signature.getName();
    try {

      SysJobLog sysJobLogStart = createJobLog();
      String startMessage = String.format("Start %s at %s", methodName, className);
      sysJobLogStart.setLogMessage(startMessage);
      this.sysJobLogService.saveAsync(sysJobLogStart);

      returnValue = proceedingJoinPoint.proceed();

      SysJobLog sysJobLogEnd = createJobLog();
      long elapsed = System.currentTimeMillis() - millis;
      String endMessage =
          String.format("End %s at %s, elapsed millis: %d", methodName, className, elapsed);
      sysJobLogEnd.setLogMessage(endMessage);
      this.sysJobLogService.saveAsync(sysJobLogEnd);

    } catch (Throwable throwable) {
      SysJobLog sysJobLogException = createJobLog();
      long elapsed = System.currentTimeMillis() - millis;
      String endMessage =
          String.format(
              "Exception for %s at %s, elapsed millis: %d - [%s: %s]",
              methodName,
              className,
              elapsed,
              throwable.getClass().getName(),
              throwable.getMessage());
      sysJobLogException.setLogMessage(endMessage);
      this.sysJobLogService.saveAsync(sysJobLogException);
      throw throwable;
    }
    return returnValue;
  }

  private static SysJobLog createJobLog() {
    SysJobLog sysJobLog = new SysJobLog();
    sysJobLog.setJobName(LOG_METHOD);
    sysJobLog.setTimestamp(Instant.now());
    return sysJobLog;
  }
}
