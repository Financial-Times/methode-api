package com.ft.methodeapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class EomFile {

	public static final String WEB_REVISE = "Stories/WebRevise";
	public static final String WEB_READY = "Stories/WebReady";
	public static final String WEB_CHANNEL = "FTcom";
	public static final String NEWSPAPER_CHANNEL = "Financial Times";

    /**
     * Marks reason for controversial design choices.
     */
    private static final String JUSTIFICATION_MEMORY_CHURN = "Likelihood of memory churn outweighs risk of security issue";

    private final String uuid;
    private final String type;

    @SuppressWarnings(value = "EI_EXPOSE_REP2", justification = JUSTIFICATION_MEMORY_CHURN)
    private final byte[] value;
    private final String attributes;
	private final String workflowStatus;
	private final String systemAttributes;

    public EomFile(@JsonProperty("uuid") String uuid,
				   @JsonProperty("type") String type,
				   @JsonProperty("value") byte[] bytes,
				   @JsonProperty("attributes") String attributes,
				   @JsonProperty("workflowStatus") String workflowStatus,
				   @JsonProperty("systemAttributes") String systemAttributes) {
        this.uuid = uuid;
        this.type = type;
        this.value = bytes;
        this.attributes = attributes;
		this.workflowStatus = workflowStatus;
		this.systemAttributes = systemAttributes;
    }

    public String getUuid() {
        return uuid;
    }

    public String getType() {
        return type;
    }

    @SuppressWarnings(value = "EI_EXPOSE_REP", justification = JUSTIFICATION_MEMORY_CHURN)
    public byte[] getValue() {
        return value;
    }

    public String getAttributes() {
        return attributes;
    }

	public String getWorkflowStatus() {
		return workflowStatus;
	}

	public String getSystemAttributes() {
		return systemAttributes;
	}

	public static class Builder {
        private String uuid;
        private String type;
        private byte[] value;
        private String attributes;
		private String workflowStatus;
		private String systemAttributes;

        public Builder withUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        @SuppressWarnings(value = "EI_EXPOSE_REP", justification = JUSTIFICATION_MEMORY_CHURN)
        public Builder withValue(byte[] value) {
            this.value = value;
            return this;
        }

        public Builder withAttributes(String attributes) {
            this.attributes = attributes;
            return this;
        }

		public Builder withWorkflowStatus(String workflowStatus) {
			this.workflowStatus = workflowStatus;
			return this;
		}

		public Builder withSystemAttributes(String systemAttributes) {
			this.systemAttributes = systemAttributes;
			return this;
		}
        
        public Builder withValuesFrom(EomFile eomFile) {
        	return withUuid(eomFile.getUuid())
        			.withType(eomFile.getType())
        			.withValue(eomFile.getValue())
        			.withAttributes(eomFile.getAttributes())
					.withWorkflowStatus(eomFile.getWorkflowStatus())
					.withSystemAttributes(eomFile.getSystemAttributes());
        }

        public EomFile build() {
            return new EomFile(uuid, type, value, attributes, workflowStatus, systemAttributes);
        }
    }
}
