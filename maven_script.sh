#!/bin/bash

# Refresh Maven project in IntelliJ IDEA
echo "Refreshing Maven project..."
mvn -U idea:idea

# Clean the project
echo "Cleaning project..."
mvn clean

# Install dependencies
echo "Installing dependencies..."
mvn install

# Build the project
echo "Building project..."
mvn package

echo "Done."
