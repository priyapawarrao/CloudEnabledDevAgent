#!/bin/bash

chmod -R 777 /agent/workspace/*
cd /agent/workspace

mvn archetype:generate -B -DarchetypeGroupId=am.ik.archetype -DarchetypeArtifactId=spring-boot-blank-archetype -DarchetypeVersion=1.0.6 -DgroupId=$1 -DartifactId=$2 -Dversion=$3 -Dpackage=$4

chmod -R 755 /agent/workspace



