/*
 * Copyright (C) 2004-2013
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
package dwo.gameserver.model.skills.stats;

import java.util.NoSuchElementException;

/**
 * Enum of basic stats.
 * @author mkizub
 */
public enum Stats
{
	// HP / MP / CP
	MAX_HP("maxHp"),                        // Максимальное HP
	MAX_MP("maxMp"),                        // Максимальное MP
	MAX_CP("maxCp"),                        // Максимальное CP
	MAX_RECOVERABLE_HP("maxRecoverableHp"),    // Максимальное количество HP которое можно восстановить
	MAX_RECOVERABLE_MP("maxRecoverableMp"),    // Максимальное количество MP которое можно восстановить
	MAX_RECOVERABLE_CP("maxRecoverableCp"),    // Максимальное количество CP которое можно восстановить
	REGENERATE_HP_RATE("regHp"),            // Регенерация HP
	REGENERATE_MP_RATE("regMp"),            // Регенерация MP
	REGENERATE_CP_RATE("regCp"),            // Регенерация CP
	RECHARGE_MP_RATE("gainMp"),             // Эффективность восстановления цели МП
	HEAL_EFFECTIVNESS("gainHp"),            // Эффективность восстановления цели ХП
	HEAL_PROFICIENCY("giveHp"),             // Эффективность восстановления  ХП  ( себе )
	HEAL_STATIC_BONUS("bonusHp"),           // Эффективность лечения  ( статичного )
	HEAL_PERCENT_BONUS("hpHealPercentBonus"),           // Эффективность восстановления цели ХП mul для % хила
	LIMIT_HP("limitHp"),

	// Атака / Защита
	POWER_DEFENCE("pDef"),                        // Физ. Защита
	MAGIC_DEFENCE("mDef"),                        // Маг. Защита
	POWER_ATTACK("pAtk"),                        // Физ. Атака
	MAGIC_ATTACK("mAtk"),                        // Маг. Атака
	PHYSICAL_SKILL_POWER("physicalSkillPower"),    // Физ. Урон Скиллом(mul)
	PHYSICAL_SKILL_POWER_ADD("physicalSkillPowerAdd"),    // Физ. Урон Скиллом (all)
	POWER_ATTACK_SPEED("pAtkSpd"),                // Физ. Скорость Атаки
	MAGIC_ATTACK_SPEED("mAtkSpd"),                // Маг. Скорость Атаки
	ATK_REUSE("atkReuse"),                        // Задержка между ударами
	P_REUSE("pReuse"),                            // Откат Физ. Умений
	MAGIC_REUSE_RATE("mReuse"),                    // Откат Маг. Умений
	SHIELD_DEFENCE("sDef"),                        // Защита Щитом
	CRITICAL_DAMAGE("cAtk"),                    // Физ. Крит. Урон (mul)
	CRITICAL_DAMAGE_ADD("cAtkAdd"),                // Физ. Крит. Урон (add)
	MAGIC_CRIT_DMG("mCritPower"),                // Маг. Крит. Урон (mul)
	MAGIC_CRIT_DMG_ADD("mCritPowerAdd"),        // Маг. Крит. Урон (add)

	// PvP Бонус
	PVP_PHYSICAL_DMG("pvpPhysDmg"),                // PvP Физ. Урон (mul)
	PVP_PHYSICAL_DMG_ADD("pvpPhysDmgAdd"),        // PvP Физ. Урон (add)
	PVP_MAGICAL_DMG("pvpMagicalDmg"),            // PvP Маг. Урон (mul)
	PVP_PHYS_SKILL_DMG("pvpPhysSkillsDmg"),        // PvP Маг. Урон (mul)
	PVP_PHYSICAL_DEF("pvpPhysDef"),                // PvP Физ. Защита
	PVP_MAGICAL_DEF("pvpMagicalDef"),            // PvP Маг. Защита
	PVP_PHYS_SKILL_DEF("pvpPhysSkillsDef"),        // PvP Физ. Защита Умениями

	// PvE Бонус
	PVE_PHYSICAL_DMG("pvePhysDmg"),            // PvE Физ. Урон
	PVE_PHYS_SKILL_DMG("pvePhysSkillsDmg"),        // PvE Физ. Урон Скиллами
	PVE_BOW_DMG("pveBowDmg"),                    // PvE Физ. Урон Луком
	PVE_BOW_SKILL_DMG("pveBowSkillsDmg"),        // PvE Физ. Урон Скиллами Лучника
	PVE_MAGICAL_DMG("pveMagicalDmg"),            // PvE Маг. Урон

	// Рейт Атаки / Защиты
	EVASION_PHYSICAL_RATE("ratePhysicalEvas"),    // Шанс Физ. Уворота
	EVASION_MAGICAL_RATE("rateMagicalEvas"),    // Шанс Маг. Уворота
	P_SKILL_EVASION("pSkillEvas"),                // Шанс Физ. Уворота от Скилла
	CRIT_DAMAGE_EVASION("critDamEvas"),            // Уворот от Крит. Атаки
	SHIELD_RATE("rShld"),                        // Шанс Блока Щитом
	PCRITICAL_RATE("pCritRate"),                // Шанс Крит. Атаки
	BLOW_RATE("blowRate"),                        // Шанс Смертельной Атаки
	LETHAL_RATE("lethalRate"),                    // Шанс Смертельной Атаки
	MCRITICAL_RATE("mCritRate"),                // Шанс Маг. Атаки
	EXPSP_RATE("rExp"),                            // Рейт EXP и SP
	BONUS_EXP("bonusExp"),                        // Бонус EXP
	BONUS_SP("bonusSp"),                        // Бонус SP
	ATTACK_CANCEL("cancel"),                    // Шанс Отменить Физ. Атаку
	MAGIC_FAILURE_RATE("magicFailureRate"),        // Шанс Отменить Маг. Атаку

	// Точность / Дальность
	ACCURACY_PHYSICAL("accPhysical"),            // Физ. Точность
	ACCURACY_MAGICAL("accMagical"),                // Маг. Точность
	POWER_ATTACK_RANGE("pAtkRange"),            // Радиус Физ. Атаки
	MAGIC_ATTACK_RANGE("mAtkRange"),            // Радиус Маг. Атаки
	POWER_ATTACK_ANGLE("pAtkAngle"),            // Угол Физ. Атаки
	ATTACK_COUNT_MAX("atkCountMax"),            // Максимальное количество атакуемых целей
	RUN_SPEED("runSpd"),                        // Скорость Бега
	WALK_SPEED("walkSpd"),                        // Скорость Ходьбы

	// Базовые Параметры
	STAT_STR("STR"),                            // Параметр STR
	STAT_CON("CON"),                            // Параметр CON
	STAT_DEX("DEX"),                            // Параметр DEX
	STAT_INT("INT"),                            // Параметр INT
	STAT_WIT("WIT"),                            // Параметр WIT
	STAT_MEN("MEN"),                            // Параметр MEN
    STAT_LUC("LUC"),                            // Параметр LUC
    STAT_CHA("CHA"),                            // Параметр CHA

	// NEW GOD MODIFERS
	PHYSICAL_RESIST("physicalResist"),          // TODO  не сделанно
	MAGICAL_RESIST("magicalResist"),            // TODO  не сделанно

	// VARIOUS
	BREATH("breath"),
	FALL("fall"),
	AGGRESSION("aggression"),
	BLEED("bleed"),
	POISON("poison"),
	STUN("stun"),
	ROOT("root"),
	MOVEMENT("movement"),
	CONFUSION("confusion"),
	SLEEP("sleep"),

	// сопротивления
	AGGRESSION_VULN("aggressionVuln"),                  // TODO не сделанно
	BLEED_VULN("bleedVuln"),                            // сопротивление к Кровотечению  sub
	POISON_VULN("poisonVuln"),                          // сопротивление к отравлению  sub
	STUN_VULN("stunVuln"),                                // сопротивление к оглушающим атакам  sub
	PARALYZE_VULN("paralyzeVuln"),                      // сопротивление к параличу sub
	ROOT_VULN("rootVuln"),                              // сопротивление к удерживающим атакам  sub
	SLEEP_VULN("sleepVuln"),                            // сопротивление к усыпляющим атакам  sub
	PHYSICAL_BLOCKADE_VULN("physicalBlockadeVuln"),      // сопротивление к блокировки атакующих действий врагов sub
	BOSS_VULN("bossVuln"),                              // сопротивление к скилам босса ( trait == BOSS )  sub
	GUST_VULN("gustVuln"),                              // сопротивление к ветру ( trait == GUST ) sub
	DAMAGE_ZONE_VULN("damageZoneVuln"),                 // сопротивление к Урону от падений sub
	MOVEMENT_VULN("movementVuln"),                      // сопротивление к умениям, снижающим скорость sub
	CANCEL_VULN("cancelVuln"),                          // сопротивление к снятию усиливающих заклинаний sub
	DERANGEMENT_VULN("derangementVuln"),                // сопротивление к ментальным атакам  sub
	DEBUFF_VULN("debuffVuln"),                          // сопротивление к наложению ослабляющих заклинаний sub
	BUFF_VULN("buffVuln"),                              // сопротивление к Бафам ( нафига ??? )
	CRIT_VULN("critVuln"),                              // сопротивление к Крит. Атк ( mul не больше 1 !! )
	CRIT_ADD_VULN("critAddVuln"),                       // сопротивление к Крит. Защ. sub
	MAGIC_CRIT_VULN("magicCritVuln"),                   // сопротивление к Маг. Крит. Атк ( mul не больше 1 !! )
	MAGIC_DAMAGE_VULN("magicDamVul"),                   // сопротивление к магическому урону sub
	VALAKAS_VULN("valakasVuln"),                        // сопротивление к атаке Валакаса sub
	KNOCKBACK_VULN("knockBackVuln"),                    // сопротивление к отталкиванию  sub
	KNOCKDOWN_VULN("knockDownVuln"),                    // сопротивление к опрокидыванию  sub
	FLYUP_VULN("flyUpVuln"),                            // сопротивление к подъему  sub
	ATTRACT_VULN("attractVuln"),                        // сопротивление к притягиванию  sub
	REFLECT_VULN("reflectVuln"),                        // сопротивление к отражению урона sub
	ABSORB_VULN("absorbVuln"),                          // сопротивление вампиризму  sub      TODO сделать в ядре

	// Сопротивления элементам
	FIRE_RES("fireRes"),                                // защита Огонь
	WIND_RES("windRes"),                                // защита Ветер
	WATER_RES("waterRes"),                                // защита Вода
	EARTH_RES("earthRes"),                                // защита Земля
	HOLY_RES("holyRes"),                                // защита Святость
	DARK_RES("darkRes"),                                // защита Тьма
	MAGIC_SUCCESS_RES("magicSuccRes"),                    // ???
	//BUFF_IMMUNITY("buffImmunity"), 					//TODO: Implement me
	DEBUFF_IMMUNITY("debuffImmunity"),                    // иммунитет к дебфам

	// Атака Атрибутами
	FIRE_POWER("firePower"),                            // Атака Огонем
	WATER_POWER("waterPower"),                            // Атака Водой
	WIND_POWER("windPower"),                            // Атака Ветром
	EARTH_POWER("earthPower"),                            // Атака Землей
	HOLY_POWER("holyPower"),                            // Атака Святостью
	DARK_POWER("darkPower"),                            // Атака Тьмой

	// PROFICIENCY
	AGGRESSION_PROF("aggressionProf"),                  // TODO не сделанно
	BLEED_PROF("bleedProf"),                            // Шанс к Кровотечению add
	POISON_PROF("poisonProf"),                          // Шанс к Отравлению  add
	STUN_PROF("stunProf"),                              // Шанс к Оглушению  add
	PARALYZE_PROF("paralyzeProf"),                      // Шанс к Параличу  add
	ROOT_PROF("rootProf"),                              // Шанс к удерживающим атаки  add
	SLEEP_PROF("sleepProf"),                            // Шанс к усыпляющим атакам  add
	PROF("movementProf"),                               // Шанс к умениям, снижающим скорость add
	CANCEL_PROF("cancelProf"),                          // Шанс к снятию усиливающих заклинаний add
	DERANGEMENT_PROF("derangementProf"),                // Шанс к ментальным атакам add
	DEBUFF_PROF("debuffProf"),                          // Шанс к наложению ослабляющих заклинаний add
	CRIT_PROF("critProf"),                              // TODO не сделанно
	VALAKAS_PROF("valakasProf"),                        // Шанс к атаке Валакаса  add
	KNOCKDOWN_PROF("knockDownProf"),                    // Шанс к отталкиванию  add
	KNOCKBACK_PROF("knockBackProf"),                    // Шанс к опрокидыванию  add
	FLYUP_PROF("flyUpProf"),                            // Шанс к подъему  add
	ATTRACT_PROF("attractProf"),                        // Шанс к притягиванию  add

	// Уязвимости Оружия  mul
	NONE_WPN_VULN("noneWpnVuln"),                        // Не используется
	SWORD_WPN_VULN("swordWpnVuln"),                        // Уязвимость к Одноручному Мечу
	BLUNT_WPN_VULN("bluntWpnVuln"),                        // Уязвимость к Двуручному Мечу
	DUAL_BLUNT_WPN_VULN("dualBluntWpnVuln"),            // Уязвимость к Двуручному Молоту
	DAGGER_WPN_VULN("daggerWpnVuln"),                    // Уязвимость к Одноручным Кинжалам
	BOW_WPN_VULN("bowWpnVuln"),                            // Уязвимость к Луку
	CROSSBOW_WPN_VULN("crossbowWpnVuln"),                // Уязвимость к Арбалету
	POLE_WPN_VULN("poleWpnVuln"),                        // Уязвимость к Копью
	ETC_WPN_VULN("etcWpnVuln"),                            // Уязвимость к Остальному оружию
	FIST_WPN_VULN("fistWpnVuln"),                        // Уязвимость к Кастетам
	DUAL_WPN_VULN("dualWpnVuln"),                        // Уязвимость к Парным Мечам
	DUALFIST_WPN_VULN("dualFistWpnVuln"),                // Уязвимость к Кастетам
	BIGSWORD_WPN_VULN("bigSwordWpnVuln"),                // Уязвимость к Двуручному Мечу
	BIGBLUNT_WPN_VULN("bigBluntWpnVuln"),                // Уязвимость к Посоху
	DUALDAGGER_WPN_VULN("dualDaggerWpnVuln"),            // Уязвимость к Парным Кинжалам
	RAPIER_WPN_VULN("rapierWpnVuln"),                    // Уязвимость к Рапире
	ANCIENT_WPN_VULN("ancientWpnVuln"),                    // Уязвимость к Древнему Мечу
	PET_WPN_VULN("petWpnVuln"),                            // Уязвимость к оружию Питомцев

	REFLECT_DAMAGE_PERCENT("reflectDam"),               // Рефлект от автоатаки / скилов в %
	REFLECT_SKILL_MAGIC_DAMAGE("reflectSkillMagicDam"),   // отражение урона от магических скилов
	REFLECT_SKILL_PHYSIC_DAMAGE("reflectSkillPhysicDam"), // отражение урона от физических скилов
	REFLECT_SKILL_MAGIC("reflectSkillMagicPercent"),    // отражение дебафов от магических скилов в %
	REFLECT_SKILL_PHYSIC("reflectSkillPhysicPercent"),  // отражение дебафов от физических скилов в %
	VENGEANCE_SKILL_MAGIC_DAMAGE("vengeanceMdam"),      // Отражение маг  скилов ( 100% рефлект ) шанс то что отразит
	VENGEANCE_SKILL_PHYSICAL_DAMAGE("vengeancePdam"),   // Отражение физ скилов ( 100% рефлект ) шанс то что отразит
	ABSORB_DAMAGE_PERCENT("absorbDam"),                 // Вампирик ( урон в жизни ) для авто атаки и скилов
	TRANSFER_DAMAGE_PERCENT("transDamToPet"),                // Передает часть полученного урона в %    ( для петов )
	MANA_SHIELD_PERCENT("manaShield"),                  // Уменьшает получаемый урон и переносит оставшийся урон в расход маны  в %
	TRANSFER_DAMAGE_TO_PLAYER("transDamToPlayer"),      // Передача урона на кастующего в %
	ABSORB_MANA_DAMAGE_PERCENT("absorbDamMana"),        // С определенной вероятностью при атаке восстанавливает MP

	WEIGHT_LIMIT("weightLimit"),                        // Максимальный переносимый вес
	WEIGHT_PENALTY("weightPenalty"),                    // Штраф на переносимый вес

	// Уязвимость mul ( урон по типам мобов )
	PATK_PLANTS("pAtk-plants"),                         // Физ. Атака по Растениям
	PATK_INSECTS("pAtk-insects"),                       // Физ. Атака по Насекомым
	PATK_ANIMALS("pAtk-animals"),                       // Физ. Атака по Животным
	PATK_MONSTERS("pAtk-monsters"),                     // Физ. Атака по Чудовищам  ( зверям )
	PATK_DRAGONS("pAtk-dragons"),                       // Физ. Атака по Драконам
	PATK_GIANTS("pAtk-giants"),                         // Физ. Атака по Гигантам
	PATK_MCREATURES("pAtk-magicCreature"),              // Физ. Атака по Магическим Существам
	PATK_NPCS("pAtk-npcs"),                             // Физ. Атака по NPC ( всех типов )

	// Ослабление mul ( урон от типов мобов )
	PDEF_PLANTS("pDef-plants"),                         // Физ. Защита от Растениям
	PDEF_INSECTS("pDef-insects"),                       // Физ. Защита от Насекомым
	PDEF_ANIMALS("pDef-animals"),                       // Физ. Защита от Животным
	PDEF_MONSTERS("pDef-monsters"),                     // Физ. Защита от Чудовищам  ( зверям )
	PDEF_DRAGONS("pDef-dragons"),                       // Физ. Защита от Драконам
	PDEF_GIANTS("pDef-giants"),                         // Физ. Защита от Гигантам
	PDEF_MCREATURES("pDef-magicCreature"),              // Физ. Защита от Магическим Существам
	PDEF_NPCS("pDef-npcs"),                             // Физ. Защита от NPC ( всех типов )

	// Уязвимость mul ( урон по типам мобов )
	MATK_PLANTS("mAtk-plants"),                         // Маг. Атака по Растениям
	MATK_INSECTS("mAtk-insects"),                       // Маг. Атака по Насекомым
	MATK_ANIMALS("mAtk-animals"),                       // Маг. Атака по Животным
	MATK_MONSTERS("mAtk-monsters"),                     // Маг. Атака по Чудовищам  ( зверям )
	MATK_DRAGONS("mAtk-dragons"),                       // Маг. Атака по Драконам
	MATK_GIANTS("mAtk-giants"),                         // Маг. Атака по Гигантам
	MATK_MCREATURES("mAtk-magicCreature"),              // Маг. Атака по Магическим Существам
	MATK_NPCS("mAtk-npcs"),                             // Маг. Атака по NPC ( всех типов )

	// ExSkill
	INV_LIM("inventoryLimit"),                            // Количество ячеек инвентаря
	WH_LIM("whLimit"),                                    // Количество ячеек в хранилище
	FREIGHT_LIM("FreightLimit"),                        // Количество ячеек у витамин менеджера
	P_SELL_LIM("PrivateSellLimit"),                     // Количество ячеек в приватной продаже
	P_BUY_LIM("PrivateBuyLimit"),                       // Количество ячеек в приватной скупке
	REC_D_LIM("DwarfRecipeLimit"),                      // Количество ячеек в книге рецептов гномов
	REC_C_LIM("CommonRecipeLimit"),                     // Количество ячеек в книге обычного крафта ( для всех )
	SUMMON_POINTS("summonPoints"),                      // Количество очков призыва ( служат для призыва новый сумонов )
	CUBIC_MASTERY("cubicMastery"),                      // Максимальное количество призванных кубиков
	SOUL_MASTERY("soulMastery"),                        // Максимальное количество собраных душ ( у карамелек )
	CRAFT_MASTERY("craftMastery"),                      // Шанс при крафте скрафтить 2 предмета сразу
	ENERGY_MASTERY("energyMastery"),                    // Максимальное значение энергии ( зарядка )
	PK_PENALTY_CHANCE("pkPenaltyChance"),               // TODO
	MAX_SKILL_DAMAGE("maxSkillDamage"),                    // Максимальный Урон от Скилов

	// C4 Stats
	PHYSICAL_MP_CONSUME_RATE("PhysicalMpConsumeRate"),    // Потребление MP Физ. Умениями
	MAGICAL_MP_CONSUME_RATE("MagicalMpConsumeRate"),    // Потребление MP Маг. Умениями
	DANCE_MP_CONSUME_RATE("DanceMpConsumeRate"),        // Потребление МП Танцами
	BOW_MP_CONSUME_RATE("BowMpConsumeRate"),            // Потребление MP Луком
	HP_CONSUME_RATE("HpConsumeRate"),                    // Потребление HP
	MP_CONSUME("MpConsume"),                            // Потребление MP

	// T1 stats
	transformId("transformId"),                            // ID трансформации
	TALISMAN_SLOTS("talisman"),                            // Количество слотов под талисманы
    BROACH_SLOT("broach"),                                 // слот брошки
    STONE_SLOTS("stone"),                                 // Количество слотов под камни брошки
	CLOAK_SLOT("cloak"),                                // Плащ слот

	// Параметры Щита
	SHIELD_DEFENCE_ANGLE("shieldDefAngle"),                // Радиус Действия Щита

	// Мастер Скилла
	SKILL_MASTERY("skillMastery"),                        // Шанс мгновенного отката скилов

	// Виталити
	VITALITY_CONSUME_RATE("vitalityConsumeRate"),        // Количество потребления виталити

	// Модификаторы от Базовых Параметров
	RUN_SPEED_MODIFY_DEX("spdModDex"),                  // Увеличивает скорость бега в зависимости от DEX ( +1 = +1 к скорости )
	PCRITICAL_RATE_MODIFY_DEX("pCritRateModDex");       // Увеличивает шанс крита в зависимости от DEX ( +1 = +1 к криту )

	public static final int NUM_STATS = values().length;

    private final String _value;

	private Stats(String s)
	{
		_value = s;
	}

	public static Stats valueOfXml(String name)
	{
		name = name.intern();
		for(Stats s : values())
		{
			if(s._value.equals(name))
			{
				return s;
			}
		}

		throw new NoSuchElementException("Unknown name '" + name + "' for enum BaseStats");
	}

	public String getValue()
	{
		return _value;
	}
}