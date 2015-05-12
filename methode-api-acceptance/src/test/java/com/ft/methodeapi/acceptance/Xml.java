package com.ft.methodeapi.acceptance;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

/**
 * Xml testing util
 *
 * @author Simon.Gibbs
 */
public class Xml {


    public static void assertParseable(String xmlSource) {

        String message = "Xml not parseable: " + xmlSource;
        Document xmlDom = null;
        try {
            xmlDom = parseAndSimplify(xmlSource);
        } catch(Exception e) {
            throw new AssertionError(message,e);
        }
        assertTrue(message,xmlDom !=null);
    }

    public static String removeInsignificantXml(String xmlSource, String[] insignificantPaths) throws Exception {

        Document xmlDom = parseAndSimplify(xmlSource);

        stripPaths(xmlDom, insignificantPaths);

        ByteArrayOutputStream out = new ByteArrayOutputStream(xmlSource.length());

        emitDocumentWithoutWhitespace(xmlDom, out);

        return resolveEntities(new String(out.toByteArray(),"UTF-8"));

    }

    public static String resolveEntities(String xmlSource) {
        Pattern pattern = Pattern.compile("&#(\\d+);");

        Matcher matcher = pattern.matcher(xmlSource);

        while(matcher.find()) {
            String codepointDecimal = matcher.group(1);
            int codepoint = Integer.parseInt(codepointDecimal);

            xmlSource = String.format("%s%s%s",
                    xmlSource.substring(0,matcher.start()),
                    (char) codepoint,
                    xmlSource.substring(matcher.end())
            );

            matcher = pattern.matcher(xmlSource);
        }

        return xmlSource;
    }

    public static void stripPaths(Document xmlDom, String[] insignificantPaths) throws XPathExpressionException {
        XPath pathUtil = XPathFactory.newInstance().newXPath();

        for(String xpath : insignificantPaths) {
            NodeList nodes = (NodeList) pathUtil.evaluate(xpath, xmlDom, XPathConstants.NODESET );

            for (int i = 0; i < nodes.getLength(); ++i) {

                Node node = nodes.item(i);
                node.getParentNode().removeChild(node);

            }
        }
    }

    public static void emitDocumentWithoutWhitespace(Node document, OutputStream oFileOut)
            throws TransformerException {

        Source xsltSource = new StreamSource( Xml.class.getResourceAsStream("com/ft/methodeapi/acceptance/strip-space.xslt") );

        TransformerFactory oTransFactory = TransformerFactory.newInstance();
        Transformer oTrans = oTransFactory.newTransformer(xsltSource);

        DOMSource oSource = new DOMSource(document);

        StreamResult oResult = new StreamResult(oFileOut);

        oTrans.transform(oSource,oResult);

    }

    public static Document parseAndSimplify(String xmlSource) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setCoalescing(true);
        factory.setIgnoringComments(true);
        factory.setExpandEntityReferences(false);
        factory.setValidating(false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setIgnoringElementContentWhitespace(true);

        return factory.newDocumentBuilder().parse( new InputSource( new StringReader(xmlSource)  ));
    }

}

