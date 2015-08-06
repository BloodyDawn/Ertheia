CREATE TABLE IF NOT EXISTS `global_tasks` (
  `id` int(11) NOT NULL auto_increment,
  `task` varchar(50) NOT NULL default '',
  `type` varchar(50) NOT NULL default '',
  `last_activation` bigint(13) unsigned NOT NULL DEFAULT '0',
  `param1` varchar(100) NOT NULL default '',
  `param2` varchar(100) NOT NULL default '',
  `param3` varchar(255) NOT NULL default '',
  PRIMARY KEY (`id`)
);

INSERT IGNORE INTO `global_tasks` VALUES
('1', 'restart', 'TYPE_GLOBAL_TASK', '1256702402993', '1', '06:00:00', '600');

INSERT INTO `global_tasks` VALUES (DEFAULT, 'chaos_festival_round', 'TYPE_GLOBAL_TASK', 0, 1, '06:30:00', '');