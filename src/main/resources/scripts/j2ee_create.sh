#!/bin/bash

cd /home/pripawar/mavenArchetypes/j2ee

mvn archetype:generate -B -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-j2ee-simple -DarchetypeVersion=1.0 -DgroupId=$1 -DartifactId=$2 -Dversion=$3 -Dpackage=$4




