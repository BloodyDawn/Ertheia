DROP TABLE IF EXISTS `kamaloka_results`;
CREATE TABLE `kamaloka_results` (
  `char_name` VARCHAR(35) NOT NULL DEFAULT '0',
  `Level` int(4) NOT NULL DEFAULT '0',
  `Grade` int(1) NOT NULL DEFAULT '0',
  `Count` int(2) NOT NULL DEFAULT '0' 
) ENGINE=MyISAM DEFAULT CHARSET=utf8;