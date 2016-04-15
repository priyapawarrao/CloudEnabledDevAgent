#!/bin/bash

cd /home/pripawar/mavenArchetypes/simple

mvn archetype:generate -B -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.1 -DgroupId=$1 -DartifactId=$2 -Dversion=$3 -Dpackage=$4
