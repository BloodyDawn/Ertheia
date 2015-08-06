package dwo.scripts.npc.town;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 17.11.12
 * Time: 3:44
 */

public class Weaver extends Quest
{
	// Вспомогательные переменные из скриптов
	private static final int fee_for_release_pin_c = 3200;
	private static final int fee_for_release_pin_b = 11800;
	private static final int fee_for_release_pin_a = 26500;
	private static final int fee_for_release_pin_s = 136600;
	private static final int fee_for_release_pou_c = 3200;
	private static final int fee_for_release_pou_b = 11800;
	private static final int fee_for_release_pou_a = 26500;
	private static final int fee_for_release_pou_s = 136600;
	private static final int fee_for_release_rune_a = 26500;
	private static final int fee_for_release_rune_s = 136600;
	private static final int fee_for_release_deco_a = 26500;
	private static final int fee_for_release_deco_s = 136600;

	// Персонажи-обменщики
	private static final int[] NPCs = {
		32610, 32612
	};

	public Weaver()
	{
		addAskId(NPCs, 23010);
		addAskId(NPCs, 23020);
		addAskId(NPCs, 23030);
		addAskId(NPCs, 23040);
	}

	public static void main(String[] args)
	{
		new Weaver();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == 23010)
		{
			if(player.getAdenaCount() > 0)
			{
				if(reply == 1)
				{
					int i0 = Rnd.get(200);
					if(player.getItemsCount(13898) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, 13898, 1, npc, true);
						player.destroyItemByItemId(ProcessType.NPC, PcInventory.ADENA_ID, fee_for_release_pin_c, npc, true);
						if(i0 <= 1)
						{
							player.addItem(ProcessType.NPC, 13905, 1, npc, true);
						}
						else if(i0 <= 10)
						{
							player.addItem(ProcessType.NPC, 13904, 1, npc, true);
						}
						else if(i0 <= 40)
						{
							player.addItem(ProcessType.NPC, 13903, 1, npc, true);
						}
						else if(i0 <= 100)
						{
							player.addItem(ProcessType.NPC, 13902, 1, npc, true);
						}
						else
						{
							npc.broadcastPacket(new NS(npc, ChatType.NPC_ALL, 1300162));
						}
					}
					else
					{
						return "weaver_wolf_adams005.htm";
					}
				}
				else if(reply == 2)
				{
					int i0 = Rnd.get(200);
					if(player.getItemsCount(13899) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, 13899, 1, npc, true);
						player.destroyItemByItemId(ProcessType.NPC, PcInventory.ADENA_ID, fee_for_release_pin_b, npc, true);
						if(i0 <= 1)
						{
							player.addItem(ProcessType.NPC, 13909, 1, npc, true);
						}
						else if(i0 <= 10)
						{
							player.addItem(ProcessType.NPC, 13908, 1, npc, true);
						}
						else if(i0 <= 40)
						{
							player.addItem(ProcessType.NPC, 13907, 1, npc, true);
						}
						else if(i0 <= 100)
						{
							player.addItem(ProcessType.NPC, 13906, 1, npc, true);
						}
						else
						{
							npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), 1300162));
						}
					}
					else
					{
						return "weaver_wolf_adams005.htm";
					}
				}
				else if(reply == 3)
				{
					int i0 = Rnd.get(200);
					if(player.getItemsCount(13900) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, 13900, 1, npc, true);
						player.destroyItemByItemId(ProcessType.NPC, PcInventory.ADENA_ID, fee_for_release_pin_a, npc, true);
						if(i0 <= 1)
						{
							player.addItem(ProcessType.NPC, 13913, 1, npc, true);
						}
						else if(i0 <= 10)
						{
							player.addItem(ProcessType.NPC, 13912, 1, npc, true);
						}
						else if(i0 <= 40)
						{
							player.addItem(ProcessType.NPC, 13911, 1, npc, true);
						}
						else if(i0 <= 100)
						{
							player.addItem(ProcessType.NPC, 13910, 1, npc, true);
						}
						else
						{
							npc.broadcastPacket(new NS(npc, ChatType.NPC_ALL, 1300162));
						}
					}
					else
					{
						return "weaver_wolf_adams005.htm";
					}
				}
				else if(reply == 4)
				{
					int i0 = Rnd.get(200);
					if(player.getItemsCount(13901) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, 13901, 1, npc, true);
						player.destroyItemByItemId(ProcessType.NPC, PcInventory.ADENA_ID, fee_for_release_pin_s, npc, true);
						if(i0 <= 1)
						{
							player.addItem(ProcessType.NPC, 13917, 1, npc, true);
						}
						else if(i0 <= 10)
						{
							player.addItem(ProcessType.NPC, 13916, 1, npc, true);
						}
						else if(i0 <= 40)
						{
							player.addItem(ProcessType.NPC, 13915, 1, npc, true);
						}
						else if(i0 <= 100)
						{
							player.addItem(ProcessType.NPC, 13914, 1, npc, true);
						}
						else
						{
							npc.broadcastPacket(new NS(npc, ChatType.NPC_ALL, 1300162));
						}
					}
					else
					{
						return "weaver_wolf_adams005.htm";
					}
				}
			}
			else
			{
				return "weaver_wolf_adams006.htm";
			}
		}
		else if(ask == 23020)
		{
			if(player.getAdenaCount() > 0)
			{
				if(reply == 1)
				{
					int i0 = Rnd.get(200);
					if(player.getItemsCount(13918) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, 13918, 1, npc, true);
						player.destroyItemByItemId(ProcessType.NPC, PcInventory.ADENA_ID, fee_for_release_pou_c, npc, true);
						if(i0 <= 1)
						{
							player.addItem(ProcessType.NPC, 13925, 1, npc, true);
						}
						else if(i0 <= 10)
						{
							player.addItem(ProcessType.NPC, 13924, 1, npc, true);
						}
						else if(i0 <= 40)
						{
							player.addItem(ProcessType.NPC, 13923, 1, npc, true);
						}
						else if(i0 <= 100)
						{
							player.addItem(ProcessType.NPC, 13922, 1, npc, true);
						}
						else
						{
							npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), 1300162));
						}
					}
					else
					{
						return "weaver_wolf_adams005.htm";
					}
				}
				else if(reply == 2)
				{
					int i0 = Rnd.get(200);
					if(player.getItemsCount(13919) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, 13919, 1, npc, true);
						player.destroyItemByItemId(ProcessType.NPC, PcInventory.ADENA_ID, fee_for_release_pou_b, npc, true);
						if(i0 <= 1)
						{
							player.addItem(ProcessType.NPC, 13929, 1, npc, true);
						}
						else if(i0 <= 10)
						{
							player.addItem(ProcessType.NPC, 13928, 1, npc, true);
						}
						else if(i0 <= 40)
						{
							player.addItem(ProcessType.NPC, 13927, 1, npc, true);
						}
						else if(i0 <= 100)
						{
							player.addItem(ProcessType.NPC, 13926, 1, npc, true);
						}
						else
						{
							npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), 1300162));
						}
					}
					else
					{
						return "weaver_wolf_adams005.htm";
					}
				}
				else if(reply == 3)
				{
					int i0 = Rnd.get(200);
					if(player.getItemsCount(13920) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, 13920, 1, npc, true);
						player.destroyItemByItemId(ProcessType.NPC, PcInventory.ADENA_ID, fee_for_release_pou_a, npc, true);
						if(i0 <= 1)
						{
							player.addItem(ProcessType.NPC, 13933, 1, npc, true);
						}
						else if(i0 <= 10)
						{
							player.addItem(ProcessType.NPC, 13932, 1, npc, true);
						}
						else if(i0 <= 40)
						{
							player.addItem(ProcessType.NPC, 13931, 1, npc, true);
						}
						else if(i0 <= 100)
						{
							player.addItem(ProcessType.NPC, 13930, 1, npc, true);
						}
						else
						{
							npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), 1300162));
						}
					}
					else
					{
						return "weaver_wolf_adams005.htm";
					}
				}
				else if(reply == 4)
				{
					int i0 = Rnd.get(200);
					if(player.getItemsCount(13921) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, 13921, 1, npc, true);
						player.destroyItemByItemId(ProcessType.NPC, PcInventory.ADENA_ID, fee_for_release_pou_s, npc, true);
						if(i0 <= 1)
						{
							player.addItem(ProcessType.NPC, 13937, 1, npc, true);
						}
						else if(i0 <= 10)
						{
							player.addItem(ProcessType.NPC, 13936, 1, npc, true);
						}
						else if(i0 <= 40)
						{
							player.addItem(ProcessType.NPC, 13935, 1, npc, true);
						}
						else if(i0 <= 100)
						{
							player.addItem(ProcessType.NPC, 13934, 1, npc, true);
						}
						else
						{
							npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), 1300162));
						}
					}
					else
					{
						return "weaver_wolf_adams005.htm";
					}
				}
			}
			else
			{
				return "weaver_wolf_adams006.htm";
			}
		}
		else if(ask == 23030)
		{
			if(player.getAdenaCount() > 0)
			{
				if(reply == 1)
				{
					int i0 = Rnd.get(200);
					if(player.getItemsCount(14902) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, 14902, 1, npc, true);
						player.destroyItemByItemId(ProcessType.NPC, PcInventory.ADENA_ID, fee_for_release_rune_a, npc, true);
						if(i0 <= 1)
						{
							player.addItem(ProcessType.NPC, 14909, 1, npc, true);
						}
						else if(i0 <= 10)
						{
							player.addItem(ProcessType.NPC, 14908, 1, npc, true);
						}
						else if(i0 <= 40)
						{
							player.addItem(ProcessType.NPC, 14907, 1, npc, true);
						}
						else if(i0 <= 100)
						{
							player.addItem(ProcessType.NPC, 14906, 1, npc, true);
						}
						else
						{
							npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), 1300162));
						}
					}
					else
					{
						return "weaver_wolf_adams005.htm";
					}
				}
				else if(reply == 2)
				{
					int i0 = Rnd.get(200);
					if(player.getItemsCount(14903) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, 14903, 1, npc, true);
						player.destroyItemByItemId(ProcessType.NPC, PcInventory.ADENA_ID, fee_for_release_rune_s, npc, true);
						if(i0 <= 1)
						{
							player.addItem(ProcessType.NPC, 14913, 1, npc, true);
						}
						else if(i0 <= 10)
						{
							player.addItem(ProcessType.NPC, 14912, 1, npc, true);
						}
						else if(i0 <= 40)
						{
							player.addItem(ProcessType.NPC, 14911, 1, npc, true);
						}
						else if(i0 <= 100)
						{
							player.addItem(ProcessType.NPC, 14910, 1, npc, true);
						}
						else
						{
							npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), 1300162));
						}
					}
					else
					{
						return "weaver_wolf_adams005.htm";
					}
				}
			}
			else
			{
				return "weaver_wolf_adams006.htm";
			}
		}
		else if(ask == 23040)
		{
			if(player.getAdenaCount() > 0)
			{
				if(reply == 1)
				{
					int i0 = Rnd.get(200);
					if(player.getItemsCount(14904) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, 14904, 1, npc, true);
						player.destroyItemByItemId(ProcessType.NPC, PcInventory.ADENA_ID, fee_for_release_deco_a, npc, true);
						if(i0 <= 1)
						{
							player.addItem(ProcessType.NPC, 14917, 1, npc, true);
						}
						else if(i0 <= 10)
						{
							player.addItem(ProcessType.NPC, 14916, 1, npc, true);
						}
						else if(i0 <= 40)
						{
							player.addItem(ProcessType.NPC, 14915, 1, npc, true);
						}
						else if(i0 <= 100)
						{
							player.addItem(ProcessType.NPC, 14914, 1, npc, true);
						}
						else
						{
							npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), 1300162));
						}
					}
					else
					{
						return "weaver_wolf_adams005.htm";
					}
				}
				else if(reply == 2)
				{
					int i0 = Rnd.get(200);
					if(player.getItemsCount(14905) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, 14905, 1, npc, true);
						player.destroyItemByItemId(ProcessType.NPC, PcInventory.ADENA_ID, fee_for_release_deco_s, npc, true);
						if(i0 <= 1)
						{
							player.addItem(ProcessType.NPC, 14921, 1, npc, true);
						}
						else if(i0 <= 10)
						{
							player.addItem(ProcessType.NPC, 14920, 1, npc, true);
						}
						else if(i0 <= 40)
						{
							player.addItem(ProcessType.NPC, 14919, 1, npc, true);
						}
						else if(i0 <= 100)
						{
							player.addItem(ProcessType.NPC, 14918, 1, npc, true);
						}
						else
						{
							npc.broadcastPacket(new NS(npc, ChatType.NPC_ALL, 1300162));
						}
					}
					else
					{
						return "weaver_wolf_adams005.htm";
					}
				}
			}
			else
			{
				return "weaver_wolf_adams006.htm";
			}
		}
		return null;
	}
}