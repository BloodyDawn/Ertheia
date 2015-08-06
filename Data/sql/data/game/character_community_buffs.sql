DROP TABLE IF EXISTS `character_community_buffs`;
CREATE TABLE `character_community_buffs` (
  `charId` int(10) default NULL,
  `skill_id` int(5) default NULL,
  `skill_level` int(2) default NULL,
  `for_pet` int(1) default NULL,
  PRIMARY KEY (`charId`,`skill_id`,`skill_level`,`for_pet`),
  KEY `NewIndex1` (`charId`,`for_pet`)
) DEFAULT CHARSET=utf8;
