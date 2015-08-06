DROP TABLE IF EXISTS `grandboss_data`;
CREATE TABLE IF NOT EXISTS `grandboss_data` (
  `boss_id` smallint(5) unsigned NOT NULL,
  `loc_x` mediumint(6) NOT NULL,
  `loc_y` mediumint(6) NOT NULL,
  `loc_z` mediumint(6) NOT NULL,
  `heading` mediumint(6) NOT NULL DEFAULT '0',
  `respawn_time` bigint(13) unsigned NOT NULL DEFAULT '0',
  `currentHP` decimal(30,15) NOT NULL,
  `currentMP` decimal(30,15) NOT NULL,
  `status` tinyint(1) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`boss_id`)
) DEFAULT CHARSET=utf8;

INSERT IGNORE INTO `grandboss_data` VALUES 
(29001, -21610, 181594, -5734, 0, 0, 229898, 667, 0), -- Queen Ant (40)
(29014, 55024, 17368, -5412, 10126, 0, 622493, 3793, 0), -- Orfen (50)
(29019, 185708, 114298, -8221, 32768, 0, 17850000, 39960, 0), -- Antharas (79)
(29020, 116033, 17447, 10104, 40188, 0, 4068372, 39960, 0), -- Baium (75)
(29028, -105200, -253104, -15264, 0, 0, 223107426, 4497143, 0), -- Valakas (85)
(29066, 185708, 114298, -8221,32768, 0, 14518000, 3996000, 0), -- Antharas Weak (79)
(29067, 185708, 114298, -8221,32768, 0, 16184000, 3996000, 0), -- Antharas Normal (79)
(29068, 185708, 114298, -8221,32768, 0, 204677324, 3996000, 0), -- Antharas Strong (85)
(25899, 46424, -26200, -1430, 0, 0, 322249137, 47100, 0); -- Lindvior (99)