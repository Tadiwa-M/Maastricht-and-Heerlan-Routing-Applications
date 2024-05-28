# Maastricht Transportation Application

## Overview
The Maastricht Transportation Application is a project developed by [Your Team Name]. It aims to provide users with information on transportation options between two locations within Maastricht, Netherlands. The application allows users to input two postal codes and choose their preferred mode of transportation: Walk, Bike, Car, or Bus. It then calculates and displays the route, estimated time, and distance for the selected transportation mode.

## Features
- **Mode Selection**: Users can choose between Walk, Bike, Car, or Bus as their mode of transportation.
- **Routing**: The application calculates the route between two locations using either straight-line distance or the shortest distance based on a pathfinding algorithm.
- **Bus Information**: For Bus mode, the application retrieves data from a GTFS file containing information about bus routes and schedules. It then queries a server to find the shortest path and provides information on the bus route, stops, and estimated time.
- **Visualization**: The application visualizes the selected route on a map of Maastricht, allowing users to see their journey visually.
- **GUI**: The application is designed with a Graphical User Interface (GUI) for ease of use.

## Installation
To install and run the application, follow these steps:

1. Clone the repository: `git clone https://github.com/Amir-Mohseni/Project1-2.git`
2. Navigate to the project directory: `cd Project1-2.src`
3. Run the Maven installation script based on your operating system:
    - For Unix/Linux: `./maven_script.sh`
    - For Windows: `maven_script.bat`
4. If you want to populate your own database, use the Maastricht GTFS folder which contains the GTFS for only Maastricht using CSV files.

## Usage
1. Open the project in your preferred Java IDE.
2. Run the `GUI.java` file to launch the application.
3. Enter two postal codes for the starting and destination locations.
4. Choose your preferred mode of transportation.
5. Click on "Calculate Route" to view the route, estimated time, and distance.
6. Optionally, visualize the route on the map.

### Application Screenshot
![Maastricht Transportation Application](/path/to/application/screenshot.png)

## Dependencies
- Java Development Kit (JDK)
- Maven
- Python (for preprocessing GTFS file)
- Pandas (Python library)

## License
This project is licensed under the MIT License.

## Screenshots
![Screenshot 1](/path/to/screenshot1.png)
![Screenshot 2](/path/to/screenshot2.png)
