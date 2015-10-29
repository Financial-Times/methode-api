package com.ft.methodeapi.service.methode;

import com.ft.methodeapi.service.methode.monitoring.ThreadsByClassGauge;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * ThreadsByClassGaugeTest
 *
 * @author Simon.Gibbs
 */
public class ThreadsByClassGaugeTest {


    @Test
    @SuppressWarnings("unchecked")
    public void shouldReportTheCurrentThreadIfAKnownClassMatches() {

        final ThreadsByClassGauge gauge = new ThreadsByClassGauge(GaugeWrapper.class);

        GaugeWrapper<Integer> wrapper = new GaugeWrapper(gauge);

        assertThat(wrapper.queryGauge(),is(1));


    }

    @Test
    public void shouldNotReportTheCurrentThreadIfAKnownDoesNotMatches() {

        final ThreadsByClassGauge gauge = new ThreadsByClassGauge(GaugeWrapper.class);
        assertThat(gauge.getValue(),is(0));

    }


    @Test(expected = IllegalArgumentException.class)
    public void shouldReportUnsupportedClass() {

        class AClassWithoutAProperNameBecauseItIsLocal {

        }

        new ThreadsByClassGauge(AClassWithoutAProperNameBecauseItIsLocal.class);

    }
}
