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
import static java.util.Arrays.asList;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import static org.dom4j.DocumentHelper.makeElement;
import static org.dom4j.DocumentHelper.parseText;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.pentaho.actionsequence.dom.ActionControlStatement;
import org.pentaho.actionsequence.dom.ActionIfStatement;
import org.pentaho.actionsequence.dom.ActionInput;
import org.pentaho.actionsequence.dom.ActionInputConstant;
import org.pentaho.actionsequence.dom.ActionLoop;
import org.pentaho.actionsequence.dom.ActionOutput;
import org.pentaho.actionsequence.dom.ActionResource;
import org.pentaho.actionsequence.dom.ActionSequenceDocument;
import static org.pentaho.actionsequence.dom.ActionSequenceDocument.fireActionChanged;
import static org.pentaho.actionsequence.dom.ActionSequenceDocument.fireActionRemoved;
import static org.pentaho.actionsequence.dom.ActionSequenceDocument.fireActionRenamed;
import static org.pentaho.actionsequence.dom.ActionSequenceDocument.fireIoAdded;
import static org.pentaho.actionsequence.dom.ActionSequenceDocument.fireResourceAdded;
import org.pentaho.actionsequence.dom.ActionSequenceInput;
import org.pentaho.actionsequence.dom.ActionSequenceValidationError;
import static org.pentaho.actionsequence.dom.ActionSequenceValidationError.INPUT_MISSING;
import static org.pentaho.actionsequence.dom.ActionSequenceValidationError.INPUT_OK;
import static org.pentaho.actionsequence.dom.ActionSequenceValidationError.INPUT_REFERENCES_UNKNOWN_VAR;
import static org.pentaho.actionsequence.dom.ActionSequenceValidationError.INPUT_UNINITIALIZED;
import static org.pentaho.actionsequence.dom.ActionSequenceValidationError.OUTPUT_MISSING;
import static org.pentaho.actionsequence.dom.IAbstractIOElement.TYPE_NAME;
import org.pentaho.actionsequence.dom.IActionControlStatement;
import org.pentaho.actionsequence.dom.IActionDefinition;
import org.pentaho.actionsequence.dom.IActionInput;
import static org.pentaho.actionsequence.dom.IActionInput.NULL_INPUT;
import org.pentaho.actionsequence.dom.IActionInputSource;
import org.pentaho.actionsequence.dom.IActionInputVariable;
import org.pentaho.actionsequence.dom.IActionOutput;
import org.pentaho.actionsequence.dom.IActionResource;
import org.pentaho.actionsequence.dom.IActionSequenceDocument;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.ACTIONS_NAME;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.ACTION_DEFINITION_NAME;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.ACTION_INPUTS_NAME;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.ACTION_OUTPUTS_NAME;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.ACTION_RESOURCES_NAME;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.ACTION_TYPE_NAME;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.COMPONENT_DEF_NAME;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.COMPONENT_NAME;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.CONDITION_NAME;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.DOC_ACTIONS_PATH;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.RESOURCE_TYPE;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.STRING_TYPE;
import org.pentaho.actionsequence.dom.IActionSequenceResource;
import org.pentaho.actionsequence.dom.IActionSequenceValidationError;
import org.pentaho.actionsequence.dom.ImplicitActionResource;
import static org.pentaho.actionsequence.dom.messages.Messages.getString;

/**
 * A wrapper for an action definition element within an action sequence.
 *
 * @author Angelo Rodriguez
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ActionDefinition implements IActionDefinition {

    private static final IActionSequenceValidationError[] EMPTY_ARRAY = new ActionSequenceValidationError[0];

    Element actionDefElement;
    IActionParameterMgr actionParameterMgr;
    private static final String[] EXPECTED_INPUTS = new String[0];
    private static final String[] EXPECTED_OUTPUTS = new String[0];
    private static final String[] EXPECTED_RESOURCES = new String[0];

    /**
     * @param actionDefElement the wrapped action definition element
     */
    public ActionDefinition() {
        this(getString("ActionDefinition.ENTER_CLASS_NAME")); //$NON-NLS-1$
        setDescription(getString("ActionDefinition.CUSTOM_ACTION_TITLE")); //$NON-NLS-1$
    }

    public ActionDefinition(Element actionDefElement, IActionParameterMgr actionParameterMgr) {
        super();
        this.actionDefElement = actionDefElement;
        this.actionParameterMgr = actionParameterMgr;
    }

    protected ActionDefinition(String componentName) {
        int i = componentName.lastIndexOf("."); //$NON-NLS-1$
        if ((i >= 0) && (i < (componentName.length() - 1))) {
            componentName = componentName.substring(i + 1);
        }
        actionDefElement = new DefaultElement(ACTION_DEFINITION_NAME);
        actionDefElement.addElement(COMPONENT_NAME).setText(componentName);
        actionDefElement.addElement(COMPONENT_DEF_NAME);
        initNewActionDefinition();
    }

    /**
     * Sets the URI and mime type of the specified resource. If the resource
     * does not exist it is created. If the URI is null the named resource is
     * deleted.
     *
     * @param privateParamName the name of the resource as it is known by the
     * component processing this action definition.
     * @param URI the resource URI. May be null if you wish to delete the
     * resource.
     * @param mimeType the resource mime type. Ignored if the resource URI is
     * null.
     * @return the modified/created resource, or null if the resource is
     * deleted.
     */
    @Override
    public IActionResource setResourceUri(String privateResourceName, URI uri, String mimeType) {
        IActionResource actionResource = getResource(privateResourceName);
        if (uri == null) {
            if (actionResource != null) {
                actionResource.setURI(null);
            }
            actionResource = null;
        } else {
            if (actionResource == null) {
                actionResource = addResource(privateResourceName);
            }
            actionResource.setURI(uri);
            actionResource.setMimeType(mimeType);
        }
        return actionResource;
    }

    protected void initNewActionDefinition() {

    }

    ;

  /**
   * Compares the component name of the given action definition element with the given component name. It is the short
   * component names that are compared. The short name is the substring following the last "." in the name provided. If
   * there is no "." in the name the short name is the provided name.
   * 
   * @param actionDefinitionElement
   *          the action definition element whose component name is to be compared.
   * @param componentName
   *          the component name to compare to.
   * @return true if the short component names are equal or false otherwise.
   */
  protected static boolean hasComponentName(Element actionDefinitionElement, String componentName) {
        String elementComponentName = getComponentName(actionDefinitionElement);
        int index = elementComponentName.lastIndexOf("."); //$NON-NLS-1$
        if ((index >= 0) && (index < elementComponentName.length() - 1)) {
            elementComponentName = elementComponentName.substring(index + 1);
        }

        index = componentName.lastIndexOf("."); //$NON-NLS-1$
        if ((index >= 0) && (index < componentName.length() - 1)) {
            componentName = componentName.substring(index + 1);
        }

        return componentName.equals(elementComponentName);
    }

    /**
     * Determines whether this action definition is capable of processing the
     * provided action definition element.
     *
     * @param actionDefinitionElement the action definition element.
     * @return true if this action definition can process the element, or false
     * otherwise.
     */
    public static boolean accepts(Element actionDefinitionElement) {
        return actionDefinitionElement.getName().equals(ACTION_DEFINITION_NAME);
    }

    /**
     * Returns the component name of the provided action definition element.
     *
     * @param actionDefinitionElement the action definition element.
     * @return the element's component name.
     */
    protected static String getComponentName(Element actionDefinitionElement) {
        Element componentClassElement = actionDefinitionElement.element(COMPONENT_NAME);
        return componentClassElement == null ? null : componentClassElement.getText();
    }

    /**
     * Returns the component name of this action definition.
     *
     * @return the action definition's component name.
     */
    @Override
    public String getComponentName() {
        return getComponentName(actionDefElement);
    }

    /**
     * Sets the component name of this action definition.
     *
     * @param name the component name.
     */
    @Override
    public void setComponentName(String name) {
        Element componentClassName = actionDefElement.element(COMPONENT_NAME);
        if (componentClassName == null) {
            componentClassName = actionDefElement.addElement(COMPONENT_NAME);
            componentClassName.setText(name);
            fireActionRenamed(this);
        } else if (!componentClassName.getText().equals(name)) {
            componentClassName.setText(name);
            fireActionRenamed(this);
        }
    }

    /**
     * Adds a new input parameter to this action definition. If the input
     * already exists the type of the input is set to the specified type.
     *
     * @param privateParamName the name of the param as it is known by this
     * action definition (the input element name).
     * @param inputType the input type
     * @return the action input
     */
    @Override
    public ActionInput addInput(String privateParamName, String inputType) {
        ActionInput input = getInputParam(privateParamName);
        Element inputElement;
        if (input == null) {
            Element[] componentDefs = getComponentDefElements(privateParamName);
            for (Element componentDef : componentDefs) {
                componentDef.detach();
            }
            inputElement
                    = makeElement(actionDefElement, ACTION_INPUTS_NAME
                            + "/" + privateParamName); //$NON-NLS-1$
            inputElement.addAttribute(TYPE_NAME, inputType);
            input = new ActionInput(inputElement, actionParameterMgr);
            fireIoAdded(input);
        } else {
            input.setType(inputType);
        }
        return input;
    }

    public void setActionInputValue(String inputPrivateName, IActionInputSource value) {
        if ((value == null) || (value instanceof ActionInputConstant)) {
            setInputValue(inputPrivateName, value != null ? ((ActionInputConstant) value).getStringValue(false) : null);
        } else {
            setInputParam(inputPrivateName, (IActionInputVariable) value);
        }
    }

    public void setActionInputValue(String inputPrivateName, ActionInput actionInput) {
        setInputParam(inputPrivateName, actionInput.getReferencedVariableName(), actionInput.getType());
    }

    /**
     * Sets the value of the named action input to the specified constant value.
     * A child element of the component definition section is created and
     * assigned the provided name. The text of this child element is assigned
     * the provided value. If there is an input parameter of the same name in
     * the action inputs section, the input parameter is deleted. If the value
     * is null, both the input parameter and the component definition child
     * element are deleted.
     *
     * @param privateParamName the name of the param as it is known by this
     * action definition (the input element name).
     * @param value the value to be assigned. May be null.
     */
    @Override
    public void setInputValue(String privateParamName, String value) {
        setInputValue(privateParamName, value, true);
    }

    /**
     * Creates or modifies the named input parameter to refer to the named
     * variable. The referenced variable should be the name of an input to the
     * parent action sequence document or the name of an output from an action
     * definition that precedes this action definition in the document. If the
     * component definition section of this action definition contains a child
     * element with the given private name, that element is removed from the
     * component definition section. If the referenced variable name is null,
     * the named action input is deleted, and the component definition section
     * is left untouched.
     *
     * @param privateParamName the name of the param as it is known by this
     * action definition (the input element name).
     * @param value the value to be assigned. May be null.
     */
    @Override
    public IActionInput setInputParam(String privateParamName, String referencedVariableName, String type) {
        ActionInput actionInput = null;
        if (referencedVariableName == null) {
            actionInput = getInputParam(privateParamName);
            if (actionInput != null) {
                actionInput.delete();
            }
        } else {
            actionInput = addInput(privateParamName, type != null ? type : STRING_TYPE);
            actionInput.setMapping(referencedVariableName);
        }
        return actionInput;
    }

    /**
     * Sets the value of the named action input to the specified constant value.
     * A child element of the component definition section is created and
     * assigned the provided name. The text of this child element is assigned
     * the provided value. If there is an input parameter of the same name in
     * the action inputs section, the input parameter is deleted. If the value
     * is null, both the input parameter and the component definition child
     * element are deleted.
     *
     * @param privateParamName the name of the param as it is known by this
     * action definition (the input element name).
     * @param value the value to be assigned. May be null.
     * @param useCData indicates whether a CDATA node should be used to store
     * the value.
     */
    @Override
    public void setInputValue(String privateParamName, String value, boolean useCData) {
        removeInput(privateParamName);
        if (value == null) {
            removeComponentDefinitions(privateParamName);
        } else {
            setComponentDefinition(privateParamName, value, useCData);
        }
    }

    @Override
    public IActionInput getInput(String privateParamName) {

        IActionInput inputParam = NULL_INPUT;
        IActionInput[] allInputs = getInputs();
        for (IActionInput allInput : allInputs) {
            if (allInput.getName().equals(privateParamName)) {
                inputParam = allInput;
                break;
            }
        }

        return inputParam;
    }

    /**
     * Creates an input resource with the given name. No operation is performed
     * if the resource already exists.
     *
     * @param privateResourceName the name of the resource as it is known by
     * this action definition (the element name).
     * @return the newly created or existing resource.
     */
    @Override
    public IActionResource addResource(String privateResourceName) {
        return addResource(privateResourceName, null);
    }

    /**
     * Creates an input resource with the given name. The resource will
     * reference the specified action sequence resource. No operation is
     * performed if the resource already exists.
     *
     * @param privateResourceName the name of the resource as it is known by
     * this action definition (the element name).
     * @return the newly created or existing resource.
     */
    @Override
    public IActionResource addResource(String privateResourceName, String referencedActionSequenceResource) {
        IActionResource resource = getResource(privateResourceName);
        Element resourceElement;
        if (resource == null) {
            resourceElement
                    = makeElement(actionDefElement, ACTION_RESOURCES_NAME
                            + "/" + privateResourceName); //$NON-NLS-1$
            resourceElement.addAttribute(TYPE_NAME, RESOURCE_TYPE);
            resource = new ActionResource(resourceElement, actionParameterMgr);
            if ((referencedActionSequenceResource != null) && (referencedActionSequenceResource.trim().length() > 0)) {
                resource.setMapping(referencedActionSequenceResource);
            }
            fireResourceAdded(resource);
        }
        return resource;
    }

    /**
     * @return the resources referenced by this action definition
     */
    @Override
    public IActionResource[] getResources() {
        List resourcesList = actionDefElement.selectNodes(ACTION_RESOURCES_NAME + "/*"); //$NON-NLS-1$
        ActionResource[] resources = new ActionResource[resourcesList.size()];
        int index = 0;
        for (Iterator iter = resourcesList.iterator(); iter.hasNext();) {
            resources[index++] = new ActionResource((Element) iter.next(), actionParameterMgr);
        }
        return resources;
    }

    /**
     * Return the named resource of null if none exists. If the resource is not
     * explicitly listed in the action resources section of this action
     * definition, an implicit resource is created if one exists. These implicit
     * resources are detected by looking at the action sequence resources listed
     * at the top of the action sequence document with the given name. If an
     * action sequence resource is found an implicit reference is created and
     * returned by this method. section of this action definition will be
     * returned
     *
     * @param privateResourceName the resource name
     * @return the resource with the given name or null if none exists.
     */
    @Override
    public IActionResource getResource(String privateResourceName) {
        return getResource(privateResourceName, true);
    }

    /**
     * Return the named resource of null if none exists. If the resource is not
     * explicitly listed in the action resources section of this action
     * definition, an implicit resource is created if one exists. These implicit
     * resources are detected by looking at the action sequence resources listed
     * at the top of the action sequence document with the given name. If an
     * action sequence resource is found an implicit reference is created and
     * returned by this method. section of this action definition will be
     * returned
     *
     * @param privateResourceName the resource name
     * @param includeImplicitResource whether to include implicit resource
     * references.
     * @return the resource with the given name or null if none exists.
     */
    @Override
    public IActionResource getResource(String privateResourceName, boolean includeImplicitResource) {
        Element inputElement
                = (Element) actionDefElement.selectSingleNode(ACTION_RESOURCES_NAME
                        + "/" + privateResourceName); //$NON-NLS-1$
        ActionResource actionResource = null;
        if (inputElement == null) {
            if (includeImplicitResource) {
                IActionSequenceDocument document = getDocument();
                if (document != null) {
                    IActionSequenceResource actionSequenceResource = getDocument().getResource(privateResourceName);
                    if (actionSequenceResource != null) {
                        actionResource = new ImplicitActionResource(this, privateResourceName, actionParameterMgr);
                    }
                }
            }
        } else {
            actionResource = new ActionResource(inputElement, actionParameterMgr);
        }
        return actionResource;
    }

    /**
     * Returns all the inputs that are listed in the action inputs section of
     * this action definition. Inputs that have been assigned a constant value
     * appear in the component definition section and will not be returned by
     * this method.
     *
     * @return the inputs list in the action inputs section
     */
    public IActionInput[] getVariableInputs() {
        List inputElements = actionDefElement.selectNodes(ACTION_INPUTS_NAME + "/*"); //$NON-NLS-1$
        IActionInput[] variableInputs = new ActionInput[inputElements.size()];
        int index = 0;
        for (Iterator iter = inputElements.iterator(); iter.hasNext();) {
            variableInputs[index++] = new ActionInput((Element) iter.next(), actionParameterMgr);
        }
        return variableInputs;
    }

    protected ActionInputConstant[] getConstantInputs() {
        ArrayList constantInputs = new ArrayList();
        List componentDefElements = actionDefElement.selectNodes(COMPONENT_DEF_NAME + "/*"); //$NON-NLS-1$
        for (Iterator iter = componentDefElements.iterator(); iter.hasNext();) {
            Element componentDefElement = (Element) iter.next();
            if (componentDefElement.elements().size() == 0) {
                constantInputs.add(new ActionInputConstant(componentDefElement, this.actionParameterMgr));
            }
        }
        return (ActionInputConstant[]) constantInputs.toArray(new ActionInputConstant[0]);
    }

    @Override
    public IActionInput[] getInputs() {
        return getInputs((IActionInputFilter) null);
    }

    @Override
    public IActionInput[] getInputs(IActionInputFilter actionInputFilter) {
        ArrayList inputs = new ArrayList();
        IActionInput[] variableInputs = getVariableInputs();
        for (IActionInput variableInput : variableInputs) {
            if ((actionInputFilter == null) || actionInputFilter.accepts(variableInput)) {
                inputs.add(variableInput);
            }
        }

        ActionInputConstant[] constants = getConstantInputs();
        for (ActionInputConstant constant : constants) {
            if ((actionInputFilter == null) || actionInputFilter.accepts(constant)) {
                inputs.add(constant);
            }
        }

        return (IActionInput[]) inputs.toArray(new IActionInput[0]);
    }

    /**
     * Returns the input in the action inputs section of this action definition
     * that the specified name. Inputs that have been assigned a constant value
     * appear in the component definition section and will not be returned by
     * this method.
     *
     * @param privateParamName the name of the param as it is known by this
     * action definition (the input element name).
     * @return the input with the specified name or null if none exists.
     */
    public ActionInput getInputParam(String privateInputName) {
        Element inputElement
                = (Element) actionDefElement
                        .selectSingleNode(ACTION_INPUTS_NAME + "/" + privateInputName); //$NON-NLS-1$
        return inputElement == null ? null : new ActionInput(inputElement, actionParameterMgr);
    }

    @Override
    public IActionOutput addOutput(String privateParamName, String outputType) {
        IActionOutput output = getOutput(privateParamName);
        Element outputElement;
        if (output == null) {
            outputElement
                    = makeElement(actionDefElement, ACTION_OUTPUTS_NAME
                            + "/" + privateParamName); //$NON-NLS-1$
            outputElement.addAttribute(TYPE_NAME, outputType);
            output = new ActionOutput(outputElement, actionParameterMgr);
            fireIoAdded(output);
        } else {
            output.setType(outputType);
        }
        return output;
    }

    /**
     * Returns all the outputs that are listed in the action outputs section of
     * this action definition.
     *
     * @return the outputs list in the action input name
     */
    @Override
    public IActionOutput[] getOutputs() {
        List outputsList = actionDefElement.selectNodes(ACTION_OUTPUTS_NAME + "/*"); //$NON-NLS-1$
        ActionOutput[] outputs = new ActionOutput[outputsList.size()];
        int index = 0;
        for (Iterator iter = outputsList.iterator(); iter.hasNext();) {
            outputs[index++] = new ActionOutput((Element) iter.next(), actionParameterMgr);
        }
        return outputs;
    }

    /**
     * Returns the output, with the specified name, listed in the action outputs
     * section of this action definition.
     *
     * @param privateParamName the name of the param as it is known by this
     * action definition (the output element name).
     * @return the named output or null if none exists.
     */
    @Override
    public IActionOutput getOutput(String privateParamName) {
        Element outputElement
                = (Element) actionDefElement.selectSingleNode(ACTION_OUTPUTS_NAME
                        + "/" + privateParamName); //$NON-NLS-1$
        return outputElement == null ? null : new ActionOutput(outputElement, actionParameterMgr);
    }

    /**
     * Returns all the outputs that are listed in the action outputs section of
     * this action definition that have the specified types.
     *
     * @param types data types of the desired outputs.
     * @return the outputs listed in the action outputs section of the specified
     * type
     */
    @Override
    public IActionOutput[] getOutputs(String[] types) {
        IActionOutput[] allOutputs = getOutputs();
        List matchingOutputs = new ArrayList();
        if (types == null) {
            matchingOutputs.addAll(asList(allOutputs));
        } else {
            for (IActionOutput allOutput : allOutputs) {
                for (String type : types) {
                    if (type.equals(allOutput.getType())) {
                        matchingOutputs.add(allOutput);
                        break;
                    }
                }
            }
        }
        return (ActionOutput[]) matchingOutputs.toArray(new ActionOutput[0]);
    }

    /**
     * Returns all the outputs that are listed in the action outputs section of
     * this action definition that have the specified type.
     *
     * @param type data types of the desired outputs.
     * @return the outputs listed in the action outputs section of the specified
     * type
     */
    @Override
    public IActionOutput[] getOutputs(String type) {
        return getOutputs(new String[]{type});
    }

    /**
     * @return the action definition description
     */
    @Override
    public String getDescription() {
        Element descriptionElement = actionDefElement.element(ACTION_TYPE_NAME);
        return descriptionElement != null ? descriptionElement.getText() : ""; //$NON-NLS-1$
    }

    /**
     * Set the action definition description
     *
     * @param description the description
     */
    @Override
    public void setDescription(String description) {
        if (description == null) {
            description = ""; //$NON-NLS-1$
        }
        Element descriptionElement = actionDefElement.element(ACTION_TYPE_NAME);
        if (descriptionElement == null) {
            descriptionElement = actionDefElement.addElement(ACTION_TYPE_NAME);
            descriptionElement.setText(description);
            fireActionRenamed(this);
        } else if (!descriptionElement.getText().equals(description)) {
            descriptionElement.setText(description);
            fireActionRenamed(this);
        }
    }

    @Override
    public boolean equals(Object arg0) {
        boolean result = false;
        if (arg0 instanceof ActionDefinition) {
            ActionDefinition actionDef = (ActionDefinition) arg0;
            result
                    = (actionDef.actionDefElement != null ? actionDef.actionDefElement.equals(this.actionDefElement)
                            : (actionDef == this));
        }
        return result;
    }

    /**
     * @return the action control statement (if or loop) that contains this
     * action definition or null if there is no parent control statement.
     */
    @Override
    public IActionControlStatement getParent() {
        ActionControlStatement controlStatement = null;
        if (actionDefElement != null) {
            Element ancestorElement = actionDefElement.getParent();
            if ((ancestorElement != null) && ancestorElement.getName().equals(ACTIONS_NAME)
                    && !ancestorElement.getPath().equals(DOC_ACTIONS_PATH)) {
                if (ancestorElement.element(CONDITION_NAME) != null) {
                    controlStatement = new ActionIfStatement(ancestorElement, actionParameterMgr);
                } else {
                    controlStatement = new ActionLoop(ancestorElement, actionParameterMgr);
                }
            }
        }
        return controlStatement;
    }

    /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.designstudio.dom.IActionSequenceElement#getElement()
     */
    @Override
    public Element getElement() {
        return actionDefElement;
    }

    /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.designstudio.dom.IActionSequenceElement#delete()
     */
    @Override
    public void delete() {
        Document doc = actionDefElement.getDocument();
        if (doc != null) {
            actionDefElement.detach();
            fireActionRemoved(new ActionSequenceDocument(doc, actionParameterMgr), this);
        }
    }

    /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.designstudio.dom.IActionSequenceElement#getDocument()
     */
    @Override
    public IActionSequenceDocument getDocument() {
        ActionSequenceDocument doc = null;
        if ((actionDefElement != null) && (actionDefElement.getDocument() != null)) {
            doc = new ActionSequenceDocument(actionDefElement.getDocument(), actionParameterMgr);
        }
        return doc;
    }

    /**
     * The value of the component definition element at the specified XPath
     *
     * @param compDefXpath the XPath of the element relative to the component
     * definition element.
     * @return the value of the element or null if the element does not exist
     */
    @Override
    public String getComponentDefinitionValue(String compDefXpath) {
        Element componentDef = getComponentDefElement(compDefXpath);
        return componentDef != null ? componentDef.getText() : null;
    }

    /**
     * The values of the component definition elements at the specified XPath
     *
     * @param compDefXpath the XPath of the elements relative to the component
     * definition element.
     * @return the values of the elements
     */
    @Override
    public String[] getComponentDefinitionValues(String compDefXpath) {
        Element[] componentDefs = getComponentDefElements(compDefXpath);
        String[] values = new String[componentDefs.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = componentDefs[i].getText();
        }
        return values;
    }

    /**
     * The component definition elements at the specified XPath
     *
     * @param compDefXpath the XPath of the elements relative to the component
     * definition element.
     * @return the elements
     */
    @Override
    public Element[] getComponentDefElements(String compDefXpath) {
        return (Element[]) actionDefElement
                .selectNodes(COMPONENT_DEF_NAME + "/" + compDefXpath).toArray(new Element[0]); //$NON-NLS-1$
    }

    /**
     * The component definition element at the specified XPath
     *
     * @param compDefXpath the XPath of the elements relative to the component
     * definition element.
     * @return the element or null if the element does not exist.
     */
    @Override
    public Element getComponentDefElement(String compDefXpath) {
        return actionDefElement == null ? null : (Element) actionDefElement
                .selectSingleNode(COMPONENT_DEF_NAME + "/" + compDefXpath); //$NON-NLS-1$
    }

    /**
     * The component definition element.
     *
     * @return the element or null if the element does not exist.
     */
    @Override
    public Element getComponentDefElement() {
        return (Element) actionDefElement.selectSingleNode(COMPONENT_DEF_NAME); //$NON-NLS-1$
    }

    /**
     * Sets the value of the component definition element at the specified
     * XPath.
     *
     * @param compDefXpath the XPath of the element relative to the component
     * definition element.
     * @param value the value to be assigned to the element
     */
    @Override
    public void setComponentDefinition(String compDefXpath, String value) {
        setComponentDefinition(compDefXpath, value, false);
    }

    /**
     * Sets the value of the component definition elements at the specified
     * XPath.
     *
     * @param compDefXpath the XPath of the element relative to the component
     * definition element.
     * @param values the value to be assigned to the elements
     */
    @Override
    public void setComponentDefinition(String compDefXpath, String[] values) {
        boolean changed = false;
        Element[] componentDefs = getComponentDefElements(compDefXpath);
        for (Element componentDef : componentDefs) {
            componentDef.detach();
        }
        if (componentDefs.length > 0) {
            changed = true;
        }
        if (values.length > 0) {
            Element componentDef
                    = makeElement(actionDefElement, COMPONENT_DEF_NAME + "/" + compDefXpath); //$NON-NLS-1$
            componentDef.setText(values[0]);
            Element parent = componentDef.getParent();
            for (int i = 1; i < values.length; i++) {
                parent.addElement(componentDef.getName()).setText(values[i]);
            }
            changed = true;
        }
        if (changed) {
            fireActionChanged(this);
        }
    }

    /**
     * Sets the attribute value of the component definition element at the
     * specified XPath.
     *
     * @param compDefXpath the XPath of the element relative to the component
     * definition element.
     * @param attributeName the attribute name
     * @param value the value to be assigned to the attribute
     */
    @Override
    public void setComponentDefinitionAttribute(String compDefXpath, String attributeName, String value) {
        Element componentDef = getComponentDefElement(compDefXpath);
        if (componentDef == null) {
            if (value != null) {
                componentDef
                        = makeElement(actionDefElement, COMPONENT_DEF_NAME
                                + "/" + compDefXpath); //$NON-NLS-1$
            }
        }
        if (componentDef != null) {
            componentDef.addAttribute(attributeName, value);
            fireActionChanged(this);
        }
    }

    /**
     * Sets the value of the component definition element at the specified
     * XPath.
     *
     * @param compDefXpath the XPath of the element relative to the component
     * definition element.
     * @param value the value to be assigned to the element
     * @param useCData whether a CDATA node should be used to save the value
     */
    @Override
    public void setComponentDefinition(String compDefXpath, String value, boolean useCData) {
        if (value == null) {
            Element[] componentDefs = getComponentDefElements(compDefXpath);
            for (Element componentDef : componentDefs) {
                componentDef.detach();
            }
            if (componentDefs.length > 0) {
                fireActionChanged(this);
            }
        } else {
            Element componentDef = getComponentDefElement(compDefXpath);
            if (componentDef == null) {
                componentDef
                        = makeElement(actionDefElement, COMPONENT_DEF_NAME
                                + "/" + compDefXpath); //$NON-NLS-1$
            }
            componentDef.clearContent();
            if (useCData) {
                componentDef.addCDATA(value);
            } else {
                componentDef.setText(value);
            }
            fireActionChanged(this);
        }
    }

    /**
     * Removes an output from this action definition
     *
     * @param privateParamName the name of the output to be removed.
     */
    public void removeOutput(String privateParamName) {
        IActionOutput actionOutput = getOutput(privateParamName);
        if (actionOutput != null) {
            actionOutput.delete();
        }
    }

    /**
     * Removes an input from this action definition
     *
     * @param inputName the name of the input to be removed.
     */
    @Override
    public void removeInput(String privateParamName) {
        ActionInput actionInput = getInputParam(privateParamName);
        if (actionInput != null) {
            actionInput.delete();
        }
    }

    /**
     * Renames the named action input. No operation is performed if the input
     * does not exist. Any inputs defined in the component definition section
     * that refer to the input using the {oldName} parameter reference are also
     * modified to refer to {newName}.
     *
     * @param oldName the name of the input to be renamed.
     * @param newName the new input name.
     */
    @Override
    public void renameInput(String oldName, String newName) {
        if (!oldName.equals(newName)) {
            ActionInput actionInput = getInputParam(oldName);
            if (actionInput != null) {
                Element componentDefElement = actionDefElement.element(COMPONENT_DEF_NAME);
                try {
                    if (componentDefElement != null) {
                        String xmlString = componentDefElement.asXML();
                        if (xmlString.indexOf("{" + oldName + "}") >= 0) { //$NON-NLS-1$ //$NON-NLS-2$
                            xmlString = xmlString.replaceAll("\\{" + oldName + "\\}", "{" + newName + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                            Document document = parseText(xmlString);
                            actionDefElement.remove(componentDefElement);
                            actionDefElement.add(document.getRootElement());
                        }
                    }
                    actionInput.setName(newName);
                } catch (DocumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Removes a resource from this action definition
     *
     * @param privateResourceName the name of the resource to be removed.
     */
    @Override
    public void removeResource(String privateResourceName) {
        IActionResource actionResource = getResource(privateResourceName);
        if (actionResource != null) {
            actionResource.delete();
        }
    }

    /**
     * Removes all inputs from this action definition
     */
    @Override
    public void deleteAllInputs() {
        IActionInput[] actionInputs = getInputs(new VariableActionInputFilter());
        for (IActionInput actionInput : actionInputs) {
            ((ActionInput) actionInput).delete();
        }
    }

    /**
     * Removes all output from this action definition
     */
    @Override
    public void deleteAllOutputs() {
        IActionOutput[] actionOutputs = getOutputs();
        for (IActionOutput actionOutput : actionOutputs) {
            actionOutput.delete();
        }
    }

    /**
     * Removes all resources from this action definition
     */
    @Override
    public void deleteAllResources() {
        IActionResource[] actionResources = getResources();
        for (IActionResource actionResource : actionResources) {
            actionResource.delete();
        }
    }

    /**
     * Removes all component definition elements at the specified XPath
     *
     * @param compDefXpath the XPath of the elements relative to the component
     * definition element.
     */
    @Override
    public void removeComponentDefinitions(String compDefXpath) {
        Element[] componentDefs = getComponentDefElements(compDefXpath);
        for (Element componentDef : componentDefs) {
            componentDef.detach();
        }
        if (componentDefs.length > 0) {
            fireActionChanged(this);
        }
    }

    /**
     * Removes all component definition child elements from this action
     * definition.
     */
    @Override
    public void removeComponentDefinitions() {
        if (actionDefElement != null) {
            Element componentDefElement = actionDefElement.element(COMPONENT_DEF_NAME);
            if (componentDefElement != null) {
                List elements = componentDefElement.elements();
                if (elements.size() > 0) {
                    elements.clear();
                    fireActionChanged(this);
                }
            }
        }
    }

    /**
     * Moves the given input to the specified position in the action inputs
     * list.
     *
     * @param actionInput the input to be moved.
     * @param index the new input position
     */
    @Override
    public void setInputIndex(ActionInput actionInput, int index) {
        if (this.equals(actionInput.getActionDefinition())) {
            Element inputsParent = actionDefElement.element(ACTION_INPUTS_NAME);
            List inputs = inputsParent.elements();
            int currentIndex = inputs.indexOf(actionInput.getElement());
            if (currentIndex != index) {
                List elements = inputsParent.elements();
                elements.remove(currentIndex);
                if (index > elements.size() - 1) {
                    elements.add(actionInput.getElement());
                } else {
                    elements.add(index, actionInput.getElement());
                }
                fireActionChanged(this);
            }
        }
    }

    /**
     * Returns all the private input names that are reserved for use by this
     * action definition. Inputs with these names are used for a particular
     * purpose by this action definition.
     *
     * @return the reserved input names
     */
    @Override
    public String[] getReservedInputNames() {
        return EXPECTED_INPUTS;
    }

    /**
     * Returns all the private outputs names that are reserved for use by this
     * action definition. Outputs with these names are used for a particular
     * purpose by this action definition.
     *
     * @return the reserved output names
     */
    @Override
    public String[] getReservedOutputNames() {
        return EXPECTED_OUTPUTS;
    }

    /**
     * Returns all the private resource names that are reserved for use by this
     * action definition. Resources with these names are used for a particular
     * purpose by this action definition.
     *
     * @return the reserved output names
     */
    @Override
    public String[] getReservedResourceNames() {
        return EXPECTED_RESOURCES;
    }

    /**
     * Creates or modifies the named input parameter to refer to the provided
     * variable. The referenced variable should be an action sequence input of
     * the parent action sequence document or an action output from an action
     * definition that precedes this action definition in the document. If the
     * component definition section of this action definition contains a child
     * element with the given private name, that element is removed from the
     * component definition section. If the referenced variable name is null,
     * the named action input is deleted, and the component definition section
     * is left untouched.
     *
     * @param privateParamName the name of the param as it is known by this
     * action definition (the input element name).
     * @param referencedVariable the variable to be referenced. May be null.
     */
    private IActionInput setInputParam(String privateParamName, IActionInputVariable referencedVariable) {
        IActionInput actionInput = null;
        if (referencedVariable != null) {
            actionInput
                    = setInputParam(privateParamName, referencedVariable.getVariableName(), referencedVariable.getType());
        } else {
            removeInput(privateParamName);
        }
        return actionInput;
    }

    /**
     * Creates the named output parameter, and assigns the output parameter the
     * specified public name and type. The public name is the name used by
     * succeeding action definitions to refer to this ouput. If the output
     * already exists it is given the specified public name. If the public name
     * is null or zero length the output is deleted.
     *
     * @param privateParamName the name of the param as it is known by this
     * action definition (the output element name).
     * @param publicParamName the public name of the output. May be null.
     * @param outputType the output type. Ignored if the publicParamName is
     * null.
     * @return the created/modified action output.
     */
    protected IActionOutput setOutput(String privateParamName, String publicParamName, String outputType) {
        IActionOutput actionOutput = null;
        if ((publicParamName == null) || (publicParamName.trim().length() == 0)) {
            removeOutput(privateParamName);
        } else {
            actionOutput = addOutput(privateParamName, outputType);
            actionOutput.setMapping(publicParamName);
        }
        return actionOutput;
    }

    /**
     * Returns the public name of the output with the specified private name.
     *
     * @param privateName the name of the param as it is known by this action
     * definition (the output element name).
     * @param outputType the output type. Ignored if the publicParamName is
     * null.
     * @return the output public name or null if the output does not exist.
     */
    protected String getPublicOutputName(String privateName) {
        String publicName = null;
        IActionOutput actionOutput = getOutput(privateName);
        if (actionOutput != null) {
            publicName = actionOutput.getPublicName();
        }
        return publicName;
    }

    /**
     * Verifies that there is an action input or component definition defined
     * for the specified parameter. If an action input exists the method insures
     * that is references a valid variable.
     *
     * @param privateParamName the name of the param to be validated.
     * @return null if no errors are detected or the validation error otherwise.
     */
    protected ActionSequenceValidationError validateInput(String privateParamName) {
        int errorCode = INPUT_OK;
        ActionInput actionInput = getInputParam(privateParamName);
        if (actionInput == null) {
            if (getComponentDefElement(privateParamName) == null) {
                errorCode = INPUT_MISSING;
            }
        } else {
            IActionInputVariable[] availableInputVariables
                    = getDocument().getAvailInputVariables(this, actionInput.getType());
            if (availableInputVariables.length == 0) {
                errorCode = INPUT_REFERENCES_UNKNOWN_VAR;
            } else {
                errorCode = INPUT_UNINITIALIZED;
                for (int i = 0; (i < availableInputVariables.length)
                        && (errorCode == INPUT_UNINITIALIZED); i++) {
                    if (availableInputVariables[i] instanceof ActionSequenceInput) {
                        ActionSequenceInput actionSequenceInput = (ActionSequenceInput) availableInputVariables[i];
                        if (actionSequenceInput.getDefaultValue() != null) {
                            errorCode = INPUT_OK;
                        }
                    } else if (availableInputVariables[i] instanceof ActionOutput) {
                        errorCode = INPUT_OK;
                    }
                }
            }
        }
        ActionSequenceValidationError validationError = null;
        if (errorCode != INPUT_OK) {
            validationError = new ActionSequenceValidationError();
            validationError.actionDefinition = this;
            validationError.errorCode = errorCode;
            validationError.parameterName = privateParamName;
            switch (errorCode) {
                case INPUT_MISSING:
                    validationError.errorMsg = "Missing input.";
                    break;
                case INPUT_REFERENCES_UNKNOWN_VAR:
                    validationError.errorMsg = "Input references unknown variable.";
                    break;
                case INPUT_UNINITIALIZED:
                    validationError.errorMsg = "Input is uninitialized.";
                    break;
            }
        }
        return validationError;
    }

    /**
     * Verifies that there is an action input or action resource defined for the
     * specified parameter. If so the method verifies that the input/resource
     * references a valid variable/action sequence resource.
     *
     * @param privateResourceName the name of the param to be validated.
     * @return null if no errors are detected or the validation error otherwise.
     */
    protected ActionSequenceValidationError validateResource(String privateResourceName) {
        ActionSequenceValidationError validationError = validateInput(privateResourceName);
        if (validationError != null) {
            validationError = null;
            int errorCode = INPUT_OK;
            IActionResource actionResource = getResource(privateResourceName);
            if (actionResource == null) {
                errorCode = INPUT_MISSING;
            } else {
                errorCode = INPUT_REFERENCES_UNKNOWN_VAR;
            }
            if (errorCode != INPUT_OK) {
                validationError = new ActionSequenceValidationError();
                validationError.actionDefinition = this;
                validationError.errorCode = errorCode;
                validationError.parameterName = privateResourceName;
                switch (errorCode) {
                    case INPUT_MISSING:
                        validationError.errorMsg = "Missing input.";
                        break;
                    case INPUT_REFERENCES_UNKNOWN_VAR:
                        validationError.errorMsg = "Input references unknown variable.";
                        break;
                    case INPUT_UNINITIALIZED:
                        validationError.errorMsg = "Input is uninitialized.";
                        break;
                }
            }
        }
        return validationError;
    }

    /**
     * This is the default implementation for validating an action definition.
     * By default no errors are returned. Subclasses of this class should
     * override this method to validate the necessary inputs, outputs, and
     * resources.
     *
     * @return the validation errors that were detected.
     */
    @Override
    public IActionSequenceValidationError[] validate() {
        return EMPTY_ARRAY;
    }

    /**
     * Verifies that there is an action output defined with the given name.
     *
     * @param privateParamName the name of the param to be validated.
     * @return null if no errors are detected or the validation error otherwise.
     */
    protected ActionSequenceValidationError validateOutput(String privateParamName) {
        ActionSequenceValidationError validationError = null;
        if (getOutput(privateParamName) == null) {
            validationError = new ActionSequenceValidationError();
            validationError.actionDefinition = this;
            validationError.errorCode = OUTPUT_MISSING;
            validationError.parameterName = privateParamName;
            validationError.errorMsg = "Missing output.";
        }
        return validationError;
    }

    @Override
    public IActionParameterMgr getActionParameterMgr() {
        return actionParameterMgr;
    }

    @Override
    public void setActionParameterMgr(IActionParameterMgr actionParameterMgr) {
        this.actionParameterMgr = actionParameterMgr;
    }

    @Override
    public int hashCode() {
        return actionDefElement != null ? actionDefElement.hashCode() : super.hashCode();
    }

}
