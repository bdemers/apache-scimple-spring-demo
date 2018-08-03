package com.example.scim.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Example Address model, used to demonstrate converting between a SCIM model and a custom domain model.
 */
@Data
@Accessors(chain = true)
public class ExampleAddress {

    private String street;

    private String city;

    private String state;

    private String zip;

    private String country;
}
