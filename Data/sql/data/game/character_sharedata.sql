CREATE TABLE IF NOT EXISTS `character_sharedata` (
  `account_name` VARCHAR(45) NOT NULL DEFAULT '',
  `var` VARCHAR(255) NOT NULL DEFAULT '',
  `value` VARCHAR(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`account_name`,`var`)
);
