DROP TABLE IF EXISTS `commission_list`;

CREATE TABLE `commission_list` (
  `id_lot` int(30) NOT NULL,
  `char_name` varchar(50) NOT NULL DEFAULT '',
  `item_name` varchar(50) NOT NULL DEFAULT '',
  `sell_price` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `item_object_id` int(11) NOT NULL DEFAULT 0,
  `end_date` bigint(20) unsigned NOT NULL DEFAULT 0,
  `days` tinyint(4) NOT NULL,
  PRIMARY KEY (`id_lot`),
  UNIQUE KEY `NewIndex1` (`item_object_id`),
  KEY `NewIndex2` (`char_name`)
) DEFAULT CHARSET=utf8;

