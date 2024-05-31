# Use the Centos image as the base image.
FROM centos

# Metadata indicating an image maintainer.
LABEL maintainer="nroberts@dignitastechnologies.com"

# Allow yum to be used to install applications in the Centos image
RUN cd /etc/yum.repos.d/
RUN sed -i 's/mirrorlist/#mirrorlist/g' /etc/yum.repos.d/CentOS-*
RUN sed -i 's|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-*

# Install dependencies for protoc and the military symbology service
RUN yum -y install libatomic
RUN yum -y install fontconfig

# Copy GIFT into the docker image
COPY . /work/

# Copy the launch script into the docker image
COPY ./GIFT/scripts/docker/launchMultiProcess /work/

# Switch the container's base directory to where the files were copied
WORKDIR /work

# Run GIFT's Linux installer.
#
# This can be sped up by building or installing on the host machine first, since already installed files are preserved
RUN /work/GIFT/scripts/dev-tools/Linux/installGIFT

# Have the container automatically launch GIFT when it starts up
CMD ["/work/launchMultiProcess"]