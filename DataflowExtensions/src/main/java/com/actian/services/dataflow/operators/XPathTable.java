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
package com.actian.services.dataflow.operators;

import static com.pervasive.datarush.types.TokenTypeConstant.STRING;
import static com.pervasive.datarush.types.TokenTypeConstant.record;

import java.io.IOException;
import java.io.StringReader;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.actian.services.dataflow.functions.evaluators.XPath;
import com.pervasive.datarush.DRException;
import com.pervasive.datarush.annotations.PortDescription;
import com.pervasive.datarush.annotations.PropertyDescription;
import com.pervasive.datarush.operators.ExecutableOperator;
import com.pervasive.datarush.operators.ExecutionContext;
import com.pervasive.datarush.operators.RecordPipelineOperator;
import com.pervasive.datarush.operators.StreamingMetadataContext;
import com.pervasive.datarush.ports.physical.RecordInput;
import com.pervasive.datarush.ports.physical.RecordOutput;
import com.pervasive.datarush.ports.physical.StringInputField;
import com.pervasive.datarush.ports.record.RecordPort;
import com.pervasive.datarush.schema.RecordTextSchema;
import com.pervasive.datarush.tokens.scalar.StringToken;
import com.pervasive.datarush.types.RecordTokenType;
import com.pervasive.datarush.types.TokenTypeConstant;
import com.pervasive.datarush.types.TypeUtil;
import org.xml.sax.SAXException;

@JsonAutoDetect(JsonMethod.NONE)
public class XPathTable extends ExecutableOperator implements RecordPipelineOperator {

    private static final String CHILD_XML = "_childXML_";
    private static final String NODE_XML = "_nodeXML_";
    private static final String SOURCE_XML = "_sourceXML_";

    private final RecordPort input = newRecordInput("input");
    private final RecordPort output = newRecordOutput("output");
    private RecordTextSchema<?> schema;
    private String expression;
    private boolean includeChildXML = false;
    private boolean includeNodeXML = false;
    private boolean includeSourceXML = true;
    private String inputField;
    private XPathFactory xpathfactory;
    private DocumentBuilderFactory documentBuilderFactory;

    public XPathTable()
    {
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
    protected void execute(ExecutionContext ctx) {
        RecordInput inputRec = (RecordInput) ctx.getInputPort(getInput());
        RecordOutput outputRec = (RecordOutput) ctx.getOutputPort(getOutput());

        while (inputRec.stepNext()) {
            javax.xml.xpath.XPath xpath = xpathfactory.newXPath();
            InputSource source = new InputSource(new StringReader(((StringInputField) inputRec.getField(inputField)).asString()));
            try {
                DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();

                Document dom = db.parse(source);

                xpath.setNamespaceContext(new UniversalNamespaceResolver(dom));

                NodeList nodes = (NodeList) xpath.evaluate(expression, dom, XPathConstants.NODESET);

                for (int i = 0; i < nodes.getLength(); i++) {
                    // Output attribute values, and text value.
                    Node n = nodes.item(i);
                    // Loop through the schema
                    for (String fieldName : schema.getFieldNames()) {
                    	outputRec.getField(fieldName).setNull();
                    	
                        if (n.hasAttributes() && fieldName.startsWith("@")) {
                            // Test for attribute with the name.
                            Node attr = n.getAttributes().getNamedItem(fieldName.substring(1));
                            if (attr != null) {
                                outputRec.getField(fieldName).set(new StringToken(attr.getNodeValue()));
                            }
                        } else if (n.hasChildNodes()){
                            Node child = n.getFirstChild();
                            while(child != null){
                            	if (child.getNodeName().equals(fieldName)) {
                            		outputRec.getField(fieldName).set(StringToken.parse(child.getTextContent()));
                            		break;
                            	}
                            	child = child.getNextSibling();
                            }
                        }
                    }
                    if (isIncludeChildXML()) {
                        if (n.hasChildNodes()) {
                            NodeList list = n.getChildNodes();
                            String xmlValue = "";
                            for (int j = 0; j < list.getLength(); j++) {
                                xmlValue += XPath.printNode(list.item(j));
                            }
                            outputRec.getField(CHILD_XML).set(new StringToken(xmlValue));
                        } else {
                            outputRec.getField(CHILD_XML).setNull();
                        }
                    }
                    if (isIncludeNodeXML()) {
                        outputRec.getField(NODE_XML).set(new StringToken(XPath.printNode(n)));
                    }
                    if (isIncludeSourceXML()) {
                        outputRec.getField(SOURCE_XML).set(inputRec.getField(inputField));
                    }
                    outputRec.push();
                }
            } catch (XPathExpressionException ex) {
                throw new DRException("Error executing expression.",ex);
            } catch (ParserConfigurationException ex) {
                throw new DRException("Error creating document builder.", ex);
            } catch (SAXException ex) {
                throw new DRException("Error parsing XML source.", ex);
            } catch (IOException ex) {
                throw new DRException("Error reading XML source.", ex);
            }
        }

        outputRec.pushEndOfData();

    }

    @Override
    protected void computeMetadata(StreamingMetadataContext ctx) {
        RecordTokenType s = this.schema.getTokenType();
        if (inputField == null || inputField.isEmpty()) {
        	throw new DRException("Please specify an input field.");
        }
        if (input.getType(ctx).get(inputField) == null) {
        	throw new DRException("Input does not contain a field named '" + inputField + "'");
        }
        if (!input.getType(ctx).get(inputField).getType().equals(TokenTypeConstant.STRING)) {
        	throw new DRException("Field '" + inputField + "' has to be a string field.");
        }
        if (isIncludeChildXML()) {
            s = TypeUtil.merge(s, record(STRING(CHILD_XML)));
        }

        if (isIncludeNodeXML()) {
            s = TypeUtil.merge(s, record(STRING(NODE_XML)));
        }

        if (isIncludeSourceXML()) {
            s = TypeUtil.merge(s, record(STRING(SOURCE_XML)));
        }

        if (s.isEmpty()) {
        	throw new DRException("No fields in output!");
        }
        output.setType(ctx, s);

    }

    @PortDescription("XML data.")
    public RecordPort getInput() {
        return input;
    }

    @PortDescription("Parsed XML data.")
    public RecordPort getOutput() {
        return output;
    }

    @JsonProperty
    @PropertyDescription("Record schema of the target data")
    public RecordTextSchema<?> getSchema() {
        return schema;
    }

    public void setSchema(RecordTextSchema<?> schema) {
        this.schema = schema;
    }

    @JsonProperty
    @PropertyDescription("XPath expression")
    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @JsonProperty("includeChildXML")
    @PropertyDescription("Output child XML")
    public boolean isIncludeChildXML() {
        return includeChildXML;
    }

    @JsonProperty
    public void setIncludeChildXML(boolean includeChildXML) {
        this.includeChildXML = includeChildXML;
    }

    @JsonProperty("includeNodeXML")
    @PropertyDescription("Output node XML")
    public boolean isIncludeNodeXML() {
        return includeNodeXML;
    }

    @JsonProperty("includeNodeXML")
    public void setIncludeNodeXML(boolean includeNodeXML) {
        this.includeNodeXML = includeNodeXML;
    }

    @JsonProperty("includeSourceXML")
    @PropertyDescription("Output source XML")
    public boolean isIncludeSourceXML() {
        return includeSourceXML;
    }

    @JsonProperty("includeSourceXML")
    public void setIncludeSourceXML(boolean includeSourceXML) {
        this.includeSourceXML = includeSourceXML;
    }

    @JsonProperty
    @PropertyDescription("Input Field containing the XML")
	public String getInputField() {
		return inputField;
	}

	public void setInputField(String inputField) {
		this.inputField = inputField;
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
