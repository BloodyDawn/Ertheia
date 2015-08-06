CREATE TABLE IF NOT EXISTS `character_account` (
  `account_name` VARCHAR(45) NOT NULL DEFAULT '',
  `password` VARCHAR(255) NOT NULL DEFAULT '',
  `wrong_password` VARCHAR(20) ,
  PRIMARY KEY (`account_name`)
);
