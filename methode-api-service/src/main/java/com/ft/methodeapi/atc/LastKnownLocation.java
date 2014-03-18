package com.ft.methodeapi.atc;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * LastKnownLocation
 *
 * @author Simon.Gibbs
 */
public class LastKnownLocation {

    public static final String IS_PASSIVE_MSG = "CHECK DISABLED (PASSIVE DC)";

    private WhereIsMethodeResponse whereIsItResponse;

    public LastKnownLocation(final AirTrafficController controller, ScheduledExecutorService scheduler) {

        final LastKnownLocation holder = this;
        whereIsItResponse = controller.fullReport();

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                synchronized (holder) {
                    holder.whereIsItResponse = controller.fullReport();
                }
            }
        },5,5, TimeUnit.MINUTES);
    }


    public WhereIsMethodeResponse lastReport() {
        synchronized (this) {
            return whereIsItResponse;
        }
    }

}
