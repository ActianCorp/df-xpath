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
package com.actian.services.knime.core.node;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObjectSpec;

import com.pervasive.datarush.knime.io.GenericSchemaSettings;
import com.pervasive.datarush.schema.SchemaBuilder;

public class SimpleSchemaModel extends SettingsModel {

	public GenericSchemaSettings schema;
	private String nullIndicator="";
	
	public SimpleSchemaModel(GenericSchemaSettings schemaSettings) {
		this.schema = schemaSettings;
	}

	@Override
	protected SimpleSchemaModel createClone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getModelTypeID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getConfigName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void loadSettingsForDialog(NodeSettingsRO settings,
			PortObjectSpec[] specs) throws NotConfigurableException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void saveSettingsForDialog(NodeSettingsWO settings)
			throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void validateSettingsForModel(NodeSettingsRO settings)
			throws InvalidSettingsException {
		schema.validateSettings(settings);
		
	}

	@Override
	protected void loadSettingsForModel(NodeSettingsRO settings)
			throws InvalidSettingsException {
		// TODO Auto-generated method stub
		schema.loadSettingsFrom(settings);
		nullIndicator = settings.getString("nullIndicator");
	}

	@Override
	protected void saveSettingsForModel(NodeSettingsWO settings) {
		// TODO Auto-generated method stub
		schema.saveSettingsTo(settings);
		settings.addString("nullIndicator", nullIndicator);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return schema.toString();
	}

	public void setNullIndicator(String nullIndicator) {
		this.nullIndicator = nullIndicator;
	}
	
	public String getNullIndicator() {
		return nullIndicator;
	}
	
}
