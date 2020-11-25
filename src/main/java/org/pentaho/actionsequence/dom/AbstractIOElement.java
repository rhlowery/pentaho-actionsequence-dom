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

import org.dom4j.Document;
import org.dom4j.Element;
import static org.pentaho.actionsequence.dom.ActionSequenceDocument.fireIoChanged;
import static org.pentaho.actionsequence.dom.ActionSequenceDocument.fireIoRemoved;
import static org.pentaho.actionsequence.dom.ActionSequenceDocument.fireIoRenamed;
import org.pentaho.actionsequence.dom.actions.IActionParameterMgr;
import static org.pentaho.actionsequence.dom.messages.Messages.getString;

/**
 * A wrapper class for an action sequence input or output element.
 *
 * @author Angelo Rodriguez
 *
 */
public abstract class AbstractIOElement implements IAbstractIOElement {

    Element ioElement;
    IActionParameterMgr actionInputProvider;

    protected AbstractIOElement(Element ioElement, IActionParameterMgr actionInputProvider) {
        super();
        this.ioElement = ioElement;
        this.actionInputProvider = actionInputProvider;
    }

    /**
     * @return the name of the action sequence input/output
     */
    @Override
    public String getName() {
        return ioElement.getName();
    }

    /**
     * Sets the name of the action sequence input/output
     *
     * @param ioName the input/output name
     */
    @Override
    public void setName(String ioName) {
        ioName = ioName.trim();
        if (ioName.split("\\s+").length > 1) { //$NON-NLS-1$
            throw new IllegalArgumentException(getString("ActionSequenceIO.NO_SPACES_IN_NAME")); //$NON-NLS-1$
        }
        if (!ioElement.getName().equals(ioName)) {
            ioElement.setName(ioName);
            fireIoRenamed(this);
        }
    }

    /**
     * @return the type of input/output
     */
    @Override
    public String getType() {
        return ioElement.attributeValue(TYPE_NAME);
    }

    /**
     * Sets the type of the IO type.
     *
     * @param ioType the io type
     */
    @Override
    public void setType(String ioType) {
        if (!ioType.equals(ioElement.attributeValue(TYPE_NAME))) {
            ioElement.addAttribute(TYPE_NAME, ioType);
            fireIoChanged(this);
        }
    }

    /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.designstudio.dom.IActionSequenceElement#delete()
     */
    @Override
    public void delete() {
        Document doc = ioElement.getDocument();
        if (doc != null) {
            ioElement.detach();
            fireIoRemoved(new ActionSequenceDocument(doc, actionInputProvider), this);
        }
    }

    /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.designstudio.dom.IActionSequenceElement#getElement()
     */
    @Override
    public Element getElement() {
        return ioElement;
    }

    @Override
    public boolean equals(Object arg0) {
        boolean result = false;
        if (arg0 != null) {
            if (arg0.getClass() == this.getClass()) {
                AbstractIOElement io = (AbstractIOElement) arg0;
                result = (io.ioElement != null ? io.ioElement.equals(this.ioElement) : (io == this));
            }
        }
        return result;
    }

    /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.designstudio.dom.IActionSequenceElement#getDocument()
     */
    @Override
    public IActionSequenceDocument getDocument() {
        IActionSequenceDocument doc = null;
        if ((ioElement != null) && (ioElement.getDocument() != null)) {
            doc = new ActionSequenceDocument(ioElement.getDocument(), actionInputProvider);
        }
        return doc;
    }

    @Override
    public IActionParameterMgr getParameterMgr() {
        return actionInputProvider;
    }
}
