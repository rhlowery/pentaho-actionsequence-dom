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

import java.io.FileNotFoundException;
import java.net.URI;
import static org.dom4j.DocumentHelper.makeElement;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import static org.pentaho.actionsequence.dom.IActionSequenceDocument.ACTION_RESOURCES_NAME;
import org.pentaho.actionsequence.dom.actions.ActionDefinition;
import org.pentaho.actionsequence.dom.actions.IActionParameterMgr;
import org.pentaho.commons.connection.IPentahoStreamSource;

/**
 * This class accommodates deprecated functionality within the action sequence.
 * It used to be the case that the resources used by a particular action
 * definition within an action sequence did not have to did not have to be
 * explicitly listed within the action definition. This is no longer the
 * preferred means of referencing resources. Each action definition now
 * explicitly lists the resources it uses. To accommodate old style action
 * sequences we create an implicit action resource when we detect that an action
 * definition is using a resource that is not explicitly defined within the
 * action definition.
 *
 * @author arodriguez
 * @created Nov 7, 2007
 */
public class ImplicitActionResource extends ActionResource {

    ActionDefinition actionDefinition;
    String resourceName;

    public ImplicitActionResource(ActionDefinition actionDefinition, String resourceName,
            IActionParameterMgr actionInputProvider) {
        super(new DefaultElement(resourceName), actionInputProvider);
        this.actionDefinition = actionDefinition;
        this.resourceName = resourceName;
    }

    @Override
    public void delete() {
        if (actionDefinition != null) {
            IActionResource actionResource = actionDefinition.getResource(resourceName, false);
            if (actionResource != null) {
                actionResource.delete();
            }
        }
        actionDefinition = null;
    }

    @Override
    public IActionDefinition getActionDefinition() {
        return actionDefinition;
    }

    @Override
    public IActionSequenceDocument getDocument() {
        IActionSequenceDocument doc = super.getDocument();
        if ((doc == null) && (actionDefinition != null)) {
            doc = actionDefinition.getDocument();
        }
        return doc;
    }

    @Override
    public void setMapping(String mapping) {
        if (actionDefinition != null) {
            IActionResource actionResource = actionDefinition.getResource(resourceName, false);
            if (actionResource != null) {
                actionResource.setMapping(mapping);
            } else {
                Element tempElement = getElement();
                Element resourcesParent
                        = makeElement(actionDefinition.getElement(), ACTION_RESOURCES_NAME); //$NON-NLS-1$
                resourcesParent.add(tempElement);
                super.setMapping(mapping);
            }
        } else {
            super.setMapping(mapping);
        }
    }

    @Override
    public void setMimeType(String mimeType) {
        if (actionDefinition != null) {
            IActionResource actionResource = actionDefinition.getResource(resourceName, false);
            if (actionResource != null) {
                actionResource.setMimeType(mimeType);
            } else {
                Element tempElement = getElement();
                Element resourcesParent
                        = makeElement(actionDefinition.getElement(), ACTION_RESOURCES_NAME); //$NON-NLS-1$
                resourcesParent.add(tempElement);
                super.setMimeType(mimeType);
            }
        } else {
            super.setMimeType(mimeType);
        }
    }

    @Override
    public void setName(String resourceName) {
        if (actionDefinition != null) {
            IActionResource actionResource = actionDefinition.getResource(this.resourceName, false);
            if (actionResource != null) {
                actionResource.setName(resourceName);
            } else {
                Element tempElement = getElement();
                Element resourcesParent
                        = makeElement(actionDefinition.getElement(), ACTION_RESOURCES_NAME); //$NON-NLS-1$
                resourcesParent.add(tempElement);
                super.setName(resourceName);
            }
        } else {
            super.setName(resourceName);
        }
        this.resourceName = resourceName;
    }

    @Override
    public void setType(String ioType) {
        if (actionDefinition != null) {
            IActionResource actionResource = actionDefinition.getResource(resourceName, false);
            if (actionResource != null) {
                actionResource.setType(ioType);
            } else {
                Element tempElement = getElement();
                Element resourcesParent
                        = makeElement(actionDefinition.getElement(), ACTION_RESOURCES_NAME); //$NON-NLS-1$
                resourcesParent.add(tempElement);
                super.setType(ioType);
            }
        } else {
            super.setType(ioType);
        }
    }

    @Override
    public boolean equals(Object arg0) {
        boolean result;
        if (actionDefinition != null) {
            IActionResource actionResource = actionDefinition.getResource(resourceName, false);
            if (actionResource != null) {
                result = actionResource.equals(arg0);
            } else {
                result = super.equals(arg0);
            }
        } else {
            result = super.equals(arg0);
        }
        return result;
    }

    @Override
    public IPentahoStreamSource getDataSource() throws FileNotFoundException {
        IPentahoStreamSource dataSource;
        if (actionDefinition != null) {
            IActionResource actionResource = actionDefinition.getResource(resourceName, false);
            if (actionResource != null) {
                dataSource = actionResource.getDataSource();
            } else {
                dataSource = super.getDataSource();
            }
        } else {
            dataSource = super.getDataSource();
        }
        return dataSource;
    }

    @Override
    public Element getElement() {
        Element element;
        if (actionDefinition != null) {
            IActionResource actionResource = actionDefinition.getResource(resourceName, false);
            if (actionResource != null) {
                element = actionResource.getElement();
            } else {
                element = super.getElement();
            }
        } else {
            element = super.getElement();
        }
        return element;
    }

    @Override
    public String getMapping() {
        String mapping;
        if (actionDefinition != null) {
            IActionResource actionResource = actionDefinition.getResource(resourceName, false);
            if (actionResource != null) {
                mapping = actionResource.getMapping();
            } else {
                mapping = super.getMapping();
            }
        } else {
            mapping = super.getMapping();
        }
        return mapping;
    }

    @Override
    public String getMimeType() {
        String mimeType;
        if (actionDefinition != null) {
            IActionResource actionResource = actionDefinition.getResource(resourceName, false);
            if (actionResource != null) {
                mimeType = actionResource.getMimeType();
            } else {
                mimeType = super.getMimeType();
            }
        } else {
            mimeType = super.getMimeType();
        }
        return mimeType;
    }

    @Override
    public String getName() {
        String name;
        if (actionDefinition != null) {
            IActionResource actionResource = actionDefinition.getResource(resourceName, false);
            if (actionResource != null) {
                name = actionResource.getName();
            } else {
                name = super.getName();
            }
        } else {
            name = super.getName();
        }
        return name;
    }

    @Override
    public String getPublicName() {
        String name;
        if (actionDefinition != null) {
            IActionResource actionResource = actionDefinition.getResource(resourceName, false);
            if (actionResource != null) {
                name = actionResource.getPublicName();
            } else {
                name = super.getPublicName();
            }
        } else {
            name = super.getPublicName();
        }
        return name;
    }

    @Override
    public String getType() {
        String type;
        if (actionDefinition != null) {
            IActionResource actionResource = actionDefinition.getResource(resourceName, false);
            if (actionResource != null) {
                type = actionResource.getType();
            } else {
                type = super.getType();
            }
        } else {
            type = super.getType();
        }
        return type;
    }

    @Override
    public URI getUri() {
        URI uri;
        if (actionDefinition != null) {
            IActionResource actionResource = actionDefinition.getResource(resourceName, false);
            if (actionResource != null) {
                uri = actionResource.getUri();
            } else {
                uri = super.getUri();
            }
        } else {
            uri = super.getUri();
        }
        return uri;
    }

    @Override
    public void setURI(URI uri) {
        if (actionDefinition != null) {
            IActionResource actionResource = actionDefinition.getResource(resourceName, false);
            if (actionResource != null) {
                actionResource.setURI(uri);
            } else {
                Element tempElement = getElement();
                Element resourcesParent
                        = makeElement(actionDefinition.getElement(), ACTION_RESOURCES_NAME); //$NON-NLS-1$
                resourcesParent.add(tempElement);
                super.setURI(uri);
            }
        } else {
            super.setURI(uri);
        }
    }
}
