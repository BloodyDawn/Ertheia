CREATE TABLE IF NOT EXISTS `character_relations` (
  `charId` INT UNSIGNED NOT NULL default 0,
  `friendId` INT UNSIGNED NOT NULL DEFAULT 0,
  `relation` INT UNSIGNED NOT NULL DEFAULT 0,
  `note` varchar(50) DEFAULT '',
  PRIMARY KEY (`charId`,`friendId`),
  KEY `NewIndex1` (`charId`)
);