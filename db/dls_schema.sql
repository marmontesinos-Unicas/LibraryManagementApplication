-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: dls_schema
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `genre`
--

DROP TABLE IF EXISTS `genre`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `genre` (
  `idGenre` int NOT NULL AUTO_INCREMENT,
  `genre` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`idGenre`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `genre`
--

LOCK TABLES `genre` WRITE;
/*!40000 ALTER TABLE `genre` DISABLE KEYS */;
INSERT INTO `genre` VALUES (1,'romance'),(2,'fiction'),(3,'fantasy'),(4,'horror'),(5,'adventure'),(6,'drama'),(7,'history'),(8,'science'),(9,'children'),(10,'blues'),(11,'jazz'),(12,'rock'),(13,'pop'),(14,'reggae'),(15,'opera'),(16,'kpop'),(17,'news'),(18,'fashion'),(19,'sports'),(20,'cooking'),(21,'fitness'),(22,'home and garden'),(23,'romance'),(24,'fiction'),(25,'fantasy'),(26,'horror'),(27,'adventure'),(28,'drama'),(29,'history'),(30,'science'),(31,'children'),(32,'blues'),(33,'jazz'),(34,'rock'),(35,'pop'),(36,'reggae'),(37,'opera'),(38,'kpop'),(39,'news'),(40,'fashion'),(41,'sports'),(42,'cooking'),(43,'fitness'),(44,'home and garden');
/*!40000 ALTER TABLE `genre` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `holds`
--

DROP TABLE IF EXISTS `holds`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `holds` (
  `idHold` int NOT NULL AUTO_INCREMENT,
  `idUser` int NOT NULL,
  `idMaterial` int NOT NULL,
  `hold_date` datetime(2) NOT NULL,
  PRIMARY KEY (`idHold`),
  KEY `idUser_idx` (`idUser`),
  KEY `idMaterial_idx` (`idMaterial`),
  CONSTRAINT `idMaterial_FK` FOREIGN KEY (`idMaterial`) REFERENCES `materials` (`idMaterial`),
  CONSTRAINT `idUser_FK` FOREIGN KEY (`idUser`) REFERENCES `users` (`idUser`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `holds`
--

LOCK TABLES `holds` WRITE;
/*!40000 ALTER TABLE `holds` DISABLE KEYS */;
INSERT INTO `holds` VALUES (1,1,3,'2025-12-10 00:00:00.00'),(4,4,7,'2025-12-10 00:00:00.00');
/*!40000 ALTER TABLE `holds` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loans`
--

DROP TABLE IF EXISTS `loans`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loans` (
  `idLoan` int NOT NULL AUTO_INCREMENT,
  `idUser` int NOT NULL,
  `idMaterial` int NOT NULL,
  `start_date` datetime(2) NOT NULL,
  `due_date` datetime(2) NOT NULL,
  `return_date` datetime(2) DEFAULT NULL,
  PRIMARY KEY (`idLoan`),
  KEY `idUser_idx` (`idUser`),
  KEY `idMaterial_idx` (`idMaterial`),
  CONSTRAINT `idMaterial_FK2` FOREIGN KEY (`idMaterial`) REFERENCES `materials` (`idMaterial`),
  CONSTRAINT `idUser_FK2` FOREIGN KEY (`idUser`) REFERENCES `users` (`idUser`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loans`
--

LOCK TABLES `loans` WRITE;
/*!40000 ALTER TABLE `loans` DISABLE KEYS */;
INSERT INTO `loans` VALUES (1,1,3,'2025-01-10 00:00:00.00','2025-02-10 00:00:00.00','2025-01-28 00:00:00.00'),(2,2,7,'2025-02-05 00:00:00.00','2025-03-05 00:00:00.00','2025-02-28 00:00:00.00'),(3,3,12,'2025-03-01 00:00:00.00','2025-04-01 00:00:00.00','2025-03-29 00:00:00.00'),(4,4,18,'2025-04-15 00:00:00.00','2025-05-15 00:00:00.00','2025-05-10 00:00:00.00'),(5,5,25,'2025-05-20 00:00:00.00','2025-06-20 00:00:00.00','2025-06-12 00:00:00.00'),(6,1,10,'2025-10-10 02:00:00.00','2025-11-10 01:00:00.00','2025-12-09 12:08:58.00'),(7,2,14,'2025-10-25 00:00:00.00','2025-11-25 00:00:00.00',NULL),(8,3,22,'2025-11-10 00:00:00.00','2025-12-10 00:00:00.00',NULL),(9,4,30,'2025-06-01 02:00:00.00','2025-07-01 02:00:00.00','2025-12-05 18:49:52.00'),(10,5,5,'2025-07-15 02:00:00.00','2025-08-15 02:00:00.00','2025-12-05 18:50:23.00'),(11,2,1,'2025-08-10 02:00:00.00','2025-09-10 02:00:00.00','2025-12-05 19:11:13.00'),(12,2,1,'2025-12-05 21:37:22.00','2026-01-05 21:37:22.00','2025-12-10 15:25:16.00'),(13,3,3,'2025-12-05 21:49:25.00','2026-01-05 21:49:25.00','2025-12-05 20:50:13.00'),(14,3,2,'2025-12-09 12:10:24.00','2026-01-09 12:10:24.00',NULL),(15,4,34,'2025-12-11 18:09:13.00','2026-01-11 18:09:13.00',NULL);
/*!40000 ALTER TABLE `loans` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `material_type`
--

DROP TABLE IF EXISTS `material_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `material_type` (
  `idMaterialType` int NOT NULL AUTO_INCREMENT,
  `material_type` varchar(45) NOT NULL,
  PRIMARY KEY (`idMaterialType`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `material_type`
--

LOCK TABLES `material_type` WRITE;
/*!40000 ALTER TABLE `material_type` DISABLE KEYS */;
INSERT INTO `material_type` VALUES (1,'book'),(2,'CD'),(3,'movie'),(4,'magazine');
/*!40000 ALTER TABLE `material_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `materials`
--

DROP TABLE IF EXISTS `materials`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `materials` (
  `idMaterial` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `author` varchar(45) DEFAULT NULL,
  `year` int DEFAULT NULL,
  `ISBN` varchar(45) DEFAULT NULL,
  `idMaterialType` int DEFAULT NULL,
  `material_status` varchar(45) NOT NULL,
  PRIMARY KEY (`idMaterial`),
  KEY `idMaterialType_idx` (`idMaterialType`),
  CONSTRAINT `idMaterialType_FK` FOREIGN KEY (`idMaterialType`) REFERENCES `material_type` (`idMaterialType`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `materials`
--

LOCK TABLES `materials` WRITE;
/*!40000 ALTER TABLE `materials` DISABLE KEYS */;
INSERT INTO `materials` VALUES (1,'Pride and Prejudice','Jane Austen',1813,'67967966766',1,'available'),(2,'The Way of Kings','Brandon Sanderson',2010,'9781429992800',1,'loaned'),(3,'The Name of the Wind','Patrick Rothfuss ',2007,'575081384',1,'hold'),(4,'The Name of the Wind','Patrick Rothfuss ',2007,'575081384',1,'hold'),(5,'The Great Gatsby','F. Scott Fitzgerald',1925,'4375687624324',1,'available'),(6,'Harry Potter and the Sorcerer Stone','J.K. Rowling',1997,'34578736345',1,'hold'),(7,'Harry Potter and the Sorcerer Stone','J.K. Rowling',1997,'34578736345',1,'hold'),(8,'Harry Potter and the Sorcerer Stone','J.K. Rowling',1997,'34578736345',1,'hold'),(9,'It','Stephen King',1986,'345463346',1,'available'),(10,'Treasure Island','Robert Louis Stevenson',1883,'4537768',1,'available'),(11,'Hamlet','William Shakespeare',1603,'3567568879',1,'available'),(12,'Sapiens: A Brief History of Humankind','Yuval Noah Harari',2011,'46765885665',1,'available'),(13,'A Brief History of Time','Stephen Hawking',1988,'6588795879',1,'available'),(14,'Charlotte\'s Web','E.B. White',1952,'6798757768',1,'loaned'),(15,'Kind of Blue','Miles Davis',1959,NULL,2,'available'),(16,'Blue Train','John Coltrane',1957,NULL,2,'available'),(17,'Abbey Road','The Beatles',1969,NULL,2,'available'),(18,'Thriller','Michael Jackson',1982,NULL,2,'available'),(19,'Legend','Bob Marley',1984,NULL,2,'available'),(20,'La Traviata','Giuseppe Verdi',1853,NULL,2,'available'),(21,'Map of the Soul: 7','BTS',2020,NULL,2,'available'),(22,'The Notebook','Nick Cassavetes',2004,NULL,3,'loaned'),(23,'Inception','Christopher Nolan',2010,NULL,3,'available'),(24,'The Lord of the Rings: The Fellowship of the Ring','Peter Jackson',2001,NULL,3,'available'),(25,'It','Andy Muschietti',2017,NULL,3,'available'),(26,'Pirates of the Caribbean: The Curse of the Black Pearl','Gore Verbinski',2003,NULL,3,'available'),(27,'The Godfather','Francis Ford Coppola',1972,NULL,3,'available'),(28,'Gladiator','Ridley Scott',2000,NULL,3,'available'),(29,'Interstellar','Christopher Nolan',2014,NULL,3,'available'),(30,'Frozen','Chris Buck & Jennifer Lee',2013,NULL,3,'available'),(31,'The Daily News','Global Press',2024,NULL,3,'available'),(32,'Vogue','Condé Nast',2024,NULL,3,'available'),(33,'Sports Illustrated','SI Media',2024,NULL,3,'available'),(34,'Bon Appétit','Condé Nast',2024,'',3,'loaned'),(35,'Men\'s Health','Hearst',2024,NULL,3,'available'),(36,'Better Homes & Gardens','Meredith',2024,NULL,3,'available');
/*!40000 ALTER TABLE `materials` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `materials_genres`
--

DROP TABLE IF EXISTS `materials_genres`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `materials_genres` (
  `idMaterial` int NOT NULL,
  `idGenre` int NOT NULL,
  PRIMARY KEY (`idMaterial`,`idGenre`),
  KEY `idGenre` (`idGenre`),
  CONSTRAINT `materials_genres_ibfk_1` FOREIGN KEY (`idMaterial`) REFERENCES `materials` (`idMaterial`) ON DELETE CASCADE,
  CONSTRAINT `materials_genres_ibfk_2` FOREIGN KEY (`idGenre`) REFERENCES `genre` (`idGenre`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `materials_genres`
--

LOCK TABLES `materials_genres` WRITE;
/*!40000 ALTER TABLE `materials_genres` DISABLE KEYS */;
INSERT INTO `materials_genres` VALUES (22,1),(23,2),(29,2),(24,3),(25,4),(26,5),(27,6),(28,6),(30,9),(15,11),(16,11),(17,12),(18,13),(19,14),(20,15),(21,16),(31,17),(32,18),(33,19),(34,20),(35,21),(36,22);
/*!40000 ALTER TABLE `materials_genres` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `idRole` int NOT NULL AUTO_INCREMENT,
  `admin_type` varchar(45) NOT NULL,
  PRIMARY KEY (`idRole`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'admin'),(2,'user');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `idUser` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `surname` varchar(45) NOT NULL,
  `username` varchar(45) NOT NULL,
  `nationalID` varchar(45) NOT NULL,
  `birthdate` date DEFAULT NULL,
  `password` varchar(45) NOT NULL,
  `email` varchar(45) DEFAULT NULL,
  `idRole` int NOT NULL,
  PRIMARY KEY (`idUser`),
  KEY `isAdmin_idx` (`idRole`),
  CONSTRAINT `idRole_FK` FOREIGN KEY (`idRole`) REFERENCES `roles` (`idRole`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Carla','Ramirez','crami','24433365K','1967-01-12','45464','cramirez@gmail.com',1),(2,'Maria','Castro','mcastro','23456793I','2002-12-03','mcastro111','mcastro@gmail.com',2),(3,'Pablo','Garcia','pgarcia','45653567G','1998-05-20','pgarcia111','pgarcia@gmail.com',2),(4,'Marc','Roig','mroig','98936489Y','1997-06-26','mroig111','mroig@gmail.com',2),(5,'Carles','Pujalte','cpujalte','45653567G','1978-09-10','cpujalte111','cpujalte@gmail.com',2);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-11 18:43:53
