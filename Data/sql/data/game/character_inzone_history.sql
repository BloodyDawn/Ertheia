CREATE TABLE IF NOT EXISTS `character_inzone_history` (
	`char_id` INT NOT NULL default 0,
	`party_id` INT NOT NULL default 0,
  PRIMARY KEY (`char_id`, `party_id`),
  KEY `index1` (`char_id`)
) DEFAULT CHARSET=utf8;