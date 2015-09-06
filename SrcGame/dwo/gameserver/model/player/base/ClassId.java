package dwo.gameserver.model.player.base;

import dwo.gameserver.util.Util;

public enum ClassId
{
	fighter(0, false, Race.Human, null, null),

	warrior(1, false, Race.Human, fighter, null),
	gladiator(2, false, Race.Human, warrior, null),
	warlord(3, false, Race.Human, warrior, null),
	knight(4, false, Race.Human, fighter, null),
	paladin(5, false, Race.Human, knight, null),
	darkAvenger(6, false, Race.Human, knight, null),
	rogue(7, false, Race.Human, fighter, null),
	treasureHunter(8, false, Race.Human, rogue, null),
	hawkeye(9, false, Race.Human, rogue, null),

	mage(10, true, Race.Human, null, null),
	wizard(11, true, Race.Human, mage, null),
	sorceror(12, true, Race.Human, wizard, null),
	necromancer(13, true, Race.Human, wizard, null),
	warlock(14, true, true, Race.Human, wizard, null),
	cleric(15, true, Race.Human, mage, null),
	bishop(16, true, Race.Human, cleric, null),
	prophet(17, true, Race.Human, cleric, null),

	elvenFighter(18, false, Race.Elf, null, null),
	elvenKnight(19, false, Race.Elf, elvenFighter, null),
	templeKnight(20, false, Race.Elf, elvenKnight, null),
	swordSinger(21, false, Race.Elf, elvenKnight, null),
	elvenScout(22, false, Race.Elf, elvenFighter, null),
	plainsWalker(23, false, Race.Elf, elvenScout, null),
	silverRanger(24, false, Race.Elf, elvenScout, null),

	elvenMage(25, true, Race.Elf, null, null),
	elvenWizard(26, true, Race.Elf, elvenMage, null),
	spellsinger(27, true, Race.Elf, elvenWizard, null),
	elementalSummoner(28, true, true, Race.Elf, elvenWizard, null),
	oracle(29, true, Race.Elf, elvenMage, null),
	elder(30, true, Race.Elf, oracle, null),

	darkFighter(31, false, Race.DarkElf, null, null),
	palusKnight(32, false, Race.DarkElf, darkFighter, null),
	shillienKnight(33, false, Race.DarkElf, palusKnight, null),
	bladedancer(34, false, Race.DarkElf, palusKnight, null),
	assassin(35, false, Race.DarkElf, darkFighter, null),
	abyssWalker(36, false, Race.DarkElf, assassin, null),
	phantomRanger(37, false, Race.DarkElf, assassin, null),

	darkMage(38, true, Race.DarkElf, null, null),
	darkWizard(39, true, Race.DarkElf, darkMage, null),
	spellhowler(40, true, Race.DarkElf, darkWizard, null),
	phantomSummoner(41, true, true, Race.DarkElf, darkWizard, null),
	shillienOracle(42, true, Race.DarkElf, darkMage, null),
	shillenElder(43, true, Race.DarkElf, shillienOracle, null),

	orcFighter(44, false, Race.Orc, null, null),
	orcRaider(45, false, Race.Orc, orcFighter, null),
	destroyer(46, false, Race.Orc, orcRaider, null),
	orcMonk(47, false, Race.Orc, orcFighter, null),
	tyrant(48, false, Race.Orc, orcMonk, null),

	orcMage(49, true, Race.Orc, null, null),
	orcShaman(50, true, Race.Orc, orcMage, null),
	overlord(51, true, Race.Orc, orcShaman, null),
	warcryer(52, true, Race.Orc, orcShaman, null),

	dwarvenFighter(53, false, Race.Dwarf, null, null),
	scavenger(54, false, Race.Dwarf, dwarvenFighter, null),
	bountyHunter(55, false, Race.Dwarf, scavenger, null),
	artisan(56, false, Race.Dwarf, dwarvenFighter, null),
	warsmith(57, false, Race.Dwarf, artisan, null),

	/*
	 * Dummy Entries (id's already in decimal format)
	 * btw FU NCSoft for the amount of work you put me
	 * through to do this!!
	 * <START>
	 */
	dummyEntry1(58, false, null, null, null),
	dummyEntry2(59, false, null, null, null),
	dummyEntry3(60, false, null, null, null),
	dummyEntry4(61, false, null, null, null),
	dummyEntry5(62, false, null, null, null),
	dummyEntry6(63, false, null, null, null),
	dummyEntry7(64, false, null, null, null),
	dummyEntry8(65, false, null, null, null),
	dummyEntry9(66, false, null, null, null),
	dummyEntry10(67, false, null, null, null),
	dummyEntry11(68, false, null, null, null),
	dummyEntry12(69, false, null, null, null),
	dummyEntry13(70, false, null, null, null),
	dummyEntry14(71, false, null, null, null),
	dummyEntry15(72, false, null, null, null),
	dummyEntry16(73, false, null, null, null),
	dummyEntry17(74, false, null, null, null),
	dummyEntry18(75, false, null, null, null),
	dummyEntry19(76, false, null, null, null),
	dummyEntry20(77, false, null, null, null),
	dummyEntry21(78, false, null, null, null),
	dummyEntry22(79, false, null, null, null),
	dummyEntry23(80, false, null, null, null),
	dummyEntry24(81, false, null, null, null),
	dummyEntry25(82, false, null, null, null),
	dummyEntry26(83, false, null, null, null),
	dummyEntry27(84, false, null, null, null),
	dummyEntry28(85, false, null, null, null),
	dummyEntry29(86, false, null, null, null),
	dummyEntry30(87, false, null, null, null),
	/*
	 * <END>
	 * Of Dummy entries
	 */

	/*
	 * Now the bad boys! new class ids :)) (3rd classes)
	 */
	duelist(88, false, Race.Human, gladiator, null),
	dreadnought(89, false, Race.Human, warlord, null),
	phoenixKnight(90, false, Race.Human, paladin, null),
	hellKnight(91, false, Race.Human, darkAvenger, null),
	sagittarius(92, false, Race.Human, hawkeye, null),
	adventurer(93, false, Race.Human, treasureHunter, null),
	archmage(94, true, Race.Human, sorceror, null),
	soultaker(95, true, Race.Human, necromancer, null),
	arcanaLord(96, true, true, Race.Human, warlock, null),
	cardinal(97, true, Race.Human, bishop, null),
	hierophant(98, true, Race.Human, prophet, null),

	evaTemplar(99, false, Race.Elf, templeKnight, null),
	swordMuse(100, false, Race.Elf, swordSinger, null),
	windRider(101, false, Race.Elf, plainsWalker, null),
	moonlightSentinel(102, false, Race.Elf, silverRanger, null),
	mysticMuse(103, true, Race.Elf, spellsinger, null),
	elementalMaster(104, true, true, Race.Elf, elementalSummoner, null),
	evaSaint(105, true, Race.Elf, elder, null),

	shillienTemplar(106, false, Race.DarkElf, shillienKnight, null),
	spectralDancer(107, false, Race.DarkElf, bladedancer, null),
	ghostHunter(108, false, Race.DarkElf, abyssWalker, null),
	ghostSentinel(109, false, Race.DarkElf, phantomRanger, null),
	stormScreamer(110, true, Race.DarkElf, spellhowler, null),
	spectralMaster(111, true, true, Race.DarkElf, phantomSummoner, null),
	shillienSaint(112, true, Race.DarkElf, shillenElder, null),

	titan(113, false, Race.Orc, destroyer, null),
	grandKhauatari(114, false, Race.Orc, tyrant, null),
	dominator(115, true, Race.Orc, overlord, null),
	doomcryer(116, true, Race.Orc, warcryer, null),

	fortuneSeeker(117, false, Race.Dwarf, bountyHunter, null),
	maestro(118, false, Race.Dwarf, warsmith, null),

	dummyEntry31(119, false, null, null, null),
	dummyEntry32(120, false, null, null, null),
	dummyEntry33(121, false, null, null, null),
	dummyEntry34(122, false, null, null, null),

	maleSoldier(123, false, Race.Kamael, null, null),
	femaleSoldier(124, false, Race.Kamael, null, null),
	trooper(125, false, Race.Kamael, maleSoldier, null),
	warder(126, false, Race.Kamael, femaleSoldier, null),
	berserker(127, false, Race.Kamael, trooper, null),
	maleSoulbreaker(128, false, Race.Kamael, trooper, null),
	femaleSoulbreaker(129, false, Race.Kamael, warder, null),
	arbalester(130, false, Race.Kamael, warder, null),
	doombringer(131, false, Race.Kamael, berserker, null),
	maleSoulhound(132, false, Race.Kamael, maleSoulbreaker, null),
	femaleSoulhound(133, false, Race.Kamael, femaleSoulbreaker, null),
	trickster(134, false, Race.Kamael, arbalester, null),
	inspector(135, false, Race.Kamael, warder, null), //DS: yes, both male/female inspectors use skills from warder
	judicator(136, false, Race.Kamael, inspector, null),

	dummyEntry35(137, false, null, null, null),
	dummyEntry36(138, false, null, null, null),

	/*
	 139  Sigil Knight  - Phoenix Knight, Hell Knight, Eva's Templar, Shillen Templar
	 140  Tyr Warrior   - Duelist, Titan, Grand Khavatari, Maestro, Doombringer, Drednought
	 141  Othell Rogue  - Adventurer, Wind Rider, Ghost Hunter, Fortune Seeker
	 142  Yuield Archer - Saggitarius, Moonlight Sentinel, Ghost Sentinel, Trickster
	 143  Pheo Wizard   - Archmage, Soultaker, Mystic muse, StormScreamer, SoulHound
	 144  Iss Enchanter - Hierophant, Doomcryer, Dominator, Sword Muse, Spectral Dancer
	 145  Win Summoner  - Arcana Lord, Elemental master, Spectral Master
	 146  Aeore Healer  - Cardinal, Eva's Saint, Shilien saint
	 */

	@Deprecated SigelKnight(139, false, null, null, null),
	@Deprecated TyrrWarrior(140, false, null, null, null),
	@Deprecated OthellRogue(141, false, null, null, null),
	@Deprecated YulArcher(142, false, null, null, null),
	@Deprecated FeohWizard(143, false, null, null, null),
	@Deprecated IssChanter(144, false, null, null, null),
	@Deprecated WynnSummoner(145, false, null, null, null),
	@Deprecated AeoreHealer(146, false, null, null, null),

	dummyEntry37(147, false, null, null, null),

	SigelKnight_PhoenixKnight(148, false, Race.Human, phoenixKnight, ClassType2.Knight_Group),
	SigelKnight_HellKnight(149, false, Race.Human, hellKnight, ClassType2.Knight_Group),
	SigelKnight_EvaTemplar(150, false, Race.Elf, evaTemplar, ClassType2.Knight_Group),
	SigelKnight_ShillienTemplar(151, false, Race.DarkElf, shillienTemplar, ClassType2.Knight_Group),

	TyrrWarrior_Duelist(152, false, Race.Human, duelist, ClassType2.Tyrr_Group),
	TyrrWarrior_Dreadnought(153, false, Race.Human, dreadnought, ClassType2.Tyrr_Group),
	TyrrWarrior_Titan(154, false, Race.Orc, titan, ClassType2.Tyrr_Group),
	TyrrWarrior_GrandKhavatari(155, false, Race.Orc, grandKhauatari, ClassType2.Tyrr_Group),
	TyrrWarrior_Maestro(156, false, Race.Dwarf, maestro, ClassType2.Tyrr_Group),
	TyrrWarrior_Doombringer(157, false, Race.Kamael, doombringer, ClassType2.Tyrr_Group),

	OthellRogue_Adventurer(158, false, Race.Human, adventurer, ClassType2.Rogue_Group),
	OthellRogue_WindRider(159, false, Race.Elf, windRider, ClassType2.Rogue_Group),
	OthellRogue_GhostHunter(160, false, Race.DarkElf, ghostHunter, ClassType2.Rogue_Group),
	OthellRogue_FortuneSeeker(161, false, Race.Dwarf, fortuneSeeker, ClassType2.Rogue_Group),

	YulArcher_Saggitarius(162, false, Race.Human, sagittarius, ClassType2.Archer_Group),
	YulArcher_MoonlightSentinel(163, false, Race.Elf, moonlightSentinel, ClassType2.Archer_Group),
	YulArcher_GhostSentinel(164, false, Race.DarkElf, ghostSentinel, ClassType2.Archer_Group),
	YulArcher_Trickster(165, false, Race.Kamael, trickster, ClassType2.Archer_Group),

	FeohWizard_Archmage(166, true, Race.Human, archmage, ClassType2.Feoh_Group),
	FeohWizard_Soultaker(167, true, Race.Human, soultaker, ClassType2.Feoh_Group),
	FeohWizard_MysticMuse(168, true, Race.Elf, mysticMuse, ClassType2.Feoh_Group),
	FeohWizard_StormScreamer(169, true, Race.DarkElf, stormScreamer, ClassType2.Feoh_Group),
	FeohWizard_Soulhound(170, true, Race.Kamael, maleSoulhound, ClassType2.Feoh_Group),

	IssEnchanter_Hierophant(171, false, Race.Human, hierophant, ClassType2.Iss_Group),
	IssEnchanter_SwordMuse(172, false, Race.Elf, swordMuse, ClassType2.Iss_Group),
	IssEnchanter_SpectralDancer(173, false, Race.DarkElf, spectralDancer, ClassType2.Iss_Group),
	IssEnchanter_Dominator(174, false, Race.Orc, dominator, ClassType2.Iss_Group),
	IssEnchanter_Doomcryer(175, false, Race.Orc, doomcryer, ClassType2.Iss_Group),

	WynnSummoner_ArcanaLord(176, true, true, Race.Human, arcanaLord, ClassType2.Summoner_Group),
	WynnSummoner_ElementalMaster(177, true, true, Race.Elf, elementalMaster, ClassType2.Summoner_Group),
	WynnSummoner_SpectralMaster(178, true, true, Race.DarkElf, spectralMaster, ClassType2.Summoner_Group),

	AeoreHealer_Cardinal(179, true, Race.Human, cardinal, ClassType2.Healer_Group),
	AeoreHealer_EvaSaint(180, true, Race.Elf, evaSaint, ClassType2.Healer_Group),
	AeoreHealer_ShillenSaint(181, true, Race.DarkElf, shillienSaint, ClassType2.Healer_Group),

	Arteas_Fighter(182, false, Race.Ertheia, null, ClassType2.Arteas_Group),
	Arteas_Wizard(183, true, Race.Ertheia, null, ClassType2.Arteas_Group),

	Marauder(184, false, Race.Ertheia, Arteas_Fighter, ClassType2.Arteas_Group),
	Cloud_Breaker(185, true, Race.Ertheia, Arteas_Wizard, ClassType2.Arteas_Group),

	Ripper(186, false, Race.Ertheia, Marauder, ClassType2.Arteas_Group),
	Stratomancer(187, true, Race.Ertheia, Cloud_Breaker, ClassType2.Arteas_Group),

	Eviscerator(188, false, Race.Ertheia, Ripper, ClassType2.Arteas_Group),
	Sayha_Seer(189, true, Race.Ertheia, Stratomancer, ClassType2.Arteas_Group);

	/* The Identifier of the Class */
	private final int _id;

	/* {@code true} if the class is a mage class */
	private final boolean _isMage;

	/* {@code true} if the class is a summoner class */
	private final boolean _isSummoner;

	/* The Race object of the class */
	private final Race _race;

	/* The parent ClassId or null if this class is a root */
	private final ClassId _parent;

    private final ClassType2 _type;

	/**
	 * Constructor of ClassId.
	 * @param pId
	 * @param pIsMage
	 * @param pRace
	 * @param pParent
	 */
	private ClassId(int pId, boolean pIsMage, Race pRace, ClassId pParent, ClassType2 type)
	{
		_id = pId;
		_isMage = pIsMage;
		_isSummoner = false;
		_race = pRace;
		_parent = pParent;
        _type = type;
	}

	/**
	 * Constructor of ClassId.
	 * @param pId
	 * @param pIsMage
	 * @param pIsSummoner
	 * @param pRace
	 * @param pParent
	 */
	private ClassId(int pId, boolean pIsMage, boolean pIsSummoner, Race pRace, ClassId pParent, ClassType2 type)
	{
		_id = pId;
		_isMage = pIsMage;
		_isSummoner = pIsSummoner;
		_race = pRace;
		_parent = pParent;
        _type = type;
	}

	public static ClassId getClassId(int cId)
	{
		try
		{
			return ClassId.values()[cId];
		}
		catch(Exception e)
		{
			return null;
		}
	}

	/**
	 * @return the Identifier of the Class.
	 */
	public int getId()
	{
		return _id;
	}

	/***
	 * Служит для удобства проверки на однотипные классы
	 * @return "родительский" ClassID для новых профессий
	 */
	public int getGeneralIdForAwaken()
	{
		return Util.getGeneralIdForAwaken(_id);
	}

	/**
	 * @return {@code true} if the class is a mage class.
	 */
	public boolean isMage()
	{
		return _isMage;
	}

	/**
	 * @return {@code true} if the class is a summoner class.
	 */
	public boolean isSummoner()
	{
		return _isSummoner;
	}

	/**
	 * @return the Race object of the class.
	 */
	public Race getRace()
	{
		return _race;
	}

	/**
	 * @param cid The parent ClassId to check
	 * @return {@code true} if this Class is a child of the selected ClassId.
	 */
	public boolean childOf(ClassId cid)
	{
		ClassId other = this;
		while(true)
		{
			if(other._parent == null)
			{
				return false;
			}

			if(other._parent == cid)
			{
				return true;
			}

			other = other._parent;
		}
	}

	/**
	 * @param cid The parent ClassId to check
	 * @return {@code true} if this Class is equal to the selected ClassId or a child of the selected ClassId.
	 */
	public boolean equalsOrChildOf(ClassId cid)
	{
		return this == cid || childOf(cid);
	}

	/**
	 * @return the child level of this Class (0=root, 1=child level 1...).
	 */
	public int level()
	{
    ClassLevel classLevel = getClassLevel();
		return classLevel == null ? 0 : classLevel.ordinal();
	}

	public ClassLevel getClassLevel()
	{
		return PlayerClass.VALUES[_id].getLevel();
	}

	public ClassType2 getType2()
	{
			return _type;
	}

	public final boolean isOfType2(ClassType2 type)
	{
			return _type == type;
	}

	public final String getName()
	{
			return Util.getGeneralAwakenName(getId());
	}

	/**
	 * @return its parent ClassId
	 */
	public ClassId getParent()
	{
		return _parent;
	}

	public static void main( String[] args )
	{
		for (ClassId classId : values())
		{
			System.out.println(classId.level());
		}
		values();
	}
}
