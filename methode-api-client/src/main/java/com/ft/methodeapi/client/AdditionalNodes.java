package com.ft.methodeapi.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.methodeapi.client.validation.MultivaluePattern;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * AdditionalNodes
 *
 * @author Simon.Gibbs
 */
public class AdditionalNodes {

    public static final String HOST_AND_PORT_PATTERN = "[a-zA-Z0-9.-]+:\\d+";

    private List<String> primaryNodes;
    private List<String> secondaryNodes;

    public AdditionalNodes(@JsonProperty List<String> primaryNodes,@JsonProperty List<String> secondaryNodes) {
        this.primaryNodes = primaryNodes;
        this.secondaryNodes = secondaryNodes;
    }

    @NotNull @MultivaluePattern(regexp= HOST_AND_PORT_PATTERN)
    public List<String> getPrimaryNodes() {
        return primaryNodes;
    }

    @MultivaluePattern(regexp= HOST_AND_PORT_PATTERN)
    public List<String> getSecondaryNodes() {
        return secondaryNodes;
    }
}
