package com.example.scim;

import com.example.scim.scimple.ScimGroupProvider;
import org.apache.directory.scim.server.exception.UnableToCreateResourceException;
import org.apache.directory.scim.server.exception.UnableToUpdateResourceException;
import org.apache.directory.scim.server.provider.UpdateRequest;
import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.spec.protocol.data.PatchOperation;
import org.apache.directory.scim.spec.protocol.data.PatchOperationPath;
import org.apache.directory.scim.spec.protocol.filter.FilterParseException;
import org.apache.directory.scim.spec.protocol.filter.FilterResponse;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.schema.ResourceReference;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
    public void testUpdate() throws UnableToCreateResourceException, UnableToUpdateResourceException {

        ScimGroupProvider provider = new ScimGroupProvider();

        ScimGroup group1 = new ScimGroup();
        group1.setDisplayName("test-me1");
        group1 = provider.create(group1);

        group1.setDisplayName("test-me2");

        UpdateRequest<ScimGroup> updateRequest = mock(UpdateRequest.class);
        when(updateRequest.getId()).thenReturn(group1.getId());
        when(updateRequest.getResource()).thenReturn(group1);
        ScimGroup result = provider.update(updateRequest);

        assertThat(result.getDisplayName(), is("test-me2"));
    }

    @Test
    public void testUpdateWithPatch_WithPath() throws UnableToCreateResourceException, UnableToUpdateResourceException, FilterParseException {

        ScimGroupProvider provider = new ScimGroupProvider();

        ScimGroup group1 = new ScimGroup();
        group1.setDisplayName("test-me1");
        group1 = provider.create(group1);

        PatchOperation patchOperation = new PatchOperation();
        patchOperation.setOperation(PatchOperation.Type.REPLACE);
        patchOperation.setPath(new PatchOperationPath("displayName"));
        patchOperation.setValue("test-me2");

        UpdateRequest<ScimGroup> updateRequest = mock(UpdateRequest.class);
        when(updateRequest.getId()).thenReturn(group1.getId());
        when(updateRequest.getPatchOperations()).thenReturn(Collections.singletonList(patchOperation));
        when(updateRequest.getOriginal()).thenReturn(group1);
        ScimGroup result = provider.update(updateRequest);

        assertThat(result.getDisplayName(), is("test-me2"));
    }

    @Test
    public void testUpdateWithPatch_NoPath() throws Exception {
        ScimGroupProvider provider = new ScimGroupProvider();

        ScimGroup group1 = new ScimGroup();
        group1.setDisplayName("test-me1");
        group1 = provider.create(group1);

        Map<String, String> props = new HashMap<>();
        props.put("id", group1.getId());
        props.put("displayName", "test-me2");

        PatchOperation patchOperation = new PatchOperation();
        patchOperation.setOperation(PatchOperation.Type.REPLACE);
        patchOperation.setValue(props);

        UpdateRequest<ScimGroup> updateRequest = mock(UpdateRequest.class);
        when(updateRequest.getId()).thenReturn(group1.getId());
        when(updateRequest.getPatchOperations()).thenReturn(Collections.singletonList(patchOperation));
        when(updateRequest.getOriginal()).thenReturn(group1);
        ScimGroup result = provider.update(updateRequest);

        assertThat(result.getDisplayName(), is("test-me2"));
    }

    @Test
    public void testUpdateWithPatch_AddToList() throws UnableToCreateResourceException, UnableToUpdateResourceException, FilterParseException {

        ScimGroupProvider provider = new ScimGroupProvider();

        ResourceReference member1 = new ResourceReference();
        member1.setValue("member1");

        ScimGroup group1 = new ScimGroup();
        group1.setDisplayName("test-me1");
        group1 = provider.create(group1);

        PatchOperation patchOperation = new PatchOperation();
        patchOperation.setOperation(PatchOperation.Type.ADD);
        patchOperation.setPath(new PatchOperationPath("members"));
        patchOperation.setValue(Collections.singletonList(member1));

        UpdateRequest<ScimGroup> updateRequest = mock(UpdateRequest.class);
        when(updateRequest.getId()).thenReturn(group1.getId());
        when(updateRequest.getPatchOperations()).thenReturn(Collections.singletonList(patchOperation));
        when(updateRequest.getOriginal()).thenReturn(group1);
        ScimGroup result = provider.update(updateRequest);

        assertThat(result.getMembers(), contains(member1));
    }

    @Test
    public void testUpdateWithPatch_AddAsProperty() throws UnableToCreateResourceException, UnableToUpdateResourceException, FilterParseException {

        ScimGroupProvider provider = new ScimGroupProvider();

        ScimGroup group1 = new ScimGroup();
        group1.setDisplayName("test-me1");
        group1 = provider.create(group1);

        PatchOperation patchOperation = new PatchOperation();
        patchOperation.setOperation(PatchOperation.Type.ADD);
        patchOperation.setPath(new PatchOperationPath("displayName"));
        patchOperation.setValue("test-me2");

        UpdateRequest<ScimGroup> updateRequest = mock(UpdateRequest.class);
        when(updateRequest.getId()).thenReturn(group1.getId());
        when(updateRequest.getPatchOperations()).thenReturn(Collections.singletonList(patchOperation));
        when(updateRequest.getOriginal()).thenReturn(group1);
        ScimGroup result = provider.update(updateRequest);

        assertThat(result.getDisplayName(), is("test-me2"));
    }

    @Test
    public void testUpdateWithPatch_RemoveAsProperty() throws UnableToCreateResourceException, UnableToUpdateResourceException, FilterParseException {

        ScimGroupProvider provider = new ScimGroupProvider();

        ScimGroup group1 = new ScimGroup();
        group1.setDisplayName("test-me1");
        group1 = provider.create(group1);

        PatchOperation patchOperation = new PatchOperation();
        patchOperation.setOperation(PatchOperation.Type.REMOVE);
        patchOperation.setPath(new PatchOperationPath("displayName"));

        UpdateRequest<ScimGroup> updateRequest = mock(UpdateRequest.class);
        when(updateRequest.getId()).thenReturn(group1.getId());
        when(updateRequest.getPatchOperations()).thenReturn(Collections.singletonList(patchOperation));
        when(updateRequest.getOriginal()).thenReturn(group1);
        ScimGroup result = provider.update(updateRequest);

        assertThat(result.getDisplayName(), nullValue());
    }

    @Test
    public void testUpdateWithPatch_AddWithMap() throws Exception {

        ScimGroupProvider provider = new ScimGroupProvider();

        ResourceReference member1 = new ResourceReference();
        member1.setValue("member1");

        Map<String, Object> member1Raw = new HashMap<>();
        member1Raw.put("value", "member1");

        ScimGroup group1 = new ScimGroup();
        group1.setDisplayName("test-me1");
        group1 = provider.create(group1);

        PatchOperation patchOperation = new PatchOperation();
        patchOperation.setOperation(PatchOperation.Type.ADD);
        patchOperation.setPath(new PatchOperationPath("members"));
        patchOperation.setValue(Collections.singletonList(member1Raw));

        Registry registry = mock(Registry.class);
        UpdateRequest<ScimGroup> updateRequest = new UpdateRequest<>(registry);
        updateRequest.initWithPatch(group1.getId(), group1, Collections.singletonList(patchOperation));

        ScimGroup result = provider.update(updateRequest);

        assertThat(result.getMembers(), contains(member1));
    }
}
