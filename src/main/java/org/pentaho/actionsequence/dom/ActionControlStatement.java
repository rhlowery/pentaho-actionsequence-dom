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

import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import static org.pentaho.actionsequence.dom.ActionSequenceDocument.fireActionAdded;
import static org.pentaho.actionsequence.dom.ActionSequenceDocument.fireControlStatementAdded;
import static org.pentaho.actionsequence.dom.ActionSequenceDocument.fireControlStatementRemoved;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.ACTIONS_NAME;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.ACTION_DEFINITION_NAME;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.CONDITION_NAME;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.DOC_ACTIONS_PATH;
import org.pentaho.actionsequence.dom.actions.ActionDefinition;
import static org.pentaho.actionsequence.dom.actions.ActionFactory.getActionDefinition;
import org.pentaho.actionsequence.dom.actions.IActionParameterMgr;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class ActionControlStatement implements IActionControlStatement {

    Element controlElement;
    IActionParameterMgr actionInputProvider;

    public ActionControlStatement(Element controlElement, IActionParameterMgr actionInputProvider) {
        this.controlElement = controlElement;
        this.actionInputProvider = actionInputProvider;
    }

    @Override
    public Element getElement() {
        return controlElement;
    }

    @Override
    public Element getControlElement() {
        return controlElement;
    }

    /**
     * Adds a new child action definition to the end of this control statements
     * list of children.
     *
     * @param componentName the name of the component that processes the action
     * definition
     * @return the newly created action definition
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Override
    public IActionDefinition addAction(Class actionDefinitionClass) {
        ActionDefinition action = null;
        try {
            action = (ActionDefinition) actionDefinitionClass.newInstance();
            controlElement.elements().add(action.getElement());
            fireActionAdded(action);
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return action;
    }

    /**
     * Adds a new child action definition to this control statement.
     *
     * @param componentName the name of the component that processes the action
     * definition
     * @param index the index of where to add the new action. If index is
     * greater than the number of children then the new action is added at the
     * end of the list of children.
     * @return the newly created action definition
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Override
    public IActionDefinition addAction(Class actionDefClass, int index) {
        IActionDefinition actionDef = null;
        try {
            Object[] children = getChildren();
            if (index >= children.length) {
                actionDef = addAction(actionDefClass);
            } else {
                Object childAtIndex = children[index];
                Element childElement
                        = (childAtIndex instanceof ActionControlStatement)
                                ? ((ActionControlStatement) childAtIndex).controlElement : ((ActionDefinition) childAtIndex)
                                        .getElement();
                List childElements = controlElement.elements();
                index = childElements.indexOf(childElement);
                if (index >= 0) {
                    actionDef = (ActionDefinition) actionDefClass.newInstance();
                    childElements.add(index, actionDef.getElement());
                    fireActionAdded(actionDef);
                } else {
                    actionDef = addAction(actionDefClass);
                }
            }
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return actionDef;
    }

    /**
     * @return the child actions and control statements
     */
    @Override
    public IActionSequenceExecutableStatement[] getChildren() {
        List allChildren = controlElement.elements();
        List filteredChildren = new ArrayList();
        for (Iterator iter = allChildren.iterator(); iter.hasNext();) {
            Element element = (Element) iter.next();
            String elementName = element.getName();
            if (elementName.equals(ACTION_DEFINITION_NAME)) {
                filteredChildren.add(getActionDefinition(element, actionInputProvider));
            } else if (elementName.equals(ACTIONS_NAME)) {
                if (element.element(CONDITION_NAME) == null) {
                    filteredChildren.add(new ActionLoop(element, actionInputProvider));
                } else {
                    filteredChildren.add(new ActionIfStatement(element, actionInputProvider));
                }
            }
        }
        return (IActionSequenceExecutableStatement[]) filteredChildren.toArray(new IActionSequenceExecutableStatement[0]);
    }

    /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.designstudio.dom.IActionSequenceElement#getDocument()
     */
    @Override
    public IActionSequenceDocument getDocument() {
        ActionSequenceDocument doc = null;
        if ((controlElement != null) && (controlElement.getDocument() != null)) {
            doc = new ActionSequenceDocument(controlElement.getDocument(), actionInputProvider);
        }
        return doc;
    }

    /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.designstudio.dom.IActionSequenceElement#delete()
     */
    @Override
    public void delete() {
        Document doc = controlElement.getDocument();
        if (doc != null) {
            controlElement.detach();
            ActionSequenceDocument asDoc = new ActionSequenceDocument(doc, actionInputProvider);
            fireControlStatementRemoved(asDoc, this);
        }
    }

    /**
     * @return the control statement that contains this action definition or
     * null if there is no parent control statement.
     */
    @Override
    public IActionControlStatement getParent() {
        IActionControlStatement controlStatement = null;
        if (controlElement != null) {
            Element ancestorElement = controlElement.getParent();
            if ((ancestorElement != null) && ancestorElement.getName().equals(ACTIONS_NAME)
                    && !ancestorElement.getPath().equals(DOC_ACTIONS_PATH)) {
                if (ancestorElement.element(CONDITION_NAME) == null) {
                    controlStatement = new ActionLoop(ancestorElement, actionInputProvider);
                } else {
                    controlStatement = new ActionIfStatement(ancestorElement, actionInputProvider);
                }
            }
        }
        return controlStatement;
    }

    /**
     * Adds an action definition to the end of this control statements list of
     * children. This control statement becomes the new parent of the action
     * definition.
     *
     * @param actionDef the action definition to be added.
     */
    @Override
    public void add(IActionDefinition actionDef) {
        actionDef.delete();
        controlElement.elements().add(actionDef.getElement());
        fireActionAdded(actionDef);
    }

    /**
     * Adds a new child action definition to this control statement.
     *
     * @param actionDef the action definition to be added.
     * @param index the index of where to add the new action. If index is
     * greater than the number of children then the new action is added at the
     * end of the list of children.
     */
    @Override
    public void add(IActionDefinition actionDef, int index) {
        IActionSequenceElement[] children = getChildren();
        if (index >= children.length) {
            add(actionDef);
        } else {
            Element childElement = children[index].getElement();
            List childElements = controlElement.elements();
            index = childElements.indexOf(childElement);
            int actionDefIndex = childElements.indexOf(actionDef.getElement());
            if (index >= 0) {
                actionDef.delete();
                if ((actionDefIndex >= 0) && (actionDefIndex < index)) {
                    index--;
                }
                controlElement.elements().add(index, actionDef.getElement());
                fireActionAdded(actionDef);
            } else {
                add(actionDef);
            }
        }
    }

    /**
     * Adds a control statement to the end of this control statments list of
     * children. This control statement becomes the new parent of the specified
     * control statement.
     *
     * @param controlStatement the control statment to be added.
     */
    @Override
    public void add(IActionControlStatement controlStatement) {
        controlStatement.delete();
        controlElement.elements().add(controlStatement.getControlElement());
        fireControlStatementAdded(controlStatement);
    }

    /**
     * Adds a control statement to this conrtol statement's list of children.
     *
     * @param controlStatement the control statement to be added.
     * @param index the index of where to add the new control statement. If
     * index is greater than the number of children then the new control
     * statement is added at the end of the list of children.
     */
    @Override
    public void add(IActionControlStatement controlStatement, int index) {
        IActionSequenceElement[] children = getChildren();
        if (index >= children.length) {
            add(controlStatement);
        } else {
            Element childElement = children[index].getElement();
            List childElements = controlElement.elements();
            index = childElements.indexOf(childElement);
            int actionLoopIndex = childElements.indexOf(controlStatement.getElement());
            if (index >= 0) {
                controlStatement.delete();
                if ((actionLoopIndex >= 0) && (actionLoopIndex < index)) {
                    index--;
                }
                controlElement.elements().add(index, controlStatement.getControlElement());
                fireControlStatementAdded(controlStatement);
            } else {
                add(controlStatement);
            }
        }
    }

    /**
     * Creates a new child action loop to the end of this control statement's
     * children.
     *
     * @param loopOn the loop on variable name
     */
    @Override
    public IActionLoop addLoop(String loopOn) {
        Element child = createLoopElement();
        controlElement.elements().add(child);
        ActionLoop loop = new ActionLoop(child, actionInputProvider);
        fireControlStatementAdded(loop);
        return loop;
    }

    /**
     * Creates a new child action loop.
     *
     * @param loopOn the loop on variable name
     * @param index the index where the new loop should be created. If index is
     * greater than the number of children then the new loop is added at the end
     * of the list of children.
     */
    @Override
    public IActionLoop addLoop(String loopOn, int index) {
        Object[] children = getChildren();
        IActionLoop actionLoop = null;
        if (index >= children.length) {
            actionLoop = addLoop(loopOn);
        } else {
            Object childAtIndex = children[index];
            Element childElement
                    = (childAtIndex instanceof ActionControlStatement) ? ((ActionControlStatement) childAtIndex).controlElement
                            : ((ActionDefinition) childAtIndex).getElement();
            List childElements = controlElement.elements();
            index = childElements.indexOf(childElement);
            if (index >= 0) {
                Element child = createLoopElement();
                childElements.add(index, child);
                actionLoop = new ActionLoop(child, actionInputProvider);
                fireControlStatementAdded(actionLoop);
            } else {
                actionLoop = addLoop(loopOn);
            }
        }
        return actionLoop;
    }

    /**
     * Creates a new child if statement to the end of this control statement's
     * children.
     *
     * @param condition the if condition
     */
    @Override
    public IActionIfStatement addIf(String condition) {
        Element child = createIfElement();
        controlElement.elements().add(child);
        ActionIfStatement actionIf = new ActionIfStatement(child, actionInputProvider);
        fireControlStatementAdded(actionIf);
        return actionIf;
    }

    /**
     * Creates a new child if statement.
     *
     * @param condition the if condition
     * @param index the index where the new if statement should be created. If
     * index is greater than the number of children then the new if statement is
     * added at the end of the list of children.
     */
    @Override
    public IActionIfStatement addIf(String condition, int index) {
        Object[] children = getChildren();
        IActionIfStatement actionIf = null;
        if (index >= children.length) {
            actionIf = addIf(condition);
        } else {
            Object childAtIndex = children[index];
            Element childElement
                    = (childAtIndex instanceof ActionControlStatement) ? ((ActionControlStatement) childAtIndex).controlElement
                            : ((ActionDefinition) childAtIndex).getElement();
            List childElements = controlElement.elements();
            index = childElements.indexOf(childElement);
            if (index >= 0) {
                Element child = createIfElement();
                childElements.add(index, child);
                actionIf = new ActionIfStatement(child, actionInputProvider);
                fireControlStatementAdded(actionIf);
            } else {
                actionIf = addIf(condition);
            }
        }
        return actionIf;
    }

    private Element createLoopElement() {
        Element element = null;
        element = new DefaultElement(ACTIONS_NAME);
        return element;
    }

    private Element createIfElement() {
        Element element = null;
        element = new DefaultElement(ACTIONS_NAME);
        element.addElement(CONDITION_NAME);
        return element;
    }

    /**
     * Returns the list of ActionSequenceInputs and ActionOutputs that are
     * available as inputs to this control statement.
     *
     * @param types the desired input type
     * @return the list of available inputs
     */
    @Override
    public IActionInputVariable[] getAvailInputVariables() {
        return getDocument().getAvailInputVariables(this);
    }

    @Override
    public boolean equals(Object arg0) {
        boolean result = false;
        if (arg0 != null) {
            if (arg0.getClass() == this.getClass()) {
                ActionControlStatement controlStatement = (ActionControlStatement) arg0;
                result
                        = (controlStatement.controlElement != null ? controlStatement.controlElement.equals(this.controlElement)
                                : (controlStatement == this));
            }
        }
        return result;
    }

    /**
     * Returns the list of action definitions that precede this control
     * statement in the action sequence.
     *
     * @param types the desired input type
     * @return the preceding acton defintions.
     */
    @Override
    public IActionDefinition[] getPrecedingActionDefinitions() {
        return getDocument().getPrecedingActionDefinitions(this);
    }

    @Override
    public IActionSequenceExecutableStatement[] getPrecedingExecutableStatements() {
        return getDocument().getPrecedingExecutables(this);
    }

    protected abstract IActionSequenceValidationError[] validateThis();

    @Override
    public IActionSequenceValidationError[] validate() {
        return validate(false);
    }

    @Override
    public IActionSequenceValidationError[] validate(boolean validateDescendants) {
        ArrayList errors = new ArrayList();
        errors.add(validateThis());
        if (validateDescendants) {
            IActionSequenceExecutableStatement[] children = getChildren();
            for (IActionSequenceExecutableStatement children1 : children) {
                if (children1 instanceof ActionDefinition) {
                    ActionDefinition actionDefinition = (ActionDefinition) children1;
                    errors.addAll(asList(actionDefinition.validate()));
                } else if (children1 instanceof ActionControlStatement) {
                    ActionControlStatement actionControlStatement = (ActionControlStatement) children1;
                    errors.addAll(asList(actionControlStatement.validate(true)));
                }
            }
        }
        return (IActionSequenceValidationError[]) errors.toArray(new ActionSequenceValidationError[0]);
    }

}
