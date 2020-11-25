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

import static java.lang.Boolean.parseBoolean;
import java.util.ArrayList;

import org.dom4j.Attribute;
import org.dom4j.Element;
import static org.pentaho.actionsequence.dom.ActionSequenceDocument.fireControlStatementChanged;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.LOOP_ON_NAME;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.PEEK_ONLY_NAME;
import org.pentaho.actionsequence.dom.actions.IActionParameterMgr;

/**
 * A wrapper class for an action loop.
 *
 * @author Angelo Rodriguez
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ActionLoop extends ActionControlStatement implements IActionLoop {

    public ActionLoop(Element loopElement, IActionParameterMgr actionInputProvider) {
        super(loopElement, actionInputProvider);
    }

    /**
     * Set the name of the parameter that is being looped on.
     *
     * @param loopOn the parameter name. If null the loop parameter is removed.
     */
    @Override
    public void setLoopOn(String loopOn) {
        Attribute attr = controlElement.attribute(LOOP_ON_NAME);
        if (loopOn == null) {
            if (attr != null) {
                attr.detach();
                fireControlStatementChanged(this);
            }
        } else {
            loopOn = loopOn.trim();
            if (attr == null) {
                controlElement.addAttribute(LOOP_ON_NAME, loopOn);
                attr = controlElement.attribute(LOOP_ON_NAME);
                fireControlStatementChanged(this);
            } else if (!loopOn.equals(attr.getValue())) {
                attr.setValue(loopOn);
                fireControlStatementChanged(this);
            }
        }
    }

    /**
     * @return loopOn the name of the parameter that is being looped on
     */
    @Override
    public String getLoopOn() {
        return controlElement.attributeValue(LOOP_ON_NAME);
    }

    @Override
    protected IActionSequenceValidationError[] validateThis() {
        ArrayList errors = new ArrayList();
        String loopOn = getLoopOn();
        if (loopOn.trim().length() == 0) {
            errors.add("Missing loop variable.");
        } else {
            IActionInputVariable[] actionVariables = getDocument().getAvailInputVariables(this);
            boolean isValid = false;
            for (int i = 0; (i < actionVariables.length) && !isValid; i++) {
                isValid = actionVariables[i].getVariableName().equals(loopOn);
            }
            if (!isValid) {
                errors.add("Loop references unknown variable.");
            }
        }
        return (ActionSequenceValidationError[]) errors.toArray(new ActionSequenceValidationError[0]);
    }

    @Override
    public Boolean getLoopUsingPeek() {
        return parseBoolean(controlElement.attributeValue(PEEK_ONLY_NAME));
    }

    @Override
    public void setLoopUsingPeek(Boolean usePeek) {

        Attribute attr = controlElement.attribute(PEEK_ONLY_NAME);
        if (usePeek == null) {
            if (attr != null) {
                attr.detach();
                fireControlStatementChanged(this);
            }
        } else {
            if (attr == null) {
                controlElement.addAttribute(PEEK_ONLY_NAME, usePeek.toString());
                attr = controlElement.attribute(PEEK_ONLY_NAME);
                fireControlStatementChanged(this);
            } else if (!usePeek.toString().equals(attr.getValue())) {
                attr.setValue(usePeek.toString());
                fireControlStatementChanged(this);
            }
        }
    }

}
