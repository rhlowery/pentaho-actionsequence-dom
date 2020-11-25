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

import java.net.URI;
import java.util.ArrayList;

import org.dom4j.Element;
import org.pentaho.actionsequence.dom.ActionSequenceValidationError;
import static org.pentaho.actionsequence.dom.ActionSequenceValidationError.INPUT_MISSING;
import static org.pentaho.actionsequence.dom.ActionSequenceValidationError.INPUT_REFERENCES_UNKNOWN_VAR;
import static org.pentaho.actionsequence.dom.ActionSequenceValidationError.INPUT_UNINITIALIZED;
import org.pentaho.actionsequence.dom.IActionInput;
import org.pentaho.actionsequence.dom.IActionInputSource;
import org.pentaho.actionsequence.dom.IActionOutput;
import org.pentaho.actionsequence.dom.IActionResource;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.CONTENT_TYPE;
import org.pentaho.actionsequence.dom.IActionSequenceValidationError;

@SuppressWarnings({"rawtypes", "unchecked"})
public class BirtReportAction extends ActionDefinition {

    public static final String COMPONENT_NAME = "org.pentaho.birt.BIRTReportComponent"; //$NON-NLS-1$
    public static final String OUTPUT_TYPE_ELEMENT = "output-type"; //$NON-NLS-1$
    public static final String REPORT_OUTPUT_ELEMENT = "report-output"; //$NON-NLS-1$
    public static final String REPORT_DEFINITION_ELEMENT = "report-definition"; //$NON-NLS-1$
    public static final String OUTPUT_REPORT = "output-report"; //$NON-NLS-1$

    protected static final String[] EXPECTED_INPUTS = new String[]{OUTPUT_TYPE_ELEMENT};
    public static final String[] EXPECTED_RESOURCES = new String[]{REPORT_DEFINITION_ELEMENT};

    public BirtReportAction(Element actionDefElement, IActionParameterMgr actionInputProvider) {
        super(actionDefElement, actionInputProvider);
    }

    public BirtReportAction() {
        super(COMPONENT_NAME);
    }

    @Override
    protected void initNewActionDefinition() {
        super.initNewActionDefinition();
        setComponentDefinition(OUTPUT_TYPE_ELEMENT, "html"); //$NON-NLS-1$
    }

    @Override
    public String[] getReservedInputNames() {
        return EXPECTED_INPUTS;
    }

    @Override
    public String[] getReservedOutputNames() {
        String privateOutputName = REPORT_OUTPUT_ELEMENT;
        if (getOutput(privateOutputName) == null) {
            IActionOutput[] actionOutputs = getOutputs(CONTENT_TYPE);
            if (actionOutputs.length > 0) {
                privateOutputName = actionOutputs[0].getName();
            }
        }
        return new String[]{privateOutputName};
    }

    @Override
    public String[] getReservedResourceNames() {
        return EXPECTED_RESOURCES;
    }

    public void setOutputType(IActionInputSource value) {
        setActionInputValue(OUTPUT_TYPE_ELEMENT, value);
    }

    public IActionInput getOutputType() {
        return getInput(OUTPUT_TYPE_ELEMENT);
    }

    public void setOutputReport(String publicOutputName) {
        setOutput(REPORT_OUTPUT_ELEMENT, publicOutputName, CONTENT_TYPE);
        if ((publicOutputName != null) && (publicOutputName.trim().length() > 0)) {
            IActionOutput[] actionOutputs = getOutputs();
            for (IActionOutput actionOutput : actionOutputs) {
                if (actionOutput.getType().equals(CONTENT_TYPE) && !actionOutput.getName().equals(REPORT_OUTPUT_ELEMENT)) {
                    actionOutput.delete();
                }
            }
        }
    }

    public IActionOutput getOutputReport() {
        IActionOutput actionOutput = getOutput(REPORT_OUTPUT_ELEMENT);

        if (actionOutput == null) {
            IActionOutput[] actionOutputs = getOutputs(CONTENT_TYPE);
            if (actionOutputs.length > 0) {
                actionOutput = actionOutputs[0];
            }
        }
        return actionOutput;
    }

    @Override
    public IActionSequenceValidationError[] validate() {
        ArrayList errors = new ArrayList();
        ActionSequenceValidationError validationError = validateResource(REPORT_DEFINITION_ELEMENT);
        if (validationError != null) {
            switch (validationError.errorCode) {
                case INPUT_MISSING:
                    validationError.errorMsg = "Missing report definition input parameter.";
                    break;
                case INPUT_REFERENCES_UNKNOWN_VAR:
                    validationError.errorMsg = "Report definition input parameter references unknown variable.";
                    break;
                case INPUT_UNINITIALIZED:
                    validationError.errorMsg = "Report definition input parameter is uninitialized.";
                    break;
            }
            errors.add(validationError);
        }

        validationError = validateInput(OUTPUT_TYPE_ELEMENT);
        if (validationError != null) {
            switch (validationError.errorCode) {
                case INPUT_MISSING:
                    validationError.errorMsg = "Missing report format input parameter.";
                    break;
                case INPUT_REFERENCES_UNKNOWN_VAR:
                    validationError.errorMsg = "Report format input parameter references unknown variable.";
                    break;
                case INPUT_UNINITIALIZED:
                    validationError.errorMsg = "Report format input parameter is uninitialized.";
                    break;
            }
            errors.add(validationError);
        }

        return (ActionSequenceValidationError[]) errors.toArray(new ActionSequenceValidationError[0]);
    }

    public static boolean accepts(Element element) {
        return ActionDefinition.accepts(element) && hasComponentName(element, COMPONENT_NAME);
    }

    public IActionResource setReportDefinition(URI uri, String mimeType) {
        return setResourceUri(REPORT_DEFINITION_ELEMENT, uri, mimeType);
    }

    public IActionResource getReportDefinition() {
        return getResource(REPORT_DEFINITION_ELEMENT);
    }
}
