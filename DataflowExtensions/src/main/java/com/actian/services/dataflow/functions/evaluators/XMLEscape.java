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

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Text;

import com.pervasive.datarush.functions.FunctionEvaluator;
import com.pervasive.datarush.tokens.scalar.StringSettable;
import com.pervasive.datarush.tokens.scalar.StringValued;

public class XMLEscape implements FunctionEvaluator {

    private final StringSettable result;
    private final StringValued expression;

    public XMLEscape(StringSettable result, StringValued expression) {
        this.result = result;
        this.expression = expression;
    }

    @Override
    public void evaluate() {
        if (expression.isNull()) {
            result.setNull();
        } else {
            result.set(escapeXML(expression.asString()));
        }
    }
    
    public static String escapeXML(String value) {
        StringWriter sw = new StringWriter();
        try {
            Text text = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createTextNode(value);
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "no");
            t.transform(new DOMSource(text), new StreamResult(sw));
        } catch (TransformerException | ParserConfigurationException te) {
            return te.getMessage();
        }
        return sw.toString();
    }

}
