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
package org.pentaho.actionsequence.dom;

import org.dom4j.Element;
import static org.pentaho.actionsequence.dom.ActionSequenceDocument.fireControlStatementChanged;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.CONDITION_NAME;
import org.pentaho.actionsequence.dom.actions.IActionParameterMgr;

/**
 * A wrapper class for an action if statement.
 *
 * @author Angelo Rodriguez
 *
 */
public class ActionIfStatement extends ActionControlStatement implements IActionIfStatement {

    private static final ActionSequenceValidationError[] EMPTY_ARRAY = new ActionSequenceValidationError[0];

    public ActionIfStatement(Element controlElement, IActionParameterMgr actionInputProvider) {
        super(controlElement, actionInputProvider);
    }

    /**
     * Sets the if condition. The condition should be well formatted javascript.
     *
     * @param condition the condition.
     */
    public void setCondition(String condition) {
        Element conditionElement = controlElement.element(CONDITION_NAME);
        if (conditionElement == null) {
            conditionElement = controlElement.addElement(CONDITION_NAME);
        }
        conditionElement.clearContent();
        conditionElement.addCDATA(condition);
        fireControlStatementChanged(this);
    }

    /**
     * @return the condition.
     */
    public String getCondition() {
        String condition = ""; //$NON-NLS-1$
        Element conditionElement = controlElement.element(CONDITION_NAME);
        if (conditionElement != null) {
            condition = conditionElement.getText();
        }
        return condition;
    }

    protected IActionSequenceValidationError[] validateThis() {
        return EMPTY_ARRAY;
    }

}
