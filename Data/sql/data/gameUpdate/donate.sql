DROP TABLE IF EXISTS `donate`;
CREATE TABLE `donate` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `charname` varchar(35) NOT NULL,
  `item_id` int(11) NOT NULL,
  `item_count` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;