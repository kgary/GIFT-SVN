@ECHO OFF

REM Navigate to the base of the GIFT checkout. This allows the Dockerfile to grab files from the GIFT folder.
cd %~dp0\..\..\..

echo Stopping existing container (if any).
docker stop gift

REM Remove the existing docker container if there is one. This allows the image to be removed.
docker rm gift

REM Remove the previously built-image, if there is one. If this is not done, then <none> images will be left
REM behind whenever the build completes, gradually consuming disk space.
docker image rm gift-image

REM Build a Docker image named "gift-image" using the Dockerfile. This will install and build GIFT in the image's
REM Linux-based environment if necessary.
REM 
REM The gift build log is too long to display in the build window. 
REM To log externally for debugging purposes set LOG to true.
set LOGGING=false

REM buildx is needed to change the maximum log size, otherwise the build output will likely exceed Docker build's
REM default buffer size.
REM 
REM Oddly, 2> is necessary to output to file instead of just > , since docker build seems to print 
REM build logs to stderr instead of stdout
set DOCKER_SCRIPTS=GIFT/scripts/docker

if %LOGGING% == true (
	echo Building new image. This process can take up to 30 minutes.
	echo For detailed build output and progress, view the build.log file.

	docker buildx create --use --name larger_log --driver-opt env.BUILDKIT_STEP_LOG_MAX_SIZE=-1 --driver-opt env.BUILDKIT_STEP_LOG_MAX_SPEED=-1
	docker buildx build --load --progress=plain -t gift-image -f %DOCKER_SCRIPTS%/Dockerfile . 2> %DOCKER_SCRIPTS%/build.log
) else (
	docker buildx create --use --name larger_log --driver-opt env.BUILDKIT_STEP_LOG_MAX_SIZE=-1
	docker buildx build --load --progress=plain -t gift-image -f %DOCKER_SCRIPTS%/Dockerfile .
)
REM If errors occur, pause so the user can see them
if %ERRORLEVEL% GTR 0 ( IF %ERRORLEVEL% NEQ 0 pause )