SET SQL_SAFE_UPDATES = 0;
DELETE FROM dls_schema.genre;
ALTER TABLE dls_schema.genre AUTO_INCREMENT = 1;
SELECT * FROM dls_schema.genre;

SET SQL_SAFE_UPDATES = 1;

INSERT INTO dls_schema.genre (genre) VALUES ('romance'), ('fiction'), ('fantasy'), ('horror'), ('adventure'), 
('drama'), ('history'), ('science'), ('children'), ('blues'), ('jazz'), ('rock'), ('pop'), ('reggae'), 
('opera'), ('kpop'), ('news'), ('fashion'), ('sports'), ('cooking'), ('fitness'), ('home and garden');

INSERT INTO dls_schema.genre (genre) VALUES ('animation')