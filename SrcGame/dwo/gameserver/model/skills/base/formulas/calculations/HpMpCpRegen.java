package dwo.gameserver.model.skills.base.formulas.calculations;

import dwo.config.Config;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.castle.CastleSiegeManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallManager;
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.actor.templates.L2CharBaseTemplate;
import dwo.gameserver.model.actor.templates.L2PcTemplate;
import dwo.gameserver.model.player.formation.clan.L2SiegeClan;
import dwo.gameserver.model.skills.stats.BaseStats;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.castle.CastleSiegeEngine;
import dwo.gameserver.model.world.residence.clanhall.ClanHall;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.model.world.residence.function.FunctionType;
import dwo.gameserver.model.world.zone.type.L2CastleZone;
import dwo.gameserver.model.world.zone.type.L2ClanHallZone;
import dwo.gameserver.model.world.zone.type.L2FortZone;
import dwo.gameserver.model.world.zone.type.L2MotherTreeZone;
import dwo.gameserver.util.Util;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 22:12
 */

public class HpMpCpRegen
{
	/**
	 * @param cha
	 * @return HP regen rate (base + modifiers)
	 */
	public static double calcHpRegen(L2Character cha)
	{
		double init = cha instanceof L2PcInstance ? ((L2PcTemplate) cha.getTemplate()).getBaseCharTemplate().getLevelData().get(L2CharBaseTemplate.LevelData.LevelDataType.HP_REGEN, cha.getLevel()) : cha.getTemplate().getBaseHpReg();
		double hpRegenMultiplier = cha.isRaid() ? Config.RAID_HP_REGEN_MULTIPLIER : Config.HP_REGEN_MULTIPLIER;
		double hpRegenBonus = 0;

		if(cha instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) cha;

			double siegeModifier = calcSiegeRegenModifier(player);
			if(siegeModifier > 0)
			{
				hpRegenMultiplier *= siegeModifier;
			}

			if(player.isInsideZone(L2Character.ZONE_CLANHALL) && player.getClan() != null && player.getClan().getClanhallId() > 0)
			{
				L2ClanHallZone zone = ZoneManager.getInstance().getZone(player, L2ClanHallZone.class);
				int posChIndex = zone == null ? -1 : zone.getClanHallId();
				int clanHallIndex = player.getClan().getClanhallId();
				if(clanHallIndex > 0 && clanHallIndex == posChIndex)
				{
					ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
					if(clansHall != null)
					{
						if(clansHall.getFunction(FunctionType.HP_REGEN) != null)
						{
							hpRegenMultiplier *= 1 + (double) clansHall.getFunctionLevel(FunctionType.HP_REGEN) / 100;
						}
					}
				}
			}

			if(player.isInsideZone(L2Character.ZONE_CASTLE) && player.getClan() != null && player.getClan().getCastleId() > 0)
			{
				L2CastleZone zone = ZoneManager.getInstance().getZone(player, L2CastleZone.class);
				int posCastleIndex = zone == null ? -1 : zone.getCastleId();
				int castleIndex = player.getClan().getCastleId();
				if(castleIndex > 0 && castleIndex == posCastleIndex)
				{
					Castle castle = CastleManager.getInstance().getCastleById(castleIndex);
					if(castle != null)
					{
						if(castle.getFunction(FunctionType.HP_REGEN) != null)
						{
							hpRegenMultiplier *= 1 + (double) castle.getFunctionLevel(FunctionType.HP_REGEN) / 100;
						}
					}
				}
			}

			if(player.isInsideZone(L2Character.ZONE_FORT) && player.getClan() != null && player.getClan().getFortId() > 0)
			{
				L2FortZone zone = ZoneManager.getInstance().getZone(player, L2FortZone.class);
				int posFortIndex = zone == null ? -1 : zone.getFortId();
				int fortIndex = player.getClan().getFortId();
				if(fortIndex > 0 && fortIndex == posFortIndex)
				{
					Fort fort = FortManager.getInstance().getFortById(fortIndex);
					if(fort != null)
					{
						if(fort.getFunction(FunctionType.HP_REGEN) != null)
						{
							hpRegenMultiplier *= 1 + (double) fort.getFunctionLevel(FunctionType.HP_REGEN) / 100;
						}
					}
				}
			}

			// Mother Tree effect is calculated at last
			if(player.isInsideZone(L2Character.ZONE_MOTHERTREE))
			{
				L2MotherTreeZone zone = ZoneManager.getInstance().getZone(player, L2MotherTreeZone.class);
				int hpBonus = zone == null ? 0 : zone.getHpRegenBonus();
				hpRegenBonus += hpBonus;
			}

			// Calculate Movement bonus
			if(player.isSitting())
			{
				hpRegenMultiplier *= 1.5; // Sitting
			}
			else if(!player.isMoving())
			{
				hpRegenMultiplier *= 1.1; // Staying
			}
			else if(player.isRunning())
			{
				hpRegenMultiplier *= 0.7; // Running
			}

			// Add CON bonus
			init *= cha.getLevelMod() * BaseStats.CON.calcBonus(cha);
		}
		else if(cha instanceof L2PetInstance)
		{
			init = ((L2PetInstance) cha).getPetLevelData().getPetRegenHP() * Config.PET_HP_REGEN_MULTIPLIER;
		}

		return cha.calcStat(Stats.REGENERATE_HP_RATE, Math.max(1, init), null, null) * hpRegenMultiplier + hpRegenBonus;
	}

	/**
	 * Calculate the MP regen rate (base + modifiers).
	 */
	public static double calcMpRegen(L2Character cha)
	{
		double init = cha instanceof L2PcInstance ? ((L2PcTemplate) cha.getTemplate()).getBaseCharTemplate().getLevelData().get(L2CharBaseTemplate.LevelData.LevelDataType.MP_REGEN, cha.getLevel()) : cha.getTemplate().getBaseMpReg();
		double mpRegenMultiplier = cha.isRaid() ? Config.RAID_MP_REGEN_MULTIPLIER : Config.MP_REGEN_MULTIPLIER;
		double mpRegenBonus = 0;

		if(cha instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) cha;

			// Mother Tree effect is calculated at last'
			if(player.isInsideZone(L2Character.ZONE_MOTHERTREE))
			{
				L2MotherTreeZone zone = ZoneManager.getInstance().getZone(player, L2MotherTreeZone.class);
				int mpBonus = zone == null ? 0 : zone.getMpRegenBonus();
				mpRegenBonus += mpBonus;
			}

			if(player.isInsideZone(L2Character.ZONE_CLANHALL) && player.getClan() != null && player.getClan().getClanhallId() > 0)
			{
				L2ClanHallZone zone = ZoneManager.getInstance().getZone(player, L2ClanHallZone.class);
				int posChIndex = zone == null ? -1 : zone.getClanHallId();
				int clanHallIndex = player.getClan().getClanhallId();
				if(clanHallIndex > 0 && clanHallIndex == posChIndex)
				{
					ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
					if(clansHall != null)
					{
						if(clansHall.getFunction(FunctionType.MP_REGEN) != null)
						{
							mpRegenMultiplier *= 1 + (double) clansHall.getFunctionLevel(FunctionType.MP_REGEN) / 100;
						}
					}
				}
			}

			if(player.isInsideZone(L2Character.ZONE_CASTLE) && player.getClan() != null && player.getClan().getCastleId() > 0)
			{
				L2CastleZone zone = ZoneManager.getInstance().getZone(player, L2CastleZone.class);
				int posCastleIndex = zone == null ? -1 : zone.getCastleId();
				int castleIndex = player.getClan().getCastleId();
				if(castleIndex > 0 && castleIndex == posCastleIndex)
				{
					Castle castle = CastleManager.getInstance().getCastleById(castleIndex);
					if(castle != null)
					{
						if(castle.getFunction(FunctionType.MP_REGEN) != null)
						{
							mpRegenMultiplier *= 1 + (double) castle.getFunctionLevel(FunctionType.MP_REGEN) / 100;
						}
					}
				}
			}

			if(player.isInsideZone(L2Character.ZONE_FORT) && player.getClan() != null && player.getClan().getFortId() > 0)
			{
				L2FortZone zone = ZoneManager.getInstance().getZone(player, L2FortZone.class);
				int posFortIndex = zone == null ? -1 : zone.getFortId();
				int fortIndex = player.getClan().getFortId();
				if(fortIndex > 0 && fortIndex == posFortIndex)
				{
					Fort fort = FortManager.getInstance().getFortById(fortIndex);
					if(fort != null)
					{
						if(fort.getFunction(FunctionType.MP_REGEN) != null)
						{
							mpRegenMultiplier *= 1 + (double) fort.getFunctionLevel(FunctionType.MP_REGEN) / 100;
						}
					}
				}
			}

			// Calculate Movement bonus
			if(player.isSitting())
			{
				mpRegenMultiplier *= 1.5; // Sitting
			}
			else if(!player.isMoving())
			{
				mpRegenMultiplier *= 1.1; // Staying
			}
			else if(player.isRunning())
			{
				mpRegenMultiplier *= 0.7; // Running
			}

			// Add MEN bonus
			init *= cha.getLevelMod() * BaseStats.MEN.calcBonus(cha);
		}
		else if(cha instanceof L2PetInstance)
		{
			init = ((L2PetInstance) cha).getPetLevelData().getPetRegenMP() * Config.PET_MP_REGEN_MULTIPLIER;
		}

		return cha.calcStat(Stats.REGENERATE_MP_RATE, Math.max(1, init), null, null) * mpRegenMultiplier + mpRegenBonus;
	}

	/**
	 * Calculate the CP regen rate (base + modifiers).
	 */
	public static double calcCpRegen(L2Character cha)
	{
		double init = cha instanceof L2PcInstance ? ((L2PcTemplate) cha.getTemplate()).getBaseCharTemplate().getLevelData().get(L2CharBaseTemplate.LevelData.LevelDataType.CP_REGEN, cha.getLevel()) : cha.getTemplate().getBaseHpReg();
		double cpRegenMultiplier = Config.CP_REGEN_MULTIPLIER;
		double cpRegenBonus = 0;

		if(cha instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) cha;

			// Calculate Movement bonus
			if(player.isSitting())
			{
				cpRegenMultiplier *= 1.5; // Sitting
			}
			else if(!player.isMoving())
			{
				cpRegenMultiplier *= 1.1; // Staying
			}
			else if(player.isRunning())
			{
				cpRegenMultiplier *= 0.7; // Running
			}
		}
		else
		{
			// Calculate Movement bonus
			if(!cha.isMoving())
			{
				cpRegenMultiplier *= 1.1; // Staying
			}
			else if(cha.isRunning())
			{
				cpRegenMultiplier *= 0.7; // Running
			}
		}

		// Apply CON bonus
		init *= cha.getLevelMod() * BaseStats.CON.calcBonus(cha);
		return cha.calcStat(Stats.REGENERATE_CP_RATE, Math.max(1, init), null, null) * cpRegenMultiplier + cpRegenBonus;
	}

	public static double calcSiegeRegenModifier(L2PcInstance activeChar)
	{
		if(activeChar == null || activeChar.getClan() == null)
		{
			return 0;
		}

		CastleSiegeEngine castleSiegeEngine = CastleSiegeManager.getInstance().getSiege(activeChar.getLocationController().getX(), activeChar.getLocationController().getY(), activeChar.getLocationController().getZ());
		if(castleSiegeEngine == null || !castleSiegeEngine.isInProgress())
		{
			return 0;
		}

		L2SiegeClan siegeClan = castleSiegeEngine.getAttackerClan(activeChar.getClan().getClanId());
		if(siegeClan == null || siegeClan.getFlag().isEmpty() || !Util.checkIfInRange(200, activeChar, siegeClan.getFlag().get(0), true))
		{
			return 0;
		}

		return 1.5; // If all is true, then modifer will be 50% more
	}
}
