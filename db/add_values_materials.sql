INSERT INTO dls_schema.materials (title, author, year, ISBN, idMaterialType, material_status)
VALUES 
('Pride and Prejudice', 'Jane Austen', 1813, 67967966766, 1, 'available'),
('The Way of Kings', 'Brandon Sanderson', 2010, 9781429992800, 1, 'available'),
('The Name of the Wind', 'Patrick Rothfuss ', 2007, 0575081384, 1, 'available'),
('The Name of the Wind', 'Patrick Rothfuss ', 2007, 0575081384, 1, 'available'),
('The Great Gatsby', 'F. Scott Fitzgerald', 1925, 4375687624324, 1, 'available'),
('Harry Potter and the Sorcerer Stone', 'J.K. Rowling', 1997, 34578736345, 1, 'available'),
('Harry Potter and the Sorcerer Stone', 'J.K. Rowling', 1997, 34578736345, 1, 'available'),
('Harry Potter and the Sorcerer Stone', 'J.K. Rowling', 1997, 34578736345, 1, 'available'),
('It', 'Stephen King', 1986, 345463346, 1, 'available'),
('Treasure Island', 'Robert Louis Stevenson', 1883, 4537768, 1, 'available'),
('Hamlet', 'William Shakespeare', 1603, 3567568879, 1, 'available'),
('Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', 2011, 46765885665, 1, 'available'),
('A Brief History of Time', 'Stephen Hawking', 1988, 6588795879, 1, 'available'),
('Charlotte''s Web', 'E.B. White', 1952, 6798757768, 1, 'available');

-- CDS
INSERT INTO dls_schema.materials (title, author, year, ISBN, idMaterialType, material_status)
VALUES
('Kind of Blue', 'Miles Davis', 1959, NULL, 2, 'available'),
('Blue Train', 'John Coltrane', 1957, NULL, 2, 'available'),
('Abbey Road', 'The Beatles', 1969, NULL, 2, 'available'),
('Thriller', 'Michael Jackson', 1982, NULL, 2, 'available'),
('Legend', 'Bob Marley', 1984, NULL, 2, 'available'),
('La Traviata', 'Giuseppe Verdi', 1853, NULL, 2, 'available'),
('Map of the Soul: 7', 'BTS', 2020, NULL, 2, 'available');

-- MOVIES
INSERT INTO dls_schema.materials (title, author, year, ISBN, idMaterialType, material_status)
VALUES
('The Notebook', 'Nick Cassavetes', 2004, NULL, 3, 'available'),
('Inception', 'Christopher Nolan', 2010, NULL, 3, 'available'),
('The Lord of the Rings: The Fellowship of the Ring', 'Peter Jackson', 2001, NULL, 3, 'available'),
('It', 'Andy Muschietti', 2017, NULL, 3, 'available'),
('Pirates of the Caribbean: The Curse of the Black Pearl', 'Gore Verbinski', 2003, NULL, 3, 'available'),
('The Godfather', 'Francis Ford Coppola', 1972, NULL, 3, 'available'),
('Gladiator', 'Ridley Scott', 2000, NULL, 3, 'available'),
('Interstellar', 'Christopher Nolan', 2014, NULL, 3, 'available'),
('Frozen', 'Chris Buck & Jennifer Lee', 2013, NULL, 3, 'available');

-- MAGAZINES
INSERT INTO dls_schema.materials (title, author, year, ISBN, idMaterialType, material_status)
VALUES
('The Daily News', 'Global Press', 2024, NULL, 3, 'available'),
('Vogue', 'Condé Nast', 2024, NULL, 3, 'available'),
('Sports Illustrated', 'SI Media', 2024, NULL, 3, 'available'),
('Bon Appétit', 'Condé Nast', 2024, NULL, 3, 'available'),
('Men''s Health', 'Hearst', 2024, NULL, 3, 'available'),
('Better Homes & Gardens', 'Meredith', 2024, NULL, 3, 'available');


SELECT idMaterial, title FROM dls_schema.materials;


-- Books
INSERT INTO dls_schema.materials_genres (idMaterial, idGenre) VALUES
(1, 1),  -- Pride and Prejudice → romance
(2, 3),  -- The Way of Kings → fantasy
(3, 3),  -- The Name of the Wind → fantasy
(4, 3),  -- The Name of the Wind duplicate → fantasy
(5, 2),  -- The Great Gatsby → fiction
(6, 3),  -- Harry Potter → fantasy
(7, 3),  -- Harry Potter duplicate → fantasy
(8, 3),  -- Harry Potter duplicate → fantasy
(9, 4),  -- It → horror
(10, 5), -- Treasure Island → adventure
(11, 6), -- Hamlet → drama
(12, 7), -- Sapiens → history
(13, 8), -- A Brief History of Time → science
(14, 9); -- Charlotte's Web → children


-- CDs
INSERT INTO dls_schema.materials_genres (idMaterial, idGenre) VALUES
(15, 11), -- Kind of Blue → jazz
(16, 11), -- Blue Train → jazz
(17, 12), -- Abbey Road → rock
(18, 13), -- Thriller → pop
(19, 14), -- Legend → reggae
(20, 15), -- La Traviata → opera
(21, 16); -- Map of the Soul: 7 → kpop

-- Movies
INSERT INTO dls_schema.materials_genres (idMaterial, idGenre) VALUES
(22, 1),  -- The Notebook → romance
(23, 2),  -- Inception → fiction / sci-fi
(24, 3),  -- Lord of the Rings → fantasy
(25, 4),  -- It → horror
(26, 5),  -- Pirates of the Caribbean → adventure
(27, 6),  -- The Godfather → drama / crime
(28, 6),  -- Gladiator → drama / historical
(29, 2),  -- Interstellar → fiction / sci-fi
(30, 9);  -- Frozen → children / animation

-- Magazines
INSERT INTO dls_schema.materials_genres (idMaterial, idGenre) VALUES
(31, 17), -- The Daily News → news
(32, 18), -- Vogue → fashion
(33, 19), -- Sports Illustrated → sports
(34, 20), -- Bon Appétit → cooking
(35, 21), -- Men's Health → fitness
(36, 22); -- Better Homes & Gardens → home and garden

SELECT * FROM dls_schema.materials;
