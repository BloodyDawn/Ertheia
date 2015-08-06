package dwo.scripts.npc.town;

import dwo.gameserver.datatables.xml.MultiSellData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Evolve;
import dwo.gameserver.util.Util;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 13.11.12
 * Time: 13:14
 */

public class PetManager extends Quest
{
	// Список менеджеров питомцев
	private static final int[] PetManagers_ev = {
		33579, 31954, 31309, 31265, 31067, 30827, 30828, 30829, 30830, 30831, 30869, 30731
	};
	private static final int PetManagers_dev = 36478;

	// Ключники КХ, которые умеют апгрейдить питомцев
	private static final int[] PetManagers_ch = {
		35440, 35442, 35444, 35446, 35448, 35450, 35567, 35569, 35571, 35573, 35575, 35577, 35579
	};

	// Вспомогательные переменные из скриптов для обычного пет-пеннеджера
	private static final int item_baby_pet1 = 2375;
	private static final int item_grown_pet1 = 9882;
	private static final int class_id_baby_pet1 = 12077;

	private static final int item_baby_pet2 = 6648;
	private static final int item_grown_pet2 = 10311;
	private static final int class_id_baby_pet2 = 12780;

	private static final int item_baby_pet3 = 6650;
	private static final int item_grown_pet3 = 10313;
	private static final int class_id_baby_pet3 = 12781;

	private static final int item_baby_pet4 = 6649;
	private static final int item_grown_pet4 = 10312;
	private static final int class_id_baby_pet4 = 12782;

	private static final int item_baby_pet5 = 9882;
	private static final int item_grown_pet5 = 10426;
	private static final int class_id_baby_pet5 = 16025;

	public PetManager()
	{
		addAskId(PetManagers_ev, -1001);
		addAskId(PetManagers_ev, -1002);
		addAskId(PetManagers_ev, -506);
		addAskId(PetManagers_ev, -507);
		addAskId(PetManagers_dev, -1002);
		addAskId(PetManagers_ch, -1002);
		addFirstTalkId(PetManagers_ev);
	}

	public static void main(String[] args)
	{
		new PetManager();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		L2Summon currentSummon = null;
		L2ItemInstance petItem;

		if(ask == -506)
		{
			MultiSellData.getInstance().separateAndSend(212, player, npc);
		}
		else if(ask == -507)
		{
			MultiSellData.getInstance().separateAndSend(221, player, npc);
		}
		else if(ask == -1001)
		{
			switch(reply)
			{
				case 0:
					if(player.getItemsCount(7585) > 0)
					{
						player.destroyItemByItemId(ProcessType.EVOLVE, 7585, 1, npc, true);

						L2ItemInstance item = ItemTable.getInstance().createItem(ProcessType.EVOLVE, 6650, 1, player);
						item.setEnchantLevel(24);
						player.addItem(ProcessType.EVOLVE, item, npc, true);
						return "pet_manager_trade_pet.htm";
					}
					else
					{
						return "pet_manager_no_ticket.htm";
					}
				case 1:
					if(player.getItemsCount(7583) > 0)
					{
						player.destroyItemByItemId(ProcessType.EVOLVE, 7583, 1, npc, true);

						L2ItemInstance item = ItemTable.getInstance().createItem(ProcessType.EVOLVE, 6648, 1, player);
						item.setEnchantLevel(25);
						player.addItem(ProcessType.EVOLVE, item, npc, true);
						return "pet_manager_trade_pet.htm";
					}
					else
					{
						return "pet_manager_no_ticket.htm";
					}
				case 2:
					if(player.getItemsCount(7584) > 0)
					{
						player.destroyItemByItemId(ProcessType.EVOLVE, 7584, 1, npc, true);

						L2ItemInstance item = ItemTable.getInstance().createItem(ProcessType.EVOLVE, 6649, 1, player);
						item.setEnchantLevel(26);
						player.addItem(ProcessType.EVOLVE, item, npc, true);
						return "pet_manager_trade_pet.htm";
					}
					else
					{
						return "pet_manager_no_ticket.htm";
					}
			}
		}
		else if(ask == -1002)
		{
			if(ArrayUtils.contains(PetManagers_ev, npc.getNpcId())) // Стандартный апгрейд у пет-менеджеров
			{
				switch(reply)
				{
					case 1:
						if(player.getItemsCount(item_baby_pet1) >= 2)
						{
							return "pet_evolution_many_pet.htm";
						}
						if(player.getItemsCount(item_baby_pet1) <= 0 && player.getItemsCount(item_grown_pet1) > 0)
						{
							return "pet_evolution_no_pet.htm";
						}

						if(!player.getPets().isEmpty())
						{
							currentSummon = player.getPets().getFirst();
						}

						if(currentSummon == null)
						{
							return "pet_evolution_farpet.htm";
						}
						if(Util.calculateDistance(npc, currentSummon, true) >= 200)
						{
							return "pet_evolution_farpet.htm";
						}
						if(currentSummon.getNpcId() != class_id_baby_pet1)
						{
							return "pet_evolution_farpet.htm";
						}

						petItem = player.getInventory().getItemByItemId(item_baby_pet1);
						if(petItem != null)
						{
							if(petItem.getEnchantLevel() < 55)
							{
								return "pet_evolution_level.htm";
							}
							return Evolve.doEvolve(player, npc, item_baby_pet1, item_grown_pet1, 55) ? "pet_evolution_success.htm" : "pet_evolution_stopped.htm";
						}
						else
						{
							return "pet_evolution_no_pet.htm";
						}
					case 2:
						if(player.getItemsCount(item_baby_pet2) >= 2)
						{
							return "pet_evolution_many_pet.htm";
						}
						if(player.getItemsCount(item_baby_pet2) <= 0 && player.getItemsCount(item_grown_pet2) > 0)
						{
							return "pet_evolution_no_pet.htm";
						}

						if(!player.getPets().isEmpty())
						{
							currentSummon = player.getPets().getFirst();
						}

						if(currentSummon == null)
						{
							return "pet_evolution_farpet.htm";
						}
						if(Util.calculateDistance(npc, currentSummon, true) >= 200)
						{
							return "pet_evolution_farpet.htm";
						}
						if(currentSummon.getNpcId() != class_id_baby_pet2)
						{
							return "pet_evolution_farpet.htm";
						}

						petItem = player.getInventory().getItemByItemId(item_baby_pet2);
						if(petItem != null)
						{
							if(petItem.getEnchantLevel() < 55)
							{
								return "pet_evolution_level.htm";
							}
							return Evolve.doEvolve(player, npc, item_baby_pet2, item_grown_pet2, 55) ? "pet_evolution_success.htm" : "pet_evolution_stopped.htm";
						}
						else
						{
							return "pet_evolution_no_pet.htm";
						}
					case 3:
						if(player.getItemsCount(item_baby_pet3) >= 2)
						{
							return "pet_evolution_many_pet.htm";
						}
						if(player.getItemsCount(item_baby_pet3) <= 0 && player.getItemsCount(item_grown_pet3) > 0)
						{
							return "pet_evolution_no_pet.htm";
						}

						if(!player.getPets().isEmpty())
						{
							currentSummon = player.getPets().getFirst();
						}

						if(currentSummon == null)
						{
							return "pet_evolution_farpet.htm";
						}
						if(Util.calculateDistance(npc, currentSummon, true) >= 200)
						{
							return "pet_evolution_farpet.htm";
						}
						if(currentSummon.getNpcId() != class_id_baby_pet3)
						{
							return "pet_evolution_farpet.htm";
						}

						petItem = player.getInventory().getItemByItemId(item_baby_pet3);
						if(petItem != null)
						{
							if(petItem.getEnchantLevel() < 55)
							{
								return "pet_evolution_level.htm";
							}
							return Evolve.doEvolve(player, npc, item_baby_pet3, item_grown_pet3, 55) ? "pet_evolution_success.htm" : "pet_evolution_stopped.htm";
						}
						else
						{
							return "pet_evolution_no_pet.htm";
						}
					case 4:
						if(player.getItemsCount(item_baby_pet4) >= 2)
						{
							return "pet_evolution_many_pet.htm";
						}
						if(player.getItemsCount(item_baby_pet4) <= 0 && player.getItemsCount(item_grown_pet4) > 0)
						{
							return "pet_evolution_no_pet.htm";
						}

						if(!player.getPets().isEmpty())
						{
							currentSummon = player.getPets().getFirst();
						}

						if(currentSummon == null)
						{
							return "pet_evolution_farpet.htm";
						}
						if(Util.calculateDistance(npc, currentSummon, true) >= 200)
						{
							return "pet_evolution_farpet.htm";
						}
						if(currentSummon.getNpcId() != class_id_baby_pet4)
						{
							return "pet_evolution_farpet.htm";
						}

						petItem = player.getInventory().getItemByItemId(item_baby_pet4);
						if(petItem != null)
						{
							if(petItem.getEnchantLevel() < 55)
							{
								return "pet_evolution_level.htm";
							}
							return Evolve.doEvolve(player, npc, item_baby_pet4, item_grown_pet4, 55) ? "pet_evolution_success.htm" : "pet_evolution_stopped.htm";
						}
						else
						{
							return "pet_evolution_no_pet.htm";
						}
					case 5:
						if(player.getItemsCount(item_baby_pet5) >= 2)
						{
							return "pet_evolution_many_pet.htm";
						}
						if(player.getItemsCount(item_baby_pet5) <= 0 && player.getItemsCount(item_grown_pet5) > 0)
						{
							return "pet_evolution_no_pet.htm";
						}

						currentSummon = null;
						if(!player.getPets().isEmpty())
						{
							currentSummon = player.getPets().getFirst();
						}

						if(currentSummon == null)
						{
							return "pet_evolution_farpet.htm";
						}
						if(Util.calculateDistance(npc, currentSummon, true) >= 200)
						{
							return "pet_evolution_farpet.htm";
						}
						if(currentSummon.getNpcId() != class_id_baby_pet5)
						{
							return "pet_evolution_farpet.htm";
						}

						petItem = player.getInventory().getItemByItemId(item_baby_pet5);
						if(petItem != null)
						{
							if(petItem.getEnchantLevel() < 70)
							{
								return "pet_evolution_level.htm";
							}
							return Evolve.doEvolve(player, npc, item_baby_pet5, item_grown_pet5, 70) ? "pet_evolution_success.htm" : "pet_evolution_stopped.htm";
						}
						else
						{
							return "pet_evolution_no_pet.htm";
						}
				}
			}
			else if(npc.getNpcId() == PetManagers_dev) // Даунгрейд петов, полученных в КХ
			{
				switch(reply)
				{
					case 1:
						if(player.getItemsCount(item_grown_pet1) >= 2)
						{
							return "pet_devolution_many_pet.htm";
						}
						if(player.getItemsCount(10307) <= 0)
						{
							return "pet_devolution_no_pet.htm";
						}
						if(!player.getPets().isEmpty())
						{
							return "pet_devolution_farpet.htm";
						}
						return Evolve.doRestore(player, npc, 10307, 9882, 55) ? "pet_devolution_success.htm" : "pet_devolution_failed.htm";
					case 2:
						if(player.getItemsCount(4422) >= 2)
						{
							return "pet_devolution_many_pet.htm";
						}
						if(player.getItemsCount(10308) <= 0)
						{
							return "pet_devolution_no_pet.htm";
						}
						if(!player.getPets().isEmpty())
						{
							return "pet_devolution_farpet.htm";
						}
						return Evolve.doRestore(player, npc, 10308, 4422, 55) ? "pet_devolution_success.htm" : "pet_devolution_failed.htm";
					case 3:
						if(player.getItemsCount(4423) >= 2)
						{
							return "pet_devolution_many_pet.htm";
						}
						if(player.getItemsCount(10309) <= 0)
						{
							return "pet_devolution_no_pet.htm";
						}
						if(!player.getPets().isEmpty())
						{
							return "pet_devolution_farpet.htm";
						}
						return Evolve.doRestore(player, npc, 10309, 4423, 55) ? "pet_devolution_success.htm" : "pet_devolution_failed.htm";
					case 4:
						if(player.getItemsCount(4424) >= 2)
						{
							return "pet_devolution_many_pet.htm";
						}
						if(player.getItemsCount(10310) <= 0)
						{
							return "pet_devolution_no_pet.htm";
						}
						if(!player.getPets().isEmpty())
						{
							return "pet_devolution_farpet.htm";
						}
						return Evolve.doRestore(player, npc, 10310, 4424, 55) ? "pet_devolution_success.htm" : "pet_devolution_failed.htm";
					case 5:
						if(player.getItemsCount(10426) >= 2)
						{
							return "pet_devolution_many_pet.htm";
						}
						if(player.getItemsCount(10611) <= 0)
						{
							return "pet_devolution_no_pet.htm";
						}
						if(!player.getPets().isEmpty())
						{
							return "pet_devolution_farpet.htm";
						}
						return Evolve.doRestore(player, npc, 10611, 10426, 70) ? "pet_devolution_success.htm" : "pet_devolution_failed.htm";
				}
			}
			else if(ArrayUtils.contains(PetManagers_ch, npc.getNpcId()))
			{
				switch(reply)
				{
					case 1:
						if(player.getItemsCount(9882) >= 2)
						{
							return "pet_evolution_many_pet.htm";
						}
						if(player.getItemsCount(9882) <= 0 && player.getItemsCount(10307) > 0)
						{
							return "pet_evolution_no_pet.htm";
						}

						if(!player.getPets().isEmpty())
						{
							currentSummon = player.getPets().getFirst();
						}

						if(currentSummon == null)
						{
							return "pet_evolution_farpet.htm";
						}
						if(Util.calculateDistance(npc, currentSummon, true) >= 200)
						{
							return "pet_evolution_farpet.htm";
						}
						if(currentSummon.getNpcId() != 16025)
						{
							return "pet_evolution_farpet.htm";
						}

						petItem = player.getInventory().getItemByItemId(9882);
						if(petItem != null)
						{
							if(petItem.getEnchantLevel() < 55)
							{
								return "pet_evolution_level.htm";
							}
							return Evolve.doEvolve(player, npc, 9882, 10307, 55) ? "pet_evolution_success.htm" : "pet_evolution_stopped.htm";
						}
						else
						{
							return "pet_evolution_no_pet.htm";
						}
					case 2:
						if(player.getItemsCount(4422) >= 2)
						{
							return "pet_evolution_many_pet.htm";
						}
						if(player.getItemsCount(4422) <= 0 && player.getItemsCount(10308) > 0)
						{
							return "pet_evolution_no_pet.htm";
						}

						if(!player.getPets().isEmpty())
						{
							currentSummon = player.getPets().getFirst();
						}

						if(currentSummon == null)
						{
							return "pet_evolution_farpet.htm";
						}
						if(Util.calculateDistance(npc, currentSummon, true) >= 200)
						{
							return "pet_evolution_farpet.htm";
						}
						if(currentSummon.getNpcId() != 12526)
						{
							return "pet_evolution_farpet.htm";
						}

						petItem = player.getInventory().getItemByItemId(4422);
						if(petItem != null)
						{
							if(petItem.getEnchantLevel() < 55)
							{
								return "pet_evolution_level.htm";
							}
							return Evolve.doEvolve(player, npc, 4422, 10308, 55) ? "pet_evolution_success.htm" : "pet_evolution_stopped.htm";
						}
						else
						{
							return "pet_evolution_no_pet.htm";
						}
					case 3:
						if(player.getItemsCount(4423) >= 2)
						{
							return "pet_evolution_many_pet.htm";
						}
						if(player.getItemsCount(4423) <= 0 && player.getItemsCount(10309) > 0)
						{
							return "pet_evolution_no_pet.htm";
						}

						if(!player.getPets().isEmpty())
						{
							currentSummon = player.getPets().getFirst();
						}

						if(currentSummon == null)
						{
							return "pet_evolution_farpet.htm";
						}
						if(Util.calculateDistance(npc, currentSummon, true) >= 200)
						{
							return "pet_evolution_farpet.htm";
						}
						if(currentSummon.getNpcId() != 12527)
						{
							return "pet_evolution_farpet.htm";
						}

						petItem = player.getInventory().getItemByItemId(4423);
						if(petItem != null)
						{
							if(petItem.getEnchantLevel() < 55)
							{
								return "pet_evolution_level.htm";
							}
							return Evolve.doEvolve(player, npc, 4423, 10309, 55) ? "pet_evolution_success.htm" : "pet_evolution_stopped.htm";
						}
						else
						{
							return "pet_evolution_no_pet.htm";
						}
					case 4:
						if(player.getItemsCount(4424) >= 2)
						{
							return "pet_evolution_many_pet.htm";
						}
						if(player.getItemsCount(4424) <= 0 && player.getItemsCount(10310) > 0)
						{
							return "pet_evolution_no_pet.htm";
						}

						if(!player.getPets().isEmpty())
						{
							currentSummon = player.getPets().getFirst();
						}

						if(currentSummon == null)
						{
							return "pet_evolution_farpet.htm";
						}
						if(Util.calculateDistance(npc, currentSummon, true) >= 200)
						{
							return "pet_evolution_farpet.htm";
						}
						if(currentSummon.getNpcId() != 12528)
						{
							return "pet_evolution_farpet.htm";
						}

						petItem = player.getInventory().getItemByItemId(4424);
						if(petItem != null)
						{
							if(petItem.getEnchantLevel() < 55)
							{
								return "pet_evolution_level.htm";
							}
							return Evolve.doEvolve(player, npc, 4424, 10310, 55) ? "pet_evolution_success.htm" : "pet_evolution_stopped.htm";
						}
						else
						{
							return "pet_evolution_no_pet.htm";
						}
					case 5:
						if(player.getItemsCount(10426) >= 2)
						{
							return "pet_evolution_many_pet.htm";
						}
						if(player.getItemsCount(10426) <= 0 && player.getItemsCount(10611) > 0)
						{
							return "pet_evolution_no_pet.htm";
						}

						if(!player.getPets().isEmpty())
						{
							currentSummon = player.getPets().getFirst();
						}

						if(currentSummon == null)
						{
							return "pet_evolution_farpet.htm";
						}
						if(Util.calculateDistance(npc, currentSummon, true) >= 200)
						{
							return "pet_evolution_farpet.htm";
						}
						if(currentSummon.getNpcId() != 16041)
						{
							return "pet_evolution_farpet.htm";
						}

						petItem = player.getInventory().getItemByItemId(10426);
						if(petItem != null)
						{
							if(petItem.getEnchantLevel() < 55)
							{
								return "pet_evolution_level.htm";
							}
							return Evolve.doEvolve(player, npc, 10426, 10611, 55) ? "pet_evolution_success.htm" : "pet_evolution_stopped.htm";
						}
						else
						{
							return "pet_evolution_no_pet.htm";
						}
				}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(player.hasBadReputation())
		{
			return npc.getServerName() + "006.htm";
		}
		return npc.getServerName() + "001.htm";
	}
}