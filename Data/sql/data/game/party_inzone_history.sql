CREATE TABLE IF NOT EXISTS `party_inzone_history` (
	`party_id` INT NOT NULL default 0,
	`char_id` INT NOT NULL default 0,
	`char_class_id` INT NOT NULL default 0,
	`instance_id` INT(3) NOT NULL default 0,
	`instance_use_time` BIGINT UNSIGNED NOT NULL DEFAULT 0,
	`instance_status` INT(3) NOT NULL default 0,
  PRIMARY KEY (`party_id`,`char_id`,`instance_use_time`),
  KEY `index1` (`party_id`)
) DEFAULT CHARSET=utf8;