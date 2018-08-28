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

import com.example.scim.model.ExampleAddress;
import com.example.scim.model.ExamplePerson;
import com.example.scim.model.ScimTypeConverter;
import org.apache.directory.scim.server.exception.UnableToCreateResourceException;
import org.apache.directory.scim.server.exception.UnableToUpdateResourceException;
import org.apache.directory.scim.server.provider.Provider;
import org.apache.directory.scim.server.provider.UpdateRequest;
import org.apache.directory.scim.spec.protocol.filter.AttributeComparisonExpression;
import org.apache.directory.scim.spec.protocol.filter.CompareOperator;
import org.apache.directory.scim.spec.protocol.filter.FilterExpression;
import org.apache.directory.scim.spec.protocol.filter.FilterResponse;
import org.apache.directory.scim.spec.protocol.search.Filter;
import org.apache.directory.scim.spec.protocol.search.PageRequest;
import org.apache.directory.scim.spec.protocol.search.SortRequest;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimUser;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Example SCIM Provider which will map a custom domain model {@link ExamplePerson} to/from SCIM objects.
 */
public class ScimUserProvider implements Provider<ScimUser> {

  private Map<String, ExamplePerson> people = new HashMap<>();

  public ScimUserProvider() {

    ExamplePerson person = new ExamplePerson()
            .setUsername("e1@example.com")
            .setFirstName("El")
            .setLastName("Coder")
            .setEmails(Collections.singletonList("e1@example.com"))
            .setAddresses(Collections.singletonList(new ExampleAddress()
                .setStreet("101 Main St.")
                .setCity("Springfield")
                .setState("ME")
                .setZip("012345")
                .setCountry("US")));

    people.put(person.getUsername(), person);
  }

  @Override
  public ScimUser create(ScimUser user) throws UnableToCreateResourceException {

    String resourceId = user.getId() != null ? user.getId() : user.getUserName();

    // SCIM spec does NOT allow for updating existing objects via create (POST).
    if (people.containsKey(resourceId)) {
      throw new UnableToCreateResourceException(Response.Status.CONFLICT, "User already exists.");
    }

    user.setId(resourceId);
    ExamplePerson person = ScimTypeConverter.fromScim(user);
    people.put(resourceId, person);
    return ScimTypeConverter.toScim(person);
  }

  @Override
  public ScimUser update(UpdateRequest<ScimUser> updateRequest) throws UnableToUpdateResourceException {
    String id = updateRequest.getId();
    ScimUser user = SimplePatchUtil.resourceFromUpdateRequest(updateRequest, ScimUser.class);

    // remove the old object if the id has changed
    if (!id.equals(updateRequest.getId())) {
        people.remove(id);
    }
    people.put(id, ScimTypeConverter.fromScim(user));
    return user;
  }

  @Override
  public ScimUser get(String id) {
    return ScimTypeConverter.toScim(people.get(id));
  }

  @Override
  public void delete(String id) {
    people.remove(id);
  }

  @Override
  public FilterResponse<ScimUser> find(Filter filter, PageRequest pageRequest, SortRequest sortRequest) {

    // if there is no filter return all users
    Collection<ExamplePerson> result = filter != null
      ? doFilter(filter.getExpression())
      : people.values();

    return new FilterResponse<>(result.stream()
                                      .map(ScimTypeConverter::toScim)
                                      .collect(Collectors.toList()),
                                pageRequest, people.size());
  }

  @Override
  public List<Class<? extends ScimExtension>> getExtensionList() {
    return Collections.emptyList();
  }

  private Collection<ExamplePerson> doFilter(FilterExpression expression) {

    // This example only supports a `userName eq <something>` filter, but you would translate the filter into what a
    // JPA query or other user store query to return results.

    if (expression instanceof AttributeComparisonExpression) {
      AttributeComparisonExpression comparisonExpression = (AttributeComparisonExpression) expression;

      if ("userName".equals(comparisonExpression.getAttributePath().getAttributeName())
           && CompareOperator.EQ == comparisonExpression.getOperation()) {

        ExamplePerson person = people.get(comparisonExpression.getCompareValue());
        return person != null
          ? Collections.singleton(person)
          : Collections.emptyList();
      }
    }

    throw new ServerErrorException("Only `userName eq *` filters are supported currently", Response.Status.NOT_IMPLEMENTED);
  }
}
