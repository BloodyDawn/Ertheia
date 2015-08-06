DROP TABLE IF EXISTS `hwid_ban`;
CREATE TABLE `hwid_ban`
(
  `hwid` varchar(32) NOT NULL,
  `account` varchar(16) NOT NULL,
  `ip` varchar(16) NOT NULL,
  `hackType` varchar(16) NOT NULL,
  `comment` varchar(128) NOT NULL,
  `banStart` bigint(13) unsigned NOT NULL DEFAULT '0',
  `banEnd` bigint(13) unsigned NOT NULL DEFAULT '0',  
  PRIMARY KEY (`hwid`),
  KEY `hwid` (`hwid`)
) DEFAULT CHARSET=utf8;