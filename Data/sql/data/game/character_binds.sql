CREATE TABLE IF NOT EXISTS `character_binds` (
  `obj_id` INT(8) NOT NULL,
  `bind_data` BLOB NOT NULL,
  PRIMARY KEY (`obj_id`)
);