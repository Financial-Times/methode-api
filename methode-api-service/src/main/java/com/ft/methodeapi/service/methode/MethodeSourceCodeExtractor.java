package com.ft.methodeapi.service.methode;

import java.io.StringReader;
import java.util.Objects;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.codehaus.stax2.XMLInputFactory2;

import com.google.common.base.Optional;

public class MethodeSourceCodeExtractor {
	
	private static final String METHODE_WEB_TYPE = "SourceCode";
	
	private final String attributes;

	public MethodeSourceCodeExtractor(String attributes) {
		Objects.requireNonNull(attributes, "Methode attributes should not be null");
		this.attributes = attributes;
	}

	public Optional<String> extract() throws XMLStreamException{
		boolean diftcomWebType = false;
		
		if (!attributes.isEmpty()) {
			XMLInputFactory xmlInputFactory =  XMLInputFactory2.newInstance();
	        XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new StringReader(attributes));
	        
	        while (xmlEventReader.hasNext()) {
	        	XMLEvent xmlEvent = xmlEventReader.nextEvent();
	            if(xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().toString().equals(METHODE_WEB_TYPE)){
	            	diftcomWebType = true;
	            }
	            
	            if(xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().toString().equals(METHODE_WEB_TYPE)){
	            	diftcomWebType = false;
	            }
	            
	            if(xmlEvent.isCharacters() && diftcomWebType){
	            	return Optional.fromNullable(xmlEvent.asCharacters().getData());
	            }
	        }
		}
              
		return Optional.absent();
	}
}
