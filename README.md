🎭 Theatre Management System
A Java Swing application for managing theatre operations with MySQL as the database.
🚀 Setup Instructions
1️⃣ Install JDK
Make sure you have Java Development Kit (JDK 8 or higher) installed.
Verify installation:
java -version
2️⃣ Install MySQL Community Edition
Download and install MySQL Community Server.
Create a database and update the connection details in the code (username/password).
3️⃣ Download MySQL Connector
Download the MySQL Connector/J from MySQL Connector/J Downloads.
Extract the ZIP file.
Copy the .jar file (e.g., mysql-connector-j-8.x.x.jar) into your project directory under the lib/ folder.
4️⃣ Update Database Credentials
In your Java code, update the MySQL username and password in the connection string:
connection = DriverManager.getConnection(
    "jdbc:mysql://localhost:3306/your_database_name",
    "your_username",
    "your_password"
);
5️⃣ Compile and Run the Project
If you’re compiling via terminal:
javac -cp ".;lib/mysql-connector-j-8.x.x.jar" TheatreManagementSystem.java
java -cp ".;lib/mysql-connector-j-8.x.x.jar" TheatreManagementSystem
💡 On Linux/Mac, replace ; with : in the classpath.
📌 Features
🎟 Manage shows, bookings, and tickets
🗄 MySQL database integration
🖥 Java Swing UI
🛠 Tech Stack
Java (Swing, JDBC)
MySQL Database
MySQL Connector/J
