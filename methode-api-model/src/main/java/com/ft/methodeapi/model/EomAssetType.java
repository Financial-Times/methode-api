package com.ft.methodeapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class EomAssetType {
	
	

	private final String uuid;
    private final String type;
    private final String sourceCode;
	private final String errorMessage;
    
	private EomAssetType(@JsonProperty("uuid") String uuid,
            @JsonProperty("type") String type,
            @JsonProperty("sourceCode") String sourceCode, 
            @JsonProperty("error") String errorMessage){
		this.uuid = uuid;
		this.type = type;
		this.sourceCode = sourceCode;
		this.errorMessage = errorMessage;
	}

	public String getUuid() {
		return uuid;
	}

	public String getType() {
		return type;
	}

	public String getSourceCode() {
		return sourceCode;
	}
	

	public String getErrorMessage() {
		return errorMessage;
	}
	
	public static class Builder {
		
		private String uuid = "";
		private String type = "";
		private String sourceCode = "";
		private String errorMessage = "";

		public Builder uuid(String uuid){
			this.uuid = uuid;
			return this;
		}
		
		public Builder type(String type){
			this.type = type;
			return this;
		}
		
		public Builder sourceCode(Optional<String> sourceCode){
			if(sourceCode.isPresent())
				this.sourceCode = sourceCode.get();
			return this;
		}
		
		public Builder error(String errorMessage){
			this.errorMessage = errorMessage;
			return this;
		}
		
		public EomAssetType build(){
			return new EomAssetType(uuid, type, sourceCode, errorMessage);
		}
		
		
		
	}

}
