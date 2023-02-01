DROP TABLE IF EXISTS cities;

CREATE TABLE cities (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   title VARCHAR(255) NOT NULL,
                                   image_location VARCHAR(1000) NOT NULL,
                                   status VARCHAR(255) NOT NULL
);
