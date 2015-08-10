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
package com.actian.services.dataflow.functions;

import static com.pervasive.datarush.types.TokenTypeConstant.STRING;
import static com.pervasive.datarush.types.TokenTypeConstant.record;

import org.junit.Test;

import com.pervasive.datarush.graphs.LogicalGraph;
import com.pervasive.datarush.graphs.LogicalGraphFactory;
import com.pervasive.datarush.operators.assertion.AssertPredicate;
import com.pervasive.datarush.operators.record.DeriveFields;
import com.pervasive.datarush.operators.sink.LogRows;
import com.pervasive.datarush.operators.source.EmitRecords;
import com.pervasive.datarush.sequences.record.RecordTokenList;
import com.pervasive.datarush.tokens.record.RecordToken;
import com.pervasive.datarush.tokens.scalar.StringToken;
import com.pervasive.datarush.types.RecordTokenType;

public class XMLFunctionsTest {
    
    public XMLFunctionsTest() {
    }

    @Test
    public void testvalidXPathScalar() {
        String validXML;
        
        validXML = "<?xml version='1.0'?>"
                + "<root><fruits>"
                + "<fruit name='apple' color='green'/>"
                + "<ignore>Me</ignore>"
                + "<fruit name='cherry' color='red'/>"
                + "</fruits>"
                + "</root>";
        
        RecordTokenType dataType = record(STRING("xmlDoc"));
        RecordTokenList data = new RecordTokenList(dataType, 2);
        data.append(new RecordToken(dataType, StringToken.parse(validXML)));

        LogicalGraph g = LogicalGraphFactory.newLogicalGraph("xpath_test");
        EmitRecords er = g.add(new EmitRecords());
        er.setInput(data);
        
        DeriveFields df = g.add(new DeriveFields());
        df.setDropUnderivedFields(true);
        
        df.setDerivedFields("xpath(\"//fruits/ignore/text()\",xmlDoc) as result," +
                            "xpath(\"//fruits/fruit[1]/@color\",xmlDoc) as attr" );
       
        AssertPredicate ap = g.add(new AssertPredicate());
        ap.setPredicate("result = \"Me\" and attr = \"green\"");
        LogRows lr = g.add(new LogRows(1));
        g.connect(er.getOutput(), df.getInput());
        g.connect(df.getOutput(), lr.getInput());
        g.connect(df.getOutput(), ap.getInput());
        g.run();
    }
    
    @Test
    public void testvalidXPathNodes() {
        String validXML;
        
        validXML = "<?xml version='1.0'?>"
                + "<root><fruits>"
                + "<fruit name='apple' color='green' v='true'/>"
                + "<ignore>Me</ignore>"
                + "<fruit name='cherry' color='red'/>"
                + "</fruits>"
                + "</root>";
        
        RecordTokenType dataType = record(STRING("xmlDoc"));
        RecordTokenList data = new RecordTokenList(dataType, 2);
        data.append(new RecordToken(dataType, StringToken.parse(validXML)));

        LogicalGraph g = LogicalGraphFactory.newLogicalGraph("xpath_test");
        EmitRecords er = g.add(new EmitRecords());
        er.setInput(data);
        
        DeriveFields df = g.add(new DeriveFields());
        df.setDropUnderivedFields(true);
        
        df.setDerivedFields("xpath(\"//fruits/fruit\",xmlDoc) as result");
       
        AssertPredicate ap = g.add(new AssertPredicate());
        ap.setPredicate("result is not null");
        LogRows lr = g.add(new LogRows(1));
        g.connect(er.getOutput(), df.getInput());
        g.connect(df.getOutput(), lr.getInput());
        g.connect(df.getOutput(), ap.getInput());
        g.run();
    }
    
    @Test
    public void testvalidNull() {
        String validXML;
        
        validXML = "<?xml version='1.0'?>"
                + "<root><fruits>"
                + "<fruit name='apple' color='green' v='true'/>"
                + "<ignore>Me</ignore>"
                + "<fruit name='cherry' color='red'/>"
                + "</fruits>"
                + "</root>";
        
        RecordTokenType dataType = record(STRING("xmlDoc"));
        RecordTokenList data = new RecordTokenList(dataType, 2);
        data.append(new RecordToken(dataType, StringToken.parse(validXML)));

        LogicalGraph g = LogicalGraphFactory.newLogicalGraph("xpath_test");
        EmitRecords er = g.add(new EmitRecords());
        er.setInput(data);
        
        DeriveFields df = g.add(new DeriveFields());
        df.setDropUnderivedFields(true);
        
        df.setDerivedFields("xpath(\"//fruits/fruit[2]/@v\",xmlDoc) as result");
       
        AssertPredicate ap = g.add(new AssertPredicate());
        ap.setPredicate("result is null");
        LogRows lr = g.add(new LogRows(1));
        g.connect(er.getOutput(), df.getInput());
        g.connect(df.getOutput(), lr.getInput());
        g.connect(df.getOutput(), ap.getInput());
        g.run();
    }
    
    @Test
    public void testinvalidXPath() {
        String invalidXML;

        invalidXML = "<root><fruits><fruit name='apple' color='green'></fruits></root>";
        
        RecordTokenType dataType = record(STRING("xmlDoc"));
        RecordTokenList data = new RecordTokenList(dataType, 2);
        data.append(new RecordToken(dataType, StringToken.parse(invalidXML)));

        LogicalGraph g = LogicalGraphFactory.newLogicalGraph("xpath_test");
        EmitRecords er = g.add(new EmitRecords());
        er.setInput(data);
        
        DeriveFields df = g.add(new DeriveFields());
        df.setDropUnderivedFields(true);
        df.setDerivedFields("xpath(\"//fruits/ignore/.\",xmlDoc) as result");
       
        AssertPredicate ap = g.add(new AssertPredicate());
        ap.setPredicate("result == \"The end-tag for element type \\\"fruit\\\" must end with a '>' delimiter.\"");
        LogRows lr = g.add(new LogRows(1));
        g.connect(er.getOutput(), df.getInput());
        g.connect(df.getOutput(), lr.getInput());
        g.connect(df.getOutput(), ap.getInput());
        g.run();
    }
}
