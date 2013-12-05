package com.ft.methodeapi.service.methode;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import com.google.common.base.Optional;

public class MethodeSourceCodeExtractorTest {
	
	@Test
	public void shouldReturnEmptyStringWhenSourceCodeNotDefined() throws XMLStreamException{
		Optional<String> maybeSourceCode = new MethodeSourceCodeExtractor("<test></test>").extract();
		assertThat(maybeSourceCode.isPresent(), is(false));
	}
	
	@Test
	public void shouldReturnEmptyStringWhenAttributesEmpty() throws XMLStreamException {
		Optional<String> maybeSourceCode = new MethodeSourceCodeExtractor("").extract();
		assertThat(maybeSourceCode.isPresent(), is(false));
	}
	
	@Test
	public void shouldReturnTheSourceCodeStringWhenSourceCodeIsDefined() throws XMLStreamException{
		String expectedSourceCode = "FTSB";
		Optional<String> maybeSourceCode = new MethodeSourceCodeExtractor("<test><SourceCode>" + expectedSourceCode + "</SourceCode></test>").extract();
		assertThat(maybeSourceCode.isPresent(), is(true));
		assertThat(maybeSourceCode.get(), is(expectedSourceCode));
	}
	
}

