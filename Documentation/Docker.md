
# GIFT Dockerization

## Prerequisites

Before getting started, ensure you have the following:
- A Linux machine or Virtual Machine (VM) with x86_64 (Intel) architecture.
- Docker
- Access to Docker Hub.

## Quick Start: Pull and Run Pre-built Docker Image

You can directly pull the Docker image that I built(@jvaida) from Docker Hub and run it.

### 1. Pull the GIFT Docker Image
Pull the pre-built Docker image from Docker Hub:

```bash
docker pull jvaida/gift2023
```

### 2. Run the Docker Container
Run the container using the following command:

```bash
docker run -d --name gift-container -p 8080:8080 jvaida/gift2023
```

The -p maps port 8080 of the container to port 8080 of your the machine.

### 3. Verify the Container is Running
Check if the container is running:

```bash
docker ps
```

You should see `gift-container` in the list of running containers.

### 4. Access GIFT
Once the container is running, you can access GIFT through `http://localhost:8080`.

## Building Your Own Docker Image

If you prefer to build your own Docker image, GIFT provides a Dockerfile.

### 1. Configuring the Gateway Module
Before running the container, configure the `GIFT\config\common.properties` file by setting the `DomainContentServerHost` variable to `localhost`:

```properties
DomainContentServerHost=localhost
```

This ensures that the Gateway module connects correctly in a monolithic setup.

### 2. Build the Docker Image
Navigate to the directory containing GIFT’s Dockerfile, located at `GIFT/scripts/docker/Dockerfile`, and build the image:

```bash
docker build -t <dockerhub-username>/gift2023 -f GIFT/scripts/docker/Dockerfile .
```

Replace `<dockerhub-username>` with an actual Docker Hub username.

### 3. Push the Docker Image to Docker Hub
If you want to push your own GIFT image to Docker Hub:

```bash
docker login
docker push <dockerhub-username>/gift2023
```

### 4. Run the Docker Container
Run the container using your custom image:

```bash
docker run -d --name gift-container -p 8080:8080 <dockerhub-username>/gift2023
```

This should start GIFT inside the container, accessible on your host machine at localhost:8080

## Troubleshooting

### Gateway Module Exception
If you encounter an error like "Gateway module threw an exception," ensure that:
- `DomainContentServerHost` is set to `localhost` in the `common.properties` file.

### Docker Image Build Issues
If the image fails to build, make sure:
- You are in the correct directory containing GIFT’s Dockerfile.
- All necessary files and dependencies are available for the build.
