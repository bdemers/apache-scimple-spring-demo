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

import com.example.scim.model.ExampleGroup;
import com.example.scim.model.ScimTypeConverter;
import org.apache.directory.scim.server.exception.UnableToCreateResourceException;
import org.apache.directory.scim.server.provider.Provider;
import org.apache.directory.scim.server.provider.UpdateRequest;
import org.apache.directory.scim.spec.protocol.filter.FilterResponse;
import org.apache.directory.scim.spec.protocol.search.Filter;
import org.apache.directory.scim.spec.protocol.search.PageRequest;
import org.apache.directory.scim.spec.protocol.search.SortRequest;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimGroup;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Example SCIM Provider which will map a custom domain model {@link ExampleGroup} to/from SCIM objects.
 */
public class ScimGroupProvider implements Provider<ScimGroup> {

  private Map<String, ExampleGroup> groups = new HashMap<>();
  
  @Override
  public ScimGroup create(ScimGroup group) throws UnableToCreateResourceException {
    String resourceId = group.getId() != null ? group.getId() : UUID.randomUUID().toString();

    // SCIM spec does NOT allow for updating existing objects via create (POST).
    if (groups.containsKey(resourceId)) {
      throw new UnableToCreateResourceException(Response.Status.CONFLICT, "User already exists.");
    }

    groups.put(resourceId, ScimTypeConverter.fromScim(group));
    group.setId(resourceId);
    return group;
  }

  @Override
  public ScimGroup update(UpdateRequest<ScimGroup> updateRequest) {
    String id = updateRequest.getId();
    ScimGroup group = updateRequest.getResource();
    groups.put(id, ScimTypeConverter.fromScim(group));
    return group;
  }

  @Override
  public ScimGroup get(String id) {
    return ScimTypeConverter.toScim(groups.get(id));
  }

  @Override
  public void delete(String id) {
    groups.remove(id);
  }

  @Override
  public FilterResponse<ScimGroup> find(Filter filter, PageRequest pageRequest, SortRequest sortRequest) {
    return new FilterResponse<>(groups.values().stream()
                                               .map(ScimTypeConverter::toScim)
                                               .collect(Collectors.toList()),
                                pageRequest, groups.size());
  }

  @Override
  public List<Class<? extends ScimExtension>> getExtensionList() {
    return Collections.emptyList();
  }

}
