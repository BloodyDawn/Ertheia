DROP TABLE IF EXISTS `donate_items`;
CREATE TABLE `donate_items` (
  `id` int(11) NOT NULL auto_increment,
  `char_name` varchar(255) NOT NULL,
  `item_id` int(11) NOT NULL,
  `item_count` int(11) NOT NULL,
  `date` decimal(20,0) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) DEFAULT CHARSET=utf8;

