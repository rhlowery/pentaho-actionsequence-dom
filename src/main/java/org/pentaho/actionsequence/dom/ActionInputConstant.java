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

import static java.lang.Integer.parseInt;
import org.dom4j.Element;
import org.pentaho.actionsequence.dom.actions.IActionParameterMgr;

// This class is used to set an action input to a constant value.
public class ActionInputConstant implements IActionInput, IActionInputSource {

    Object value;
    String inputName;
    IActionParameterMgr actionParameterMgr;

    public ActionInputConstant(Element componentDefElement, IActionParameterMgr actionParameterMgr) {
        inputName = componentDefElement.getName();
        value = componentDefElement.getText();
        this.actionParameterMgr = actionParameterMgr;
    }

    // Not intended for general use. Use one parameter option.
    public ActionInputConstant(Object value, IActionParameterMgr actionParameterMgr) {
        this.value = value;
        this.actionParameterMgr = actionParameterMgr;
    }

    // Not intended for general use. Use one parameter option.
    public ActionInputConstant(String value, IActionParameterMgr actionParameterMgr) {
        this.value = value;
        this.actionParameterMgr = actionParameterMgr;
    }

    // Not intended for general use. Use one parameter option.
    public ActionInputConstant(Boolean value, IActionParameterMgr actionParameterMgr) {
        this.value = value;
        this.actionParameterMgr = actionParameterMgr;
    }

    // Not intended for general use. Use one parameter option.
    public ActionInputConstant(boolean value, IActionParameterMgr actionParameterMgr) {
        this.value = value;
        this.actionParameterMgr = actionParameterMgr;
    }

    // Not intended for general use. Use one parameter option.
    public ActionInputConstant(Integer value, IActionParameterMgr actionParameterMgr) {
        this.value = value;
        this.actionParameterMgr = actionParameterMgr;
    }

    // Not intended for general use. Use one parameter option.
    public ActionInputConstant(int value, IActionParameterMgr actionParameterMgr) {
        this.value = value;
        this.actionParameterMgr = actionParameterMgr;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String getStringValue() {
        return getStringValue(true, null);
    }

    @Override
    public String getStringValue(boolean replaceParamReferences) {
        return getStringValue(replaceParamReferences, null);
    }

    @Override
    public String getStringValue(boolean replaceParamReferences, String defaultValue) {
        Object theValue = value;
        if (replaceParamReferences && (actionParameterMgr != null) && (theValue != null)) {
            theValue = actionParameterMgr.replaceParameterReferences(theValue.toString());
        }
        return theValue != null ? theValue.toString() : defaultValue;
    }

    @Override
    public String getStringValue(String defaultValue) {
        return getStringValue(true, defaultValue);
    }

    @Override
    public Boolean getBooleanValue() {
        Boolean boolValue = null;
        String stringValue = getStringValue();
        if (stringValue != null) {
            boolValue = new Boolean(stringValue);
        }
        return boolValue;
    }

    @Override
    public boolean getBooleanValue(boolean defaultValue) {
        Boolean boolValue = getBooleanValue();
        return boolValue != null ? boolValue : defaultValue;
    }

    @Override
    public Integer getIntValue() {
        Integer intValue = null;
        String stringValue = getStringValue();
        if (stringValue != null) {
            try {
                intValue = parseInt(stringValue);
            } catch (NumberFormatException e) {
                intValue = null;
            }
        }
        return intValue;
    }

    @Override
    public int getIntValue(int defaultValue) {
        Integer intValue = getIntValue();
        return intValue != null ? intValue : defaultValue;
    }

    @Override
    public boolean equals(Object obj) {
        return value != null && (obj instanceof ActionInputConstant)
                && value.equals(((ActionInputConstant) obj).getValue());
    }

    @Override
    public String getName() {
        return inputName;
    }

    public String getType() {
        return "";
    }

    public String getVariableName() {
        return getName();
    }

}
