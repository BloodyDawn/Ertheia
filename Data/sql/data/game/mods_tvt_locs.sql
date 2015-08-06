DROP TABLE IF EXISTS `mods_tvt_locs`;
CREATE TABLE IF NOT EXISTS `mods_tvt_locs` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `locationName` varchar(255) NOT NULL,
  `locationId` int(10) unsigned NOT NULL,
  `teamId` int(1) unsigned NOT NULL,
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  `z` int(11) NOT NULL,
  KEY `id` (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8;

INSERT INTO `mods_tvt_locs` VALUES 
('1', 'Gludio', '1', '0', '-84408', '150911', '-3112'),
('2', 'Gludio', '1', '1', '-81662', '150761', '-3112'),
('5','Orbis',3,0,206369,119843,-10013),
('6','Orbis',3,1,207992,121415,-10013),
('7','Tauti',4,0,-145903,212894,-10043),
('8','Tauti',4,1,-148651,212892,-10043);