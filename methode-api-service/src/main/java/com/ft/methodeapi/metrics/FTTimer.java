package com.ft.methodeapi.metrics;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Copies timer samples to Yammer Metrics and SL4J.
 *
 * @author Simon.Gibbs
 */
public class FTTimer {

    private final String label;
    private final Timer timer;
    private Logger logger;

    public static FTTimer newTimer(Class clazz, String label) {
        return new FTTimer(Metrics.newTimer(clazz,label),clazz,label);
    }

    private FTTimer(Timer timer, Class loggingClass, String label) {
        logger = LoggerFactory.getLogger(loggingClass);
        this.label = label;
        this.timer = timer;
    }

    public RunningTimer start() {
        return new LoggingTimerContext(timer.time(),System.currentTimeMillis());
    }


    private class LoggingTimerContext implements RunningTimer {

        private TimerContext timerContext;
        long startTimeMillis;

        public LoggingTimerContext (TimerContext timerContext, long startTimeMillis) {
            this.timerContext = timerContext;
            this.startTimeMillis = startTimeMillis;
        }

        @Override
        public void stop() {
            timerContext.stop();
            long duration = System.currentTimeMillis() - startTimeMillis;
            logger.info("[{}] duration={}",label,duration);
        }
    }

}
