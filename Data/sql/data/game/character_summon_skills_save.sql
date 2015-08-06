DROP TABLE IF EXISTS `character_summon_skills_save`;
CREATE TABLE `character_summon_skills_save` (
  `owner_id` int(11) NOT NULL DEFAULT '0',
  `owner_class_index` int(1) NOT NULL DEFAULT '0',
  `summon_object_id` int(11) NOT NULL DEFAULT '0',
  `skill_id` int(11) NOT NULL,
  `skill_level` int(3) NOT NULL DEFAULT '1',
  `effect_count` int(11) NOT NULL DEFAULT '0',
  `effect_cur_time` int(11) NOT NULL DEFAULT '0',
  `buff_index` int(2) NOT NULL DEFAULT '0',
  PRIMARY KEY (`owner_id`,`owner_class_index`,`summon_object_id`,`skill_id`,`skill_level`),
  KEY `NewIndex1` (`owner_id`,`owner_class_index`,`summon_object_id`)
) DEFAULT CHARSET=utf8;