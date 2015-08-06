DROP TABLE IF EXISTS `armorsets`;
CREATE TABLE `armorsets` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `chest` smallint(5) unsigned NOT NULL DEFAULT '0',
  `legs` smallint(5) unsigned NOT NULL DEFAULT '0',
  `head` smallint(5) unsigned NOT NULL DEFAULT '0',
  `gloves` smallint(5) unsigned NOT NULL DEFAULT '0',
  `feet` smallint(5) unsigned NOT NULL DEFAULT '0',
  `skill` varchar(70) NOT NULL DEFAULT '0-0;',
  `shield` smallint(5) unsigned NOT NULL DEFAULT '0',
  `shield_skill_id` smallint(5) unsigned NOT NULL DEFAULT '0',
  `enchant6skill` smallint(5) unsigned NOT NULL DEFAULT '0',
  `mw_legs` smallint(5) unsigned NOT NULL DEFAULT '0',
  `mw_head` smallint(5) unsigned NOT NULL DEFAULT '0',
  `mw_gloves` smallint(5) unsigned NOT NULL DEFAULT '0',
  `mw_feet` smallint(5) unsigned NOT NULL DEFAULT '0',
  `mw_shield` smallint(5) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`,`chest`)
);

INSERT INTO `armorsets` VALUES


-- S80 Dynasty PVP Armor Sets
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (129, 10820, 9421, 9422, 9423, 9424, '3006-1;8219-1;3659-1;3662-1;', 9441, 3417, 3623,     11512,   11557,   11513,     11526,   11532), -- Dynasty Platinum Breastplate - PvP Shield Master
  (130, 10821, 9421, 9422, 9423, 9424, '3006-1;8220-1;3659-1;3662-1;', 0,    0,    3623,     11512,   11557,   11513,     11526,   0),     -- Dynasty Platinum Breastplate - PvP Weapon Master
  (131, 10822, 9421, 9422, 9423, 9424, '3006-1;8222-1;3659-1;3662-1;', 0,    0,    3623,     11512,   11557,   11513,     11526,   0),     -- Dynasty Platinum Breastplate - PvP Force Master
  (132, 10823, 9421, 9422, 9423, 9424, '3006-1;8221-1;3659-1;3662-1;', 0,    0,    3623,     11512,   11557,   11513,     11526,   0),     -- Dynasty Platinum Breastplate - PvP Bard
  (133, 10825, 9428, 9429, 9430, 9431, '3006-1;8223-1;3663-1;',        0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Jewel Leather Armor - PvP Dagger Master
  (134, 10826, 9428, 9429, 9430, 9431, '3006-1;8224-1;3663-1;',        0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Jewel Leather Armor - PvP Bow Master
  (135, 10832, 9428, 9429, 9430, 9431, '3006-1;8225-1;3663-1;',        0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Jewel Leather Armor - PvP	Force Master
  (136, 10833, 9428, 9429, 9430, 9431, '3006-1;8226-1;3663-1;',        0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Jewel Leather Armor - PvP	Weapon Master
  (137, 10834, 9428, 9429, 9430, 9431, '3006-1;8228-1;3663-1;',        0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Jewel Leather Armor - PvP	Enchanter
  (138, 10835, 9428, 9429, 9430, 9431, '3006-1;8227-1;3663-1;',        0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Jewel Leather Armor - PvP	Summoner
  (139, 10828, 9437, 9438, 9439, 9440, '3006-1;8229-1;3660-1;',        0,    0,    3625,     11558,   11539,   11514,     11533,   0),     -- Dynasty Silver Satin Tunic - PvP Healer
  (140, 10829, 9437, 9438, 9439, 9440, '3006-1;8230-1;3660-1;',        0,    0,    3625,     11558,   11539,   11514,     11533,   0),     -- Dynasty Silver Satin Tunic - PvP Enchanter
  (141, 10830, 9437, 9438, 9439, 9440, '3006-1;8231-1;3660-1;',        0,    0,    3625,     11558,   11539,   11514,     11533,   0),     -- Dynasty Silver Satin Tunic - PvP Summoner
  (142, 10831, 9437, 9438, 9439, 9440, '3006-1;8232-1;3660-1;',        0,    0,    3625,     11558,   11539,   11514,     11533,   0),     -- Dynasty Silver Satin Tunic - PvP Wizard

-- Friendship Armor Sets
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (158, 15092, 0,    15093,15094,15095,'3006-1;3535-1;',               0,    0,    0,        0,       14979,   14980,     14981,   0),     -- Dark Crystal robe set
  (159, 14978, 0,    15093,15094,15095,'3006-1;3535-1;',               0,    0,    0,        0,       14979,   14980,     14981,   0),     -- Dark Crystal robe set
  (160, 15097, 0,    14984,14985,14986,'3006-1;3523-1;',               0,    0,    0,        0,       15098,   15099,     15100,   0),     -- Avadon robe set
  (161, 14983, 0,    14984,14985,14986,'3006-1;3523-1;',               0,    0,    0,        0,       15098,   15099,     15100,   0),     -- Avadon robe set
  (162, 15102, 15103,0,    15104,0,    '3006-1;3510-1;',               0,    0,    0,        14989,   0,       14990,     0,       0),     -- Karmian robe set
  (163, 14988, 15103,0,    15104,0,    '3006-1;3510-1;',               0,    0,    0,        14989,   0,       14990,     0,       0),     -- Karmian robe set
  (164, 15106, 15107,0,    15108,0,    '3006-1;3507-1;',               0,    0,    0,        14993,   0,       14994,     0,       0),     -- Mithril Tunic
  (165, 14992, 15107,0,    15108,0,    '3006-1;3507-1;',               0,    0,    0,        14993,   0,       14994,     0,       0),     -- Mithril Tunic
  (166, 15110, 15111,15093,15112,15113,'3006-1;3532-1;',               0,    0,    0,        14997,   14979,   14998,     14999,   0),     -- Dark Crystal leather set
  (167, 14996, 15111,15093,15112,15113,'3006-1;3532-1;',               0,    0,    0,        14997,   14979,   14998,     14999,   0),     -- Dark Crystal leather set
  (168, 15114, 0,    15115,15117,15119,'3006-1;3527-1;',               0,    0,    0,        0,       15001,   15003,     15005,   0),     -- Doom leather set
  (169, 15000, 0,    15115,15117,15119,'3006-1;3527-1;',               0,    0,    0,        0,       15001,   15003,     15005,   0),     -- Doom leather set
  (170, 15122, 15123,0,    0,    15124,'3006-1;3511-1;',               0,    0,    0,        15009,   0,       0,         15010,   0),     -- Plated leather set
  (171, 15008, 15123,0,    0,    15124,'3006-1;3511-1;',               0,    0,    0,        15009,   0,       0,         15010,   0),     -- Plated leather set
  (172, 15131, 15132,0,    0,    15133,'3006-1;3505-1;',               0,    0,    0,        15018,   0,       0,         15019,   0),     -- Manticore skin set
  (173, 15017, 15132,0,    0,    15133,'3006-1;3505-1;',               0,    0,    0,        15018,   0,       0,         15019,   0),     -- Manticore skin set
  (174, 15141, 0,    15142,15143,15144,'3006-1;3536-1;',               15145,3551, 0,        0,       15028,   15029,     15030,   15031), -- Nightmare heavy set
  (175, 15027, 0,    15142,15143,15144,'3006-1;3536-1;',               15145,3551, 0,        0,       15028,   15029,     15030,   15031), -- Nightmare heavy set
  (176, 15120, 0,    15001,15002,15004,'3006-1;3525-1;',               15007,3549, 0,        0,       15115,   15116,     15118,   15121), -- Doom plate heavy set
  (177, 15006, 0,    15001,15002,15004,'3006-1;3525-1;',               15007,3549, 0,        0,       15115,   15116,     15118,   15121), -- Doom plate heavy set
  (178, 15127, 0,    15012,0,    0,    '3006-1;3516-1;',               15016,3547, 0,        0,       15126,   0,         0,       15130), -- Full Plate Armor set
  (179, 15013, 0,    15012,0,    0,    '3006-1;3516-1;',               15016,3547, 0,        0,       15126,   0,         0,       15130), -- Full Plate Armor set
  (180, 15135, 15022,15023,0,    0,    '3006-1;3506-1;',               15026,3544, 0,        15136,   15137,   0,         0,       15140), -- Brigandine Armor set
  (181, 15021, 15022,15023,0,    0,    '3006-1;3506-1;',               15026,3544, 0,        15136,   15137,   0,         0,       15140), -- Brigandine Armor set

-- Friendship Armor Sets
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (200, 16866, 0,    16867,16868,16869,'3006-1;3535-1;',               0,    0,    0,        0,       0,       0,         0,       0),     -- Dark Crystal robe set
  (201, 16871, 0,    16872,16873,16874,'3006-1;3523-1;',               0,    0,    0,        0,       0,       0,         0,       0),     -- Avadon robe set
  (202, 16876, 16877,0,    16878,0,    '3006-1;3510-1;',               0,    0,    0,        0,       0,       0,         0,       0),     -- Karmian robe set
  (203, 16880, 16881,0,    16882,0,    '3006-1;3507-1;',               0,    0,    0,        0,       0,       0,         0,       0),     -- Mithril Tunic
  (204, 16884, 16885,16867,16886,16887,'3006-1;3532-1;',               0,    0,    0,        0,       0,       0,         0,       0),     -- Dark Crystal leather set
  (205, 16888, 0,    16889,16891,16893,'3006-1;3527-1;',               0,    0,    0,        0,       0,       0,         0,       0),     -- Doom leather set
  (206, 16896, 16897,0,    0,    16898,'3006-1;3511-1;',               0,    0,    0,        0,       0,       0,         0,       0),     -- Plated leather set
  (207, 16905, 16906,0,    0,    16907,'3006-1;3505-1;',               0,    0,    0,        0,       0,       0,         0,       0),     -- Manticore skin set
  (208, 16915, 0,    16916,16917,16918,'3006-1;3536-1;',               16919,3551, 0,        0,       0,       0,         0,       0),     -- Nightmare heavy set
  (209, 16894, 0,    16889,16890,16892,'3006-1;3525-1;',               16895,3549, 0,        0,       0,       0,         0,       0),     -- Doom plate heavy set
  (210, 16901, 0,    16900,0,    0,    '3006-1;3516-1;',               16904,3547, 0,        0,       0,       0,         0,       0),     -- Full Plate Armor set
  (211, 16909, 16910,16911,0,    0,    '3006-1;3506-1;',               16914,3544, 0,        0,       0,       0,         0,       0);     -- Brigandine Armor set