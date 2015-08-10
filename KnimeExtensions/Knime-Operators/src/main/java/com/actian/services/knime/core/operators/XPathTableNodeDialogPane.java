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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.util.StringHistory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.actian.services.dataflow.operators.XPathTable;
import com.actian.services.knime.core.node.RecordPreviewPanel;
import com.pervasive.datarush.knime.coreui.common.CustomDialogComponent;
import com.pervasive.datarush.knime.io.FileBrowserUtil;
import com.pervasive.datarush.ports.PortMetadata;
import com.pervasive.datarush.ports.record.RecordMetadata;
import com.pervasive.datarush.schema.SchemaBuilder;
import com.pervasive.datarush.schema.SchemaBuilder.SchemaField;
import com.pervasive.datarush.sequences.record.RecordTokenList;
import com.pervasive.datarush.tokens.record.RecordSettable;
import com.pervasive.datarush.tokens.scalar.StringToken;
import com.pervasive.datarush.types.Field;
import com.pervasive.datarush.types.RecordTokenType;
import com.pervasive.datarush.types.TokenTypeConstant;
import com.pervasive.datarush.util.FileUtil;

final class XPathTableNodeDialogPane extends JPanel implements CustomDialogComponent<XPathTable> {
	
	private static final long serialVersionUID = -4403087954343002697L;
	public static final String HISTORY_KEY="xpath-xml";
	private XPathTableNodeSettings settings = new XPathTableNodeSettings();
	
	private RecordPreviewPanel preview;
	private JTextField expressionTextField;
	private JComboBox<String> sampleFileComboBox;
	private JComboBox<String> comboXML;
	private JCheckBox checkSource;
	private JCheckBox checkNode;
	private JCheckBox checkChild;
	private StringHistory history;

	public XPathTableNodeDialogPane() {
		initComponents();
	}
	
	@Override
	public XPathTableNodeSettings getSettings() {
		return settings;
	}

	@Override
	public boolean isMetadataRequiredForConfiguration(int portIndex) {
		return true;
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public void refresh(PortMetadata[] specs) {
		RecordTokenType inputType = ((RecordMetadata)specs[0]).getType();
		comboXML.removeAllItems();
		for(Field f : inputType) {
			if (f.getType().equals(TokenTypeConstant.STRING)) {
				comboXML.addItem(f.getName());
			}
		}
		comboXML.setSelectedItem(settings.inputField.getStringValue());
		expressionTextField.setText(settings.expression.getStringValue());
		sampleFileComboBox.setSelectedItem(settings.sampleFile.getStringValue());
		checkSource.setSelected(settings.includeSource.getBooleanValue());
		checkNode.setSelected(settings.includeNode.getBooleanValue());
		checkChild.setSelected(settings.includeChildren.getBooleanValue());
		RecordTokenList rtl = new RecordTokenList(settings.model.schema.toRecordTokenType(),1);
		rtl.appendNull();
		preview.setSampleData(rtl);
	}

	@Override
	public void validateAndApplySettings() throws InvalidSettingsException {
		// Check if file location is valid.
		byte[] xml;
		try {
			xml = FileUtil.readFileBytes(new File(sampleFileComboBox.getSelectedItem().toString()));
		} catch (IOException e) {
			throw new InvalidSettingsException("Cannot read file " + sampleFileComboBox.getSelectedItem());
		}
		String expression = expressionTextField.getText();
		XPath xp = XPathFactory.newInstance().newXPath();
		try {
			NodeList nodeList = (NodeList) xp.evaluate(expression, new InputSource(new ByteArrayInputStream(xml)),XPathConstants.NODESET);
			if (nodeList.getLength() == 0) {
				throw new InvalidSettingsException("Expression returned zero data, so cannot configure schema.");
			}
			// Amend schema to match.
			List<String> fieldNames = new ArrayList<>();
			List<SchemaField> fields = new ArrayList<>();
			for(int i=0;i<nodeList.getLength();i++) {
				Node n = nodeList.item(i);
				// Have we a list of nodes or scalars?
				if (n.hasAttributes()) {
					for (int j = 0; j < n.getAttributes().getLength(); j++) {
						String attributeName = "@"
								+ n.getAttributes().item(j).getNodeName();
						if (!fieldNames.contains(attributeName)) {
							fieldNames.add(attributeName);
							fields.add(SchemaBuilder.STRING(attributeName));
						}
					}
				}
				// Add on any children with a single text element.
				Node child = n.getFirstChild();
				while(child != null) {
					if (isSimpleElement(child) ) {
						String attributeName = child.getNodeName();
						if (!fieldNames.contains(attributeName)) {
							fieldNames.add(attributeName);
							fields.add(SchemaBuilder.STRING(attributeName));
						}
					}
					child = child.getNextSibling();
				}
			}
			settings.model.schema.setSchema(SchemaBuilder.define(fields.toArray(new SchemaField[]{})));
			// Now put the data into a RecordTokenList
			RecordTokenList rtl = new RecordTokenList(settings.model.schema.toRecordTokenType(), nodeList.getLength());
			rtl.appendNull(nodeList.getLength());
			for(int i=0;i<nodeList.getLength();i++)
			{
				Node n = nodeList.item(i);
				RecordSettable setter = rtl.getTokenSetter(i);
				for(int j=0;j<fieldNames.size();j++)
				 {
					if (n.hasAttributes() && fieldNames.get(j).startsWith("@")) {
						Node attr = n.getAttributes().getNamedItem(fieldNames.get(j).substring(1));
						if (attr != null) {
						    setter.getField(fieldNames.get(j)).set(StringToken.parse(attr.getNodeValue()));
						} else {
							setter.getField(fieldNames.get(j)).setNull();
						}
					} else if(n.hasChildNodes()) {
						Node child = n.getFirstChild();
						
						while(child != null) {
							if (child.getNodeName().equals(fieldNames.get(j))) {
								setter.getField(fieldNames.get(j)).set(StringToken.parse(child.getTextContent()));
								break;
							}
							child = child.getNextSibling();
						}
					}
				}
			}
			preview.setSampleData(rtl);
		} catch (XPathExpressionException e) {
			throw new InvalidSettingsException("Expression does not evaluate against input.",e);
		}
		// Everything is ok.
		settings.expression.setStringValue(expression);
		settings.sampleFile.setStringValue(sampleFileComboBox.getSelectedItem().toString());
		settings.inputField.setStringValue((String) comboXML.getSelectedItem().toString());
		settings.includeSource.setBooleanValue(checkSource.isSelected());
		settings.includeNode.setBooleanValue(checkNode.isSelected());
		settings.includeChildren.setBooleanValue(checkChild.isSelected());
		history.add(sampleFileComboBox.getSelectedItem().toString());
	}
	
	private boolean isSimpleElement(Node node) {
		// Simple elements have child, none of which are elements.
		boolean candidate = node.hasChildNodes()  ? true : false;
		
		NodeList nl = node.getChildNodes();
		for (int i=0;i<nl.getLength();i++) {
			if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
				candidate = false;
			}
		}
		return candidate;
	}

	private void initComponents() {
		
		JPanel xpathPanel = new JPanel();
		xpathPanel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		JLabel xpathLabel = new JLabel("XPath Expression");
		
		expressionTextField = new JTextField();
		xpathLabel.setLabelFor(expressionTextField);
		expressionTextField.setColumns(45);
		
		JLabel sampleFileLabel = new JLabel("Sample File");
		sampleFileLabel.setLabelFor(sampleFileComboBox);
		
		sampleFileComboBox = new JComboBox<String>();
		sampleFileComboBox.setEditable(true);
		history = StringHistory.getInstance(HISTORY_KEY);
		for (String s : history.getHistory()) {
			sampleFileComboBox.addItem(s);
		}
		
		JPanel previewPanel = new JPanel();
		previewPanel.setBorder(new TitledBorder(null, "Preview", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		preview = new RecordPreviewPanel(settings.model,false);
		previewPanel.add(preview);

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(previewPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
						.addComponent(xpathPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(xpathPanel, GroupLayout.PREFERRED_SIZE, 137, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(previewPanel, GroupLayout.PREFERRED_SIZE, 312, Short.MAX_VALUE)
					.addContainerGap())
		);
		
		checkSource = new JCheckBox("Include Source");
		checkNode = new JCheckBox("Include Node");
		checkChild = new JCheckBox("Include Children");
		
		JLabel xmlFieldLabel = new JLabel("XML Field");
		
		comboXML = new JComboBox<String>();
		
		JButton btnBrowse = new JButton("Browse...");
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileBrowserUtil.browseFiles(XPathTableNodeDialogPane.this.sampleFileComboBox, JFileChooser.OPEN_DIALOG, JFileChooser.FILES_ONLY, null, "xml");
			}
		});
		GroupLayout gl_xpathPanel = new GroupLayout(xpathPanel);
		gl_xpathPanel.setHorizontalGroup(
			gl_xpathPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_xpathPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_xpathPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_xpathPanel.createSequentialGroup()
							.addComponent(checkSource)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(checkNode)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(checkChild))
						.addGroup(gl_xpathPanel.createSequentialGroup()
							.addGroup(gl_xpathPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(xpathLabel, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
								.addComponent(sampleFileLabel))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_xpathPanel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_xpathPanel.createSequentialGroup()
									.addComponent(sampleFileComboBox, 0, 273, Short.MAX_VALUE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnBrowse, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE))
								.addComponent(expressionTextField, GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)))
						.addGroup(gl_xpathPanel.createSequentialGroup()
							.addComponent(xmlFieldLabel, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(comboXML, GroupLayout.PREFERRED_SIZE, 104, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap())
		);
		gl_xpathPanel.setVerticalGroup(
			gl_xpathPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_xpathPanel.createSequentialGroup()
					.addGroup(gl_xpathPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(xmlFieldLabel, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
						.addComponent(comboXML, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(gl_xpathPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(expressionTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(xpathLabel, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_xpathPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(sampleFileLabel, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
						.addComponent(sampleFileComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnBrowse))
					.addGap(10)
					.addGroup(gl_xpathPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(checkSource)
						.addComponent(checkNode)
						.addComponent(checkChild))
					.addGap(19))
		);
		xpathPanel.setLayout(gl_xpathPanel);
		setLayout(groupLayout);
		
	}
}
