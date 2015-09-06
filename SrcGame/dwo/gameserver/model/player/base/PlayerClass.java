/*
 * $Header: PlayerClass.java, 24/11/2005 12:56:01 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 24/11/2005 12:56:01 $
 * $Revision: 1 $
 * $Log: PlayerClass.java,v $
 * Revision 1  24/11/2005 12:56:01  luisantonioa
 * Added copyright notice
 *
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.model.player.base;

import dwo.config.Config;
import dwo.gameserver.model.actor.instance.L2PcInstance;

import java.util.AbstractMap;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static dwo.gameserver.model.player.base.ClassLevel.FIRST;
import static dwo.gameserver.model.player.base.ClassLevel.AWAKEN;
import static dwo.gameserver.model.player.base.ClassLevel.SECOND;
import static dwo.gameserver.model.player.base.ClassLevel.THIRD;
import static dwo.gameserver.model.player.base.ClassLevel.NONE;
import static dwo.gameserver.model.player.base.ClassType.Fighter;
import static dwo.gameserver.model.player.base.ClassType.Mystic;
import static dwo.gameserver.model.player.base.ClassType.Priest;
import static dwo.gameserver.model.player.base.Race.*;

public enum PlayerClass
{
	HumanFighter(Human, Fighter, NONE),
	Warrior(Human, Fighter, FIRST),
	Gladiator(Human, Fighter, SECOND),
	Warlord(Human, Fighter, SECOND),
	HumanKnight(Human, Fighter, FIRST),
	Paladin(Human, Fighter, SECOND),
	DarkAvenger(Human, Fighter, SECOND),
	Rogue(Human, Fighter, FIRST),
	TreasureHunter(Human, Fighter, SECOND),
	Hawkeye(Human, Fighter, SECOND),
	HumanMystic(Human, Mystic, NONE),
	HumanWizard(Human, Mystic, FIRST),
	Sorceror(Human, Mystic, SECOND),
	Necromancer(Human, Mystic, SECOND),
	Warlock(Human, Mystic, SECOND),
	Cleric(Human, Priest, FIRST),
	Bishop(Human, Priest, SECOND),
	Prophet(Human, Priest, SECOND),

	ElvenFighter(Elf, Fighter, NONE),
	ElvenKnight(Elf, Fighter, FIRST),
	TempleKnight(Elf, Fighter, SECOND),
	Swordsinger(Elf, Fighter, SECOND),
	ElvenScout(Elf, Fighter, FIRST),
	Plainswalker(Elf, Fighter, SECOND),
	SilverRanger(Elf, Fighter, SECOND),
	ElvenMystic(Elf, Mystic, NONE),
	ElvenWizard(Elf, Mystic, FIRST),
	Spellsinger(Elf, Mystic, SECOND),
	ElementalSummoner(Elf, Mystic, SECOND),
	ElvenOracle(Elf, Priest, FIRST),
	ElvenElder(Elf, Priest, SECOND),

	DarkElvenFighter(DarkElf, Fighter, NONE),
	PalusKnight(DarkElf, Fighter, FIRST),
	ShillienKnight(DarkElf, Fighter, SECOND),
	Bladedancer(DarkElf, Fighter, SECOND),
	Assassin(DarkElf, Fighter, FIRST),
	AbyssWalker(DarkElf, Fighter, SECOND),
	PhantomRanger(DarkElf, Fighter, SECOND),
	DarkElvenMystic(DarkElf, Mystic, NONE),
	DarkElvenWizard(DarkElf, Mystic, FIRST),
	Spellhowler(DarkElf, Mystic, SECOND),
	PhantomSummoner(DarkElf, Mystic, SECOND),
	ShillienOracle(DarkElf, Priest, FIRST),
	ShillienElder(DarkElf, Priest, SECOND),

	OrcFighter(Orc, Fighter, NONE),
	OrcRaider(Orc, Fighter, FIRST),
	Destroyer(Orc, Fighter, SECOND),
	OrcMonk(Orc, Fighter, FIRST),
	Tyrant(Orc, Fighter, SECOND),
	OrcMystic(Orc, Mystic, NONE),
	OrcShaman(Orc, Mystic, FIRST),
	Overlord(Orc, Mystic, SECOND),
	Warcryer(Orc, Mystic, SECOND),

	DwarvenFighter(Dwarf, Fighter, NONE),
	DwarvenScavenger(Dwarf, Fighter, FIRST),
	BountyHunter(Dwarf, Fighter, SECOND),
	DwarvenArtisan(Dwarf, Fighter, FIRST),
	Warsmith(Dwarf, Fighter, SECOND),

	dummyEntry1(null, null, null),
	dummyEntry2(null, null, null),
	dummyEntry3(null, null, null),
	dummyEntry4(null, null, null),
	dummyEntry5(null, null, null),
	dummyEntry6(null, null, null),
	dummyEntry7(null, null, null),
	dummyEntry8(null, null, null),
	dummyEntry9(null, null, null),
	dummyEntry10(null, null, null),
	dummyEntry11(null, null, null),
	dummyEntry12(null, null, null),
	dummyEntry13(null, null, null),
	dummyEntry14(null, null, null),
	dummyEntry15(null, null, null),
	dummyEntry16(null, null, null),
	dummyEntry17(null, null, null),
	dummyEntry18(null, null, null),
	dummyEntry19(null, null, null),
	dummyEntry20(null, null, null),
	dummyEntry21(null, null, null),
	dummyEntry22(null, null, null),
	dummyEntry23(null, null, null),
	dummyEntry24(null, null, null),
	dummyEntry25(null, null, null),
	dummyEntry26(null, null, null),
	dummyEntry27(null, null, null),
	dummyEntry28(null, null, null),
	dummyEntry29(null, null, null),
	dummyEntry30(null, null, null),
	/*
	  * (3rd classes)
	  */
	duelist(Human, Fighter, THIRD),
	dreadnought(Human, Fighter, THIRD),
	phoenixKnight(Human, Fighter, THIRD),
	hellKnight(Human, Fighter, THIRD),
	sagittarius(Human, Fighter, THIRD),
	adventurer(Human, Fighter, THIRD),
	archmage(Human, Mystic, THIRD),
	soultaker(Human, Mystic, THIRD),
	arcanaLord(Human, Mystic, THIRD),
	cardinal(Human, Priest, THIRD),
	hierophant(Human, Priest, THIRD),

	evaTemplar(Elf, Fighter, THIRD),
	swordMuse(Elf, Fighter, THIRD),
	windRider(Elf, Fighter, THIRD),
	moonlightSentinel(Elf, Fighter, THIRD),
	mysticMuse(Elf, Mystic, THIRD),
	elementalMaster(Elf, Mystic, THIRD),
	evaSaint(Elf, Priest, THIRD),

	shillienTemplar(DarkElf, Fighter, THIRD),
	spectralDancer(DarkElf, Fighter, THIRD),
	ghostHunter(DarkElf, Fighter, THIRD),
	ghostSentinel(DarkElf, Fighter, THIRD),
	stormScreamer(DarkElf, Mystic, THIRD),
	spectralMaster(DarkElf, Mystic, THIRD),
	shillienSaint(DarkElf, Priest, THIRD),

	titan(Orc, Fighter, THIRD),
	grandKhauatari(Orc, Fighter, THIRD),
	dominator(Orc, Mystic, THIRD),
	doomcryer(Orc, Mystic, THIRD),

	fortuneSeeker(Dwarf, Fighter, THIRD),
	maestro(Dwarf, Fighter, THIRD),

	dummyEntry31(null, null, null),
	dummyEntry32(null, null, null),
	dummyEntry33(null, null, null),
	dummyEntry34(null, null, null),

	maleSoldier(Kamael, Fighter, NONE),
	femaleSoldier(Kamael, Fighter, NONE),
	trooper(Kamael, Fighter, FIRST),
	warder(Kamael, Fighter, FIRST),
	berserker(Kamael, Fighter, SECOND),
	maleSoulbreaker(Kamael, Fighter, SECOND),
	femaleSoulbreaker(Kamael, Fighter, SECOND),
	arbalester(Kamael, Fighter, SECOND),
	doombringer(Kamael, Fighter, THIRD),
	maleSoulhound(Kamael, Fighter, THIRD),
	femaleSoulhound(Kamael, Fighter, THIRD),
	trickster(Kamael, Fighter, THIRD),
	inspector(Kamael, Fighter, SECOND),
	judicator(Kamael, Fighter, THIRD),

	//GoD
	dummyEntry35(null, null, null),
	dummyEntry36(null, null, null),

	// Реальные клиентские классы 4-ой профы (139-146)
	SigelKnight(null, null, null),
	TyrrWarrior(null, null, null),
	OthellRogue(null, null, null),
	YulArcher(null, null, null),
	FeohWizard(null, null, null),
	IssChanter(null, null, null),
	WynnSummoner(null, null, null),
	AeoreHealer(null, null, null),

	dummyEntry37(null, null, null),

	// Серверные класссы 4-ой профы
	SigelKnight_PhoenixKnight(Human, Fighter, AWAKEN),
	SigelKnight_HellKnight(Human, Fighter, AWAKEN),
	SigelKnight_EvaTemplar(Elf, Fighter, AWAKEN),
	SigelKnight_ShillienTemplar(DarkElf, Fighter, AWAKEN),

	TyrrWarrior_Duelist(Human, Fighter, AWAKEN),
	TyrrWarrior_Titan(Orc, Fighter, AWAKEN),
	TyrrWarrior_GrandKhavatari(Orc, Fighter, AWAKEN),
	TyrrWarrior_Maestro(Dwarf, Fighter, AWAKEN),
	TyrrWarrior_Doombringer(Kamael, Fighter, AWAKEN),
	TyrrWarrior_Dreadnought(Human, Fighter, AWAKEN),

	OthellRogue_Adventurer(Human, Fighter, AWAKEN),
	OthellRogue_WindRider(Elf, Fighter, AWAKEN),
	OthellRogue_GhostHunter(DarkElf, Fighter, AWAKEN),
	OthellRogue_FortuneSeeker(Dwarf, Fighter, AWAKEN),

	YulArcher_Saggitarius(Human, Fighter, AWAKEN),
	YulArcher_MoonlightSentinel(Elf, Fighter, AWAKEN),
	YulArcher_GhostSentinel(DarkElf, Fighter, AWAKEN),
	YulArcher_Trickster(Kamael, Fighter, AWAKEN),

	FeohWizard_Archmage(Human, Mystic, AWAKEN),
	FeohWizard_Soultaker(Human, Mystic, AWAKEN),
	FeohWizard_MysticMuse(Elf, Mystic, AWAKEN),
	FeohWizard_StormScreamer(DarkElf, Mystic, AWAKEN),
	FeohWizard_Soulhound(Kamael, Mystic, AWAKEN),

	IssEnchanter_Hierophant(Human, Priest, AWAKEN),
	IssEnchanter_SwordMuse(Elf, Priest, AWAKEN),
	IssEnchanter_SpectralDancer(DarkElf, Priest, AWAKEN),
	IssEnchanter_Dominator(Orc, Priest, AWAKEN),
	IssEnchanter_Doomcryer(Orc, Priest, AWAKEN),

	WynnSummoner_ArcanaLord(Human, Mystic, AWAKEN),
	WynnSummoner_ElementalMaster(Elf, Mystic, AWAKEN),
	WynnSummoner_SpectralMaster(DarkElf, Mystic, AWAKEN),

	AeoreHealer_Cardinal(Human, Priest, AWAKEN),
	AeoreHealer_EvaSaint(Elf, Priest, AWAKEN),
	AeoreHealer_ShillenSaint(DarkElf, Priest, AWAKEN),

	ertheiaFighter( Ertheia, Fighter, FIRST),
	ertheiaWizzard( Ertheia, Mystic, FIRST),

	marauder( Ertheia, Fighter, SECOND),
	cloudBreaker( Ertheia, Mystic, SECOND),

	ripper( Ertheia, Fighter, THIRD),
	Stratomancer( Ertheia, Mystic, THIRD),

	eviscerator( Ertheia, Fighter, AWAKEN),
	sayhaSeer( Ertheia, Mystic, AWAKEN);

	public static final PlayerClass[] VALUES = values();
	private static final Set<PlayerClass> mainSubclassSet;
	private static final Set<PlayerClass> neverSubclassed = EnumSet.of(Overlord, Warsmith);
	private static final Set<PlayerClass> subclasseSet1 = EnumSet.of(DarkAvenger, Paladin, TempleKnight, ShillienKnight);
	private static final Set<PlayerClass> subclasseSet2 = EnumSet.of(TreasureHunter, AbyssWalker, Plainswalker);
	private static final Set<PlayerClass> subclasseSet3 = EnumSet.of(Hawkeye, SilverRanger, PhantomRanger);
	private static final Set<PlayerClass> subclasseSet4 = EnumSet.of(Warlock, ElementalSummoner, PhantomSummoner);
	private static final Set<PlayerClass> subclasseSet5 = EnumSet.of(Sorceror, Spellsinger, Spellhowler);
	private static final AbstractMap<PlayerClass, Set<PlayerClass>> subclassSetMap = new EnumMap<>(PlayerClass.class);

	static
	{
		Set<PlayerClass> subclasses = getSet(null, SECOND);
		subclasses.removeAll(neverSubclassed);

		mainSubclassSet = subclasses;

		subclassSetMap.put(DarkAvenger, subclasseSet1);
		subclassSetMap.put(Paladin, subclasseSet1);
		subclassSetMap.put(TempleKnight, subclasseSet1);
		subclassSetMap.put(ShillienKnight, subclasseSet1);

		subclassSetMap.put(TreasureHunter, subclasseSet2);
		subclassSetMap.put(AbyssWalker, subclasseSet2);
		subclassSetMap.put(Plainswalker, subclasseSet2);

		subclassSetMap.put(Hawkeye, subclasseSet3);
		subclassSetMap.put(SilverRanger, subclasseSet3);
		subclassSetMap.put(PhantomRanger, subclasseSet3);

		subclassSetMap.put(Warlock, subclasseSet4);
		subclassSetMap.put(ElementalSummoner, subclasseSet4);
		subclassSetMap.put(PhantomSummoner, subclasseSet4);

		subclassSetMap.put(Sorceror, subclasseSet5);
		subclassSetMap.put(Spellsinger, subclasseSet5);
		subclassSetMap.put(Spellhowler, subclasseSet5);
	}

	private final Race _race;
	private final ClassLevel _level;
	private final ClassType _type;

	PlayerClass(Race pRace, ClassType pType, ClassLevel pLevel)
	{
		_race = pRace;
		_level = pLevel;
		_type = pType;
	}

	public static EnumSet<PlayerClass> getSet(Race race, ClassLevel level)
	{
		EnumSet<PlayerClass> allOf = EnumSet.noneOf(PlayerClass.class);

		allOf.addAll(EnumSet.allOf(PlayerClass.class).stream().filter(playerClass -> race == null || playerClass.isOfRace(race)).collect(Collectors.toList()));

		return allOf;
	}

	public Set<PlayerClass> getAvailableSubclasses(L2PcInstance player)
	{
		Set<PlayerClass> subclasses = null;

        /* Добавил в проверке _level == Fourth, для того чтобы могли брать сабы после перерождения */
		if(_level == SECOND || _level == THIRD)
		{
            subclasses = EnumSet.copyOf(mainSubclassSet);
            subclasses.remove(this);
            subclasses.removeAll(getSet( Ertheia, SECOND));

            if(player.getRace() == Kamael)
			{
				subclasses = getSet(Kamael, SECOND);
				subclasses.remove(this);

				if(Config.MAX_SUBCLASS <= 3)
				{
					if(player.getAppearance().getSex())
					{
						subclasses.removeAll(EnumSet.of(femaleSoulbreaker));
					}
					else
					{
						subclasses.removeAll(EnumSet.of(maleSoulbreaker));
					}
				}
			}
			else
            {
                subclasses.removeAll(getSet(Kamael, SECOND));
            }

            Set<PlayerClass> unavailableClasses = subclassSetMap.get(this);

            if(unavailableClasses != null)
            {
                subclasses.removeAll(unavailableClasses);
            }
		}
		return subclasses;
	}

	public boolean isOfRace(Race pRace)
	{
		return _race == pRace;
	}

	public boolean isOfType(ClassType pType)
	{
		return _type == pType;
	}

	public boolean isOfLevel(ClassLevel pLevel)
	{
		return _level == pLevel;
	}

	public ClassLevel getLevel()
	{
		return _level;
	}
}
