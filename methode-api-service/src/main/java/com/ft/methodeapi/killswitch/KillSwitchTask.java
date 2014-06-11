package com.ft.methodeapi.killswitch;

import com.google.common.collect.ImmutableMultimap;
import com.yammer.dropwizard.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.Arrays;

public class KillSwitchTask extends Task {

    private final static Logger LOGGER = LoggerFactory.getLogger(KillSwitchTask.class);

    public KillSwitchTask() {
        super("killswitch");
    }

    @Override
    public void execute(final ImmutableMultimap<String, String> parameters, final PrintWriter out) throws Exception {

        LOGGER.info("Consuming all memory to force OutOfMemoryError synthetically");
        LOGGER.debug("Forcing error, {}", Arrays.asList(new byte[Integer.MAX_VALUE]));
    }

}

