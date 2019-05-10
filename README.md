[![Build Status](http://jenkins.sonata-nfv.eu/buildStatus/icon?job=tng-vnv-executor/master)](https://jenkins.sonata-nfv.eu/job/tng-vnv-executor)


# Planner for 5GTANGO Verification and Validation
This is a [5GTANGO](http://www.5gtango.eu) component to execute the Verification and Validation Tests

## What it is

The Executor module is responsible for executing the Verification and Validation tests requested by the [Curator](https://github.com/sonata-nfv/tng-vnv-curator) component.

It receives a test request with the associated descriptor file (Test Descriptor), a file that contains the test configurations, dependencies, validation and verification conditions, etc. With this information, Executor generates a docker-compose.yaml file and executes the tests sequence with docker-compose tools.

Once tests are finished, Executor check validation and verification conditions, stores the results in the V&V repository and generates a "Completion Test Response" to Curator component.

Please, visit the associated [wiki](https://github.com/sonata-nfv/tng-vnv-executor/wiki) to obtain more information (architecture, workflow, examples, REST Api, etc)

## Build from source code

This will generate a docker image with the latest version of the code. Before building, a test suite is executed.

```bash
./gradlew clean test build docker
```

## Run the docker image

```bash
 docker run -d -it --name tng-vnv-executor \
 -e CALLBACKS='enabled' \
 -e DELETE_FINISHED_TEST='disabled' \
 -e RESULTS_REPO_URL=<RESULTS_REPO_URL> \
 -v /var/run/docker.sock:/var/run/docker.sock \
 -v /usr/bin/docker:/usr/bin/docker \
 -v /executor:/executor \
 -p 6300:8080 \
 --network tango \
 registry.sonata-nfv.eu:5000/tng-vnv-executor:latest
```

where:
- CALLBACKS: optional environment variable to enable/disable the callbacks from executor to curator
  - values: enabled/disabled
  - default: enabled
- DELETE_FINISHED_TESTS: by default, the generated probes' output files are deleted from VnV Executor host once tests are completed and the results are stored in the repo. For developing purposes. This optional environment variable can maintain these files in the output folders without deletion when is disabled.
  - values: enabled/disabled
  - default: enabled
- RESULTS_REPO_URL: mandatory environment variable that contains the tests results repository http://host:port URL where the results will be stored
- port: internally, the VnV executor uses the 8080 port. This port can be mapped to another desired port. In 5GTango environments the selected port is 6300 
- network: network where all VnV components (planner, curator, platform adaptor and executor) are configured

### Health checking

Once started, you can check the health endpoint at

http://<server>:<port>/actuator/health

### Swagger UI

Swagger UI can be accessed at

http://server:port/swagger-ui.html

## Dependencies

- `Java JDK (10+)`
- `gradle`
- `docker`
- `Spring Boot (2.1.3)`
- `Groovy (2.5.6)`
- `Swagger (2.9.2)`

## Contributing
Contributing to the V&V Executor is really easy. You must:

1. Clone [this repository](http://github.com/sonata-nfv/tng-vnv-executor);
1. Work on your proposed changes, preferably through submiting [issues](https://github.com/sonata-nfv/tng-vnv-executor/issues);
1. Submit a Pull Request;
1. Follow/answer related [issues](https://github.com/sonata-nfv/tng-vnv-executor/issues) (see Feedback, below).

## License
This 5GTANGO component is published under Apache 2.0 license. Please see the [LICENSE](LICENSE) file for more details.

## Lead Developers

The following lead developers are responsible for this repository and have admin rights. They can, for example, merge pull requests.

* Santiago Rodríguez ([srodriguezOPT](https://github.com/srodriguezOPT))
* Laura Álvarez ([LauraAnt](https://github.com/LauraAnt))
* Felipe Vicens ([felipevicens](https://github.com/felipevicens))
* José Bonnet ([jbonnet](https://github.com/jbonnet))

## Feedback
Please use the [GitHub issues](https://github.com/sonata-nfv/tng-vnv-executor/issues) and the 5GTANGO Verification and Validation group mailing list `5gtango-dev@list.atosresearch.eu` for feedback.