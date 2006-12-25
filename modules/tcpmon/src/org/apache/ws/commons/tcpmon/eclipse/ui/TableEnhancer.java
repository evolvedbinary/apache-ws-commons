/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ws.commons.tcpmon.eclipse.ui;


import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.SWT;

/**
 * 
 *  A table enhancer class that wraps a table - A 'decorator' :)
 *
 */
public class TableEnhancer {
    private Table table;
    private int secondArgument;

    public TableEnhancer(Table table) {
        this.table = table;
    }

    public void setValueAt(String value, int row, int column) {
        TableItem item = table.getItem(row);
        item.setText(column, value);
    }

    public int getLeadSelectionIndex() {
        return secondArgument;
    }

    public int getMaxSelectionIndex() {
        int[] selectionIndices = table.getSelectionIndices();
        if (selectionIndices.length != 0) {
            int temp = selectionIndices[0];
            for (int i = 1; i < selectionIndices.length; i++) {
                if (temp < selectionIndices[i]) {
                    temp = selectionIndices[i];
                }
            }
            return temp;
        }
        return -1;
    }

    public int getNearestSelectionToZero() {
        int[] selectionIndices = sortArray(table.getSelectionIndices());
        if (selectionIndices[0] == 0) {
            return selectionIndices[1];
        } else {
            return selectionIndices[0];
        }
    }

    public int[] getSelectionIndicesWithoutZero() {
        int[] selectionIndices = sortArray(table.getSelectionIndices());
        if (selectionIndices[0] == 0) {
            int[] tempArray = new int[selectionIndices.length - 1];
            for (int i = 1; i < selectionIndices.length; i++) {
                tempArray[i - 1] = selectionIndices[i];
            }
            return tempArray;
        }
        return selectionIndices;
    }

    private int[] sortArray(int[] selectionIndices) {
        if (selectionIndices.length != 0) {
            for (int i = 0; i < selectionIndices.length; i++) {
                int temp = selectionIndices[i];
                int index = i;
                for (int j = i; j < selectionIndices.length; j++) {
                    if (temp > selectionIndices[j]) {
                        temp = selectionIndices[j];
                        index = j;
                    }
                }
                int element = selectionIndices[i];
                selectionIndices[i] = selectionIndices[index];
                selectionIndices[index] = element;
            }
            return selectionIndices;
        }
        return null;
    }

    public int getMinSelectionIndex() {
        int[] selectionIndices = table.getSelectionIndices();
        if (selectionIndices.length != 0) {
            int temp = selectionIndices[0];
            for (int i = 1; i < selectionIndices.length; i++) {
                if (temp > selectionIndices[i]) {
                    temp = selectionIndices[i];
                }
            }
            return temp;
        }
        return -1;
    }

    /**
     * @param index0
     * @param index1 Similar to setSelectionInterval(int index0, int index1) in DefaultListSelectionModel of SWING
     *               but index0 should be less than index1
     */
    public void setSelectionInterval(int index0, int index1) {
        secondArgument = index1;
        table.setSelection(index0, index1);
        notifyListeners(SWT.Selection, new Event());
    }


    public boolean isSelectionEmpty() {
        return (table.getSelectionIndices().length == 0);
    }

    public void selectAll() {
        table.selectAll();
    }

    public void clearSelection() {
        if (table.getSelectionCount() != 0) {
            notifyListeners(SWT.Selection, new Event());
        }
        table.deselectAll();
    }

    public int indexOf(TableItem item) {
        return (table.indexOf(item));
    }

    public void remove(int index) {
        table.remove(index);
    }


    public void notifyListeners(int type, Event e) {
        table.notifyListeners(type, e);
    }

    public boolean isSelectedIndex(int index) {
        int arr[] = table.getSelectionIndices();
        for (int i = 0; i < arr.length; i++) {
            if (index == arr[i]) {
                return true;
            }
        }
        return false;
    }
}
