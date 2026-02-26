package com.nxp.iemdm.shared.aop;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceAspect {

  private static final Logger logger = Logger.getLogger(PerformanceAspect.class.getName());

  @Pointcut("@annotation(com.nxp.iemdm.shared.aop.annotations.MethodLog)")
  public void loggableMethods() {}

  @Around("loggableMethods()")
  public Object measureMethodExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
    long start = System.nanoTime();
    Object retval;
    try {

      if (logger.isLoggable(Level.FINEST)) {
        String methodName = pjp.getSignature().getName();
        Object[] args = pjp.getArgs();
        String className = pjp.getSignature().getDeclaringTypeName();

        logger.finest(
            "Starting execution of method: "
                + className
                + "."
                + methodName
                + " with arguments:"
                + Arrays.toString(args));
      } else if (logger.isLoggable(Level.CONFIG)) {
        String methodName = pjp.getSignature().getName();
        String className = pjp.getSignature().getDeclaringTypeName();

        logger.finest("Starting execution of method: " + className + "." + methodName);
      }
      retval = pjp.proceed();
      long end = System.nanoTime();
      if (logger.isLoggable(Level.CONFIG)) {
        String methodName = pjp.getSignature().getName();
        String className = pjp.getSignature().getDeclaringTypeName();
        logger.finer(
            "Successfully finished execution of "
                + className
                + "."
                + methodName
                + ", with return value: "
                + retval
                + ", which took "
                + TimeUnit.NANOSECONDS.toMillis(end - start)
                + " ms");
      }
    } catch (Throwable t) {
      long end = System.nanoTime();
      if (logger.isLoggable(Level.CONFIG)) {
        String methodName = pjp.getSignature().getName();
        String className = pjp.getSignature().getDeclaringTypeName();
        logger.finer(
            "Finished execution of "
                + className
                + "."
                + methodName
                + " with exception: "
                + t.getClass().getName()
                + ", which took "
                + TimeUnit.NANOSECONDS.toMillis(end - start)
                + " ms");
      }
      throw t;
    }

    return retval;
  }
}
