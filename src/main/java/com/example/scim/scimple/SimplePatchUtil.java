package com.example.scim.scimple;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonPatch;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.directory.scim.server.exception.UnableToUpdateResourceException;
import org.apache.directory.scim.server.provider.UpdateRequest;
import org.apache.directory.scim.server.rest.ObjectMapperFactory;
import org.apache.directory.scim.spec.protocol.data.PatchOperation;
import org.apache.directory.scim.spec.protocol.data.PatchOperationPath;
import org.apache.directory.scim.spec.protocol.filter.FilterParseException;
import org.apache.directory.scim.spec.resources.ScimResource;

import javax.ws.rs.core.Response;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

final class SimplePatchUtil {

    private SimplePatchUtil() {}

    /**
     * Simple Proof of Concept patch util. This implementation ONLY supports paths and NOT expressions.
     * @param updateRequest The source request
     * @param <T> a ScimResource
     * @return An updated ScimResource based on the current request
     * @throws UnableToUpdateResourceException
     */
    protected static <T extends ScimResource> T resourceFromUpdateRequest(UpdateRequest<T> updateRequest, Class<T> resultType) throws UnableToUpdateResourceException {

        if (CollectionUtils.isEmpty(updateRequest.getPatchOperations())) {
            return updateRequest.getResource();
        } else {

            try {
                // TODO: This is a POC, actual implementation should not create an ObjectMapper directly
                ObjectMapper objectMapper = new ObjectMapperFactory(null).createObjectMapper();

                List<PatchOperation> patchOperations = new ArrayList<>();

                for( PatchOperation it : updateRequest.getPatchOperations()) {

                    PatchOperationPath path = it.getPath();

                    if (path == null) {
                        // convert SCIM patch to rfc 6902 patch
                        if (it.getValue() instanceof Map) {
                            Map<String, Object> properties = (Map<String, Object>) it.getValue();

                            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                                PatchOperation newPatchOperation = new PatchOperation();
                                newPatchOperation.setOperation(it.getOperation());
                                newPatchOperation.setPath(new PatchOperationPath(entry.getKey()));
                                newPatchOperation.setValue(entry.getValue());
                                patchOperations.add(newPatchOperation);
                            }
                        } else {
                            throw new UnableToUpdateResourceException(Response.Status.BAD_REQUEST, "Patch 'value' expected to be a type Map.");
                        }
                    } else {

                        // removing items from collections is NOT supported by this example
                        // Removing items this way would likely require using a path expression simlar to:
                        // members[ id eq "some id value"]
                        String simplePath = path.toString();
                        T originalResource = updateRequest.getOriginal();

                        // figure out the property type
                        PropertyUtilsBean bean = new PropertyUtilsBean();
                        PropertyDescriptor propertyDescriptor = bean.getPropertyDescriptor(originalResource, simplePath);
                        boolean isCollection = Collection.class.isAssignableFrom(propertyDescriptor.getPropertyType());

                        if (it.getOperation() == PatchOperation.Type.REMOVE && isCollection) {
                            throw new UnableToUpdateResourceException(Response.Status.NOT_IMPLEMENTED, "Removing items from collections via a patch operation is NOT supported by this example");
                        }

                        // if not a collection remove operation, just include it
                        patchOperations.add(it);
                    }
                }

                JsonNode patchOperationList = objectMapper.valueToTree(patchOperations);
                JsonNode original = objectMapper.valueToTree(updateRequest.getOriginal());
                JsonNode result = JsonPatch.apply(patchOperationList, original);

                return objectMapper.treeToValue(result, resultType);
            } catch (JsonProcessingException | FilterParseException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                throw new UnableToUpdateResourceException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to apply json patch", e);
            }
        }
    }
}
