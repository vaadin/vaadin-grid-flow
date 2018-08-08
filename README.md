# Grid component for Vaadin Flow

This project is the Component wrapper implementation of [`<vaadin-grid>`](https://github.com/vaadin/vaadin-grid) element
for use from the server side with [Vaadin Flow](https://github.com/vaadin/flow).

This branch is the latest version that will at some point be released in the [Vaadin platform](https://github.com/vaadin/platform). See other branches for other Flow / Vaadin platform versions:
* 1.0 branch is Vaadin 10 LTS version (Flow version 1.0)
* 1.1 branch is Vaadin 10 compatible with TreeGrid (Flow version 1.0, but no LTS support)

## Running the component demo
Run from the command line:
- `mvn jetty:run -PrunTests`

Then navigate to `http://localhost:9998/vaadin-grid`

## Installing the component
Run from the command line:
- `mvn clean install -DskipTests`

## Using the component in a Flow application
To use the component in an application using maven,
add the following dependency to your `pom.xml`:
```
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-grid-flow</artifactId>
    <version>${component.version}</version>
</dependency>
```

## Flow documentation
Documentation for Flow can be found on [vaadin.com website](https://vaadin.com/docs/v10/flow/Overview.html) or on [GitHub](https://github.com/vaadin/flow-and-components-documentation).

## Contributing
- All contributions should be made for `master` branch, from where those will be picked into any platform LTS versions if necessary.
- Use the coding conventions from [Flow coding conventions](https://github.com/vaadin/flow/tree/master/eclipse)
- [Submit a pull request](https://www.digitalocean.com/community/tutorials/how-to-create-a-pull-request-on-github) with detailed title and description
- Wait for response from one of Vaadin Flow team members

## License
Apache License 2.0
