package com.example.scim;

import com.example.scim.scimple.ScimUserProvider;
import org.apache.directory.scim.server.exception.UnableToCreateResourceException;
import org.apache.directory.scim.server.provider.UpdateRequest;
import org.apache.directory.scim.spec.protocol.filter.FilterParseException;
import org.apache.directory.scim.spec.protocol.filter.FilterResponse;
import org.apache.directory.scim.spec.protocol.search.Filter;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.testng.annotations.Test;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static io.codearte.catchexception.shade.mockito.Mockito.mock;
import static io.codearte.catchexception.shade.mockito.Mockito.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class ScimUserProviderTest {

    @Test
    public void testExtensionList() {
        ScimUserProvider provider = new ScimUserProvider();
        assertThat(provider.getExtensionList(), empty());
    }

    @Test
    public void testSimpleCreateAndGet() throws UnableToCreateResourceException {

        ScimUser user = new ScimUser();
        user.setUserName("jcoder");

        ScimUserProvider provider = new ScimUserProvider();
        ScimUser createResult = provider.create(user);
        assertThat(createResult.getUserName(), is("jcoder"));
        assertThat(createResult.getId(), notNullValue());

        ScimUser result = provider.get(createResult.getId());
        assertThat(result, equalTo(createResult));
    }

    @Test
    public void recreateTest() throws UnableToCreateResourceException {

        ScimUser user = new ScimUser();
        user.setUserName("jcoder");

        ScimUserProvider provider = new ScimUserProvider();
        ScimUser result = provider.create(user);

        catchException(provider).create(result);
        assertThat(caughtException(), instanceOf(UnableToCreateResourceException.class));
    }

    @Test
    public void testDeleteAndGet() throws UnableToCreateResourceException {

        ScimUser user = new ScimUser();
        user.setUserName("jcoder");

        ScimUserProvider provider = new ScimUserProvider();
        ScimUser result = provider.create(user);

        provider.delete(result.getId());

        assertThat(provider.get(result.getId()), nullValue());
    }

    @Test
    public void testFind() throws UnableToCreateResourceException {

        ScimUserProvider provider = new ScimUserProvider();
        FilterResponse<ScimUser> response = provider.find(null, null, null);
        assertThat(response.getResources(), hasSize(1)); // In memory collection is created with one user

        ScimUser user1 = new ScimUser();
        user1.setUserName("jcoder1");
        user1 = provider.create(user1);

        ScimUser user2 = new ScimUser();
        user2.setUserName("jcoder2");
        user2 = provider.create(user2);

        response = provider.find(null, null, null);
        assertThat(response.getResources(), hasItem(user1));
        assertThat(response.getResources(), hasItem(user2));
        assertThat(response.getResources(), hasSize(3));
    }

    @Test
    public void testFindWithFilter() throws UnableToCreateResourceException, FilterParseException {

        ScimUserProvider provider = new ScimUserProvider();

        ScimUser user1 = new ScimUser();
        user1.setUserName("jcoder1");
        user1 = provider.create(user1);

        ScimUser user2 = new ScimUser();
        user2.setUserName("jcoder2");
        user2 = provider.create(user2);

        FilterResponse<ScimUser> response = provider.find(new Filter("userName Eq \"jcoder2\""), null, null);
        assertThat(response.getResources(), contains(user2));
    }

    @Test
    public void testUpdate() throws UnableToCreateResourceException {

        ScimUserProvider provider = new ScimUserProvider();

        ScimUser group1 = new ScimUser();
        group1.setUserName("jcoder1");
        group1 = provider.create(group1);

        group1.setUserName("jcoder2");

        UpdateRequest<ScimUser> updateRequest = mock(UpdateRequest.class);
        when(updateRequest.getResource()).thenReturn(group1);
        ScimUser result = provider.update(updateRequest);

        assertThat(result.getUserName(), is("jcoder2"));
    }
}