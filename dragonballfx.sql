USE dragonballfx;

CREATE TABLE Characters (
    id INT PRIMARY KEY AUTO_INCREMENT,
    characterName VARCHAR(50) NOT NULL,
    damage INT NOT NULL,
    defense INT NOT NULL,
    speed FLOAT NOT NULL
);

INSERT INTO Characters (characterName, damage, defense, speed) VALUES 
('Goku_SSJ', 5, 2, 1.0),
('Vegeta_SSJ', 3, 2, 1.5),
('Gohan_SSJ2', 5, 1, 1.5),
('Piccolo', 3, 1, 2),
('Trunks_SSJ', 3, 2, 1.5),
('Gotenks_SSJ3', 5, 0, 2.0),
('Freezer', 4, 1, 1.5),
('Perfect_Cell', 3, 1, 2.0),
('Kid_Buu', 6, 0, 1.5);
