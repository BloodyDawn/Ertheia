CREATE TABLE IF NOT EXISTS `character_recommendation` (
  `charId` int(10) unsigned NOT NULL,
  `rec_have` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `rec_left` tinyint(3) unsigned NOT NULL DEFAULT '0',
  UNIQUE KEY `charId` (`charId`)
);