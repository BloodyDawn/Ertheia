DROP TABLE IF EXISTS `kamaloka_ranks`;
CREATE TABLE `kamaloka_ranks` (
  `charId` int(11) unsigned NOT NULL DEFAULT '0',
  `kanabionsCount` int(10) unsigned NOT NULL DEFAULT '0',
  `dopplersCount` int(10) unsigned NOT NULL DEFAULT '0',
  `voidersCount` int(10) unsigned NOT NULL DEFAULT '0',
  `total` int(10) unsigned NOT NULL DEFAULT '0'
) ENGINE=MyISAM DEFAULT CHARSET=utf8;