@echo off

call docker run -d -p 6379:6379 --name dt-redis redis