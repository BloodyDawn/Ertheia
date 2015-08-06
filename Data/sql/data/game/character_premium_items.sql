CREATE TABLE IF NOT EXISTS `character_premium_items` (
  `charId` INT UNSIGNED NOT NULL,
  `itemNum` int(11) NOT NULL,
  `itemId` int(11) NOT NULL,
  `itemCount` bigint(20) unsigned NOT NULL,
  `itemSender` varchar(100) NOT NULL,
  `itemSenderMessage` varchar(300) NOT NULL,
  `time` bigint(13) NOT NULL,
  KEY `charId` (`charId`),
  KEY `itemNum` (`itemNum`,`charId`)
);