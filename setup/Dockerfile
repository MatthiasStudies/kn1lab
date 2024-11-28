# Start with the official Debian image
FROM debian:latest

# Disable interactive mode
ARG DEBIAN_FRONTEND=noninteractive

# Set environment variables
ARG SSH_PUB_KEY

# Update and install required packages
RUN apt-get update && apt-get install -y \
  sudo \
  git \
  openssh-server

# Add labrat user
RUN useradd -m -s /bin/bash -G sudo labrat -p "$(openssl passwd -1 kn1lab)" -d /home/labrat

# Set up SSH
RUN mkdir -p /home/labrat/.ssh
RUN echo "$SSH_PUB_KEY" > /home/labrat/.ssh/authorized_keys
RUN chown -R labrat:labrat /home/labrat/.ssh
RUN chmod 600 /home/labrat/.ssh/authorized_keys

# Clone repositories
USER labrat
RUN git clone https://github.com/owaldhorst-hka/CPUnetPLOT /home/labrat/CPUnetPLOT
RUN git clone https://github.com/owaldhorst-hka/kn1lab /home/labrat/kn1lab

# Create Maildir directories with appropriate permissions
RUN mkdir -m 777 /home/labrat/Maildir && \
  mkdir -m 777 /home/labrat/Maildir/new && \
  mkdir -m 777 /home/labrat/Maildir/cur && \
  mkdir -m 777 /home/labrat/Maildir/tmp

# Set working directory
WORKDIR /home/labrat

# Switch back to root user for final configurations
USER root

# Idle command
CMD ["sleep", "infinity"]
