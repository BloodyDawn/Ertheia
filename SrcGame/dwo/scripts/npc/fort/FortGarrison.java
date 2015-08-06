package dwo.scripts.npc.fort;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.residence.fort.FortFacilityType;
import dwo.gameserver.model.world.residence.fort.FortState;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 16.01.13
 * Time: 18:25
 */

public class FortGarrison extends Quest
{
	private static final int[] NPCs = {
		35660, 35691, 35729, 35760, 35798, 35829, 35860, 35898, 35929, 35967, 36005, 36036, 36074, 36112, 36143, 36174,
		36212, 36250, 36288, 36319, 36357
	};

	// Переменные из скриптов
	int fortress_id;
	int fee_photocannon = 300000;
	int fee_scout = 300000;
	int fee_door = 400000;
	int fee_guard_reinforce_lv1 = 100000;
	int fee_guard_reinforce_lv2 = 250000;
	String fnGuardLevel = "fortress_guard_lv.htm";
	int hasPledgePower = 19;
	String fnIsUnderSiege = "";
	String fnNotMyLord = "";
	String fnNoAuthority = "";
	String fnNoCastleContract = "";
	String fnAfterSetFacility = "";
	String fnUpgradeSuccess = "";
	String fnUpgradeFail = "";

	public FortGarrison()
	{
		addFirstTalkId(NPCs);
	}

	public static void main(String[] args)
	{
		new FortGarrison();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.getFort().getFortState() != FortState.CONTRACTED)
		{
			return fnNoCastleContract;
		}
		if(npc.isMyLord(player, true))
		{
			if(npc.getFort().getSiege().isInProgress())
			{
				return fnIsUnderSiege;
			}
		}
		else
		{
			return fnNoAuthority;
		}
		if(ask == -291) // Посмотреть информацию о страже
		{
			if(reply == 1)
			{
				String content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnGuardLevel);
				int i0 = npc.getFort().getFacilityLevel(FortFacilityType.FORTRESS_GUARD_REINFORCEMENT);
				content = content.replace("<?reinforce_level?>", String.valueOf(i0));
				i0 = npc.getFort().getFacilityLevel(FortFacilityType.FORTRESS_PHOTOCANNON);
				content = content.replace("<?photocannon_level?>", String.valueOf(i0));
				i0 = npc.getFort().getFacilityLevel(FortFacilityType.FORTRESS_SCOUT);
				content = content.replace("<?scout_level?>", String.valueOf(i0));
				i0 = npc.getFort().getFacilityLevel(FortFacilityType.FORTRESS_DOOR_POWER_UP);
				content = content.replace("<?door_level?>", String.valueOf(i0));
				return content;
			}
		}
		else if(ask == -292) // Нанять Артиллеристов Гномов
		{
			if(reply == 1)
			{
				int i0 = npc.getFort().getFacilityLevel(FortFacilityType.FORTRESS_PHOTOCANNON);
				if(i0 > 0)
				{
					return "fortress_already_upgraded.htm";
				}
				i0 = fee_photocannon;
				if(player.getAdenaCount() >= i0)
				{
					player.reduceAdena(ProcessType.FORT, i0, npc, true);
					npc.getFort().setFacilityLevel(FortFacilityType.FORTRESS_PHOTOCANNON, 1);
					return fnAfterSetFacility;
				}
				else
				{
					return "fortress_not_enough_money.htm";
				}
			}
		}
		else if(ask == -293) // Распределить наблюдателей
		{
			if(reply == 1)
			{
				int i0 = npc.getFort().getFacilityLevel(FortFacilityType.FORTRESS_SCOUT);
				if(i0 > 0)
				{
					return "fortress_already_upgraded.htm";
				}
				i0 = fee_scout;
				if(player.getAdenaCount() >= i0)
				{
					player.reduceAdena(ProcessType.FORT, i0, npc, true);
					npc.getFort().setFacilityLevel(FortFacilityType.FORTRESS_SCOUT, 1);
					return fnAfterSetFacility;
				}
				else
				{
					return "fortress_not_enough_money.htm";
				}
			}
		}
		else if(ask == -294) // Усилить ворота крепости
		{
			if(reply == 1)
			{
				int i0 = npc.getFort().getFacilityLevel(FortFacilityType.FORTRESS_DOOR_POWER_UP);
				if(i0 > 0)
				{
					return "fortress_already_upgraded.htm";
				}
				i0 = fee_door;
				if(player.getAdenaCount() >= i0)
				{
					player.reduceAdena(ProcessType.FORT, i0, npc, true);
					npc.getFort().setFacilityLevel(FortFacilityType.FORTRESS_DOOR_POWER_UP, 1);
					return fnAfterSetFacility;
				}
				else
				{
					return "fortress_not_enough_money.htm";
				}
			}
		}
		else if(ask == -295) // Усилить охрану
		{
			if(reply == 1)
			{
				int i0 = npc.getFort().getFacilityLevel(FortFacilityType.FORTRESS_GUARD_REINFORCEMENT);
				if(i0 > 0)
				{
					return "fortress_already_upgraded.htm";
				}
				i0 = fee_guard_reinforce_lv1;
				if(player.getAdenaCount() >= i0)
				{
					player.reduceAdena(ProcessType.FORT, i0, npc, true);
					npc.getFort().setFacilityLevel(FortFacilityType.FORTRESS_GUARD_REINFORCEMENT, 1);
					return fnAfterSetFacility;
				}
				else
				{
					return "fortress_not_enough_money.htm";
				}
			}
			else if(reply == 2)
			{
				int i0 = npc.getFort().getFacilityLevel(FortFacilityType.FORTRESS_GUARD_REINFORCEMENT);
				if(i0 > 1)
				{
					return "fortress_already_upgraded.htm";
				}
				i0 = fee_guard_reinforce_lv2;
				if(player.getAdenaCount() >= i0)
				{
					if(npc.getFort().getFacilityLevel(FortFacilityType.FORTRESS_GUARD_REINFORCEMENT) >= 1)
					{
						player.reduceAdena(ProcessType.FORT, i0, npc, true);
						npc.getFort().setFacilityLevel(FortFacilityType.FORTRESS_GUARD_REINFORCEMENT, 2);
						return fnAfterSetFacility;
					}
					else
					{
						return "fortress_not_lv1.htm";
					}
				}
				else
				{
					return "fortress_not_enough_money.htm";
				}
			}
			else if(reply == 3)
			{
				int i0 = npc.getFort().getFacilityLevel(FortFacilityType.FORTRESS_GUARD_REINFORCEMENT);
				if(i0 >= 2)
				{
					return "fortress_already_upgraded.htm";
				}
				i0 = fee_guard_reinforce_lv1 + fee_guard_reinforce_lv2;
				if(player.getAdenaCount() >= i0)
				{
					player.reduceAdena(ProcessType.FORT, i0, npc, true);
					npc.getFort().setFacilityLevel(FortFacilityType.FORTRESS_GUARD_REINFORCEMENT, 2);
					return fnAfterSetFacility;
				}
				else
				{
					return "fortress_not_enough_money.htm";
				}
			}
		}
		return null;
	}
}
