package com.example.scim.model;

import org.apache.directory.scim.spec.resources.Address;
import org.apache.directory.scim.spec.resources.Email;
import org.apache.directory.scim.spec.resources.Name;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.schema.ResourceReference;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

/**
 * Type conversion utility converts between SCIM and {@code Example*} classes.
 */
public final class ScimTypeConverter {

    private ScimTypeConverter() {}


    private static Address toScim(ExampleAddress exampleAddress) {

        if (exampleAddress == null) {
            return null;
        }

        Address address = new Address();
        address.setStreetAddress(exampleAddress.getStreet());
        address.setLocality(exampleAddress.getCity());
        address.setRegion(exampleAddress.getState());
        address.setPostalCode(exampleAddress.getZip());
        address.setCountry(exampleAddress.getCountry());
        return address;
    }

    private static ExampleAddress fromScim(Address address) {

        if (address == null) {
            return null;
        }

        return new ExampleAddress()
                .setStreet(address.getStreetAddress())
                .setCity(address.getLocality())
                .setState(address.getRegion())
                .setZip(address.getPostalCode())
                .setCountry(address.getCountry());
    }

    private static String fromScim(Email email) {
        return email != null ? email.getValue() : null;
    }

    private static Email toScim(String emailAddress) {
        if (!StringUtils.hasText(emailAddress)) {
            return null;
        }

        Email email = new Email();
        email.setValue(emailAddress);
        return email;
    }

    public static ScimUser toScim(ExamplePerson person) {

        if (person == null) {
            return null;
        }

        ScimUser user = new ScimUser();
        user.setId(person.getUsername());
        user.setUserName(person.getUsername());
        user.setActive(true);

        String formatted = person.getLastName() + ", " + person.getFirstName();
        Name name = new Name();
        name.setGivenName(person.getFirstName());
        name.setMiddleName(person.getMiddleName());
        name.setFamilyName(person.getLastName());
        name.setFormatted(formatted);
        user.setName(name);
        user.setDisplayName(formatted);

        if (person.getEmails() != null) {
            user.setEmails(person.getEmails().stream()
                    .map(ScimTypeConverter::toScim)
                    .collect(Collectors.toList()));
        }

        if (person.getAddresses() != null) {
            user.setAddresses(person.getAddresses().stream()
                    .map(ScimTypeConverter::toScim)
                    .collect(Collectors.toList()));
        }

        return user;
    }

    public static ExamplePerson fromScim(ScimUser user) {

        if (user == null) {
            return null;
        }

        ExamplePerson person = new ExamplePerson()
                .setUsername(user.getUserName());

        if (user.getName() != null) {
            Name name = user.getName();
            person.setFirstName(name.getGivenName())
                  .setMiddleName(name.getMiddleName())
                  .setLastName(name.getFamilyName());
        }

        if (user.getEmails() != null) {
            person.setEmails(user.getEmails().stream()
                    .map(ScimTypeConverter::fromScim)
                    .collect(Collectors.toList()));
        }

        if (user.getAddresses() != null) {
            person.setAddresses(user.getAddresses().stream()
                    .map(ScimTypeConverter::fromScim)
                    .collect(Collectors.toList()));
        }

        return person;
    }

    public static ScimGroup toScim(ExampleGroup exampleGroup) {

        if (exampleGroup == null) {
            return null;
        }

        ScimGroup group = new ScimGroup();
        group.setId(exampleGroup.getId());
        group.setDisplayName(exampleGroup.getDescription());

        if (exampleGroup.getMemberIds() != null) {
            group.setMembers(exampleGroup.getMemberIds().stream()
                    .map(id -> {
                        ResourceReference ref = new ResourceReference();
                        ref.setValue(id);
                        return ref;
                    })
                    .collect(Collectors.toList()));
        }

        return group;
    }

    public static ExampleGroup fromScim(ScimGroup group) {

        if (group == null) {
            return null;
        }

        ExampleGroup exampleGroup = new ExampleGroup()
                .setId(group.getId())
                .setDescription(group.getDisplayName());
        if (group.getMembers() != null) {

            System.out.println(group.getMembers());

            exampleGroup.setMemberIds( group.getMembers().stream()
                    .map(wtf -> {
                        System.out.println(wtf.getValue());
                        return wtf.getValue();
                    })
                    .collect(Collectors.toList()));
        }
        return exampleGroup;
    }
}
