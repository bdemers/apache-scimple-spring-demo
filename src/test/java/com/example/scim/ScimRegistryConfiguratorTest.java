package com.example.scim;

import com.example.scim.scimple.ScimGroupProvider;
import com.example.scim.scimple.ScimRegistryConfigurator;
import com.example.scim.scimple.ScimUserProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.directory.scim.server.configuration.ServerConfiguration;
import org.apache.directory.scim.server.exception.InvalidProviderException;
import org.apache.directory.scim.server.exception.UnableToRetrieveExtensionsException;
import org.apache.directory.scim.server.provider.ProviderRegistry;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

import javax.enterprise.inject.Instance;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ScimRegistryConfiguratorTest {

    @Test
    public void testInit() throws InvalidProviderException, UnableToRetrieveExtensionsException, JsonProcessingException {

        ServerConfiguration serverConfiguration = mock(ServerConfiguration.class);
        ProviderRegistry providerRegistry = mock(ProviderRegistry.class);
        Instance<ScimUserProvider> userProviderInstance = mock(Instance.class);
        Instance<ScimGroupProvider> groupProviderInstance = mock(Instance.class);

        new ScimRegistryConfigurator(serverConfiguration, providerRegistry, userProviderInstance, groupProviderInstance);

        // verify auth schema was added
        ArgumentCaptor<ServiceProviderConfiguration.AuthenticationSchema> authSchemaCapture = ArgumentCaptor.forClass(ServiceProviderConfiguration.AuthenticationSchema.class);
        verify(serverConfiguration).addAuthenticationSchema(authSchemaCapture.capture());
        ServiceProviderConfiguration.AuthenticationSchema authSchema = authSchemaCapture.getValue();
        assertThat(authSchema.getType(), is(ServiceProviderConfiguration.AuthenticationSchema.Type.HTTP_BASIC));

        // verify the ScimUser and ScimGroup providers were added
        ArgumentCaptor<Class> classArgumentCaptor = ArgumentCaptor.forClass(Class.class);
        ArgumentCaptor<Instance> instanceArgumentCaptor = ArgumentCaptor.forClass(Instance.class);
        verify(providerRegistry, times(2)).registerProvider(classArgumentCaptor.capture(), instanceArgumentCaptor.capture());
        assertThat(classArgumentCaptor.getAllValues(), contains(ScimUser.class, ScimGroup.class));
        assertThat(instanceArgumentCaptor.getAllValues(), contains(userProviderInstance, groupProviderInstance));
    }
}
