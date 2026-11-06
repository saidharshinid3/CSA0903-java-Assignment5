
CREATE DATABASE transport_db;

USE transport_db;

CREATE TABLE users (
    id VARCHAR(50) PRIMARY KEY,
    pwd VARCHAR(100) NOT NULL
);

INSERT INTO users VALUES
('student1', 'pass123'),
('student2', 'pass456');
