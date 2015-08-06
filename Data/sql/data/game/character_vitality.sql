CREATE TABLE IF NOT EXISTS `character_vitality` (
  `char_id` INT UNSIGNED NOT NULL DEFAULT 0,
  `class_index` INT(1) NOT NULL DEFAULT 0,
  `vitality_points` INT NOT NULL DEFAULT 0,
  `vitality_items` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`char_id`,`class_index`)
);