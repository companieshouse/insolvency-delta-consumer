insolvency-delta-consumer
===========================

insolvency-delta-consumer is responsible for transforming insolvency data from the insolvency-delta kafka as part of chips and chs data sync

## Development

Common commands used for development and running locally can be found in the Makefile, each make target has a 
description which can be listed by running `make help`

```text
Target               Description
------               -----------
all                  Calls methods required to build a locally runnable version, typically the build target
build                Pull down any dependencies and compile code into an executable if required
clean                Reset repo to pre-build state (i.e. a clean checkout state)
deps                 Install dependencies
docker/kafka         Run kafka and create topics within docker
docker/kafka-create-topics Create kafka topics within docker
docker/kafka-start   Run kafka within docker
docker/kafka-stop    Stop kafka within docker
package              Create a single versioned deployable package (i.e. jar, zip, tar, etc.). May be dependent on the build target being run before package
sonar                Run sonar scan
test                 Run all test-* targets (convenience method for developers)
test-integration     Run integration tests
test-unit            Run unit tests

```
## Running kafka locally
From root folder of this project run ```docker-compose up -d```

Once containers up, run ```docker-compose exec kafka bash``` to enter kafka bash to create topics

### Create kafka topics locally
kafka-topics.sh --create   --zookeeper zookeeper:2181   --replication-factor 1 --partitions 1   --topic insolvency-delta

kafka-topics --create --topic insolvency-delta --bootstrap-server localhost:9092

kafka-topics.sh --create   --zookeeper zookeeper:2181   --replication-factor 1 --partitions 1   --topic retry-topic

kafka-topics.sh --create   --zookeeper zookeeper:2181   --replication-factor 1 --partitions 1   --topic error-topic

### Create kafka topics locally
kafka-topics.sh --list --zookeeper zookeeper:2181

### Produce kafka test messages locally
kafka-console-producer.sh --topic delta-topic --broker-list localhost:9092

#
## Terraform ECS

### What does this code do?

The code present in this repository is used to define and deploy a dockerised container in AWS ECS.
This is done by calling a [module](https://github.com/companieshouse/terraform-modules/tree/main/aws/ecs) from terraform-modules. Application specific attributes are injected and the service is then deployed using Terraform via the CICD platform 'Concourse'.

The __insolvency-delta-consumer__ service also includes (as a pipeline resource) a kafka-error-consumer image called here [kafka-error-release-tag](https://github.com/companieshouse/ci-pipelines/blob/7e0cfd7c9db47d0323e87f0956549796ef12d5a7/pipelines/ssplatform/team-development/insolvency-delta-consumer#L1274), which is used to deploy a corresponding kafka-error service alongside this delta-consumer service. This is done via terraform in an additional ecs-service-kafka-error module, within the insolvency-delta-consumer terraform code.

The operation of this task has been scheduled to run for a period of 30 minutes from 00:00 to 00:30, Monday through Friday. During this time, the __kafka-error-consumer task__ will process existing errors in the queue, which will then appear in the AWS console application logs. Once processed, the task will disconnect until the next scheduled period. 

If you wish to change this cron established period, you must modify it in the environment variables file of the service: 

__terraform/groups/ecs-service/profiles/live-eu-west-2/live/vars__

__terraform/groups/ecs-service/profiles/staging-eu-west-2/staging/vars__

```

# Scheduler configuration (for kafka error consumer)
enable_scale_up_eventbridge_scheduler     = true
enable_scale_down_eventbridge_scheduler   = true
startup_eventbridge_scheduler_cron        = "cron(0 0 ? * MON-FRI)"
shutdown_eventbridge_scheduler_cron       = "cron(30 0 ? * MON-FRI)"

```
#


Application specific attributes | Value                                | Description
:---------|:-----------------------------------------------------------------------------|:-----------
**ECS Cluster**        |data-sync                                      | ECS cluster (stack) the service belongs to
**Load balancer**      |N/A consumer service                                           | The load balancer that sits in front of the service
**Concourse pipeline**     |[Pipeline link](https://ci-platform.companieshouse.gov.uk/teams/team-development/pipelines/insolvency-delta-consumer) <br> [Pipeline code](https://github.com/companieshouse/ci-pipelines/blob/master/pipelines/ssplatform/team-development/insolvency-delta-consumer)                                  | Concourse pipeline link in shared services


### Contributing
- Please refer to the [ECS Development and Infrastructure Documentation](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/4390649858/Copy+of+ECS+Development+and+Infrastructure+Documentation+Updated) for detailed information on the infrastructure being deployed.

### Testing
- Ensure the terraform runner local plan executes without issues. For information on terraform runners please see the [Terraform Runner Quickstart guide](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/1694236886/Terraform+Runner+Quickstart).
- If you encounter any issues or have questions, reach out to the team on the **#platform** slack channel.

### Vault Configuration Updates
- Any secrets required for this service will be stored in Vault. For any updates to the Vault configuration, please consult with the **#platform** team and submit a workflow request.

### Useful Links
- [ECS service config dev repository](https://github.com/companieshouse/ecs-service-configs-dev)
- [ECS service config production repository](https://github.com/companieshouse/ecs-service-configs-production)
