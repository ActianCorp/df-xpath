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

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import com.pervasive.datarush.sequences.record.RecordTokenList;
import com.pervasive.datarush.tokens.TokenValued;
import com.pervasive.datarush.tokens.scalar.IntSettable;
import com.pervasive.datarush.tokens.scalar.IntValued;
import com.pervasive.datarush.tokens.scalar.ScalarSettable;
import com.pervasive.datarush.tokens.scalar.StringSettable;
import com.pervasive.datarush.tokens.scalar.StringValued;

public class RecordTableModel implements TableModel {

	private final RecordTokenList data;
	
	public RecordTableModel(RecordTokenList data) {
		this.data = data;
	}

	@Override
	public int getRowCount() {
		// TODO some kind of NPE in data.size();
		int size=0;
		if (data != null) {
			try {
				size = data.size();
			} catch (Exception ex) {
				;
			}
		}
		return size;
	}

	@Override
	public int getColumnCount() {
		return data == null ? 0 : data.columns() + 1;
	}

	@Override
	public String getColumnName(int columnIndex) {
		if (columnIndex == 0) {
			return "Row Number";
		}
		return data.getType().getName(columnIndex-1);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return Long.class;
		}
		return String.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) { 
			return new Long(rowIndex + 1);
		}
		ScalarSettable cell = data.getTokenSetter(rowIndex).getField(columnIndex-1);
		if (((TokenValued)cell).isNull()) {
			return "?";
		}
		if (cell instanceof StringValued) {
			return ((StringValued)cell).asString();
		} else if (cell instanceof IntValued) {
			return String.valueOf(((IntValued)cell).asInt());
		}
		return "?";
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
	}

}
