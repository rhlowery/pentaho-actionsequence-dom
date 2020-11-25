/*
 * Copyright 2002 - 2017 Hitachi Vantara.  All rights reserved.
 * 
 * This software was developed by Hitachi Vantara and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.actionsequence.dom.actions;

import java.util.ArrayList;
import org.dom4j.Element;
import org.pentaho.actionsequence.dom.ActionInput;
import org.pentaho.actionsequence.dom.ActionInputConstant;
import org.pentaho.actionsequence.dom.IActionInput;
import static org.pentaho.actionsequence.dom.IActionInput.NULL_INPUT;
import org.pentaho.actionsequence.dom.IActionInputSource;
import org.pentaho.actionsequence.dom.IActionInputVariable;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.COMPONENT_DEF_NAME;
import org.pentaho.actionsequence.dom.IActionSequenceElement;
import static org.pentaho.actionsequence.dom.actions.CopyParamAction.COPY_PARAM_COMMAND;
import static org.pentaho.actionsequence.dom.actions.FormatMsgAction.FORMAT_MSG_COMMAND;
import static org.pentaho.actionsequence.dom.actions.PrintMapValsAction.PRINT_MAP_VALS_COMMAND;

/**
 * @deprecated As of 2.0
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class PrintParamAction extends ActionDefinition {

    public static final String COMPONENT_NAME = "org.pentaho.component.UtilityComponent"; //$NON-NLS-1$
    public static final String PRINT_PARAMS_COMMAND = "print"; //$NON-NLS-1$
    public static final String DELIMITER_ELEMENT = "delimiter"; //$NON-NLS-1$
    public static final String PRINT_PARAMS_XPATH = "print/arg"; //$NON-NLS-1$
    public static final String PRINT_PARAM_PREFIX = "printParam"; //$NON-NLS-1$

    public PrintParamAction(Element actionDefElement, IActionParameterMgr actionInputProvider) {
        super(actionDefElement, actionInputProvider);
    }

    public PrintParamAction() {
        super(COMPONENT_NAME);
    }

    @Override
    protected void initNewActionDefinition() {
        super.initNewActionDefinition();
        setComponentDefinition(PRINT_PARAMS_COMMAND, ""); //$NON-NLS-1$
    }

    @SuppressWarnings("deprecation")
    public static boolean accepts(Element element) {
        boolean accepts = false;
        if (ActionDefinition.accepts(element) && hasComponentName(element, COMPONENT_NAME)) {
            accepts
                    = (element.selectNodes(COMPONENT_DEF_NAME + "/" + PRINT_PARAMS_COMMAND).size() == 1) //$NON-NLS-1$
                    && (element.selectSingleNode(COMPONENT_DEF_NAME
                            + "/" + FORMAT_MSG_COMMAND) == null) //$NON-NLS-1$
                    && (element.selectSingleNode(COMPONENT_DEF_NAME
                            + "/" + PRINT_MAP_VALS_COMMAND) == null) //$NON-NLS-1$
                    && (element.selectSingleNode(COMPONENT_DEF_NAME
                            + "/" + COPY_PARAM_COMMAND) == null); //$NON-NLS-1$
        }
        return accepts;
    }

    public boolean accepts(IActionSequenceElement actionDef) {
        return actionDef instanceof PrintParamAction;
    }

    public void setDelimiter(ActionInputConstant value) {
        String delimiter = (value != null ? value.getStringValue() : null);
        if (delimiter != null) {
            delimiter = "\"" + delimiter + "\"";
        }
        setComponentDefinition(PRINT_PARAMS_COMMAND + "/" + DELIMITER_ELEMENT, delimiter); //$NON-NLS-1$
    }

    public ActionInputConstant getDelimiter() {
        ActionInputConstant actionInputConstant = NULL_INPUT;
        String delimiter = getComponentDefinitionValue(PRINT_PARAMS_COMMAND + "/" + DELIMITER_ELEMENT); //$NON-NLS-1$
        if (delimiter != null) {
            if (delimiter.startsWith("\"") && delimiter.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
                if (delimiter.length() < 3) {
                    delimiter = ""; //$NON-NLS-1$
                } else {
                    delimiter = delimiter.substring(1, delimiter.length() - 1);
                }
            }
            actionInputConstant = new ActionInputConstant(delimiter, this.actionParameterMgr);
        }
        return actionInputConstant;
    }

    public IActionInput[] getInputsToPrint() {
        ArrayList printParams = new ArrayList();
        Element[] elements = getComponentDefElements(PRINT_PARAMS_XPATH);
        for (Element element : elements) {
            String printParamName = element.getText();
            IActionInput key = getInput(printParamName);
            if (key != NULL_INPUT) {
                printParams.add(key);
            }
        }
        return (IActionInput[]) printParams.toArray(new IActionInput[0]);
    }

    public void addInputToPrint(IActionInputSource inputSource) {
        IActionInput[] oldPrintParams = getInputsToPrint();
        for (IActionInput oldPrintParam : oldPrintParams) {
            if (oldPrintParam instanceof ActionInput) {
                ((ActionInput) oldPrintParam).delete();
            }
        }
        setComponentDefinition(PRINT_PARAMS_XPATH, new String[0]);

        ArrayList printParamNames = new ArrayList();
        for (IActionInput oldPrintParam : oldPrintParams) {
            if ((oldPrintParam instanceof ActionInputConstant) && (oldPrintParam.getValue() != null)) {
                String printParamName = getUniqueNameParam();
                printParamNames.add(printParamName);
                setActionInputValue(printParamName, (ActionInputConstant) oldPrintParam);
            } else if (oldPrintParam instanceof ActionInput) {
                ActionInput actionInput = (ActionInput) oldPrintParam;
                printParamNames.add(actionInput.getName());
                setActionInputValue(actionInput.getName(), actionInput);
            }
        }

        if (inputSource instanceof IActionInputVariable) {
            IActionInputVariable actionVariable = (IActionInputVariable) inputSource;
            printParamNames.add(actionVariable.getVariableName());
            setActionInputValue(actionVariable.getVariableName(), actionVariable);
        } else if ((inputSource instanceof ActionInputConstant)
                && (((ActionInputConstant) inputSource).getValue() != null)) {
            String printParamName = getUniqueNameParam();
            printParamNames.add(printParamName);
            setActionInputValue(printParamName, (ActionInputConstant) inputSource);
        }

        if (printParamNames.size() > 0) {
            setComponentDefinition(PRINT_PARAMS_XPATH, (String[]) printParamNames.toArray(new String[0]));
        }
    }

    public void setInputsToPrint(IActionInput[] values) {
        Object[] oldPrintParams = getInputsToPrint();
        for (Object oldPrintParam : oldPrintParams) {
            if (oldPrintParam instanceof ActionInput) {
                ((ActionInput) oldPrintParam).delete();
            }
        }
        setComponentDefinition(PRINT_PARAMS_XPATH, new String[0]);

        ArrayList printParamNames = new ArrayList();
        for (IActionInput value : values) {
            if (value instanceof IActionInputVariable) {
                IActionInputVariable actionVariable = (IActionInputVariable) value;
                printParamNames.add(actionVariable.getVariableName());
                setActionInputValue(actionVariable.getVariableName(), actionVariable);
            } else if ((value instanceof ActionInputConstant) && (value.getValue() != null)) {
                String printParamName = getUniqueNameParam();
                printParamNames.add(printParamName);
                setActionInputValue(printParamName, (ActionInputConstant) value);
            } else if (value instanceof ActionInput) {
                ActionInput actionInput = (ActionInput) value;
                printParamNames.add(actionInput.getName());
                setActionInputValue(actionInput.getName(), actionInput);
            }
        }
        if (printParamNames.size() > 0) {
            setComponentDefinition(PRINT_PARAMS_XPATH, (String[]) printParamNames.toArray(new String[0]));
        }
    }

    private String getUniqueNameParam() {
        String name = null;
        boolean isUnique = false;
        for (int i = 1; !isUnique; i++) {
            name = PRINT_PARAM_PREFIX + i;
            isUnique = (getInputParam(name) == null) && (getComponentDefElement(name) == null);
        }
        return name;
    }
}
