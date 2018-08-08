package com.example.scim.scimple.workaround;

import org.apache.directory.scim.server.exception.UnableToResolveIdException;
import org.apache.directory.scim.server.provider.SelfIdResolver;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;
import java.security.Principal;

/**
 * Work around until https://github.com/apache/directory-scimple/pull/16 is resolved.
 */
@ApplicationScoped
public class NotImplementedSelfIdResolver implements SelfIdResolver {
    @Override
    public String resolveToInternalId(Principal principal) throws UnableToResolveIdException {
        throw new UnableToResolveIdException(Response.Status.NOT_IMPLEMENTED, "Method Not implemented");
    }
}
