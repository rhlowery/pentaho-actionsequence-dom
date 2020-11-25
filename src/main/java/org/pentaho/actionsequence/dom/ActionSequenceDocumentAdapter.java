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

public class ActionSequenceDocumentAdapter implements IActionSequenceDocumentListener {

    public ActionSequenceDocumentAdapter() {
        super();
    }

    @Override
    public void ioAdded(IAbstractIOElement io) {
    }

    @Override
    public void ioRemoved(Object parent, IAbstractIOElement io) {
    }

    @Override
    public void ioRenamed(IAbstractIOElement io) {
    }

    @Override
    public void ioChanged(IAbstractIOElement io) {
    }

    @Override
    public void resourceAdded(Object resource) {
    }

    @Override
    public void resourceRemoved(Object parent, Object resource) {
    }

    @Override
    public void resourceRenamed(Object resource) {
    }

    @Override
    public void resourceChanged(Object resource) {
    }

    @Override
    public void actionAdded(IActionDefinition action) {
    }

    @Override
    public void actionRemoved(Object parent, IActionDefinition action) {
    }

    @Override
    public void actionRenamed(IActionDefinition action) {
    }

    @Override
    public void actionChanged(IActionDefinition action) {
    }

    @Override
    public void controlStatementAdded(IActionControlStatement controlStatement) {
    }

    @Override
    public void controlStatementRemoved(Object parent, IActionControlStatement controlStatement) {
    }

    @Override
    public void controlStatementChanged(IActionControlStatement controlStatement) {
    }

    @Override
    public void headerChanged(IActionSequenceDocument actionSequenceDocument) {
    }

}
