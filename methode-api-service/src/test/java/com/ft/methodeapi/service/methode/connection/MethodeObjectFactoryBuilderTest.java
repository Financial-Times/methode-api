package com.ft.methodeapi.service.methode.connection;

import com.google.common.base.Optional;
import com.yammer.dropwizard.util.Duration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * MethodeObjectFactoryBuilderTest
 *
 * @author Simon.Gibbs
 */
public class MethodeObjectFactoryBuilderTest {

    @Test(expected = IllegalStateException.class)
    public void shouldMandateAThreadPoolIfPoolingConfigured() {
        PoolConfiguration configuration = new PoolConfiguration(Optional.of(5), Optional.of(Duration.milliseconds(1900)), Optional.of(Duration.minutes(30)));

        MethodeObjectFactoryBuilder builder = new MethodeObjectFactoryBuilder();
        builder.withPooling(Optional.of(configuration));
        builder.build();
    }

    @Test
    public void shouldNotWireInPoolingIfPoolSizeIsZero() {
        PoolConfiguration configuration = new PoolConfiguration(Optional.of(0), Optional.of(Duration.milliseconds(1900)), Optional.of(Duration.minutes(30)));

        MethodeObjectFactoryBuilder builder = new MethodeObjectFactoryBuilder();
        builder.withPooling(Optional.of(configuration));
        MethodeObjectFactory result = builder.build();

        assertThat(result,not(instanceOf(PoolingMethodeObjectFactory.class)));

    }

    @Test
    public void shouldNotWireInPoolingIfNoPoolConfigIsPresent() {

        MethodeObjectFactoryBuilder builder = new MethodeObjectFactoryBuilder();
        builder.withPooling(Optional.<PoolConfiguration>absent());
        MethodeObjectFactory result = builder.build();

        assertThat(result,not(instanceOf(PoolingMethodeObjectFactory.class)));

    }

}
