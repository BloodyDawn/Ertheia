CREATE TABLE IF NOT EXISTS `fort_facility` (
  `fort_id` int(11) NOT NULL,
  `facility_type` int(11) NOT NULL default '0',
  `facility_level` int(11) NOT NULL default '0',
  PRIMARY KEY (`fort_id`, `facility_type`)
);