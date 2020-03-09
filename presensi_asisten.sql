# Host: localhost  (Version 5.5.5-10.1.38-MariaDB)
# Date: 2019-12-01 11:36:35
# Generator: MySQL-Front 6.0  (Build 2.20)


#
# Structure for table "users"
#

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `nim` int(10) NOT NULL DEFAULT '0',
  `name` varchar(50) DEFAULT NULL,
  `email` varchar(50) DEFAULT NULL,
  `phone_number` varchar(15) DEFAULT NULL,
  `position` enum('Asisten','Supervisor') DEFAULT NULL,
  `password` varchar(64) DEFAULT NULL,
  `imei` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`nim`),
  KEY `imei` (`imei`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

#
# Data for table "users"
#

INSERT INTO `users` VALUES (1234567890,'123','1@1.com','1111','Asisten','$2y$10$1l8V21uFMeJvoHsMphA4jOuCfP2ybq.J0i9WF/awIKveAdwNod/na','1111'),(1711500000,'Testing','test@test.test','081234567890','Supervisor','$2y$10$MN2vS3sMyw3EZ2G5.HvUHuvybdYmM0bkDMwWzx0vP.QpLanFqGohu','1234567890'),(1711501559,'Mus Priandi','mus@mus.mus','0812','Asisten','$2y$10$BAsz6zSYa9jq6gqviLltI.G/8FYKQ86zCXPZ8JiyNeFDhcznG/zym','1234567890');
