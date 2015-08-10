/*
   Copyright 2015 Actian Corporation
 
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.actian.services.dataflow.functions.evaluators;

import com.pervasive.datarush.functions.FunctionEvaluator;
import com.pervasive.datarush.tokens.scalar.StringSettable;
import com.pervasive.datarush.tokens.scalar.StringValued;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XPath implements FunctionEvaluator {

    private final StringSettable result;
    private final StringValued expression;
    private final StringValued value;
    private XPathFactory xpathfactory;
    private DocumentBuilderFactory documentBuilderFactory;

    public XPath(StringSettable result, StringValued expression, StringValued value) {
        this.result = result;
        this.expression = expression;
        this.value = value;

        Thread t = Thread.currentThread();
        ClassLoader cl = t.getContextClassLoader();
        t.setContextClassLoader(getClass().getClassLoader());
        try {
            xpathfactory = XPathFactory.newInstance();
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
        } finally {
            t.setContextClassLoader(cl);
        }
    }

    @Override
    public void evaluate() {
        javax.xml.xpath.XPath xpath = xpathfactory.newInstance().newXPath();
        InputSource input = new InputSource(new StringReader(value.asString()));
        String output = "";
        try {
            DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();

            Document dom = db.parse(input);

            xpath.setNamespaceContext(new UniversalNamespaceResolver(dom));

            // First attempt to see if we have a list of nodes
            NodeList nodes = (NodeList) xpath.evaluate(expression.asString(), dom, XPathConstants.NODESET);
            if (nodes != null) {
                if (nodes.getLength() == 0) {
                    output = null;
                } else {
                    for (int i = 0; i < nodes.getLength(); i++) {
                        Node n = nodes.item(i);
                        output = output + printNode(n);
                    }
                }
            }
        } catch (Exception ex) {
            output = ex.getMessage();
        }
        result.set(output);
    }

    public static String printNode(Node n) {
        if (n.getNodeType() == Node.ATTRIBUTE_NODE
                || n.getNodeType() == Node.TEXT_NODE) {
            return n.getNodeValue();
        }

        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "no");
            t.transform(new DOMSource(n), new StreamResult(sw));
        } catch (TransformerException te) {
            return te.getMessage();
        }
        return sw.toString();
    }

    // The following is from the article "Using the Java language NamespaceContext object with XPath"
    // http://www.ibm.com/developerworks/library/x-nmspccontext/
    private final static class UniversalNamespaceResolver implements NamespaceContext {

        private Document sourceDocument;

        public UniversalNamespaceResolver(Document document) {
            sourceDocument = document;
        }

        @Override
        public String getNamespaceURI(String prefix) {
            if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                return sourceDocument.lookupNamespaceURI(null);
            } else {
                return sourceDocument.lookupNamespaceURI(prefix);
            }
        }

        @Override
        public String getPrefix(String namespaceURI) {
            return sourceDocument.lookupPrefix(namespaceURI);
        }

        @Override
        public Iterator<?> getPrefixes(String namespaceURI) {
            return null;
        }
    }
}
