package com.example.scim.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Example Group model, used to demonstrate converting between a SCIM model and a custom domain model.
 */
@Data
@Accessors(chain = true)
public class ExampleGroup {

    private String id;

    private String description;
}
