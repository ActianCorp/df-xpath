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

import com.pervasive.datarush.io.FileClient;
import com.pervasive.datarush.operators.io.ByteSink;
import com.pervasive.datarush.operators.io.ByteSource;
import com.pervasive.datarush.operators.io.DataFormat;
import com.pervasive.datarush.operators.io.FileMetadata;
import com.pervasive.datarush.operators.io.FormattingOptions;
import com.pervasive.datarush.operators.io.ParsingOptions;
import com.pervasive.datarush.operators.io.SplitParsingContext;
import com.pervasive.datarush.schema.RecordTextSchema;
import com.pervasive.datarush.tokens.record.RecordSettable;
import com.pervasive.datarush.types.RecordTokenType;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class XMLTextFormat implements DataFormat {

    @JsonProperty
    private final RecordTextSchema<?> schema;
    
    @JsonCreator
    public XMLTextFormat(
                    @JsonProperty("schema") RecordTextSchema<?> schema) {
        this.schema = schema;
    }
    
    @Override
    public RecordTokenType getType() {
        return schema.getTokenType();
    }

    @Override
    public FileMetadata getMetadata() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setMetadata(FileMetadata metadata) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileMetadata readMetadata(FileClient fileClient, ByteSource source) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void writeMetadata(FileMetadata metadata, FileClient fileClient, ByteSink target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DataParser createParser(ParsingOptions options) {
        return new XMLDataParser(options);
    }

    @Override
    public DataFormatter createWriter(FormattingOptions options) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSplittable() {
        return false;
    }
    
    public class XMLDataParser implements DataParser {

        public XMLDataParser(ParsingOptions options) {
        }
        
        @Override
        public void bindOutput(RecordSettable target) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void parseSplit(SplitParsingContext ctx) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void release() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    
    }
}
