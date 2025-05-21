package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;

public class CdkCursoApp {
    public static void main(final String[] args) {
        App app = new App();
        VpcStack vpc = new VpcStack(app, "VPC");

        ClusterStack cluster = new ClusterStack(app, "Cluster", vpc.getVpc());

        cluster.addDependency(vpc);

        RdsMySqlStack mySqlStack = new RdsMySqlStack(app, "Rds", vpc.getVpc());
        mySqlStack.addDependency(vpc);

        Service01Stack service01 = new Service01Stack(app, "Service01", cluster.getCluster());

        service01.addDependency(cluster);
        service01.addDependency(mySqlStack);
        app.synth();
    }
}

