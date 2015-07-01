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

import java.awt.Component;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Enumeration;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.pervasive.datarush.core.common.Icons;
import com.pervasive.datarush.knime.coreui.common.SimpleDialog;
import com.pervasive.datarush.knime.io.DefaultSchemaModel;
import com.pervasive.datarush.knime.io.GenericSchemaSettings;
import com.pervasive.datarush.knime.io.SchemaEditorPanel;
import com.pervasive.datarush.knime.io.SchemaEditorPanel.EditMode;
import com.pervasive.datarush.knime.io.SchemaModel;
import com.pervasive.datarush.knime.io.SchemaModel.SchemaChangeEvent;
import com.pervasive.datarush.knime.io.SchemaModel.SchemaModelListener;
import com.pervasive.datarush.schema.TextConversionDefaults;
import com.pervasive.datarush.sequences.record.RecordTokenList;

import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class RecordPreviewPanel extends JPanel implements SchemaModelListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTable table;
	private JScrollPane scrollPane;
	private final SimpleSchemaModel model;

	/**
	 * Create the panel.
	 */
	public RecordPreviewPanel(final SimpleSchemaModel model, boolean allowEdit) {
		this.model=model;
		scrollPane = new JScrollPane();

		table = new JTable();
		table.setFillsViewportHeight(true);
		table.setBorder(null);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		scrollPane.setViewportView(table);

		JButton btnNewButton = new JButton("Edit Schema");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SimpleSchemaModel model = RecordPreviewPanel.this.model;
				DefaultSchemaModel m = new DefaultSchemaModel(Arrays.asList(model.schema.fieldInfos),model.getNullIndicator());
				m.addModelListener(RecordPreviewPanel.this);
				SimpleDialog.invoke((Component) e.getSource(), "Edit Schema", new SchemaEditorPanel(m, EditMode.REFINE));
			}
		});
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane)
						.addComponent(btnNewButton))
					.addGap(1))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 219, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(btnNewButton)
					.addGap(29))
		);
		setLayout(groupLayout);

		table.getTableHeader().setDefaultRenderer(
				new DefaultTableCellRenderer() {

					@Override
					public Component getTableCellRendererComponent(
							JTable table, Object value, boolean isSelected,
							boolean hasFocus, int row, int column) {
						if (column > 0 && RecordPreviewPanel.this.model != null) {
							Icon ic = Icons
									.getIcon(RecordPreviewPanel.this.model.schema.toRecordTokenType()
											.get(column - 1)
											.getType());
							setIcon(ic);
						} else {
						    setIcon(null);
						}
						setValue(table.getModel().getColumnName(column));
						setBorder(UIManager.getBorder("TableHeader.cellBorder"));
						setHorizontalAlignment(LEFT);
						return this;
					}
				});
		table.getTableHeader().setReorderingAllowed(false);
		
		if (!allowEdit) {
			btnNewButton.setVisible(false);
		}
	}

	public void setSampleData(RecordTokenList data) {
		table.setModel((TableModel) new RecordTableModel(data));
		// Resize columns
		TableColumnModel cm = table.getColumnModel();
		Enumeration<TableColumn> it = cm.getColumns();
		while(it.hasMoreElements()) {
			it.nextElement().setMinWidth(75);
		}
	}

	@Override
	public void schemaChanged(SchemaChangeEvent event) {
		SchemaModel model = event.getSource();
		this.model.schema.setSchema(model.getSchema());
		this.repaint();
	}
	
}
