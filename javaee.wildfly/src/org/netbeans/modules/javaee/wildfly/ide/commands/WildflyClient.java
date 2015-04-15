/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javaee.wildfly.ide.commands;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.callback.CallbackHandler;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext;
import org.netbeans.modules.javaee.wildfly.WildflyDeploymentFactory;
import org.netbeans.modules.javaee.wildfly.WildflyTargetModuleID;
import org.netbeans.modules.javaee.wildfly.config.WildflyConnectionFactory;
import org.netbeans.modules.javaee.wildfly.config.WildflyDatasource;
import org.netbeans.modules.javaee.wildfly.config.WildflyMailSessionResource;
import org.netbeans.modules.javaee.wildfly.config.WildflyMessageDestination;
import org.netbeans.modules.javaee.wildfly.config.WildflyResourceAdapter;
import org.netbeans.modules.javaee.wildfly.config.WildflySocket;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.addModelNodeChild;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.closeClient;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.createAddOperation;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.createClient;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.createDeploymentPathAddressAsModelNode;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.createModelNode;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.createOperation;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.createPathAddressAsModelNode;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.createReadResourceOperation;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.createRemoveOperation;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.getClientConstant;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.getModelDescriptionConstant;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.getModelNodeChild;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.getModelNodeChildAtIndex;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.getModelNodeChildAtPath;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.getPropertyName;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.getPropertyValue;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.isSuccessfulOutcome;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.modelNodeAsBoolean;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.modelNodeAsInt;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.modelNodeAsList;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.modelNodeAsPropertyForName;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.modelNodeAsPropertyForValue;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.modelNodeAsPropertyList;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.modelNodeAsString;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.modelNodeHasChild;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.modelNodeHasDefinedChild;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.modelNodeIsDefined;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.readResult;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.setModelNodeChild;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.setModelNodeChildBytes;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.setModelNodeChildEmptyList;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.setModelNodeChildString;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginProperties;
import org.netbeans.modules.javaee.wildfly.nodes.WildflyDatasourceNode;
import org.netbeans.modules.javaee.wildfly.nodes.WildflyDestinationNode;
import org.netbeans.modules.javaee.wildfly.nodes.WildflyEarApplicationNode;
import org.netbeans.modules.javaee.wildfly.nodes.WildflyEjbComponentNode;
import org.netbeans.modules.javaee.wildfly.nodes.WildflyEjbModuleNode;
import org.netbeans.modules.javaee.wildfly.nodes.WildflyWebModuleNode;
import org.openide.util.Lookup;

/**
 *
 * @author ehugonnet
 */
public class WildflyClient {

    private static final Logger LOGGER = Logger.getLogger(WildflyClient.class.getName());

    private static final String SERVER_STATE = "server-state"; // NOI18N
    private static final String WEB_SUBSYSTEM = "undertow"; // NOI18N
    private static final String EJB3_SUBSYSTEM = "ejb3"; // NOI18N
    private static final String DATASOURCES_SUBSYSTEM = "datasources"; // NOI18N
    private static final String MAIL_SUBSYSTEM = "mail"; // NOI18N
    private static final String MESSAGING_SUBSYSTEM = "messaging"; // NOI18N
    private static final String RESOURCE_ADAPTER_SUBSYSTEM = "resource-adapters"; // NOI18N

    private static final String DATASOURCE_TYPE = "data-source"; // NOI18N
    private static final String HORNETQ_SERVER_TYPE = "hornetq-server"; // NOI18N
    private static final String MAIL_SESSION_TYPE = "mail-session"; // NOI18N
    private static final String JMSQUEUE_TYPE = "jms-queue"; // NOI18N
    private static final String JMSTOPIC_TYPE = "jms-topic"; // NOI18N
    private static final String CONNECTION_FACTORY_TYPE = "connection-factory"; // NOI18N
    private static final String RESOURCE_ADAPTER_TYPE = "resource-adapter"; // NOI18N

    private final String serverAddress;
    private final int serverPort;
    private final CallbackHandler handler;
    private final InstanceProperties ip;
    private Object client;

    /**
     * Get the value of serverPort
     *
     * @return the value of serverPort
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Get the value of serverPort
     *
     * @return the value of serverPort
     */
    public String getServerLog() throws IOException {
        return readPath("jboss.server.log.dir") + File.separatorChar + "server.log";
    }

    /**
     * Get the value of serverAddress
     *
     * @return the value of serverAddress
     */
    public String getServerAddress() {
        return serverAddress;
    }

    public WildflyClient(InstanceProperties ip, String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.ip = ip;
        handler = new Authentication().getCallbackHandler();
    }

    public WildflyClient(InstanceProperties ip, String serverAddress, int serverPort, String login,
            String password) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.ip = ip;
        handler = new Authentication(login, password.toCharArray()).getCallbackHandler();
    }

    // ModelControllerClient
    private synchronized Object getClient(WildflyDeploymentFactory.WildFlyClassLoader cl) {
        if (client == null) {
            try {
                this.client = createClient(cl, serverAddress, serverPort, handler);
            } catch (Throwable ex) {
                LOGGER.log(Level.WARNING, null, ex);
                return null;
            }
        }
        return this.client;
    }

    private synchronized void close() {
        try {
            if (this.client != null) {
                closeClient(WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip), client);
            }
            this.client = null;
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    public synchronized void shutdownServer() throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            // ModelNode
            Object shutdownOperation = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, shutdownOperation, getModelDescriptionConstant(cl, "OP")), getModelDescriptionConstant(cl, "SHUTDOWN"));
            executeAsync(cl, shutdownOperation, null);
            close();
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        }
    }

    public synchronized boolean isServerRunning() {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            // ModelNode
            Object statusOperation = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, statusOperation, getClientConstant(cl, "OP")), getClientConstant(cl, "READ_ATTRIBUTE_OPERATION"));
            setModelNodeChildEmptyList(cl, getModelNodeChild(cl, statusOperation, getClientConstant(cl, "OP_ADDR")));
            setModelNodeChildString(cl, getModelNodeChild(cl, statusOperation, getClientConstant(cl, "NAME")), SERVER_STATE);
            // ModelNode
            Object response = executeAsync(cl, statusOperation, null).get();
            return getClientConstant(cl, "SUCCESS").equals(modelNodeAsString(cl, getModelNodeChild(cl, response, getClientConstant(cl, "OUTCOME"))))
                    && !getClientConstant(cl, "CONTROLLER_PROCESS_STATE_STARTING").equals(modelNodeAsString(cl, getModelNodeChild(cl, response, getModelDescriptionConstant(cl, "RESULT"))))
                    && !getClientConstant(cl, "CONTROLLER_PROCESS_STATE_STOPPING").equals(modelNodeAsString(cl, getModelNodeChild(cl, response, getModelDescriptionConstant(cl, "RESULT"))));
        } catch (InvocationTargetException ex) {
            LOGGER.log(Level.FINE, null, ex.getTargetException());
            close();
            return false;
        } catch (Exception ex) {
            LOGGER.log(Level.FINE, null, ex);
            close();
            return false;
        }
    }

    // ModelNode
    private synchronized Object executeOnModelNode(WildflyDeploymentFactory.WildFlyClassLoader cl, Object modelNode) throws IOException, ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Object clientLocal = getClient(cl);
        if (clientLocal == null) {
            throw new IOException("Not connected to WildFly server");
        }
        Method method = clientLocal.getClass().getMethod("execute", modelClazz);
        return method.invoke(clientLocal, modelNode);
    }

    // ModelNode
    private synchronized Object executeOnOperation(WildflyDeploymentFactory.WildFlyClassLoader cl, Object operation) throws IOException, ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        Class operationClazz = cl.loadClass("org.jboss.as.controller.client.Operation"); // NOI18N
        Object clientLocal = getClient(cl);
        if (clientLocal == null) {
            throw new IOException("Not connected to WildFly server");
        }
        Method method = clientLocal.getClass().getMethod("execute", operationClazz);
        return method.invoke(clientLocal, operation);
    }

    private synchronized Future<?> executeAsync(WildflyDeploymentFactory.WildFlyClassLoader cl, Object modelNode, Object operationMessageHandler) throws IOException, ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Class handlerClazz = cl.loadClass("org.jboss.as.controller.client.OperationMessageHandler"); // NOI18N
        Object clientLocal = getClient(cl);
        if (clientLocal == null) {
            throw new IOException("Not connected to WildFly server");
        }
        Method method = clientLocal.getClass().getMethod("executeAsync", modelClazz, handlerClazz);
        return (Future) method.invoke(clientLocal, modelNode, operationMessageHandler);
    }

    public Collection<WildflyModule> listAvailableModules() throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyModule> modules = new ArrayList<WildflyModule>();
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, null);
            // ModelNode
            Object readDeployments = createReadResourceOperation(cl, deploymentAddressModelNode, true);
            // ModelNode
            Object response = executeOnModelNode(cl, readDeployments);
            String httpPort = ip.getProperty(WildflyPluginProperties.PROPERTY_PORT);
            if (isSuccessfulOutcome(cl, response)) {
                // ModelNode
                Object result = readResult(cl, response);
                // List<ModelNode>
                List webapps = modelNodeAsList(cl, result);
                for (Object application : webapps) {
                    String applicationName = modelNodeAsString(cl, getModelNodeChild(cl, readResult(cl, application), getClientConstant(cl, "NAME")));
                    // ModelNode
                    Object deployment = getModelNodeChild(cl, getModelNodeChild(cl, readResult(cl, application), getClientConstant(cl, "SUBSYSTEM")), WEB_SUBSYSTEM);
                    WildflyModule module = new WildflyModule(applicationName, true);
                    if (modelNodeIsDefined(cl, deployment)) {
                        String url = "http://" + serverAddress + ':' + httpPort + modelNodeAsString(cl, getModelNodeChild(cl, deployment, "context-root"));
                        module.setUrl(url);
                    }
                    modules.add(module);
                }
            }
            return modules;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        }
    }

    public Collection<WildflyWebModuleNode> listWebModules(Lookup lookup) throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyWebModuleNode> modules = new ArrayList<WildflyWebModuleNode>();
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, null);
            // ModelNode
            Object readDeployments = createReadResourceOperation(cl, deploymentAddressModelNode, true);
            // ModelNode
            Object response = executeOnModelNode(cl, readDeployments);
            String httpPort = ip.getProperty(WildflyPluginProperties.PROPERTY_PORT);
            if (isSuccessfulOutcome(cl, response)) {
                // ModelNode
                Object result = readResult(cl, response);
                // List<ModelNode>
                List webapps = modelNodeAsList(cl, result);
                for (Object application : webapps) {
                    String applicationName = modelNodeAsString(cl, getModelNodeChild(cl, readResult(cl, application), getClientConstant(cl, "NAME")));
                    if (applicationName.endsWith(".war")) {
                        // ModelNode
                        Object deployment = getModelNodeChild(cl, getModelNodeChild(cl, readResult(cl, application), getClientConstant(cl, "SUBSYSTEM")), WEB_SUBSYSTEM);
                        if (modelNodeIsDefined(cl, deployment)) {
                            String url = "http://" + serverAddress + ':' + httpPort + modelNodeAsString(cl, getModelNodeChild(cl, deployment, "context-root"));
                            modules.add(new WildflyWebModuleNode(applicationName, lookup, url));
                        } else {
                            modules.add(new WildflyWebModuleNode(applicationName, lookup, null));
                        }
                    }
                }
            }
            return modules;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        }
    }

    public String getWebModuleURL(String webModuleName) throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyWebModuleNode> modules = new ArrayList<WildflyWebModuleNode>();
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, webModuleName);
            // ModelNode
            Object readDeployments = createReadResourceOperation(cl, deploymentAddressModelNode, true);
            // ModelNode
            Object response = executeOnModelNode(cl, readDeployments);
            String httpPort = ip.getProperty(WildflyPluginProperties.PROPERTY_PORT);
            if (isSuccessfulOutcome(cl, response)) {
                // ModelNode
                Object result = readResult(cl, response);
                String applicationName = modelNodeAsString(cl, getModelNodeChild(cl, result, getClientConstant(cl, "NAME")));
                if (applicationName.endsWith(".war")) {
                    // ModelNode
                    Object deployment = getModelNodeChild(cl, getModelNodeChild(cl, result, getClientConstant(cl, "SUBSYSTEM")), WEB_SUBSYSTEM);
                    if (modelNodeIsDefined(cl, deployment)) {
                        return "http://" + serverAddress + ':' + httpPort + modelNodeAsString(cl, getModelNodeChild(cl, deployment, "context-root"));
                    }
                }
            }
            return "";
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        }
    }

    public Collection<WildflyEjbModuleNode> listEJBModules(Lookup lookup) throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyEjbModuleNode> modules = new ArrayList<WildflyEjbModuleNode>();
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, null);
            // ModelNode
            Object readDeployments = createReadResourceOperation(cl, deploymentAddressModelNode, true);
            // ModelNode
            Object response = executeOnModelNode(cl, readDeployments);
            if (isSuccessfulOutcome(cl, response)) {
                // ModelNode
                Object result = readResult(cl, response);
                // List<ModelNode>
                List ejbs = modelNodeAsList(cl, result);
                for (Object ejb : ejbs) {
                    // ModelNode
                    Object deployment = getModelNodeChild(cl, getModelNodeChild(cl, readResult(cl, ejb), getClientConstant(cl, "SUBSYSTEM")), EJB3_SUBSYSTEM);
                    if (modelNodeIsDefined(cl, deployment)) {
                        List<WildflyEjbComponentNode> ejbInstances = new ArrayList<WildflyEjbComponentNode>();
                        ejbInstances.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.ENTITY));
                        ejbInstances.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.MDB));
                        ejbInstances.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.SINGLETON));
                        ejbInstances.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.STATEFULL));
                        ejbInstances.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.STATELESS));
                        modules.add(new WildflyEjbModuleNode(modelNodeAsString(cl, getModelNodeChild(cl, readResult(cl, ejb), getClientConstant(cl, "NAME"))), lookup, ejbInstances, true));
                    }
                }
            }
            return modules;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        }
    }

    public boolean startModule(WildflyTargetModuleID tmid) throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, tmid.getModuleID());
            // ModelNode
            Object enableDeployment = createOperation(cl, getClientConstant(cl, "DEPLOYMENT_REDEPLOY_OPERATION"), deploymentAddressModelNode);
            Object result = executeOnModelNode(cl, enableDeployment);
            if (isSuccessfulOutcome(cl, result)) {
                tmid.setContextURL(getWebModuleURL(tmid.getModuleID()));
                return true;
            }
            return false;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        }
    }

    public boolean startModule(String moduleName) throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, moduleName);
            // ModelNode
            Object enableDeployment = createOperation(cl, getClientConstant(cl, "DEPLOYMENT_REDEPLOY_OPERATION"), deploymentAddressModelNode);
            Object result = executeOnModelNode(cl, enableDeployment);
            return isSuccessfulOutcome(cl, result);
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        }
    }

    public boolean stopModule(String moduleName) throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, moduleName);
            // ModelNode
            Object enableDeployment = createOperation(cl, getClientConstant(cl, "DEPLOYMENT_UNDEPLOY_OPERATION"), deploymentAddressModelNode);
            return isSuccessfulOutcome(cl, executeOnModelNode(cl, enableDeployment));
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        }
    }

    public boolean undeploy(String fileName) throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, fileName);

            // ModelNode
            final Object undeploy = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, undeploy, getClientConstant(cl, "OP")), getClientConstant(cl, "COMPOSITE"));
            setModelNodeChildEmptyList(cl, getModelNodeChild(cl, undeploy, getModelDescriptionConstant(cl, "ADDRESS")));
            // ModelNode
            Object steps = getModelNodeChild(cl, undeploy, getClientConstant(cl, "STEPS"));
            addModelNodeChild(cl, steps, createOperation(cl, getClientConstant(cl, "DEPLOYMENT_UNDEPLOY_OPERATION"), deploymentAddressModelNode));
            addModelNodeChild(cl, steps, createRemoveOperation(cl, deploymentAddressModelNode));
            return isSuccessfulOutcome(cl, executeOnModelNode(cl, undeploy));
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        }
    }

    public boolean deploy(DeploymentContext deployment) throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            String fileName = deployment.getModuleFile().getName();
            undeploy(fileName);

            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, fileName);

            // ModelNode
            final Object deploy = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, deploy, getClientConstant(cl, "OP")), getClientConstant(cl, "COMPOSITE"));
            setModelNodeChildEmptyList(cl, getModelNodeChild(cl, deploy, getModelDescriptionConstant(cl, "ADDRESS")));
            // ModelNode
            Object steps = getModelNodeChild(cl, deploy, getClientConstant(cl, "STEPS"));
            // ModelNode
            Object addModule = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, addModule, getClientConstant(cl, "OP")), getClientConstant(cl, "ADD"));
            setModelNodeChildString(cl, getModelNodeChildAtPath(cl, addModule,
                    new Object[]{getModelDescriptionConstant(cl, "ADDRESS"), getClientConstant(cl, "DEPLOYMENT")}), fileName);
            setModelNodeChildString(cl, getModelNodeChild(cl, addModule, getClientConstant(cl, "RUNTIME_NAME")), fileName);
            setModelNodeChildBytes(cl, getModelNodeChild(cl, getModelNodeChildAtIndex(cl, getModelNodeChild(cl, addModule, getClientConstant(cl, "CONTENT")), 0),
                    getModelDescriptionConstant(cl, "BYTES")), deployment.getModule().getArchive().asBytes());

            addModelNodeChild(cl, steps, addModule);
            addModelNodeChild(cl, steps, createOperation(cl, getClientConstant(cl, "DEPLOYMENT_REDEPLOY_OPERATION"), deploymentAddressModelNode));
            // ModelNode
            Object result = executeOnModelNode(cl, deploy);
            return isSuccessfulOutcome(cl, result);
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        }
    }

    public Collection<WildflyDatasourceNode> listDatasources(Lookup lookup) throws IOException {
        Set<Datasource> datasources = listDatasources();
        List<WildflyDatasourceNode> modules = new ArrayList<WildflyDatasourceNode>(datasources.size());
        for (Datasource ds : datasources) {
            modules.add(new WildflyDatasourceNode(((WildflyDatasource) ds).getName(), ds, lookup));
        }
        return modules;
    }

    public Set<Datasource> listDatasources() throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            Set<Datasource> listedDatasources = new HashSet<Datasource>();
            // ModelNode
            final Object readDatasources = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readDatasources, getClientConstant(cl, "OP")), getClientConstant(cl, "READ_CHILDREN_NAMES_OPERATION"));

            LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
            values.put(getClientConstant(cl, "SUBSYSTEM"), DATASOURCES_SUBSYSTEM);
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readDatasources, getModelDescriptionConstant(cl, "ADDRESS")), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readDatasources, getModelDescriptionConstant(cl, "RECURSIVE_DEPTH")), 0);
            setModelNodeChildString(cl, getModelNodeChild(cl, readDatasources, getClientConstant(cl, "CHILD_TYPE")), DATASOURCE_TYPE);

            // ModelNode
            Object response = executeOnModelNode(cl, readDatasources);
            if (isSuccessfulOutcome(cl, response)) {
                // List<ModelNode>
                List names = modelNodeAsList(cl, readResult(cl, response));
                for (Object datasourceName : names) {
                    listedDatasources.add(getDatasource(cl, modelNodeAsString(cl, datasourceName)));
                }
            }
            return listedDatasources;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        }
    }

    private WildflyDatasource getDatasource(WildflyDeploymentFactory.WildFlyClassLoader cl, String name) throws IOException {
        try {
            // ModelNode
            final Object readDatasource = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readDatasource, getClientConstant(cl, "OP")), getClientConstant(cl, "READ_RESOURCE_OPERATION"));
            LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
            values.put(getClientConstant(cl, "SUBSYSTEM"), DATASOURCES_SUBSYSTEM);
            values.put(DATASOURCE_TYPE, name);
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readDatasource, getModelDescriptionConstant(cl, "ADDRESS")), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readDatasource, getModelDescriptionConstant(cl, "RECURSIVE_DEPTH")), 0);
            // ModelNode
            Object response = executeOnModelNode(cl, readDatasource);
            if (isSuccessfulOutcome(cl, response)) {
                // ModelNode
                Object datasource = readResult(cl, response);
                return new WildflyDatasource(name, modelNodeAsString(cl, getModelNodeChild(cl, datasource, "jndi-name")),
                        modelNodeAsString(cl, getModelNodeChild(cl, datasource, "connection-url")),
                        modelNodeAsString(cl, getModelNodeChild(cl, datasource, "user-name")),
                        modelNodeAsString(cl, getModelNodeChild(cl, datasource, "password")),
                        modelNodeAsString(cl, getModelNodeChild(cl, datasource, "driver-class")));
            }
            return null;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        }
    }

    public Collection<WildflyDestinationNode> listDestinations(Lookup lookup) throws IOException {
        List<MessageDestination> destinations = listDestinations();
        List<WildflyDestinationNode> modules = new ArrayList<WildflyDestinationNode>(destinations.size());
        for (MessageDestination destination : destinations) {
            modules.add(new WildflyDestinationNode(destination.getName(), destination, lookup));
        }
        return modules;
    }

    public List<WildflyDestinationNode> listDestinationForDeployment(Lookup lookup, String jeeDeploymentName) throws IOException {
        List<MessageDestination> destinations = listDestinationForDeployment(jeeDeploymentName);
        List<WildflyDestinationNode> modules = new ArrayList<WildflyDestinationNode>(destinations.size());
        for (MessageDestination destination : destinations) {
            modules.add(new WildflyDestinationNode(destination.getName(), destination, lookup));
        }
        return modules;
    }

    public List<MessageDestination> listDestinationForDeployment(String deployment) throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<MessageDestination> destinations = new ArrayList<MessageDestination>();
            // ModelNode
            final Object readHornetQServers = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readHornetQServers, getClientConstant(cl, "OP")), getClientConstant(cl, "READ_CHILDREN_NAMES_OPERATION"));

            LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
            values.put(getClientConstant(cl, "DEPLOYMENT"), deployment);
            values.put(getClientConstant(cl, "SUBSYSTEM"), MESSAGING_SUBSYSTEM);
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readHornetQServers, getModelDescriptionConstant(cl, "ADDRESS")), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readHornetQServers, getModelDescriptionConstant(cl, "RECURSIVE_DEPTH")), 0);
            setModelNodeChildString(cl, getModelNodeChild(cl, readHornetQServers, getClientConstant(cl, "CHILD_TYPE")), HORNETQ_SERVER_TYPE);

            // ModelNode
            Object response = executeOnModelNode(cl, readHornetQServers);
            if (isSuccessfulOutcome(cl, response)) {
                // List<ModelNode>
                List names = modelNodeAsList(cl, readResult(cl, response));
                for (Object hornetqServer : names) {
                    String hornetqServerName = modelNodeAsString(cl, hornetqServer);
                    destinations.addAll(getJMSDestinationForServerDeployment(deployment, hornetqServerName, Type.QUEUE));
                    destinations.addAll(getJMSDestinationForServerDeployment(deployment, hornetqServerName, Type.TOPIC));
                }
            }
            return destinations;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        }
    }

    public List<MessageDestination> listDestinations() throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<MessageDestination> destinations = new ArrayList<MessageDestination>();
            // ModelNode
            final Object readHornetQServers = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readHornetQServers, getClientConstant(cl, "OP")), getClientConstant(cl, "READ_CHILDREN_NAMES_OPERATION"));

            LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
            values.put(getClientConstant(cl, "SUBSYSTEM"), MESSAGING_SUBSYSTEM);
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readHornetQServers, getModelDescriptionConstant(cl, "ADDRESS")), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readHornetQServers, getModelDescriptionConstant(cl, "RECURSIVE_DEPTH")), 0);
            setModelNodeChildString(cl, getModelNodeChild(cl, readHornetQServers, getClientConstant(cl, "CHILD_TYPE")), HORNETQ_SERVER_TYPE);

            // ModelNode
            Object response = executeOnModelNode(cl, readHornetQServers);
            if (isSuccessfulOutcome(cl, response)) {
                // List<ModelNode>
                List names = modelNodeAsList(cl, readResult(cl, response));
                for (Object hornetqServer : names) {
                    String hornetqServerName = modelNodeAsString(cl, hornetqServer);
                    destinations.addAll(getJMSDestinationForServer(hornetqServerName, Type.QUEUE));
                    destinations.addAll(getJMSDestinationForServer(hornetqServerName, Type.TOPIC));
                }
            }
            return destinations;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        }
    }

    private List<WildflyMessageDestination> getJMSDestinationForServerDeployment(String deployment, String serverName, Type messageType) throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyMessageDestination> listedDestinations = new ArrayList<WildflyMessageDestination>();
            // ModelNode
            final Object readQueues = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readQueues, getClientConstant(cl, "OP")),
                    getModelDescriptionConstant(cl, "READ_CHILDREN_RESOURCES_OPERATION"));

            LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
            values.put(getClientConstant(cl, "DEPLOYMENT"), deployment);
            values.put(getClientConstant(cl, "SUBSYSTEM"), MESSAGING_SUBSYSTEM);
            values.put(HORNETQ_SERVER_TYPE, serverName);
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readQueues, getModelDescriptionConstant(cl, "ADDRESS")), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readQueues, getModelDescriptionConstant(cl, "RECURSIVE_DEPTH")), 0);
            if (messageType == Type.QUEUE) {
                setModelNodeChildString(cl, getModelNodeChild(cl, readQueues, getClientConstant(cl, "CHILD_TYPE")), JMSQUEUE_TYPE);
            } else {
                setModelNodeChildString(cl, getModelNodeChild(cl, readQueues, getClientConstant(cl, "CHILD_TYPE")), JMSTOPIC_TYPE);
            }
            setModelNodeChildString(cl, getModelNodeChild(cl, readQueues, getClientConstant(cl, "INCLUDE_RUNTIME")), "true");

            // ModelNode
            Object response = executeOnModelNode(cl, readQueues);
            if (isSuccessfulOutcome(cl, response)) {
                // List<ModelNode>
                List destinations = modelNodeAsList(cl, readResult(cl, response));
                for (Object destination : destinations) {
                    Object value = modelNodeAsPropertyForValue(cl, destination);
                    if (modelNodeHasChild(cl, value, "entries")) {
                        List entries = modelNodeAsList(cl, getModelNodeChild(cl, modelNodeAsPropertyForValue(cl, destination), "entries"));
                        for (Object entry : entries) {
                            listedDestinations.add(new WildflyMessageDestination(modelNodeAsString(cl, entry), messageType));
                        }
                    } else {
                        listedDestinations.add(new WildflyMessageDestination(modelNodeAsPropertyForName(cl, destination), messageType));
                    }
                }
            }
            return listedDestinations;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        }
    }

    private List<WildflyMessageDestination> getJMSDestinationForServer(String serverName, Type messageType) throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyMessageDestination> listedDestinations = new ArrayList<WildflyMessageDestination>();
            // ModelNode
            final Object readQueues = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readQueues, getClientConstant(cl, "OP")),
                    getModelDescriptionConstant(cl, "READ_CHILDREN_RESOURCES_OPERATION"));

            LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
            values.put(getClientConstant(cl, "SUBSYSTEM"), MESSAGING_SUBSYSTEM);
            values.put(HORNETQ_SERVER_TYPE, serverName);
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readQueues, getModelDescriptionConstant(cl, "ADDRESS")), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readQueues, getModelDescriptionConstant(cl, "RECURSIVE_DEPTH")), 0);
            if (messageType == Type.QUEUE) {
                setModelNodeChildString(cl, getModelNodeChild(cl, readQueues, getClientConstant(cl, "CHILD_TYPE")), JMSQUEUE_TYPE);
            } else {
                setModelNodeChildString(cl, getModelNodeChild(cl, readQueues, getClientConstant(cl, "CHILD_TYPE")), JMSTOPIC_TYPE);
            }

            // ModelNode
            Object response = executeOnModelNode(cl, readQueues);
            if (isSuccessfulOutcome(cl, response)) {
                // List<ModelNode>
                List destinations = modelNodeAsList(cl, readResult(cl, response));
                for (Object destination : destinations) {
                    Object value = modelNodeAsPropertyForValue(cl, destination);
                    if (modelNodeHasChild(cl, value, "entries")) {
                        List entries = modelNodeAsList(cl, getModelNodeChild(cl, modelNodeAsPropertyForValue(cl, destination), "entries"));
                        for (Object entry : entries) {
                            listedDestinations.add(new WildflyMessageDestination(modelNodeAsString(cl, entry), messageType));
                        }
                    } else {
                        listedDestinations.add(new WildflyMessageDestination(modelNodeAsPropertyForName(cl, destination), messageType));
                    }
                }
            }
            return listedDestinations;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        }
    }

    public boolean addMessageDestinations(final Collection<WildflyMessageDestination> destinations) throws IOException {
        boolean result = isServerRunning();
        if (result) {
            for (WildflyMessageDestination destination : destinations) {
                result = result && addMessageDestination(destination);
            }
        }
        return result;
    }

    public boolean addMessageDestination(WildflyMessageDestination destination) throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
            values.put(getClientConstant(cl, "SUBSYSTEM"), MESSAGING_SUBSYSTEM);
            values.put("hornetq-server", "default");
            if (destination.getType() == Type.QUEUE) {
                values.put("jms-queue", destination.getName());
            } else {
                values.put("jms-topic", destination.getName());
            }
            Object address = createPathAddressAsModelNode(cl, values);
            Object operation = setModelNodeChild(cl, getModelNodeChild(cl, createAddOperation(cl, address), "entries"), destination.getJndiNames());
            Object response = executeOnOperation(cl, operation);
            return (isSuccessfulOutcome(cl, response));
        } catch (ClassNotFoundException ex) {
            return false;
        } catch (IllegalAccessException ex) {
            return false;
        } catch (NoSuchMethodException ex) {
            return false;
        } catch (InvocationTargetException ex) {
            return false;
        } catch (InstantiationException ex) {
            return false;
        }
    }

    public Collection<WildflyMailSessionResource> listMailSessions() throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyMailSessionResource> modules = new ArrayList<WildflyMailSessionResource>();
            LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
            values.put(getClientConstant(cl, "SUBSYSTEM"), MAIL_SUBSYSTEM);
            Object address = createPathAddressAsModelNode(cl, values);
            final Object readMailSessions = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readMailSessions, getClientConstant(cl, "OP")),
                    getModelDescriptionConstant(cl, "READ_CHILDREN_RESOURCES_OPERATION"));
            setModelNodeChild(cl, getModelNodeChild(cl, readMailSessions, getModelDescriptionConstant(cl, "ADDRESS")), address);
            setModelNodeChildString(cl, getModelNodeChild(cl, readMailSessions, getClientConstant(cl, "CHILD_TYPE")), MAIL_SESSION_TYPE);
            setModelNodeChild(cl, getModelNodeChild(cl, readMailSessions, getModelDescriptionConstant(cl, "RECURSIVE_DEPTH")), 0);
            setModelNodeChildString(cl, getModelNodeChild(cl, readMailSessions, getClientConstant(cl, "INCLUDE_RUNTIME")), "true");
            setModelNodeChildString(cl, getModelNodeChild(cl, readMailSessions, getClientConstant(cl, "RECURSIVE")), "true");
            Object response = executeOnModelNode(cl, readMailSessions);
            if (isSuccessfulOutcome(cl, response)) {
                Object result = readResult(cl, response);
                List mailSessions = modelNodeAsList(cl, result);
                for (Object mailSession : mailSessions) {
                    String sessionName = modelNodeAsPropertyForName(cl, mailSession);
                    modules.add(fillMailSession(sessionName, mailSession));
                }
            }
            return modules;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        }
    }

    public Collection listEarApplications(Lookup lookup) throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyEarApplicationNode> modules = new ArrayList<WildflyEarApplicationNode>();
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, null);
            Object readDeployments = createReadResourceOperation(cl, deploymentAddressModelNode, true);
            Object response = executeOnModelNode(cl, readDeployments);
            if (isSuccessfulOutcome(cl, response)) {
                Object result = readResult(cl, response);
                List applications = modelNodeAsList(cl, result);
                for (Object application : applications) {
                    String applicationName = modelNodeAsString(cl, getModelNodeChild(cl, readResult(cl, application), getClientConstant(cl, "NAME")));
                    if (applicationName.endsWith(".ear")) {
                        modules.add(new WildflyEarApplicationNode(applicationName, lookup));
                    }
                }
            }
            return modules;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        }
    }

    public Collection listEarSubModules(Lookup lookup, String jeeApplicationName) throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List modules = new ArrayList();
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, jeeApplicationName);
            Object readDeployments = createReadResourceOperation(cl, deploymentAddressModelNode, true);
            Object response = executeOnModelNode(cl, readDeployments);
            if (isSuccessfulOutcome(cl, response)) {
                String httpPort = ip.getProperty(WildflyPluginProperties.PROPERTY_PORT);
                Object result = readResult(cl, response);
                List subDeployments = modelNodeAsList(cl, getModelNodeChild(cl, result, "subdeployment"));
                for (Object subDeployment : subDeployments) {
                    String applicationName = modelNodeAsPropertyForName(cl, subDeployment);
                    if (applicationName.endsWith(".war")) {
                        // ModelNode
                        Object deployment = getModelNodeChild(cl, getModelNodeChild(cl, modelNodeAsPropertyForValue(cl, subDeployment), getClientConstant(cl, "SUBSYSTEM")), WEB_SUBSYSTEM);
                        if (modelNodeIsDefined(cl, deployment)) {
                            String url = "http://" + serverAddress + ':' + httpPort + modelNodeAsString(cl, getModelNodeChild(cl, deployment, "context-root"));
                            modules.add(new WildflyWebModuleNode(applicationName, lookup, url));
                        } else {
                            modules.add(new WildflyWebModuleNode(applicationName, lookup, null));
                        }
                    } else if (applicationName.endsWith(".jar")) {
                        // ModelNode
                        Object deployment = getModelNodeChild(cl, getModelNodeChild(cl, modelNodeAsPropertyForValue(cl, subDeployment), getClientConstant(cl, "SUBSYSTEM")), EJB3_SUBSYSTEM);
                        if (modelNodeIsDefined(cl, deployment)) {
                            List<WildflyEjbComponentNode> ejbs = new ArrayList<WildflyEjbComponentNode>();
                            ejbs.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.ENTITY));
                            ejbs.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.MDB));
                            ejbs.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.SINGLETON));
                            ejbs.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.STATEFULL));
                            ejbs.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.STATELESS));
                            modules.add(new WildflyEjbModuleNode(applicationName, lookup, ejbs, true));
                        }
                    }
                }
            }
            return modules;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        }
    }

    private List<WildflyEjbComponentNode> listEJBs(WildflyDeploymentFactory.WildFlyClassLoader cl,
            Object deployment, WildflyEjbComponentNode.Type type) throws IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        List<WildflyEjbComponentNode> modules = new ArrayList<WildflyEjbComponentNode>();
        if (modelNodeHasDefinedChild(cl, deployment, type.getPropertyName())) {
            List ejbs = modelNodeAsList(cl, getModelNodeChild(cl, deployment, type.getPropertyName()));
            for (Object ejb : ejbs) {
                modules.add(new WildflyEjbComponentNode(modelNodeAsPropertyForName(cl, ejb), type));
            }
        }
        return modules;
    }

    private WildflySocket fillSocket(String name, boolean outBound) throws
            ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
        WildflySocket socket = new WildflySocket();
        LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
        values.put("socket-binding-group", "standard-sockets");
        if (outBound) {
            values.put("remote-destination-outbound-socket-binding", name);
        } else {
            values.put("socket-binding", name);
        }
        Object address = createPathAddressAsModelNode(cl, values);
        final Object readSocket = createModelNode(cl);
        setModelNodeChildString(cl, getModelNodeChild(cl, readSocket, getClientConstant(cl, "OP")),
                getModelDescriptionConstant(cl, "READ_RESOURCE_OPERATION"));
        setModelNodeChild(cl, getModelNodeChild(cl, readSocket, getModelDescriptionConstant(cl, "ADDRESS")), address);
        setModelNodeChild(cl, getModelNodeChild(cl, readSocket, getModelDescriptionConstant(cl, "RECURSIVE_DEPTH")), 0);
        setModelNodeChildString(cl, getModelNodeChild(cl, readSocket, getClientConstant(cl, "INCLUDE_RUNTIME")), "true");
        setModelNodeChildString(cl, getModelNodeChild(cl, readSocket, getClientConstant(cl, "RECURSIVE")), "true");
        Object response = executeOnModelNode(cl, readSocket);
        if (isSuccessfulOutcome(cl, response)) {
            Object binding = readResult(cl, response);
            if (modelNodeHasDefinedChild(cl, binding, "fixed-source-port")) {
                socket.setFixedSourcePort(modelNodeAsBoolean(cl, getModelNodeChild(cl, binding, "fixed-source-port")));
            }
            if (modelNodeHasDefinedChild(cl, binding, "host")) {
                socket.setHost(modelNodeAsString(cl, getModelNodeChild(cl, binding, "host")));
            }
            if (modelNodeHasDefinedChild(cl, binding, "port")) {
                socket.setPort(modelNodeAsInt(cl, getModelNodeChild(cl, binding, "port")));
            }
        }
        return socket;
    }

    public Collection<WildflyConnectionFactory> listConnectionFactories() throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyConnectionFactory> connectionFactories = new ArrayList<WildflyConnectionFactory>();
            // ModelNode
            final Object readHornetQServers = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readHornetQServers, getClientConstant(cl, "OP")), getClientConstant(cl, "READ_CHILDREN_NAMES_OPERATION"));

            LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
            values.put(getClientConstant(cl, "SUBSYSTEM"), MESSAGING_SUBSYSTEM);
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readHornetQServers, getModelDescriptionConstant(cl, "ADDRESS")), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readHornetQServers, getModelDescriptionConstant(cl, "RECURSIVE_DEPTH")), 0);
            setModelNodeChildString(cl, getModelNodeChild(cl, readHornetQServers, getClientConstant(cl, "CHILD_TYPE")), HORNETQ_SERVER_TYPE);

            // ModelNode
            Object response = executeOnModelNode(cl, readHornetQServers);
            if (isSuccessfulOutcome(cl, response)) {
                // List<ModelNode>
                List names = modelNodeAsList(cl, readResult(cl, response));
                for (Object hornetqServer : names) {
                    String hornetqServerName = modelNodeAsString(cl, hornetqServer);
                    connectionFactories.addAll(getConnectionFactoriesForServer(hornetqServerName));
                }
            }
            return connectionFactories;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        }
    }

    private Collection<? extends WildflyConnectionFactory> getConnectionFactoriesForServer(String hornetqServerName) throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyConnectionFactory> listedConnectionFactories = new ArrayList<WildflyConnectionFactory>();
            // ModelNode
            final Object readConnectionFactories = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readConnectionFactories, getClientConstant(cl, "OP")),
                    getModelDescriptionConstant(cl, "READ_CHILDREN_RESOURCES_OPERATION"));

            LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
            values.put(getClientConstant(cl, "SUBSYSTEM"), MESSAGING_SUBSYSTEM);
            values.put(HORNETQ_SERVER_TYPE, hornetqServerName);
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readConnectionFactories, getModelDescriptionConstant(cl, "ADDRESS")), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readConnectionFactories, getModelDescriptionConstant(cl, "RECURSIVE_DEPTH")), 0);
            setModelNodeChildString(cl, getModelNodeChild(cl, readConnectionFactories, getClientConstant(cl, "CHILD_TYPE")), CONNECTION_FACTORY_TYPE);
            setModelNodeChildString(cl, getModelNodeChild(cl, readConnectionFactories, getClientConstant(cl, "INCLUDE_RUNTIME")), "true");

            // ModelNode
            Object response = executeOnModelNode(cl, readConnectionFactories);
            if (isSuccessfulOutcome(cl, response)) {
                // List<ModelNode>
                List connectionFactories = modelNodeAsPropertyList(cl, readResult(cl, response));
                for (Object connectionFactory : connectionFactories) {
                    listedConnectionFactories.add(fillConnectionFactory(
                            getPropertyName(cl, connectionFactory),
                            getPropertyValue(cl, connectionFactory)));

                }
            }
            return listedConnectionFactories;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        }
    }

    private WildflyConnectionFactory fillConnectionFactory(String name, Object configuration) throws
            ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
        List properties = modelNodeAsPropertyList(cl, configuration);
        Map<String, String> attributes = new HashMap<String, String>(properties.size());
        for (Object property : properties) {
            String propertyName = getPropertyName(cl, property);
            Object propertyValue = getPropertyValue(cl, property);
            if (modelNodeIsDefined(cl, propertyValue)) {
                attributes.put(propertyName, modelNodeAsString(cl, propertyValue));
            }
        }
        return new WildflyConnectionFactory(attributes, name);
    }

    private WildflyMailSessionResource fillMailSession(String name, Object mailSession) throws
            ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);

        Object configuration = modelNodeAsPropertyForValue(cl, mailSession);
        List properties = modelNodeAsPropertyList(cl, configuration);
        Map<String, String> attributes = new HashMap<String, String>(properties.size());
        for (Object property : properties) {
            String propertyName = getPropertyName(cl, property);
            Object propertyValue = getPropertyValue(cl, property);
            if (!"debug".equals(propertyName) && !"jndi-name".equals(propertyName) && modelNodeIsDefined(cl, propertyValue)) {
                attributes.put(propertyName, modelNodeAsString(cl, propertyValue));
            }
        }
        WildflyMailSessionResource session = new WildflyMailSessionResource(attributes, name);
        List serverProperties = modelNodeAsList(cl, getModelNodeChild(cl, configuration, "server"));
        for (Object property : serverProperties) {
            if (modelNodeIsDefined(cl, property)) {
                Object settings = modelNodeAsPropertyForValue(cl, property);
                if (modelNodeHasDefinedChild(cl, settings, "username")) {
                    session.setUserName(modelNodeAsString(cl, getModelNodeChild(cl, settings, "username")));
                }
                if (modelNodeHasDefinedChild(cl, settings, "outbound-socket-binding-ref")) {
                    session.setSocket(fillSocket(modelNodeAsString(cl, getModelNodeChild(cl, settings, "outbound-socket-binding-ref")), true));
                }

            }
        }
        session.setIsDebug(modelNodeAsString(cl, getModelNodeChild(cl, configuration, "debug")));
        session.setJndiName(modelNodeAsString(cl, getModelNodeChild(cl, configuration, "jndi-name")));
        return session;
    }

    public String getDeploymentDirectory() throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
            values.put(getClientConstant(cl, "SUBSYSTEM"), "deployment-scanner");
            values.put("scanner", "default");

            final Object readPathOperation = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readPathOperation, getClientConstant(cl, "OP")),
                    getModelDescriptionConstant(cl, "READ_RESOURCE_OPERATION"));
            // ModelNode
            Object scannerAddress = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readPathOperation, getModelDescriptionConstant(cl, "ADDRESS")), scannerAddress);
            setModelNodeChild(cl, getModelNodeChild(cl, readPathOperation, getModelDescriptionConstant(cl, "RECURSIVE_DEPTH")), 0);
            setModelNodeChildString(cl, getModelNodeChild(cl, readPathOperation, getClientConstant(cl, "INCLUDE_RUNTIME")), "true");
            Object response = executeOnModelNode(cl, readPathOperation);
            if (isSuccessfulOutcome(cl, response)) {
                Object scanner = readResult(cl, response);
                if (modelNodeAsBoolean(cl, getModelNodeChild(cl, scanner, "scan-enabled"))) {
                    String path = modelNodeAsString(cl, getModelNodeChild(cl, scanner, getClientConstant(cl, "PATH")));
                    String relativeTo = readPath(modelNodeAsString(cl, getModelNodeChild(cl, scanner, "relative-to")));
                    return relativeTo + File.separatorChar + path;
                }
            }
            return "";
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        }
    }

    private String readPath(String pathName) throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
            values.put(getClientConstant(cl, "PATH"), pathName);
            final Object readPathOperation = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readPathOperation, getClientConstant(cl, "OP")),
                    getModelDescriptionConstant(cl, "READ_RESOURCE_OPERATION"));
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readPathOperation, getModelDescriptionConstant(cl, "ADDRESS")), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readPathOperation, getModelDescriptionConstant(cl, "RECURSIVE_DEPTH")), 0);
            setModelNodeChildString(cl, getModelNodeChild(cl, readPathOperation, getClientConstant(cl, "INCLUDE_RUNTIME")), "true");
            Object response = executeOnModelNode(cl, readPathOperation);
            if (isSuccessfulOutcome(cl, response)) {
                return modelNodeAsString(cl, getModelNodeChild(cl, readResult(cl, response), getClientConstant(cl, "PATH")));
            }
            return pathName;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        }
    }

    private void enableExplodedDeployment(String scannerName) throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, NoSuchMethodException, InvocationTargetException, IOException {
        WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
        LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
        values.put(getClientConstant(cl, "SUBSYSTEM"), "deployment-scanner");
        values.put("scanner", scannerName);

        final Object updateDeploymentScanner = createModelNode(cl);
        setModelNodeChildString(cl, getModelNodeChild(cl, updateDeploymentScanner, getClientConstant(cl, "OP")),
                getModelDescriptionConstant(cl, "WRITE_ATTRIBUTE_OPERATION"));
        // ModelNode
        Object scannerAddress = createPathAddressAsModelNode(cl, values);
        setModelNodeChild(cl, getModelNodeChild(cl, updateDeploymentScanner, getModelDescriptionConstant(cl, "ADDRESS")), scannerAddress);
        setModelNodeChildString(cl, getModelNodeChild(cl, updateDeploymentScanner, getClientConstant(cl, "INCLUDE_RUNTIME")), "true");
        setModelNodeChildString(cl, getModelNodeChild(cl, updateDeploymentScanner, getModelDescriptionConstant(cl, "NAME")), "auto-deploy-exploded");
        setModelNodeChildString(cl, getModelNodeChild(cl, updateDeploymentScanner, getModelDescriptionConstant(cl, "VALUE")), "true");
        executeOnModelNode(cl, updateDeploymentScanner);
    }

    public Collection<WildflyResourceAdapter> listResourceAdapters() throws IOException {
        try {
            WildflyDeploymentFactory.WildFlyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyResourceAdapter> resourceAdapters = new ArrayList<WildflyResourceAdapter>();
            // ModelNode
            final Object readResourceAdapters = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readResourceAdapters,
                    getClientConstant(cl, "OP")), getModelDescriptionConstant(cl, "READ_CHILDREN_RESOURCES_OPERATION"));

            LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
            values.put(getClientConstant(cl, "SUBSYSTEM"), RESOURCE_ADAPTER_SUBSYSTEM);
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readResourceAdapters, getModelDescriptionConstant(cl, "ADDRESS")), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readResourceAdapters, getModelDescriptionConstant(cl, "RECURSIVE_DEPTH")), 0);
            setModelNodeChildString(cl, getModelNodeChild(cl, readResourceAdapters, getClientConstant(cl, "INCLUDE_RUNTIME")), "true");
            setModelNodeChildString(cl, getModelNodeChild(cl, readResourceAdapters, getClientConstant(cl, "CHILD_TYPE")), RESOURCE_ADAPTER_TYPE);

            // ModelNode
            Object response = executeOnModelNode(cl, readResourceAdapters);
            if (isSuccessfulOutcome(cl, response)) {
                // List<ModelNode>
                List ressources = modelNodeAsList(cl, readResult(cl, response));
                for (Object resource : ressources) {
                    Object configuration = modelNodeAsPropertyForValue(cl, resource);
                    List properties = modelNodeAsPropertyList(cl, configuration);
                    Map<String, String> attributes = new HashMap<String, String>(properties.size());
                    for (Object property : properties) {
                        String propertyName = getPropertyName(cl, property);
                        Object propertyValue = getPropertyValue(cl, property);
                        if (modelNodeIsDefined(cl, propertyValue)) {
                            attributes.put(propertyName, modelNodeAsString(cl, propertyValue));
                        }
                    }
                    WildflyResourceAdapter resourceAdapter = new WildflyResourceAdapter(attributes, modelNodeAsPropertyForName(cl, resource));
                    resourceAdapters.add(resourceAdapter);
                }
            }
            return resourceAdapters;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        }
    }
}
