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

import com.pervasive.datarush.graphs.LogicalGraph;
import com.pervasive.datarush.graphs.LogicalGraphFactory;
import com.pervasive.datarush.operators.assertion.AssertPredicate;
import com.pervasive.datarush.operators.record.DeriveFields;
import com.pervasive.datarush.operators.sink.LogRows;
import com.pervasive.datarush.operators.source.EmitRecords;
import com.pervasive.datarush.schema.SchemaBuilder;
import com.pervasive.datarush.sequences.record.RecordTokenList;
import com.pervasive.datarush.tokens.record.RecordToken;
import com.pervasive.datarush.tokens.scalar.StringToken;
import com.pervasive.datarush.types.RecordTokenType;
import static com.pervasive.datarush.types.TokenTypeConstant.STRING;
import static com.pervasive.datarush.types.TokenTypeConstant.record;
import org.junit.Test;
import static org.junit.Assert.*;

public class XPathTableTest {
    
    public XPathTableTest() {
    }

    @Test
    public void testSimpleRead() {
        
        String validXML;
        
        validXML = "<?xml version='1.0'?>"
                + "<root><fruits>"
                + "<fruit name='apple' color='green' v='true'/>"
                + "<ignore>Me</ignore>"
                + "<fruit><since/></fruit>"
                + "<fruit name='cherry' color='red'/>"
                + "<fruit name='banana' color='yellow'>Eeek!</fruit>"
                + "</fruits>"
                + "</root>";
        
        RecordTokenType dataType = record(STRING("xmlDoc"));
        RecordTokenList data = new RecordTokenList(dataType, 2);
        data.append(new RecordToken(dataType, StringToken.parse(validXML)));

        LogicalGraph g = LogicalGraphFactory.newLogicalGraph("xpath_test");
        EmitRecords er = g.add(new EmitRecords());
        er.setInput(data);
        
        XPathTable table = g.add(new XPathTable());
        table.setInputField("xmlDoc");
        table.setExpression("//fruits/fruit");
        table.setSchema(SchemaBuilder.define(
                SchemaBuilder.STRING("name"), SchemaBuilder.STRING("color")
        ));
       
        LogRows lr = g.add(new LogRows(1));
        g.connect(er.getOutput(), table.getInput());
        g.connect(table.getOutput(), lr.getInput());
        g.run();
    }
    
}
