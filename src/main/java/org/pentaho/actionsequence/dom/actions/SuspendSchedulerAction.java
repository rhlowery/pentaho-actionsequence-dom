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

import org.dom4j.Element;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.COMPONENT_DEF_NAME;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.SUSPEND_SCHEDULER_CMND;

public class SuspendSchedulerAction extends ActionDefinition {

    public static final String COMPONENT_NAME = "org.pentaho.component.SchedulerAdminComponent"; //$NON-NLS-1$
    public static final String SCHEDULER_ACTION_ELEMENT = "schedulerAction"; //$NON-NLS-1$

    public SuspendSchedulerAction(Element actionDefElement, IActionParameterMgr actionInputProvider) {
        super(actionDefElement, actionInputProvider);
    }

    public SuspendSchedulerAction() {
        super(COMPONENT_NAME);
    }

    @Override
    protected void initNewActionDefinition() {
        super.initNewActionDefinition();
        setComponentDefinition(SCHEDULER_ACTION_ELEMENT, SUSPEND_SCHEDULER_CMND);
    }

    public static boolean accepts(Element element) {
        boolean result = false;
        if (ActionDefinition.accepts(element) && hasComponentName(element, COMPONENT_NAME)) {
            element
                    = (Element) element.selectSingleNode(COMPONENT_DEF_NAME
                            + "/" + SCHEDULER_ACTION_ELEMENT); //$NON-NLS-1$
            result = (element != null) && element.getText().equals(SUSPEND_SCHEDULER_CMND);
        }
        return result;
    }
}
