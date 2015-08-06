CREATE TABLE IF NOT EXISTS `clanhall_functions` 
(
  `agit_id` int(2) NOT NULL default '0',
  `deco_id` int(2) NOT NULL default '0',
  `endTime` decimal(20,0) NOT NULL default '0',
  PRIMARY KEY  (`agit_id`,`deco_id`)
);
