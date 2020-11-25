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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import static org.dom4j.DocumentHelper.makeElement;
import static org.dom4j.DocumentHelper.parseText;
import org.dom4j.Element;
import static org.pentaho.actionsequence.dom.ActionSequenceDocument.fireResourceChanged;
import static org.pentaho.actionsequence.dom.ActionSequenceDocument.fireResourceRemoved;
import static org.pentaho.actionsequence.dom.ActionSequenceDocument.fireResourceRenamed;
import org.pentaho.actionsequence.dom.actions.IActionParameterMgr;

/**
 * A wrapper class for an action sequence resource element.
 *
 * @author Angelo Rodriguez
 *
 */
@SuppressWarnings({"rawtypes"})
public class ActionSequenceResource extends AbstractIOElement implements IActionSequenceResource {

    public ActionSequenceResource(Element resourceElement, IActionParameterMgr actionInputProvider) {
        super(resourceElement, actionInputProvider);
    }

    /**
     * @return the resource name
     */
    @Override
    public String getName() {
        return ioElement.getName();
    }

    /**
     * Sets the resource name.
     *
     * @param resourceName the resource name
     */
    @Override
    public void setName(String resourceName) {
        if (!ioElement.getName().equals(resourceName)) {
            ioElement.setName(resourceName);
            fireResourceRenamed(this);
        }
    }

    /**
     * Sets the resource mime type.
     *
     * @param mimeType the mime type
     */
    @Override
    public void setMimeType(String mimeType) {
        String resType = getType();
        Element mimeElement = null;
        if (null != resType) {
            switch (resType) {
                case SOLUTION_FILE_RESOURCE_TYPE:
                    mimeElement = (Element) ioElement.selectSingleNode(SOLUTION_FILE_RESOURCE_TYPE + "/" + RES_MIME_TYPE_NAME); //$NON-NLS-1$
                    break;
                case FILE_RESOURCE_TYPE:
                    mimeElement = (Element) ioElement.selectSingleNode(FILE_RESOURCE_TYPE + "/" + RES_MIME_TYPE_NAME); //$NON-NLS-1$
                    break;
                case URL_RESOURCE_TYPE:
                    mimeElement = (Element) ioElement.selectSingleNode(URL_RESOURCE_TYPE + "/" + RES_MIME_TYPE_NAME); //$NON-NLS-1$
                    break;
                default:
                    break;
            }
        }

        if (mimeElement == null) {
            if (null != resType) {
                switch (resType) {
                    case SOLUTION_FILE_RESOURCE_TYPE:
                        mimeElement = makeElement(ioElement, SOLUTION_FILE_RESOURCE_TYPE + "/" + RES_MIME_TYPE_NAME); //$NON-NLS-1$
                        break;
                    case FILE_RESOURCE_TYPE:
                        mimeElement = makeElement(ioElement, FILE_RESOURCE_TYPE + "/" + RES_MIME_TYPE_NAME); //$NON-NLS-1$
                        break;
                    case URL_RESOURCE_TYPE:
                        mimeElement = makeElement(ioElement, URL_RESOURCE_TYPE + "/" + RES_MIME_TYPE_NAME); //$NON-NLS-1$
                        break;
                    default:
                        break;
                }
            }
            if (mimeElement != null) {
                mimeElement.setText(mimeType);
            }
            fireResourceChanged(this);
        } else if (!mimeElement.getText().equals(mimeType)) {
            mimeElement.setText(mimeType);
            fireResourceChanged(this);
        }
    }

    /**
     * @return the resource mime type
     */
    @Override
    public String getMimeType() {
        String mimeType = ""; //$NON-NLS-1$
        String resType = getType();
        Element mimeElement = null;
        if (null != resType) {
            switch (resType) {
                case SOLUTION_FILE_RESOURCE_TYPE:
                    mimeElement = (Element) ioElement.selectSingleNode(SOLUTION_FILE_RESOURCE_TYPE + "/" + RES_MIME_TYPE_NAME); //$NON-NLS-1$
                    break;
                case FILE_RESOURCE_TYPE:
                    mimeElement = (Element) ioElement.selectSingleNode(FILE_RESOURCE_TYPE + "/" + RES_MIME_TYPE_NAME); //$NON-NLS-1$
                    break;
                case URL_RESOURCE_TYPE:
                    mimeElement = (Element) ioElement.selectSingleNode(URL_RESOURCE_TYPE + "/" + RES_MIME_TYPE_NAME); //$NON-NLS-1$
                    break;
                default:
                    break;
            }
        }

        if (mimeElement != null) {
            mimeType = mimeElement.getText();
        }

        return mimeType;
    }

    /**
     * Sets the resource URI
     *
     * @param uri the resource URI
     */
    @Override
    public void setPath(String uri) {
        String resType = getType();
        Element pathElement = null;
        if (null != resType) {
            switch (resType) {
                case SOLUTION_FILE_RESOURCE_TYPE:
                    pathElement = (Element) ioElement.selectSingleNode(SOLUTION_FILE_RESOURCE_TYPE + "/" + RES_LOCATION_NAME); //$NON-NLS-1$
                    break;
                case FILE_RESOURCE_TYPE:
                    pathElement = (Element) ioElement.selectSingleNode(FILE_RESOURCE_TYPE + "/" + RES_LOCATION_NAME); //$NON-NLS-1$
                    break;
                case URL_RESOURCE_TYPE:
                    pathElement = (Element) ioElement.selectSingleNode(URL_RESOURCE_TYPE + "/" + RES_LOCATION_NAME); //$NON-NLS-1$
                    break;
                default:
                    break;
            }
        }

        if (pathElement == null) {
            if (null != resType) {
                switch (resType) {
                    case SOLUTION_FILE_RESOURCE_TYPE:
                        pathElement = makeElement(ioElement, SOLUTION_FILE_RESOURCE_TYPE + "/" + RES_LOCATION_NAME); //$NON-NLS-1$
                        break;
                    case FILE_RESOURCE_TYPE:
                        pathElement = makeElement(ioElement, FILE_RESOURCE_TYPE + "/" + RES_LOCATION_NAME); //$NON-NLS-1$
                        break;
                    case URL_RESOURCE_TYPE:
                        pathElement = makeElement(ioElement, URL_RESOURCE_TYPE + "/" + RES_LOCATION_NAME); //$NON-NLS-1$
                        break;
                    default:
                        break;
                }
            }
            if (pathElement != null && uri != null) {
                pathElement.setText(uri);
            } else if (pathElement != null) {
                pathElement.setText("");
            }
            fireResourceChanged(this);
        } else if (!pathElement.getText().equals(uri)) {
            pathElement.setText(uri);
            fireResourceChanged(this);
        }
    }

    /**
     * @return the resource URI
     */
    @Override
    public String getPath() {
        String uri = ""; //$NON-NLS-1$
        String resType = getType();
        Element pathElement = null;
        if (null != resType) {
            switch (resType) {
                case SOLUTION_FILE_RESOURCE_TYPE:
                    pathElement = (Element) ioElement.selectSingleNode(SOLUTION_FILE_RESOURCE_TYPE + "/" + RES_LOCATION_NAME); //$NON-NLS-1$
                    break;
                case FILE_RESOURCE_TYPE:
                    pathElement = (Element) ioElement.selectSingleNode(FILE_RESOURCE_TYPE + "/" + RES_LOCATION_NAME); //$NON-NLS-1$
                    break;
                case URL_RESOURCE_TYPE:
                    pathElement = (Element) ioElement.selectSingleNode(URL_RESOURCE_TYPE + "/" + RES_LOCATION_NAME); //$NON-NLS-1$
                    break;
                default:
                    break;
            }
        }

        if (pathElement != null) {
            uri = pathElement.getText();
        }

        return uri;
    }

    /**
     * @return the resource file type
     */
    @Override
    public String getType() {
        Element solutionFileElement = ioElement.element(SOLUTION_FILE_RESOURCE_TYPE);
        Element fileElement = ioElement.element(FILE_RESOURCE_TYPE);
        Element urlElement = ioElement.element(URL_RESOURCE_TYPE);
        Element xmlElement = ioElement.element(XML_RESOURCE_TYPE);
        Element stringElement = ioElement.element(STRING_RESOURCE_TYPE);
        String resourceType = null;
        if ((solutionFileElement != null) && (fileElement == null) && (urlElement == null) && (xmlElement == null)
                && (stringElement == null)) {
            resourceType = SOLUTION_FILE_RESOURCE_TYPE;
        } else if ((solutionFileElement == null) && (fileElement != null) && (urlElement == null)
                && (xmlElement == null) && (stringElement == null)) {
            resourceType = FILE_RESOURCE_TYPE;
        } else if ((solutionFileElement == null) && (fileElement == null) && (urlElement != null)
                && (xmlElement == null) && (stringElement == null)) {
            resourceType = URL_RESOURCE_TYPE;
        } else if ((solutionFileElement == null) && (fileElement == null) && (urlElement == null)
                && (xmlElement != null) && (stringElement == null)) {
            resourceType = XML_RESOURCE_TYPE;
        } else if ((solutionFileElement == null) && (fileElement == null) && (urlElement == null)
                && (xmlElement == null) && (stringElement != null)) {
            resourceType = STRING_RESOURCE_TYPE;
        }
        return resourceType;
    }

    /**
     * Sets the resource file type
     *
     * @param resourceType the resource file type
     */
    @Override
    public void setType(String resourceType) {
        if (SOLUTION_FILE_RESOURCE_TYPE.equals(resourceType) || FILE_RESOURCE_TYPE.equals(resourceType)
                || URL_RESOURCE_TYPE.equals(resourceType) || XML_RESOURCE_TYPE.equals(resourceType)
                || STRING_RESOURCE_TYPE.equals(resourceType)) {
            Element solutionFileElement = ioElement.element(SOLUTION_FILE_RESOURCE_TYPE);
            Element fileElement = ioElement.element(FILE_RESOURCE_TYPE);
            Element urlElement = ioElement.element(URL_RESOURCE_TYPE);
            Element stringElement = ioElement.element(STRING_RESOURCE_TYPE);
            Element xmlElement = ioElement.element(XML_RESOURCE_TYPE);
            if (getType() == null) {
                if (solutionFileElement != null) {
                    solutionFileElement.detach();
                    solutionFileElement = null;
                }
                if (fileElement != null) {
                    fileElement.detach();
                    fileElement = null;
                }
                if (urlElement != null) {
                    urlElement.detach();
                    urlElement = null;
                }
                if (stringElement != null) {
                    stringElement.detach();
                    stringElement = null;
                }
                if (xmlElement != null) {
                    xmlElement.detach();
                    xmlElement = null;
                }
            }

            Element existingElement = solutionFileElement;
            if (existingElement == null) {
                existingElement = fileElement;
            }
            if (existingElement == null) {
                existingElement = urlElement;
            }
            if (existingElement == null) {
                existingElement = stringElement;
            }
            if (existingElement == null) {
                existingElement = xmlElement;
            }

            if (existingElement == null) {
                existingElement = ioElement.addElement(resourceType);
                if (!STRING_RESOURCE_TYPE.equals(resourceType)) {
                    existingElement.addElement(RES_LOCATION_NAME);
                }
                existingElement.addElement(RES_MIME_TYPE_NAME);
                fireResourceChanged(this);
            } else {
                existingElement.setName(resourceType);
                Element locationElement = existingElement.element(RES_LOCATION_NAME);
                if (STRING_RESOURCE_TYPE.equals(resourceType)) {
                    if (locationElement != null) {
                        locationElement.detach();
                    }
                }
                if (!XML_RESOURCE_TYPE.equals(resourceType)) {
                    if (locationElement != null) {
                        List elements = locationElement.elements();
                        elements.forEach(obj -> {
                            ((Element) obj).detach();
                        });
                    }
                }
                fireResourceChanged(this);
            }
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
            fireResourceRemoved(new ActionSequenceDocument(doc, actionInputProvider), this);
        }
    }

    /**
     * (non-Javadoc)
     *
     * @return 
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
                ActionSequenceResource resource = (ActionSequenceResource) arg0;
                result = (resource.ioElement != null ? resource.ioElement.equals(this.ioElement) : (resource == this));
            }
        }
        return result;
    }

    /**
     * (non-Javadoc)
     *
     * @return 
     * @see org.pentaho.designstudio.dom.IActionSequenceElement#getDocument()
     */
    @Override
    public IActionSequenceDocument getDocument() {
        ActionSequenceDocument doc = null;
        if ((ioElement != null) && (ioElement.getDocument() != null)) {
            doc = new ActionSequenceDocument(ioElement.getDocument(), actionInputProvider);
        }
        return doc;
    }

    @Override
    public URI getUri() {
        URI uri;
        try {
            String schemaSpecificPart = getPath();
            switch (getType()) {
                case SOLUTION_FILE_RESOURCE_TYPE:
                    uri = new URI(SOLUTION_SCHEME, schemaSpecificPart, null);
                    break;
                case FILE_RESOURCE_TYPE:
                    uri = new URI(FILE_SCHEME, schemaSpecificPart, null);
                    break;
                default:
                    uri = new URI(schemaSpecificPart);
                    break;
            }
        } catch (URISyntaxException e) {
            uri = null;
            e.printStackTrace();
        }
        return uri;
    }

    @Override
    public void setUri(URI uri) {
        if (!uri.isAbsolute() || FILE_SCHEME.equals(uri.getScheme())) {
            setType(FILE_RESOURCE_TYPE);
            setPath(uri.getSchemeSpecificPart());
        } else if (SOLUTION_SCHEME.equals(uri.getScheme())) {
            setType(SOLUTION_FILE_RESOURCE_TYPE);
            setPath(uri.getSchemeSpecificPart());
        } else {
            try {
                URL url = uri.toURL();
                setType(URL_RESOURCE_TYPE);
                setPath(url.toString());
            } catch (MalformedURLException ex) {
                setType(FILE_RESOURCE_TYPE);
                setPath(uri.toString());
            }
        }
    }

    /**
     *
     * @return
     */
    @Override
    public String getString() {
        String string = null;
        if (STRING_RESOURCE_TYPE.equals(getType())) {
            string = ioElement.element(STRING_RESOURCE_TYPE).getText();
        }
        return string;
    }

    /**
     *
     * @param string
     */
    @Override
    public void setString(String string) {
        setType(STRING_RESOURCE_TYPE);
        ioElement.element(STRING_RESOURCE_TYPE).setText(string);
    }

    /**
     *
     * @return
     */
    @Override
    public String getXml() {
        String xml = null;
        if (XML_RESOURCE_TYPE.equals(getType())) {
            Element element = (Element) ioElement.selectSingleNode(XML_RESOURCE_TYPE + "/" + RES_LOCATION_NAME + "/*"); //$NON-NLS-1$
            if (element != null) {
                xml = element.asXML();
            }
        }
        return xml;
    }

    @Override
    public void setXml(String xml) throws DocumentException {
        setType(XML_RESOURCE_TYPE);
        Document document = parseText(xml);
        Element locationElement = (Element) ioElement.selectSingleNode(XML_RESOURCE_TYPE + "/" + RES_LOCATION_NAME); //$NON-NLS-1$
        List elements = locationElement.elements();
        for (Object obj : elements) {
            ((Element) obj).detach();
        }
        locationElement.add(document.getRootElement());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }
}
