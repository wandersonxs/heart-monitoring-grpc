CREATE TABLE patient (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        name VARCHAR(100) NOT NULL,
                        age INT NOT NULL,
                        email VARCHAR(100) NOT NULL
);

CREATE TABLE heartbeat (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           patient_id BIGINT NOT NULL,
                           bpm INT NOT NULL, -- beats per minute
                           timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT fk_heartbeat_client FOREIGN KEY (patient_id) REFERENCES patient(id)
);