
package org.pentaho.actionsequence.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.dom4j.DocumentHelper;
import static org.dom4j.DocumentHelper.makeElement;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import static org.pentaho.actionsequence.dom.ActionSequenceDocument.fireIoChanged;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.BIGDECIMAL_TYPE;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.DEFAULT_STRING_LIST_ITEM;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.DEFAULT_VAL_NAME;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.INPUT_SOURCES_NAME;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.INTEGER_TYPE;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.LIST_TYPE;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.LONG_TYPE;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.PROPERTY_MAP_ENTRY;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.PROPERTY_MAP_ENTRY_KEY;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.PROPERTY_MAP_LIST_TYPE;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.PROPERTY_MAP_TYPE;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.RESULTSET_DEFAULT_COLUMNS;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.RESULTSET_ROW;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.RESULTSET_TYPE;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.STRING_LIST_TYPE;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.STRING_TYPE;
import org.pentaho.actionsequence.dom.actions.IActionParameterMgr;

/**
 * A wrapper class for an action definition input or output element.
 *
 * @author Angelo Rodriguez
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ActionSequenceInput extends AbstractIOElement implements IActionSequenceInput {

    public static final int REQUEST_INPUT_SOURCE_ID = 1;
    public static final int SESSION_INPUT_SOURCE_ID = 2;
    public static final int RUNTIME_INPUT_SOURCE_ID = 3;
    public static final int GLOBAL_INPUT_SOURCE_ID = 4;

    public ActionSequenceInput(Element inputElement, IActionParameterMgr actionInputProvider) {
        super(inputElement, actionInputProvider);
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.pentaho.designstudio.dom.ActionSequenceIO#setType(java.lang.String)
     */
    @Override
    public void setType(String ioType) {
        if ((ioType != null) && !ioType.equals(getType())) {
            ioElement.addAttribute(TYPE_NAME, ioType);
            Element defValElement = ioElement.element(DEFAULT_VAL_NAME);
            if (defValElement != null) {
                defValElement.clearContent();
            }
            fireIoChanged(this);
        }
    }

    /**
     * Sets the input default value.
     *
     * @param defValue the default value
     */
    @Override
    public void setDefaultValue(String defValue) {
        Element defValElement = ioElement.element(DEFAULT_VAL_NAME);
        ioElement.elements(RESULTSET_DEFAULT_COLUMNS).clear();
        if (defValue == null) {
            if (defValElement != null) {
                defValElement.detach();
                fireIoChanged(this);
            }
        } else {
            if (defValElement == null) {
                defValElement = ioElement.addElement(DEFAULT_VAL_NAME);
            } else {
                defValElement.clearContent();
            }
            defValElement.addAttribute(TYPE_NAME, null);
            if (defValue.length() > 0) {
                defValElement.addCDATA(defValue);
            }
            fireIoChanged(this);
        }
    }

    /**
     * Sets the input default value.
     *
     * @param defValue the default value
     */
    @Override
    public void setDefaultValue(String[] defValue) {
        Element defValElement = ioElement.element(DEFAULT_VAL_NAME);
        ioElement.elements(RESULTSET_DEFAULT_COLUMNS).clear();
        if (defValue == null) {
            if (defValElement != null) {
                defValElement.detach();
                fireIoChanged(this);
            }
        } else {
            if (defValElement == null) {
                defValElement = ioElement.addElement(DEFAULT_VAL_NAME);
            } else {
                defValElement.clearContent();
            }
            if (defValue.length > 0) {
                defValElement.addAttribute(TYPE_NAME, STRING_LIST_TYPE);
                for (String defValue1 : defValue) {
                    defValElement.addElement(DEFAULT_STRING_LIST_ITEM).setText(defValue1);
                }
            }
            fireIoChanged(this);
        }
    }

    /**
     * Sets the input default value.
     *
     * @param paramMap the default value
     */
    @Override
    public void setDefaultValue(HashMap paramMap) {
        Element defValElement = ioElement.element(DEFAULT_VAL_NAME);
        ioElement.elements(RESULTSET_DEFAULT_COLUMNS).clear();
        if (paramMap == null) {
            if (defValElement != null) {
                defValElement.detach();
                fireIoChanged(this);
            }
        } else {
            if (defValElement == null) {
                defValElement = ioElement.addElement(DEFAULT_VAL_NAME);
            } else {
                defValElement.clearContent();
            }
            if (paramMap.size() > 0) {
                defValElement.addAttribute(TYPE_NAME, PROPERTY_MAP_TYPE);

                DefaultTableModel defaultTableModel = new DefaultTableModel();
                for (Iterator keyIter = paramMap.keySet().iterator(); keyIter.hasNext();) {
                    defaultTableModel.addColumn(keyIter.next().toString());
                }
                defaultTableModel.addRow((String[]) paramMap.values().toArray(new String[0]));
                initPropertyMap(defValElement, defaultTableModel);
            }
            fireIoChanged(this);
        }
    }

    /**
     * Sets the input default value.
     *
     * @param defValue the default value
     */
    @Override
    public void setDefaultValue(TableModel defValue) {
        setDefaultValue(defValue, false);
    }

    private void initPropertyMap(Element defValElement, TableModel defValue) {
        for (int rowIdx = 0; rowIdx < defValue.getRowCount(); rowIdx++) {
            Element propertyMapElement = defValElement.addElement(PROPERTY_MAP_TYPE);
            for (int colIdx = 0; colIdx < defValue.getColumnCount(); colIdx++) {
                Element entryElement = propertyMapElement.addElement(PROPERTY_MAP_ENTRY);
                entryElement.addAttribute(PROPERTY_MAP_ENTRY_KEY, defValue.getColumnName(colIdx));
                Object value = defValue.getValueAt(rowIdx, colIdx);
                entryElement.setText(value == null ? "" : value.toString()); //$NON-NLS-1$
            }
        }
    }

    private void setPropMapListDefVal(TableModel defValue) {
        Element defValElement = ioElement.element(DEFAULT_VAL_NAME);
        ioElement.elements(RESULTSET_DEFAULT_COLUMNS).clear();
        if (defValue == null) {
            if (defValElement != null) {
                defValElement.detach();
                fireIoChanged(this);
            }
        } else {
            if (defValElement == null) {
                defValElement = ioElement.addElement(DEFAULT_VAL_NAME);
            } else {
                defValElement.clearContent();
            }
            if (defValue.getColumnCount() > 0) {
                defValElement.addAttribute(TYPE_NAME, PROPERTY_MAP_LIST_TYPE);
                initPropertyMap(defValElement, defValue);
            }
            fireIoChanged(this);
        }
    }

    private void setResultSetDefVal(TableModel defValue) {
        Element defValElement = ioElement.element(DEFAULT_VAL_NAME);
        ioElement.elements(RESULTSET_DEFAULT_COLUMNS).clear();
        if (defValue == null) {
            if (defValElement != null) {
                defValElement.detach();
                fireIoChanged(this);
            }
        } else {
            if (defValElement == null) {
                defValElement = ioElement.addElement(DEFAULT_VAL_NAME);
            } else {
                defValElement.clearContent();
            }
            if (defValue.getColumnCount() > 0) {
                defValElement.addAttribute(TYPE_NAME, RESULTSET_TYPE);
                Element columnsElement = ioElement.addElement(RESULTSET_DEFAULT_COLUMNS);
                for (int colIdx = 0; colIdx < defValue.getColumnCount(); colIdx++) {
                    columnsElement.addElement(defValue.getColumnName(colIdx)).addAttribute(ActionSequenceResource.TYPE_NAME, STRING_TYPE);
                }
                for (int rowIdx = 0; rowIdx < defValue.getRowCount(); rowIdx++) {
                    Element rowElement = defValElement.addElement(RESULTSET_ROW);
                    for (int colIdx = 0; colIdx < defValue.getColumnCount(); colIdx++) {
                        Object value = defValue.getValueAt(rowIdx, colIdx);
                        Element cellElement = rowElement.addElement(defValue.getColumnName(colIdx));
                        cellElement.setText(value == null ? "" : value.toString()); //$NON-NLS-1$
                    }
                }
            }
            fireIoChanged(this);
        }
    }

    /**
     * Sets the input default value.
     *
     * @param defValue the default value
     * @param usePropertyMapList indicates whether the property map list element
     * or result set element should be used to save the default value.
     */
    @Override
    public void setDefaultValue(TableModel defValue, boolean usePropertyMapList) {
        if (usePropertyMapList) {
            setPropMapListDefVal(defValue);
        } else {
            setResultSetDefVal(defValue);
        }
    }

    private String[] getDefaultStringList() {
        String[] defaultStringList = null;
        Element defValElement = ioElement.element(DEFAULT_VAL_NAME);
        if (defValElement != null) {
            List listItems = defValElement.elements(DEFAULT_STRING_LIST_ITEM);
            defaultStringList = new String[listItems.size()];
            int index = 0;
            for (Iterator iter = listItems.iterator(); iter.hasNext();) {
                Element listItem = (Element) iter.next();
                defaultStringList[index++] = listItem.getText();
            }
        }
        return defaultStringList;
    }

    private String getDefaultString() {
        String defaultString = null;
        Element defValElement = ioElement.element(DEFAULT_VAL_NAME);
        if (defValElement != null) {
            defaultString = defValElement.getText();
        }
        return defaultString;
    }

    private LinkedHashMap getDefaultPropertyMap() {
        LinkedHashMap linkedHashMap = null;
        Element defValElement = ioElement.element(DEFAULT_VAL_NAME);
        if (defValElement != null) {
            linkedHashMap = new LinkedHashMap();
            Element propertyMapElement = defValElement.element(PROPERTY_MAP_TYPE);
            if (propertyMapElement != null) {
                List entries = propertyMapElement.elements(PROPERTY_MAP_ENTRY);
                for (Iterator iter = entries.iterator(); iter.hasNext();) {
                    Element entry = (Element) iter.next();
                    linkedHashMap.put(entry.attributeValue(PROPERTY_MAP_ENTRY_KEY), entry.getText());
                }
            }
        }
        return linkedHashMap;
    }

    private TableModel getDefaultPropertyMapList() {
        DefaultTableModel defaultTableModel = null;
        Element defValElement = ioElement.element(DEFAULT_VAL_NAME);
        if (defValElement != null) {
            defaultTableModel = new DefaultTableModel();
            List propertyMaps = defValElement.elements(PROPERTY_MAP_TYPE);
            HashSet columnSet = new HashSet();
            for (Iterator iter = propertyMaps.iterator(); iter.hasNext();) {
                Element propertyMap = (Element) iter.next();
                List propertyMapEntries = propertyMap.elements(PROPERTY_MAP_ENTRY);
                for (Iterator entryIter = propertyMapEntries.iterator(); entryIter.hasNext();) {
                    Element entry = (Element) entryIter.next();
                    columnSet.add(entry.attributeValue(PROPERTY_MAP_ENTRY_KEY));
                }
            }

            defaultTableModel.setColumnIdentifiers(columnSet.toArray());

            for (Iterator iter = propertyMaps.iterator(); iter.hasNext();) {
                Element propertyMap = (Element) iter.next();
                ArrayList row = new ArrayList();
                for (Iterator columnIter = columnSet.iterator(); columnIter.hasNext();) {
                    String columnName = ((String) columnIter.next());
                    String cellValue = ""; //$NON-NLS-1$
                    List propertyMapEntries = propertyMap.elements(PROPERTY_MAP_ENTRY);
                    for (Iterator entryIter = propertyMapEntries.iterator(); entryIter.hasNext();) {
                        Element entry = (Element) entryIter.next();
                        if (columnName.equals(entry.attributeValue(PROPERTY_MAP_ENTRY_KEY))) {
                            cellValue = entry.getText();
                            break;
                        }
                    }
                    row.add(cellValue);
                }
                defaultTableModel.addRow(row.toArray());
            }
        }
        return defaultTableModel;
    }

    private TableModel getDefaultResultSet() {
        DefaultTableModel defaultTableModel = null;
        Element defValElement = ioElement.element(DEFAULT_VAL_NAME);
        if (defValElement != null) {
            defaultTableModel = new DefaultTableModel();
            ArrayList columnList = new ArrayList();
            Element columnsElement = ioElement.element(RESULTSET_DEFAULT_COLUMNS);
            if (columnsElement != null) {
                List columns = columnsElement.elements();
                for (Iterator iter = columns.iterator(); iter.hasNext();) {
                    Element columnElement = (Element) iter.next();
                    columnList.add(columnElement.getName());
                }
            }

            defaultTableModel.setColumnIdentifiers(columnList.toArray());

            List rows = defValElement.elements(RESULTSET_ROW);
            for (Iterator rowIterator = rows.iterator(); rowIterator.hasNext();) {
                Element rowElement = (Element) rowIterator.next();
                ArrayList rowValues = new ArrayList();
                for (Iterator columnIter = columnList.iterator(); columnIter.hasNext();) {
                    String columnName = ((String) columnIter.next());
                    String cellValue = ""; //$NON-NLS-1$
                    Element cellElement = rowElement.element(columnName);
                    if (cellElement != null) {
                        cellValue = cellElement.getText();
                    }
                    rowValues.add(cellValue);
                }
                defaultTableModel.addRow(rowValues.toArray());
            }
        }
        return defaultTableModel;
    }

    /**
     * @return the default value or null if none exists.
     */
    @Override
    public Object getDefaultValue() {
        Object defVal = null;
        String type = getType();
        if (null != type) switch (type) {
            case STRING_LIST_TYPE:
                defVal = getDefaultStringList();
                break;
            case LIST_TYPE:
                defVal = getDefaultStringList();
                break;
            case RESULTSET_TYPE:
                defVal = getDefaultResultSet();
                break;
            case PROPERTY_MAP_TYPE:
                defVal = getDefaultPropertyMap();
                break;
            case PROPERTY_MAP_LIST_TYPE:
                defVal = getDefaultPropertyMapList();
                break;
            case STRING_TYPE:
                defVal = getDefaultString();
                break;
            case LONG_TYPE:
                defVal = getDefaultString();
                break;
            case INTEGER_TYPE:
                defVal = getDefaultString();
                break;
            case BIGDECIMAL_TYPE:
                defVal = getDefaultString();
                break;
            default:
                break;
        }
        return defVal;
    }

    /**
     *
     * @return
     */
    @Override
    public IActionSequenceInputSource[] getSources() {
        ArrayList inputSources = new ArrayList();
        List sourceElements = ioElement.selectNodes(INPUT_SOURCES_NAME + "/*"); //$NON-NLS-1$
        for (Iterator iter = sourceElements.iterator(); iter.hasNext();) {
            inputSources.add(new ActionSequenceInputSource((Element) iter.next(), actionInputProvider));
        }
        return (IActionSequenceInputSource[]) inputSources.toArray(new ActionSequenceInputSource[0]);
    }

    /**
     *
     * @param origin
     * @param name
     * @return
     */
    @Override
    public IActionSequenceInputSource addSource(String origin, String name) {
        Element sourceParent = makeElement(ioElement, INPUT_SOURCES_NAME);
        Element newSourceElement = sourceParent.addElement(origin);
        newSourceElement.setText(name);
        IActionSequenceInputSource actionSequenceInputSource
                = new ActionSequenceInputSource(newSourceElement, actionInputProvider);
        fireIoChanged(this);
        return actionSequenceInputSource;
    }

    /**
     *
     * @param index
     * @param origin
     * @param name
     * @return
     */
    @Override
    public IActionSequenceInputSource addSource(int index, String origin, String name) {
        if (index >= getSources().length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        Element sourceParent = ioElement.element(INPUT_SOURCES_NAME);
        Element newSourceElement = new DefaultElement(origin);
        List sources = sourceParent.elements();
        sources.add(index, newSourceElement);
        return new ActionSequenceInputSource(newSourceElement, actionInputProvider);
    }

    /**
     *
     * @return
     */
    @Override
    public String getVariableName() {
        return getName();
    }

}
