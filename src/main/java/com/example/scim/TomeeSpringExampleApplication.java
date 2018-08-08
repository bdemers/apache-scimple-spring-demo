package com.example.scim;

import com.example.scim.scimple.workaround.NotImplementedSelfIdResolver;
import org.apache.directory.scim.server.rest.ScimResourceHelper;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * Starts an embedded Tomee server (no WAR file needed!).
 */
@ApplicationPath("v2")
public class TomeeSpringExampleApplication extends Application {

  public static void main(String[] args) {
    org.apache.tomee.embedded.Main.main(args);
  }

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<>(ScimResourceHelper.getScimClassesToLoad());
    classes.add(NotImplementedSelfIdResolver.class);
    return classes;
  }
}
