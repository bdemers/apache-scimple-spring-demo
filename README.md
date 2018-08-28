Apache SCIMple Example using Spring
===================================

This example is a self contained SCIM server using Apache SCIMple.

To run, clone the project and run `mvn`!

This service uses basic authentication username:password. By default the password is unset, and will appear in the console after starting, similar to:

```txt
Using generated security password: your-generated-password-here
```

To set a static password, add the following line to your `application.properties`

```properties
spring.security.user.password=Foobar1 
```

To keep things simple, all objects are stored in-memory, but objects are translated between SCIM models and Example* models to represent _real word_ usage.   
