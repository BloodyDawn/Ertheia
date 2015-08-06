CREATE TABLE IF NOT EXISTS `fort_functions` (
  `fort_id` int(2) NOT NULL default '0',
  `deco_id` int(2) NOT NULL default '0',
  `endTime` decimal(20,0) NOT NULL default '0',
  PRIMARY KEY  (`fort_id`,`deco_id`)
);