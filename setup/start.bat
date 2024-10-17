@echo off
docker compose up -d
docker exec -it setup-kn1lab-1 bash
docker compse down
echo Press enter to exit...
set /p input=
