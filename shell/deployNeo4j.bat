@echo off

call docker run -d -p 7474:7474 -p 7687:7687 -e NEO4J_AUTH=none --name dt-neo4j neo4j