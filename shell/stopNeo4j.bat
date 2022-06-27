@echo off

call docker stop dt-neo4j > NUL
call docker container rm dt-neo4j > NUL