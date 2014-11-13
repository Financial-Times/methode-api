package com.ft.methodeapi.atc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * LastKnownLocation
 *
 * @author Simon.Gibbs
 */
public class LastKnownLocation {

    public static final String IS_PASSIVE_MSG = "CHECK DISABLED (PASSIVE DC)";
    private static final Logger LOGGER = LoggerFactory.getLogger(LastKnownLocation.class);

    private WhereIsMethodeResponse whereIsItResponse;

    public LastKnownLocation(final AirTrafficController controller, ScheduledExecutorService scheduler) {

        final LastKnownLocation holder = this;
        whereIsItResponse = controller.fullReport();

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                synchronized (holder) {
                    try {
                        holder.whereIsItResponse = controller.fullReport();
                    } catch(AirTrafficControllerException atce) {
                        LOGGER.error("Scheduled ATC refresh failed",atce);
                    }
                }
            }
        },5,5, TimeUnit.MINUTES);
    }


    public WhereIsMethodeResponse lastReport() {
        synchronized (this) {
            return whereIsItResponse;
        }
    }

    public boolean iAmActive() {
        return lastReport().isAmIActive();
    }

}
