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
import org.pentaho.actionsequence.dom.ActionSequenceValidationError;
import static org.pentaho.actionsequence.dom.ActionSequenceValidationError.OUTPUT_MISSING;
import static org.pentaho.actionsequence.dom.IAbstractIOElement.TYPE_NAME;
import org.pentaho.actionsequence.dom.IActionOutput;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.ACTION_OUTPUTS_NAME;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.XQUERY_CONNECTION_TYPE;
import org.pentaho.actionsequence.dom.IActionSequenceValidationError;

@SuppressWarnings({"rawtypes", "unchecked"})
public class XQueryConnectionAction extends ActionDefinition {

    public static final String COMPONENT_NAME = "org.pentaho.component.XQueryLookupRule"; //$NON-NLS-1$
    public static final String PREPARED_COMPONENT_ELEMENT = "prepared_component"; //$NON-NLS-1$
    public static final String DEFAULT_CONNECTION_NAME = "shared_xquery_connection"; //$NON-NLS-1$

    protected static final String[] EXPECTED_OUTPUTS = new String[]{PREPARED_COMPONENT_ELEMENT,};

    public XQueryConnectionAction(Element actionDefElement, IActionParameterMgr actionInputProvider) {
        super(actionDefElement, actionInputProvider);
    }

    public XQueryConnectionAction() {
        super(COMPONENT_NAME);
    }

    public static boolean accepts(Element element) {
        boolean result = false;
        if (ActionDefinition.accepts(element) && hasComponentName(element, COMPONENT_NAME)) {
            Element connectionOutput
                    = (Element) element.selectSingleNode(ACTION_OUTPUTS_NAME
                            + "/" + PREPARED_COMPONENT_ELEMENT); //$NON-NLS-1$
            result
                    = (connectionOutput != null)
                    && XQUERY_CONNECTION_TYPE.equals(connectionOutput
                            .attributeValue(TYPE_NAME));
        }
        return result;
    }

    @Override
    public String[] getReservedInputNames() {
        return new String[0];
    }

    @Override
    public String[] getReservedOutputNames() {
        return EXPECTED_OUTPUTS;
    }

    @Override
    public String[] getReservedResourceNames() {
        return new String[0];
    }

    public void setOutputConnection(String publicOutputName) {
        setOutput(PREPARED_COMPONENT_ELEMENT, publicOutputName, XQUERY_CONNECTION_TYPE);
    }

    public IActionOutput getOutputConnection() {
        return getOutput(PREPARED_COMPONENT_ELEMENT);
    }

    @Override
    public IActionSequenceValidationError[] validate() {
        ArrayList errors = new ArrayList();
        ActionSequenceValidationError validationError = validateOutput(PREPARED_COMPONENT_ELEMENT);
        if (validationError != null) {
            if (validationError.errorCode == OUTPUT_MISSING) {
                validationError.errorMsg = "Missing output connection name.";
            }
            errors.add(validationError);
        }

        return (ActionSequenceValidationError[]) errors.toArray(new ActionSequenceValidationError[0]);

    }

    @Override
    protected void initNewActionDefinition() {
        super.initNewActionDefinition();
        setOutputConnection(DEFAULT_CONNECTION_NAME);
    }

}
