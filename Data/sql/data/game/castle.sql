DROP TABLE IF EXISTS `castle`;
CREATE TABLE `castle` (
  `id` int(11) NOT NULL DEFAULT '0',
  `name` varchar(25) NOT NULL,
  `treasury` bigint(20) NOT NULL DEFAULT '0',
  `siegeDate` bigint(13) unsigned NOT NULL DEFAULT '0',
  `regTimeOver` enum('true','false') NOT NULL DEFAULT 'true',
  `regTimeEnd` bigint(13) unsigned NOT NULL DEFAULT '0',
  `showNpcCrest` enum('true','false') NOT NULL DEFAULT 'false',
  `side` enum('DARK','LIGHT') NOT NULL DEFAULT 'LIGHT',
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8;

INSERT INTO `castle` VALUES 
('1', 'Gludio', '0', '1338073200000', 'true', '0', 'false', 'LIGHT'),
('2', 'Dion', '0', '1338073200000', 'true', '0', 'false', 'LIGHT'),
('3', 'Giran', '0', '1338073200000', 'true', '0', 'false', 'LIGHT'),
('4', 'Oren', '0', '1338073200000', 'true', '0', 'false', 'LIGHT'),
('5', 'Aden', '0', '1338073200000', 'true', '0', 'false', 'LIGHT'),
('6', 'Innadril', '0', '1338073200000', 'true', '0', 'false', 'LIGHT'),
('7', 'Goddard', '0', '1338073200000', 'true', '0', 'false', 'LIGHT'),
('8', 'Rune', '0', '1338073200000', 'true', '0', 'false', 'LIGHT'),
('9', 'Schuttgart', '0', '1338073200000', 'true', '0', 'false', 'LIGHT');