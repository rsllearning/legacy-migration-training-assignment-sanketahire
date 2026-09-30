DROP TABLE IF EXISTS posts;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INT PRIMARY KEY,
    username VARCHAR(50),
    follower_count INT,
    is_private BOOLEAN
);

CREATE TABLE posts (
    id INT PRIMARY KEY,
    user_id INT,
    content VARCHAR(255),
    created_at BIGINT,
    is_deleted BOOLEAN,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
