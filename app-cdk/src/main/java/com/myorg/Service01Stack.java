package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;

import java.util.HashMap;
import java.util.Map;

public class Service01Stack extends Stack {
    public Service01Stack(final Construct scope, final String id, Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public Service01Stack(final Construct scope, final String id, final StackProps props, Cluster cluster) {
        super(scope, id, props);
        String urlBanco = "jdbc:mysql://%s:3306/aws-project01-db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
       // String urlBanco = "jdbc:mysql://%s:3306/aws-project01?createDatabaseIfNotExist=true";
        Map<String, String> envVariables = new HashMap<>();
        envVariables.put("SPRING_DATASOURCE_URL",
                //String.format("jdbc:mariadb://%s:3306/aws_project01?createDatabaseIfNotExist=true",
                        String.format(urlBanco,
                        Fn.importValue("rds-endpoint"))
        );
        envVariables.put("SPRING_DATASOURCE_USERNAME", "admin");
        envVariables.put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("rds-password"));


        ApplicationLoadBalancedFargateService service01 = ApplicationLoadBalancedFargateService.Builder.create(this, "ABB01")
                .serviceName("service-01")
                .cluster(cluster)
                .cpu(512)
                .desiredCount(2)
                .listenerPort(8080)
                .memoryLimitMiB(1024)
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .containerName("aws-project01")
                                .image(ContainerImage.fromRegistry("gilmaresende/awsjava:0.0.3"))
                                .containerPort(8080)
                                .logDriver(LogDriver.awsLogs(
                                                AwsLogDriverProps.builder().logGroup(
                                                        LogGroup.Builder.create(this, "Service01LogGroup")
                                                                .logGroupName("Service01")
                                                                .removalPolicy(RemovalPolicy.DESTROY)
                                                                .build()
                                                ).streamPrefix("Service01").build()
                                        )
                                )
                                .environment(envVariables)
                                .build()
                )
                .publicLoadBalancer(true)
                .build();

        service01.getTargetGroup().configureHealthCheck(new HealthCheck.Builder()
                .path("/actuator/health")
                .port("8080")
                .healthyHttpCodes("200")
                .build());

        ScalableTaskCount scalableTaskCount = service01.getService().autoScaleTaskCount(EnableScalingProps.builder()
                .minCapacity(2)
                .maxCapacity(4)
                .build());

        scalableTaskCount.scaleOnCpuUtilization("Service01AutoScaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(50)
                .scaleInCooldown(Duration.seconds(60))
                .scaleOutCooldown(Duration.seconds(60))
                .build());

    }
}
