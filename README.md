# nexus-plugins

Custom build of Sonatype Nexus. Custom Nexus Repository build includes plugins, and components.

## Usage

### Building with Maven

```
# mvn clean install
```

### Building Docker Image

After, building with maven, use:

```
# ./build.sh -d
```

The above will create a docker image and upload to ECR.

### Running Locally

After, building with maven, use:

```
# ./target/nexus-template-0.1.0-SNAPSHOT/bin/nexus run
```

### Docker Execution

After, building with maven, use:

```
~/s/g/l/nexus git:master ❯❯❯ docker images
REPOSITORY                                                        TAG                 IMAGE ID            CREATED             SIZE
r351574nc3/nexus-chart       0.1.0               f6d231613ff6        3 hours ago         502 MB

```

This displays images that are available. You will see that there is a new `nexus-chart` image available. Then, execute:

```
# docker run -p 8081:8081 r351574nc3/nexus-chart:0.1.0
```

## Plugin Development

Instructions for adding/maintaining plugins within the project.

### Create new Plugin

### Update `coreui-plugin`
