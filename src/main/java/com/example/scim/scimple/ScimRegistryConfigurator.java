/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at

* http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package com.example.scim.scimple;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.directory.scim.server.configuration.ServerConfiguration;
import org.apache.directory.scim.server.exception.InvalidProviderException;
import org.apache.directory.scim.server.exception.UnableToRetrieveExtensionsException;
import org.apache.directory.scim.server.provider.ProviderRegistry;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Registers our custom {@link ScimUserProvider} and {@link ScimGroupProvider}, in the {@link ProviderRegistry}
 * when the servlet container starts up.
 */
@WebListener
public class ScimRegistryConfigurator implements ServletContextListener {

  @Inject
  public ScimRegistryConfigurator(ServerConfiguration serverConfiguration,
                                  ProviderRegistry providerRegistry,
                                  Instance<ScimUserProvider> userProviderInstance,
                                  Instance<ScimGroupProvider> groupProviderInstance) {
    try {

      // configure the server
      ServiceProviderConfiguration.AuthenticationSchema authSchema = new ServiceProviderConfiguration.AuthenticationSchema();
      authSchema.setName("SCIMple Example");
      authSchema.setDescription("Tomee + Spring SCIMple Example");
      authSchema.setType(ServiceProviderConfiguration.AuthenticationSchema.Type.HTTP_BASIC);
      serverConfiguration.addAuthenticationSchema(authSchema);

      // add the providers
      providerRegistry.registerProvider(ScimUser.class, userProviderInstance);
      providerRegistry.registerProvider(ScimGroup.class, groupProviderInstance);

    } catch (InvalidProviderException | JsonProcessingException | UnableToRetrieveExtensionsException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    // NOOP
  }

  @Override
  public void contextDestroyed(ServletContextEvent arg0) {
    // NOOP
  }
}
