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
package com.actian.services.knime.core.operators;

import java.util.Arrays;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.actian.services.dataflow.operators.XPathTable;
import com.actian.services.knime.core.node.SimpleSchemaModel;
import com.pervasive.datarush.knime.core.framework.AbstractDRSettingsModel;
import com.pervasive.datarush.knime.io.GenericSchemaSettings;
import com.pervasive.datarush.ports.PortMetadata;
import com.pervasive.datarush.ports.record.RecordMetadata;
import com.pervasive.datarush.schema.TextConversionDefaults;
import com.pervasive.datarush.types.Field;
import com.pervasive.datarush.types.RecordTokenType;
import com.pervasive.datarush.types.TokenTypeConstant;

public class XPathTableNodeSettings extends AbstractDRSettingsModel<XPathTable> {

	public final SettingsModelString expression = new SettingsModelString("expression", "//*");
	public final SimpleSchemaModel model = new SimpleSchemaModel(new GenericSchemaSettings());
	public final SettingsModelString sampleFile = new SettingsModelString("sampleFile","");
	public final SettingsModelString inputField = new SettingsModelString("inputField","");
	public final SettingsModelBoolean includeSource = new SettingsModelBoolean("includeSourceXML", true);
	public final SettingsModelBoolean includeNode = new SettingsModelBoolean("includeNodeXML",false);
	public final SettingsModelBoolean includeChildren = new SettingsModelBoolean("includeChildXML",false);
	
	@Override
	public void configure(PortMetadata[] inputTypes, XPathTable operator)
			throws InvalidSettingsException {
		RecordTokenType rtt = ((RecordMetadata)inputTypes[0]).getType();
		if (inputField.getStringValue().isEmpty()) {
			// Choose first string column.
			boolean found = false;
			for(Field f : rtt) {
				if (f.getType().equals(TokenTypeConstant.STRING)) {
					found = true;
					inputField.setStringValue(f.getName());
				}
			}
			if (!found) {
				inputField.setStringValue(rtt.get(0).getName());
			}
		}
		operator.setExpression(expression.getStringValue());
		operator.setSchema(model.schema.getSchema(new TextConversionDefaults()));
		operator.setInputField(inputField.getStringValue());
		operator.setIncludeSourceXML(includeSource.getBooleanValue());
		operator.setIncludeNodeXML(includeNode.getBooleanValue());
		operator.setIncludeChildXML(includeChildren.getBooleanValue());
	}

	@Override
	protected List<SettingsModel> getComponentSettings() {
		return Arrays.<SettingsModel>asList(
				expression,
				sampleFile,
				inputField,
				includeSource,
				includeNode,
				includeChildren,
				model
				);
	}

}
