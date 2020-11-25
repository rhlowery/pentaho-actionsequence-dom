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

import static java.lang.Integer.parseInt;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import org.pentaho.actionsequence.dom.ActionInput;
import org.pentaho.actionsequence.dom.ActionInputConstant;
import org.pentaho.actionsequence.dom.IActionInput;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.BIGDECIMAL_TYPE;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.INTEGER_TYPE;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.LONG_TYPE;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.STRING_TYPE;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ActionInputTypeFilter implements IActionInputFilter {

    ArrayList types = new ArrayList();
    boolean includeConstants = false;

    public ActionInputTypeFilter(String[] types, boolean includeConstants) {
        if (types != null) {
            this.types.addAll(asList(types));
        }
        this.includeConstants = includeConstants;
    }

    public ActionInputTypeFilter(String[] types) {
        this(types, false);
    }

    public ActionInputTypeFilter(String type) {
        this(new String[]{type}, false);
    }

    @Override
    public boolean accepts(IActionInput actionInput) {
        boolean result = false;
        if (includeConstants && (actionInput instanceof ActionInputConstant)) {
            ActionInputConstant constant = (ActionInputConstant) actionInput;
            if (types.contains(STRING_TYPE)) {
                result = constant.getValue() instanceof String;
            } else if (types.contains(LONG_TYPE)
                    || types.contains(INTEGER_TYPE)
                    || types.contains(BIGDECIMAL_TYPE)) {
                if (constant.getValue() instanceof String) {
                    try {
                        parseInt(constant.getStringValue());
                        result = true;
                    } catch (NumberFormatException ex) {
                        result = false;
                    }
                }
            }
        } else {
            result = (actionInput instanceof ActionInput) && (types.contains(((ActionInput) actionInput).getType()));
        }
        return result;
    }

}
