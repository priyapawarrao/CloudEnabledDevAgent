#!/bin/bash

cd /home/pripawar/mavenArchetypes/webapp

mvn archetype:generate -B -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-webapp -DarchetypeVersion=1.0 -DgroupId=$1 -DartifactId=$2 -Dversion=$3 -Dpackage=$4




