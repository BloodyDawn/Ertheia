package dwo.scripts.npc.fort;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.residence.fort.FortFacilityType;
import dwo.gameserver.model.world.residence.fort.FortState;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 12.11.12
 * Time: 0:17
 */

public class FortSupply extends Quest
{
	// Список нпц
	private static final int[] NPCs = {
		35664, 35696, 35733, 35765, 35802, 35833, 35865, 35902, 35934, 35972, 36009, 36041, 36079, 36116, 36147, 36179,
		36217, 36255, 36292, 36324, 36362
	};

	// Коробки поддержки форта
	private static final int[] SUPPLY_BOX_IDS = {
		35665, 35697, 35734, 35766, 35803, 35834, 35866, 35903, 35935, 35973, 36010, 36042, 36080, 36117, 36148, 36180,
		36218, 36256, 36293, 36325, 36363
	};

	private static final int ItemMedal = 9910;

	// Оплата апгрейда гвардов
	private static final int fee_power_up_lv1 = 100000;
	private static final int fee_power_up_lv2 = 250000;

	public FortSupply()
	{
		addAskId(NPCs, -297);
		addAskId(NPCs, -298);
		addAskId(NPCs, -271);
		addAskId(NPCs, -272);
	}

	public static void main(String[] args)
	{
		new FortSupply();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ArrayUtils.contains(NPCs, npc.getNpcId()))
		{
			switch(ask)
			{
				case -297: // Проверить защиту крепости
					if(reply == 1)
					{
						String content = HtmCache.getInstance().getHtm(player.getLang(), "default/fortress_supply_officer002.htm");
						content = content.replace("<?guard_level?>", String.valueOf(npc.getFort().getFacilityLevel(FortFacilityType.FORTRESS_GUARD_POWER_UP)));
						return content;
					}
					break;
				case -298:
					if(reply == 1) // Выдать оружие/доспехи высшего ранга 1
					{
						int currentFacility = npc.getFort().getFacilityLevel(FortFacilityType.FORTRESS_GUARD_POWER_UP);
						if(currentFacility > 0)
						{
							return "fortress_already_upgraded.htm";
						}
						if(player.getAdenaCount() >= fee_power_up_lv1)
						{
							player.reduceAdena(ProcessType.FORT, fee_power_up_lv1, npc, true);
							npc.getFort().setFacilityLevel(FortFacilityType.FORTRESS_GUARD_POWER_UP, 1);
							return "fortress_supply_officer006.htm";
						}
						else
						{
							return "fortress_not_enough_money.htm";
						}
					}
					if(reply == 2) // Выдать оружие/доспехи наивысшего ранга 2
					{
						int currentFacility = npc.getFort().getFacilityLevel(FortFacilityType.FORTRESS_GUARD_POWER_UP);
						if(currentFacility > 1)
						{
							return "fortress_already_upgraded.htm";
						}
						if(player.getAdenaCount() >= fee_power_up_lv2)
						{
							if(currentFacility >= 1)
							{
								player.reduceAdena(ProcessType.FORT, fee_power_up_lv2, npc, true);
								npc.getFort().setFacilityLevel(FortFacilityType.FORTRESS_GUARD_POWER_UP, 2);
								return "fortress_supply_officer006.htm";
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
					if(reply == 3)
					{
						int currentFacility = npc.getFort().getFacilityLevel(FortFacilityType.FORTRESS_GUARD_POWER_UP);
						if(currentFacility >= 2)
						{
							return "fortress_already_upgraded.htm";
						}
						if(player.getAdenaCount() >= fee_power_up_lv1 + fee_power_up_lv2)
						{
							if(currentFacility >= 2)
							{
								player.reduceAdena(ProcessType.FORT, fee_power_up_lv1 + fee_power_up_lv2, npc, true);
								npc.getFort().setFacilityLevel(FortFacilityType.FORTRESS_GUARD_POWER_UP, 2);
								npc.getFort().save();
								return "fortress_supply_officer006.htm";
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
					break;
				case -271: // Проверить поставки
					if(npc.getFort().getFortState() != FortState.CONTRACTED)
					{
						return "fortress_supply_officer005.htm";
					}
					if(!npc.isMyLord(player, true))
					{
						return "fortress_supply_officer004.htm";
					}
					if(npc.getFort().getSiege().isInProgress())
					{
						return "fortress_supply_officer007.htm";
					}
					if(reply == 0)
					{
						String content = HtmCache.getInstance().getHtm(player.getLang(), "default/fortress_supply_officer009.htm");
						content = content.replace("<?treasure_level?>", String.valueOf(npc.getFort().getSupplyLvL()));
						return content;
					}
					if(reply == 1)
					{
						int supplyLvL = npc.getFort().getSupplyLvL();

						if(supplyLvL > 0)
						{
							L2NpcTemplate BoxTemplate = NpcTable.getInstance().getTemplate(SUPPLY_BOX_IDS[supplyLvL - 1]);
							L2MonsterInstance box = new L2MonsterInstance(IdFactory.getInstance().getNextId(), BoxTemplate);
							box.setCurrentHp(box.getMaxHp());
							box.setCurrentMp(box.getMaxMp());
							box.setHeading(0);
							box.getLocationController().spawn(npc.getX() - 23, npc.getY() + 41, npc.getZ());

							npc.getFort().setSupplyLvL(0);
							npc.getFort().save();
							return "fortress_supply_officer016.htm";
						}
					}
					break;
				case -272: // Проверить награды
					if(!npc.isMyLord(player, true))
					{
						return "fortress_supply_officer004.htm";
					}
					switch(reply)
					{
						case 0:
							String content = HtmCache.getInstance().getHtm(player.getLang(), "default/fortress_supply_officer010.htm");
							content = content.replace("<?medal_level?>", String.valueOf(player.getClan().getBloodOathCount()));
							return content;
						case 1:
							int bloodCount = player.getClan().getBloodOathCount();
							if(bloodCount == 0)
							{
								return "fortress_no_treasure.htm";
							}
							else
							{
								player.addItem(ProcessType.FORT, ItemMedal, bloodCount, npc, true);
								player.getClan().resetBloodOathCount();
								return "fortress_supply_officer013.htm";
							}
					}
					break;
			}
		}
		return null;
	}
}