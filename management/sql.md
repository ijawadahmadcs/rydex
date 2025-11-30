MySQL Recommendation
University-friendly: Very common MySQL
Easy to integrate with Java: Excellent, lightweight
MySQL ERD tools (free): Many
Cross-platform: Windows, Linux, Mac
Easy to deploy in Kubernetes: Very easy
Community help: Excellent

CREATE TABLE Users (
user_id INT AUTO_INCREMENT PRIMARY KEY,
name VARCHAR(100) NOT NULL,
email VARCHAR(100) NOT NULL UNIQUE,
password VARCHAR(100) NOT NULL,
user_type ENUM('Driver', 'Rider') NOT NULL
);

CREATE TABLE Vehicles (
vehicle_id INT AUTO_INCREMENT PRIMARY KEY,
model VARCHAR(100) NOT NULL,
plate_number VARCHAR(50) UNIQUE NOT NULL,
capacity INT NOT NULL,
color VARCHAR(50)
);

CREATE TABLE Drivers (
driver_id INT PRIMARY KEY,
license_number VARCHAR(50) UNIQUE NOT NULL,
total_earnings DECIMAL(10,2) DEFAULT 0,
vehicle_id INT UNIQUE,
FOREIGN KEY (driver_id) REFERENCES Users(user_id),
FOREIGN KEY (vehicle_id) REFERENCES Vehicles(vehicle_id) ON DELETE SET NULL
);

CREATE TABLE Riders (
rider_id INT PRIMARY KEY,
balance DECIMAL(10,2) DEFAULT 0,
FOREIGN KEY (rider_id) REFERENCES Users(user_id)
);

CREATE TABLE Routes (
route_id INT AUTO_INCREMENT PRIMARY KEY,
start_location VARCHAR(150) NOT NULL,
end_location VARCHAR(150) NOT NULL,
distance_km DECIMAL(10,2) NOT NULL
);

CREATE TABLE Rides (
ride_id INT AUTO_INCREMENT PRIMARY KEY,
rider_id INT NOT NULL,
driver_id INT NOT NULL,
route_id INT NOT NULL,
fare DECIMAL(10,2) NOT NULL,
status ENUM('Pending', 'Confirmed', 'In Progress', 'Completed', 'Cancelled') NOT NULL DEFAULT 'Pending',
ride_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (rider_id) REFERENCES Riders(rider_id),
FOREIGN KEY (driver_id) REFERENCES Drivers(driver_id),
FOREIGN KEY (route_id) REFERENCES Routes(route_id)
);

CREATE TABLE Payments (
payment_id INT AUTO_INCREMENT PRIMARY KEY,
ride_id INT UNIQUE NOT NULL,
amount DECIMAL(10,2) NOT NULL,
payment_method ENUM('Cash', 'Card', 'Wallet') NOT NULL,
payment_status ENUM('Pending', 'Completed', 'Failed', 'Refunded') NOT NULL DEFAULT 'Pending',
FOREIGN KEY (ride_id) REFERENCES Rides(ride_id)
);

CREATE TABLE Feedback (
feedback_id INT AUTO_INCREMENT PRIMARY KEY,
ride_id INT UNIQUE NOT NULL,
rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
comments TEXT,
FOREIGN KEY (ride_id) REFERENCES Rides(ride_id)
);

CREATE TABLE Driver_Shifts (
shift_id INT AUTO_INCREMENT PRIMARY KEY,
driver_id INT NOT NULL,
shift_date DATE NOT NULL,
start_time TIME NOT NULL,
end_time TIME NOT NULL,
FOREIGN KEY (driver_id) REFERENCES Drivers(driver_id)
);

CREATE TABLE Ride_Assistants (
ride_id INT NOT NULL,
rider_id INT NOT NULL,
assistant_name VARCHAR(100) NOT NULL,
PRIMARY KEY (ride_id, rider_id),
FOREIGN KEY (ride_id) REFERENCES Rides(ride_id),
FOREIGN KEY (rider_id) REFERENCES Riders(rider_id)
);

java -cp ".;libs/mysql-connector-j-9.5.0.jar;management" RideSharingApp
javac -cp ".;libs/mysql-connector-j-9.5.0.jar" management\*.java
