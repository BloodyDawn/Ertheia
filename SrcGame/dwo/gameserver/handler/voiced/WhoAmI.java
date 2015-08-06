package dwo.gameserver.handler.voiced;

import dwo.config.Config;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.type.L2WeaponType;
import dwo.gameserver.model.skills.base.formulas.calculations.HpMpCpRegen;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import org.apache.log4j.Level;

/**
 * Who am I command handler.
 * Who am I is player full statistics window.
 * // TODO: May be need to clean up this class [Yorie]
 *
 * @author Keiichi
 * @author Yorie
 */
public class WhoAmI extends CommandHandler<String>
{
	private static final boolean USE_STATIC_HTML = true;
	private static final String HTML = HtmCache.getInstance().getHtm(null, "mods/command/whoami.htm");

	@TextCommand
	public boolean whoAmI(HandlerParams<String> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		String htmContent = HTML != null && !HTML.isEmpty() ? HTML : HtmCache.getInstance().getHtm(activeChar.getLang(), "mods/command/whoami.htm");

		try
		{
			L2Character trgt = null;

			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
			npcHtmlMessage.setHtml(htmContent);

			// Regen Cp Hp Mp
			double hpRegen = HpMpCpRegen.calcHpRegen(activeChar);
			double cpRegen = HpMpCpRegen.calcCpRegen(activeChar);
			double mpRegen = HpMpCpRegen.calcMpRegen(activeChar);

			// Absorb Hp Mp Damage
			double hpDrain = activeChar.calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0.0, trgt, null);
			double mpDrain = activeChar.calcStat(Stats.ABSORB_MANA_DAMAGE_PERCENT, 0.0, trgt, null);

			// Gain Cp Hp Mp
			double hpGain = activeChar.calcStat(Stats.HEAL_EFFECTIVNESS, 100.0, trgt, null);
			double mpGain = activeChar.calcStat(Stats.RECHARGE_MP_RATE, 100.0, trgt, null);
			double cpGain = activeChar.calcStat(Stats.REGENERATE_CP_RATE, 100.0, trgt, null);

			// Crit
			double critPerc = 2 * activeChar.calcStat(Stats.CRITICAL_DAMAGE, 0.0, trgt, null);
			double critAdd = activeChar.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0.0, trgt, null);
			double mCritRate = activeChar.calcStat(Stats.MCRITICAL_RATE, 0.0, trgt, null);

			double blowRate = activeChar.calcStat(Stats.BLOW_RATE, 0.0, trgt, null);

			L2ItemInstance shld = activeChar.getSecondaryWeaponInstance();
			boolean shield = shld != null && shld.getItemType() == L2WeaponType.NONE;

			double shieldDef = shield ? activeChar.calcStat(Stats.SHIELD_DEFENCE, activeChar.getTemplate().getBaseShldDef(), trgt, null) : 0.0;
			double shieldRate = shield ? activeChar.calcStat(Stats.SHIELD_RATE, 0.0, trgt, null) : 0.0;

			double xpRate = Config.RATE_XP;
			double spRate = Config.RATE_SP;
			double dropRate = Config.RATE_DROP_ITEMS;
			double adenaRate = 0;
			double spoilRate = Config.RATE_DROP_SPOIL;

			double fireResist = activeChar.calcStat(Stats.FIRE_RES, 0, trgt, null);
			double windResist = activeChar.calcStat(Stats.WIND_RES, 0, trgt, null);
			double waterResist = activeChar.calcStat(Stats.WATER_RES, 0, trgt, null);
			double earthResist = activeChar.calcStat(Stats.EARTH_RES, 0, trgt, null);
			double holyResist = activeChar.calcStat(Stats.HOLY_RES, 0, trgt, null);
			double unholyResist = activeChar.calcStat(Stats.DARK_RES, 0, trgt, null);

			double bleedPower = activeChar.calcStat(Stats.BLEED, 0, trgt, null);
			double bleedResist = activeChar.calcStat(Stats.BLEED_VULN, 0, trgt, null);
			double poisonPower = activeChar.calcStat(Stats.POISON, 0, trgt, null);
			double poisonResist = activeChar.calcStat(Stats.POISON_VULN, 0, trgt, null);
			double stunPower = activeChar.calcStat(Stats.STUN, 0, trgt, null);
			double stunResist = activeChar.calcStat(Stats.STUN_VULN, 0, trgt, null);
			double rootPower = activeChar.calcStat(Stats.ROOT, 0, trgt, null);
			double rootResist = activeChar.calcStat(Stats.ROOT_VULN, 0, trgt, null);
			double sleepPower = activeChar.calcStat(Stats.SLEEP, 0, trgt, null);
			double sleepResist = activeChar.calcStat(Stats.SLEEP_VULN, 0, trgt, null);
			double paralyzePower = activeChar.calcStat(Stats.PARALYZE_PROF, 0, trgt, null); //?
			double paralyzeResist = activeChar.calcStat(Stats.PARALYZE_VULN, 0, trgt, null);
			double mentalPower = 0; //?
			double mentalResist = 0; //?
			double debuffPower = activeChar.calcStat(Stats.DEBUFF_PROF, 0, trgt, null); //?
			double debuffResist = activeChar.calcStat(Stats.DEBUFF_VULN, 0, trgt, null);
			double cancelPower = activeChar.calcStat(Stats.CANCEL_PROF, 0, trgt, null); //?
			double cancelResist = activeChar.calcStat(Stats.CANCEL_VULN, 0, trgt, null);

			double swordResist = 100.0 - activeChar.calcStat(Stats.SWORD_WPN_VULN, 0, trgt, null);
			double dualResist = 100.0 - activeChar.calcStat(Stats.DUAL_WPN_VULN, 0, trgt, null);
			double bluntResist = 100.0 - activeChar.calcStat(Stats.BLUNT_WPN_VULN, 0, trgt, null);
			double daggerResist = 100.0 - activeChar.calcStat(Stats.DAGGER_WPN_VULN, 0, trgt, null);
			double bowResist = 100.0 - activeChar.calcStat(Stats.BOW_WPN_VULN, 0, trgt, null);
			double crossbowResist = 100.0 - activeChar.calcStat(Stats.CROSSBOW_WPN_VULN, 0, trgt, null);
			double poleResist = 100.0 - activeChar.calcStat(Stats.POLE_WPN_VULN, 0, trgt, null);
			double fistResist = 100.0 - activeChar.calcStat(Stats.FIST_WPN_VULN, 0, trgt, null);

			double critChanceResist = 100.0 - activeChar.calcStat(Stats.CRIT_VULN, 0, trgt, null);
			double critDamResistStatic = activeChar.calcStat(Stats.CRIT_ADD_VULN, 0, trgt, null);
			double critDamResist = 100.0 - 100 * (activeChar.calcStat(Stats.CRIT_ADD_VULN, 1.0, trgt, null) - critDamResistStatic);

			// Regen Cp Hp Mp
			npcHtmlMessage.replace("%hpRegen%", String.valueOf(hpRegen));
			npcHtmlMessage.replace("%cpRegen%", String.valueOf(cpRegen));
			npcHtmlMessage.replace("%mpRegen%", String.valueOf(mpRegen));

			// Absorb Damage
			npcHtmlMessage.replace("%hpDrain%", String.valueOf(hpDrain));
			npcHtmlMessage.replace("%mpDrain%", String.valueOf(mpDrain));

			// Gain Cp Hp Mp
			npcHtmlMessage.replace("%hpGain%", String.valueOf(hpGain));
			npcHtmlMessage.replace("%mpGain%", String.valueOf(mpGain));
			npcHtmlMessage.replace("%cpGain%", String.valueOf(cpGain));

			npcHtmlMessage.replace("%critPerc%", String.valueOf(critPerc));
			npcHtmlMessage.replace("%critStatic%", String.valueOf(critAdd));
			npcHtmlMessage.replace("%mCritRate%", String.valueOf(mCritRate));
			npcHtmlMessage.replace("%blowRate%", String.valueOf(blowRate));
			npcHtmlMessage.replace("%shieldDef%", String.valueOf(shieldDef));
			npcHtmlMessage.replace("%shieldRate%", String.valueOf(shieldRate));
			npcHtmlMessage.replace("%xpRate%", String.valueOf(xpRate));
			npcHtmlMessage.replace("%spRate%", String.valueOf(spRate));
			npcHtmlMessage.replace("%dropRate%", String.valueOf(dropRate));
			npcHtmlMessage.replace("%adenaRate%", String.valueOf(adenaRate));
			npcHtmlMessage.replace("%spoilRate%", String.valueOf(spoilRate));
			npcHtmlMessage.replace("%fireResist%", String.valueOf(fireResist));
			npcHtmlMessage.replace("%windResist%", String.valueOf(windResist));
			npcHtmlMessage.replace("%waterResist%", String.valueOf(waterResist));
			npcHtmlMessage.replace("%earthResist%", String.valueOf(earthResist));
			npcHtmlMessage.replace("%holyResist%", String.valueOf(holyResist));
			npcHtmlMessage.replace("%darkResist%", String.valueOf(unholyResist));
			npcHtmlMessage.replace("%bleedPower%", String.valueOf(bleedPower));
			npcHtmlMessage.replace("%bleedResist%", String.valueOf(bleedResist));
			npcHtmlMessage.replace("%poisonPower%", String.valueOf(poisonPower));
			npcHtmlMessage.replace("%poisonResist%", String.valueOf(poisonResist));
			npcHtmlMessage.replace("%stunPower%", String.valueOf(stunPower));
			npcHtmlMessage.replace("%stunResist%", String.valueOf(stunResist));
			npcHtmlMessage.replace("%rootPower%", String.valueOf(rootPower));
			npcHtmlMessage.replace("%rootResist%", String.valueOf(rootResist));
			npcHtmlMessage.replace("%sleepPower%", String.valueOf(sleepPower));
			npcHtmlMessage.replace("%sleepResist%", String.valueOf(sleepResist));
			npcHtmlMessage.replace("%paralyzePower%", String.valueOf(paralyzePower));
			npcHtmlMessage.replace("%paralyzeResist%", String.valueOf(paralyzeResist));
			npcHtmlMessage.replace("%mentalPower%", String.valueOf(mentalPower));
			npcHtmlMessage.replace("%mentalResist%", String.valueOf(mentalResist));
			npcHtmlMessage.replace("%debuffPower%", String.valueOf(debuffPower));
			npcHtmlMessage.replace("%debuffResist%", String.valueOf(debuffResist));
			npcHtmlMessage.replace("%cancelPower%", String.valueOf(cancelPower));
			npcHtmlMessage.replace("%cancelResist%", String.valueOf(cancelResist));
			npcHtmlMessage.replace("%swordResist%", String.valueOf(swordResist));
			npcHtmlMessage.replace("%dualResist%", String.valueOf(dualResist));
			npcHtmlMessage.replace("%bluntResist%", String.valueOf(bluntResist));
			npcHtmlMessage.replace("%daggerResist%", String.valueOf(daggerResist));
			npcHtmlMessage.replace("%bowResist%", String.valueOf(bowResist));
			npcHtmlMessage.replace("%crossbowResist%", String.valueOf(crossbowResist));
			npcHtmlMessage.replace("%fistResist%", String.valueOf(fistResist));
			npcHtmlMessage.replace("%poleResist%", String.valueOf(poleResist));
			npcHtmlMessage.replace("%critChanceResist%", String.valueOf(critChanceResist));
			npcHtmlMessage.replace("%critDamResist%", String.valueOf(critDamResist));

			activeChar.sendPacket(npcHtmlMessage);
		}
		catch(Exception e)
		{
			log.log(Level.ERROR, "Wrong WhoAmI voiced!: " + e);
		}

		return true;
	}
}
