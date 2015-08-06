CREATE TABLE IF NOT EXISTS `character_quests` (
  `charId` INT UNSIGNED NOT NULL DEFAULT 0,
  `name` VARCHAR(120) NOT NULL DEFAULT '',
  `var`  VARCHAR(20) NOT NULL DEFAULT '',
  `value` VARCHAR(255) ,
  `class_index` int(1) NOT NULL default '0',
  PRIMARY KEY (`charId`,`name`,`var`,`class_index`),
  KEY `NewIndex1` (`charId`,`var`),
  KEY `NewIndex2` (`charId`)
);