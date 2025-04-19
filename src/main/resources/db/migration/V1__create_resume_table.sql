CREATE TABLE resume (
                        id TEXT PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        email VARCHAR(255) NOT NULL,
                        education TEXT,
                        experience TEXT,
                        skills TEXT,
                        publications TEXT,
                        awards TEXT,
                        extra_title VARCHAR(255),
                        extra_points TEXT
);