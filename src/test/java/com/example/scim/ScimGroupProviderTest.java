package com.example.scim;

import com.example.scim.scimple.ScimGroupProvider;
import org.apache.directory.scim.server.exception.UnableToCreateResourceException;
import org.apache.directory.scim.server.provider.UpdateRequest;
import org.apache.directory.scim.spec.protocol.filter.FilterResponse;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.testng.annotations.Test;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static io.codearte.catchexception.shade.mockito.Mockito.mock;
import static io.codearte.catchexception.shade.mockito.Mockito.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ScimGroupProviderTest {

    @Test
    public void testExtensionList() {
        ScimGroupProvider provider = new ScimGroupProvider();
        assertThat(provider.getExtensionList(), empty());
    }

    @Test
    public void testSimpleCreateAndGet() throws UnableToCreateResourceException {

        ScimGroup group = new ScimGroup();
        group.setDisplayName("test-me");

        ScimGroupProvider provider = new ScimGroupProvider();
        ScimGroup createResult = provider.create(group);
        assertThat(createResult.getDisplayName(), is("test-me"));
        assertThat(createResult.getId(), notNullValue());

        ScimGroup getGroup = provider.get(createResult.getId());
        assertThat(getGroup, equalTo(createResult));
    }

    @Test
    public void recreateTest() throws UnableToCreateResourceException {

        ScimGroup group = new ScimGroup();
        group.setDisplayName("test-me");

        ScimGroupProvider provider = new ScimGroupProvider();
        ScimGroup result = provider.create(group);

        catchException(provider).create(result);
        assertThat(caughtException(), instanceOf(UnableToCreateResourceException.class));
    }

    @Test
    public void testDeleteAndGet() throws UnableToCreateResourceException {

        ScimGroup group = new ScimGroup();
        group.setDisplayName("test-me");

        ScimGroupProvider provider = new ScimGroupProvider();
        ScimGroup result = provider.create(group);

        provider.delete(result.getId());

        assertThat(provider.get(result.getId()), nullValue());
    }

    @Test
    public void testFind() throws UnableToCreateResourceException {

        ScimGroupProvider provider = new ScimGroupProvider();
        FilterResponse<ScimGroup> response = provider.find(null, null, null);
        assertThat(response.getResources(), empty());

        ScimGroup group1 = new ScimGroup();
        group1.setDisplayName("test-me1");
        group1 = provider.create(group1);

        ScimGroup group2 = new ScimGroup();
        group2.setDisplayName("test-me2");
        group2 = provider.create(group2);

        response = provider.find(null, null, null);
        assertThat(response.getResources(), containsInAnyOrder(group1, group2));
        assertThat(response.getResources(), hasSize(2));
    }

    @Test
    public void testUpdate() throws UnableToCreateResourceException {

        ScimGroupProvider provider = new ScimGroupProvider();

        ScimGroup group1 = new ScimGroup();
        group1.setDisplayName("test-me1");
        group1 = provider.create(group1);

        group1.setDisplayName("test-me2");

        UpdateRequest<ScimGroup> updateRequest = mock(UpdateRequest.class);
        when(updateRequest.getResource()).thenReturn(group1);
        ScimGroup result = provider.update(updateRequest);

        assertThat(result.getDisplayName(), is("test-me2"));
    }
}
