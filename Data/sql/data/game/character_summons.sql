CREATE TABLE IF NOT EXISTS `character_summons` (
  `owner_id` int(10) unsigned NOT NULL,
  `summon_object_id` int(11) NOT NULL,
  `summon_skill_id` int(10) unsigned NOT NULL,
  `cur_hp` int(9) unsigned DEFAULT '0',
  `cur_mp` int(9) unsigned DEFAULT '0',
  PRIMARY KEY (`owner_id`,`summon_object_id`),
  KEY `NewIndex1` (`owner_id`)
);