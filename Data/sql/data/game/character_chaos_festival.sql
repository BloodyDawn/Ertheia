DROP TABLE IF EXISTS `character_chaos_festival`;
CREATE TABLE `character_chaos_festival` (
  `player_id` INT(11) NOT NULL,
  `myst_signs` INT(11) DEFAULT '0',
  `skip_rounds` INT(11) DEFAULT '0',
  `total_bans` INT(11) DEFAULT '0',
  PRIMARY KEY (`player_id`)
);