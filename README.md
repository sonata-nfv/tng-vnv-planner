[![Build Status](https://jenkins.sonata-nfv.eu/buildStatus/icon?job=tng-vnv-planner/master)](https://jenkins.sonata-nfv.eu/job/tng-vnv-planner/master/)

<p align="center"><img src="https://github.com/sonata-nfv/tng-api-gtw/wiki/images/sonata-5gtango-logo-500px.png" /></p>

# Planner component for the Verification and Validation Platform

This is a [5GTANGO](http://www.5gtango.eu) component to coordinate the verification and validation activities of 5G Network Services.

The Planner acts as the main manager for the V&V test requests. It is responsible for test plans management, sequencing, and triggering requests of the corresponding test requests. Although the Planner is responsible for overall test activity, execution of individual test plans is given over to the curator and executor.
An outline of the architecture is shown below.

![Architecture](./src/main/resources/images/planner_architecture.png?raw=true "Architecture")

More information available in the [wiki](https://github.com/sonata-nfv/tng-vnv-planner/wiki) section.

## Installing / Getting Started

This component is implemented in Spring Boot 2.1.3, using Java 11

### Installing from code

Please, do the following:


```bash
$ git clone https://github.com/sonata-nfv/tng-vnv-planner.git # Clone this repository
$ cd tng-vnv-planner
$ gradlew bootRun
```

### Installing from Docker container

```bash
$ git clone https://github.com/sonata-nfv/tng-vnv-planner.git # Clone this repository
$ cd tng-vnv-planner
$ gradlew clean docker
$ docker run -d --name tng-vnv-planner -p 6100:6100 \
    -e  APP_GK_BASE_URL: "{{ app_gk_base_url }}"
    -e  APP_CAT_BASE_URL: "{{ catalogue_url }}"
    -e  DATABASE_URL: "postgres://{{ gtk_db_user }}:{{ gtk_db_pass }}@{{ postgres_db_host }}:{{ database_port }}/{{ planner_db_name }}"
    -e  DATABASE_PASSWORD: "{{ gtk_db_pass }}"
    -e  DATABASE_USER: "{{ gtk_db_user }}"
    -e  DATABASE_NAME: "{{ planner_db_name }}"
    -e  DATABASE_HOST: "{{ postgres_db_host }}"
    -e  DATABASE_PORT: "{{ database_port }}"
    sonatanfv/tng-vnv-planner
```

## Developing

### Built With

We are using the Spring Boot Framework, org.springframework.boot' version '2.1.3.RELEASE' with the next dependencies (mavenCentral):

| Group | Name | Version |
|---|---|---|
|gradle.plugin.com.palantir.gradle.docker|gradle-docker, version|0.21.0
|org.codehaus.groovy|groovy-all|2.5.6
|com.fasterxml.jackson.dataformat|jackson-dataformat-yaml|2.9.8
|org.apache.commons|commons-lang3:3.4
|org.apache.httpcomponents|httpclient|
|org.springframework.boot|spring-boot-starter-web|2.1.3.RELEASE
|org.springframework.boot|spring-boot-starter-data-jpa|2.1.3.RELEASE
|io.springfox|springfox-swagger2|2.9.2
|io.springfox|springfox-swagger-ui|2.9.2
|org.postgresql|postgresql|42.2.5
|com.h2database|h2|1.4.199
|org.spockframework|spock-core|1.2-groovy-2.4
|org.springframework.boot|spring-boot-starter-test
|org.spockframework|spock-spring|1.2-groovy-2.4


### Prerequisites

No specific libraries are required for building this project. The following tools are used to build the component:

- `Java JDK (11+)`
- `gradle (4.9)`
- `docker (18.x)`

### Submiting changes

Changes to the repository can be requested using [this repository's issues](https://github.com/sonata-nfv/tng-vnv-planner/issues) and [pull requests](https://github.com/sonata-nfv/tng-vnv-planner/pulls) mechanisms.

## Versioning

For the versions available, see the [link to tags on this repository](https://github.com/sonata-nfv/tng-vnv-planner/releases).

## Configuration

The configuration of the micro-service is done through the following environment variables, defined in the [Dockerfile](https://github.com/sonata-nfv/tng-vnv-planner/blob/master/src/main/docker/Dockerfile):
* DATABASE_HOST, which defines the host where the planner's son-postgres database is running
* DATABASE_NAME, which defines the database name (default: planner)
* DATABASE_USER, which defines the planner's database user
* DATABASE_PASSWORD, which defines the planner's database password
* ENV DATABASE_PORT, which defines the planner's database port (default: 5432)
* CAT_VNV_HOST, which defines the catalogue host
* CAT_VNV_PORT, which defines the catalogue port
* CURATOR_HOST, which defines the curator host
* CURATOR_PORT, which defines the curator port

## Tests

Unit tests are defined in the /src/test folder. To run these tests:

```bash
$ gradle clean test
```

## Database

Attending to the profile set (docker/test), the planner will use two different driver database types:
* org.h2.Driver, using a internal "In memory" H2 database
* org.postgresql.Driver, using a external postgresql database which planner needs to be linked using the environment variables

## Api Reference

Please, check the API in this swagger file: [swagger.json](https://github.com/sonata-nfv/tng-vnv-planner/blob/master/doc/swagger.json), or visit this [url](https://sonata-nfv.github.io/tng-doc/) and select the "5GTANGO V&V Planner API v1" spec in the drop down menu.

## Licensing

This 5GTANGO component is published under Apache 2.0 license. Please see the [LICENSE](LICENSE) file for more details.

## Lead Developers

The following lead developers are responsible for this repository and have admin rights. They can, for example, merge pull requests.

* Santiago Rodríguez ([srodriguez](https://github.com/srodriguezOPT))

## Feedback-Channel

- You may use the mailing list [sonata-dev-list](mailto:sonata-dev@lists.atosresearch.eu)
- Gitter room [![Gitter](https://badges.gitter.im/sonata-nfv/Lobby.svg)](https://gitter.im/sonata-nfv/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
