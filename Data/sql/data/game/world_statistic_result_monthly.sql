DROP TABLE IF EXISTS `world_statistic_result_monthly`;
CREATE TABLE `world_statistic_result_monthly` (
	`categoryId` INT UNSIGNED NOT NULL DEFAULT 0,
	`subCategoryId` INT UNSIGNED NOT NULL DEFAULT 0,
	`place` INT UNSIGNED NOT NULL DEFAULT 0,
	`charId` INT UNSIGNED NOT NULL DEFAULT 0,
	`char_name` VARCHAR(35) NOT NULL,
	`statValue` bigint unsigned NOT NULL default 0,
  PRIMARY KEY (`categoryId`,`subCategoryId`,`charId`),
  KEY `Categories` (`categoryId`,`subCategoryId`)
) DEFAULT CHARSET=utf8;
