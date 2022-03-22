@echo off

call docker stop dt-redis > NUL
call docker container rm dt-redis > NUL