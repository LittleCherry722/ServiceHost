use sbpm_groupware;

TRUNCATE TABLE groups;
TRUNCATE TABLE users;
TRUNCATE TABLE users_x_groups;

INSERT INTO `users` (`ID`, `name`) VALUES
(1, 'Admin'),
(2, 'jhartwig'),
(3, 'mschrammek'),
(4, 'aroeder'),
(5, 'sborgert'),
(6, 'pbeyer');