package com.ft.methodeapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class EomAssetType {
	
	private final String uuid;
    private final String type;
    private final Optional<String> sourceCode;
    
	public EomAssetType(@JsonProperty("uuid") String uuid,
            @JsonProperty("type") String type,
            @JsonProperty("sourceCode") Optional<String> sourceCode){
		this.uuid = uuid;
		this.type = type;
		this.sourceCode = sourceCode;
	}

	public String getUuid() {
		return uuid;
	}

	public String getType() {
		return type;
	}

	public String getSourceCode() {
		if(!sourceCode.isPresent())
			return "";
		return sourceCode.get();
	}


}
