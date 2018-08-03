package com.example.scim.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Example Person model, used to demonstrate converting between a SCIM model and a custom domain model.
 */
@Data
@Accessors(chain = true)
public class ExamplePerson {

    private String username;

    private String firstName;

    private String middleName;

    private String lastName;

    private List<String> emails;

    private List<ExampleAddress> addresses;

}
