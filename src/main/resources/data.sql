INSERT INTO users (id, username, follower_count, is_private) VALUES
(1, 'alice', 500, false),
(2, 'bob_private', 10, true),
(3, 'charlie_no_posts', 0, false);

-- Alice has 4 posts total. 1 is deleted. 
-- Legacy feed limits to 2 posts, ordered by created_at DESC.
INSERT INTO posts (id, user_id, content, created_at, is_deleted) VALUES
(101, 1, 'First post ever', 1700000000, false),
(102, 1, 'Having a great day', 1700000010, false),
(103, 1, 'This is a secret I will delete', 1700000020, true),
(104, 1, 'Wow, learning to code is fun', 1700000030, false);
