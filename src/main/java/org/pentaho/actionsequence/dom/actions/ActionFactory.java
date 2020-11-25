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

import java.io.IOException;
import java.io.InputStream;
import static java.lang.Boolean.TRUE;
import static java.lang.Class.forName;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;
import org.apache.commons.logging.Log;
import static org.apache.commons.logging.LogFactory.getLog;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ActionFactory {

    private static final Log logger = getLog(ActionFactory.class);

    public static LinkedHashMap<String, Class> pluginActions = new LinkedHashMap<String, Class>();

    public static String PLUGIN_XML_FILENAME = "pentaho_platform_plugin.xml";
    public static String PLUGIN_ROOT_NODE = "pentaho-plugin";
    public static String PLUGIN_ACTION_DEFINITION_NODE = "action-definition";

    protected static boolean pluginsLoaded = false;

    protected static synchronized void loadPlugins() {
        if (!pluginsLoaded) {
            try {
                // see if we have any pentaho_action_plugin.xml files in the root of the classpath
                ActionFactory factory = new ActionFactory();
                Enumeration<URL> enumer = factory.getClass().getClassLoader().getResources(PLUGIN_XML_FILENAME);

                // we might have multiple documents
                while (enumer.hasMoreElements()) {
                    URL url = enumer.nextElement();

                    // make sure a failure for one resource does not affect any other ones
                    try {
                        // ESR-168 - This resolves an issue in Websphere/Weblogic
                        //
                        // url.getContent() throws an UnknownServiceException (no content-type)
                        InputStream is = url.openStream();
                        if (is != null) {
                            SAXReader reader = createSafeSaxReader();
                            Document doc = reader.read(is);
                            if (doc != null) {
                                // look for nodes
                                List nodes = doc.selectNodes(PLUGIN_ROOT_NODE + "/" + PLUGIN_ACTION_DEFINITION_NODE);
                                Iterator it = nodes.iterator();
                                while (it.hasNext()) {
                                    // make sure that one failed class will not affect any others
                                    try {
                                        // pull the class name from each node
                                        Element node = (Element) it.next();
                                        String className = node.getText();
                                        String id = node.attributeValue("id");
                                        // load the class
                                        Class componentClass = forName(className.trim());
                                        // add the class to the plugin list
                                        pluginActions.put(id, componentClass);
                                    } catch (ClassNotFoundException e) {
                                        logger.error(e);
                                    }
                                }
                            }
                        }
                    } catch (IOException | DocumentException e) {
                        logger.error(e);
                    }
                }
            } catch (IOException e) {
                logger.error(e);
            }
            pluginsLoaded = true;
        }
    }

    public static ActionDefinition getActionDefinition(Element actionDefDomElement,
            IActionParameterMgr actionInputProvider) {
        ActionDefinition actionDefinition = null;

        if (!pluginsLoaded) {
            loadPlugins();
        }

        // TODO a map would improve performance here
        for (Class actionClass : pluginActions.values()) {
            try {
                Method acceptElementMethod = actionClass.getMethod("accepts", new Class[]{Element.class});
                if (TRUE.equals(acceptElementMethod.invoke(null, new Object[]{actionDefDomElement}))) {
                    Constructor constructor
                            = actionClass.getConstructor(new Class[]{Element.class, IActionParameterMgr.class});
                    actionDefinition
                            = (ActionDefinition) constructor.newInstance(new Object[]{actionDefDomElement, actionInputProvider});
                    break;
                }
            } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
                logger.error(e);
            }
        }

        if (actionDefinition == null) {
            actionDefinition = new ActionDefinition(actionDefDomElement, actionInputProvider);
        }
        return actionDefinition;
    }

    public static Class getActionDefinition(String actionId) {
        if (!pluginsLoaded) {
            loadPlugins();
        }

        return pluginActions.get(actionId);
    }

    private static SAXReader createSafeSaxReader() {
        SAXReader reader = new SAXReader();
        try {
            reader.setFeature(FEATURE_SECURE_PROCESSING, true);
            reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
            reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (SAXException e) {
            logger.error(e);
        }
        return reader;
    }

}
