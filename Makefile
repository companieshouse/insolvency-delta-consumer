artifact_name       := insolvency-delta-consumer
version             := unversioned

## Create help from comments in Makefile
help:
	@printf "%-20s %s\n" "Target" "Description"
	@printf "%-20s %s\n" "------" "-----------"
	@make -pqR : 2>/dev/null \
        | awk -v RS= -F: '{if ($$1 !~ "^[#.]") {print $$1}}' \
        | sort \
        | egrep -v -e '^[^[:alnum:]]' -e '^$@$$' \
        | xargs -I _ sh -c 'printf "%-20s " _; make _ -nB | (grep -i "^# Help:" || echo "") | tail -1 | sed "s/^# Help: //g"'

.PHONY: all
all:
	@# Help: Calls methods required to build a locally runnable version, typically the build target
	mvn clean install

.PHONY: clean
clean:
	@# Help: Reset repo to pre-build state (i.e. a clean checkout state)
	mvn clean 

.PHONY: build
build:
	@# Help: Pull down any dependencies and compile code into an executable if required
	mvn package -Dmaven.test.skip=true
	cp ./target/$(artifact_name)-$(version).jar ./target/$(artifact_name).jar

.PHONY: test
test: test-integration test-unit 
	@# Help: Run all test-* targets (convenience method for developers)

.PHONY: test-unit
test-unit:
	@# Help: Run unit tests
	mvn test -Dskip.integration.tests=true

.PHONY: test-integration
test-integration:
	@# Help: Run integration tests
	mvn integration-test -Dskip.unit.tests=true

.PHONY: package
package:
	@# Help: Create a single versioned deployable package (i.e. jar, zip, tar, etc.). May be dependent on the build target being run before package
	mvn clean install

.PHONY: sonar
sonar:
	@# Help: Run sonar scan
	mvn sonar:sonar

.PHONY: deps
deps:
	@# Help: Install dependencies
	brew install kafka

.PHONY: docker/kafka
docker/kafka: docker/kafka-start docker/kafka-create-topics
	@# Help: Run kafka and create topics within docker

.PHONY: docker/kafka-start
docker/kafka-start:
	@# Help: Run kafka within docker
	tilt up
	sleep 15

.PHONY: docker/kafka-create-topics
docker/kafka-create-topics:
	@# Help: Create kafka topics within docker
	@kafka-topics --create   --bootstrap-server localhost:29092   --replication-factor 1 --partitions 1   --topic insolvency-delta
	@kafka-topics --create   --bootstrap-server localhost:29092   --replication-factor 1 --partitions 1   --topic insolvency-delta-retry
	@kafka-topics --create   --bootstrap-server localhost:29092   --replication-factor 1 --partitions 1   --topic insolvency-delta-error
	@printf "\nKafka Topics:\n\n"
	@kafka-topics --list --bootstrap-server localhost:29092

.PHONY: docker/kafka-stop
docker/kafka-stop:
	@# Help: Stop kafka within docker
	tilt down

.PHONY: lint
lint: lint/docker-compose sonar
	@# Help: Run all lint/* targets and sonar

.PHONY: lint/docker-compose
lint/docker-compose:
	@# Help: Lint docker file
	docker-compose -f docker-compose.yml config


