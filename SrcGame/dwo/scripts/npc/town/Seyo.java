package dwo.scripts.npc.town;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;

public class Seyo extends Quest
{
	private static final int SEYO = 32737;

	private static final int FRAGMENT = 15486;

	public Seyo()
	{
		addAskId(SEYO, -415);
	}

	public static void main(String[] args)
	{
		new Seyo();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("TRICKERY_TIMER"))
		{
			if(npc.getCustomInt() == 1)
			{
				int i0 = Rnd.get(5) + 1;
				switch(i0)
				{
					case 1:
						makeNpcSay(npc, NpcStringId.OK_WHOS_NEXT_IT_ALL_DEPENDS_ON_YOUR_FATE_AND_LUCK_RIGHT_AT_LEAST_COME_AND_TAKE_A_LOOK, null, null);
						break;
					case 2:
						makeNpcSay(npc, NpcStringId.NO_ONE_ELSE_DONT_WORRY_I_DONT_BITE_HAHA, null, null);
						break;
					case 3:
						makeNpcSay(npc, NpcStringId.THERE_WAS_SOMEONE_WHO_WON_10000_FROM_ME_A_WARRIOR_SHOULDNT_JUST_BE_GOOD_AT_FIGHTING_RIGHT_YOUVE_GOTTA_BE_GOOD_IN_EVERYTHING, null, null);
						break;
					case 4:
						makeNpcSay(npc, NpcStringId.OK_MASTER_OF_LUCK_THATS_YOU_HAHA_WELL_ANYONE_CAN_COME_AFTER_ALL, null, null);
						break;
					case 5:
						makeNpcSay(npc, NpcStringId.SHEDDING_BLOOD_IS_A_GIVEN_ON_THE_BATTLEFIELD_AT_LEAST_ITS_SAFE_HERE, null, null);
						break;
				}

				npc.setCustomInt(0);
			}
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.getNpcId() == SEYO)
		{
			if(ask == -415)
			{
				switch(reply)
				{
					case 1:
						if(player.getItemsCount(FRAGMENT) > 0)
						{
							npc.setCustomInt(1);
							player.destroyItemByItemId(ProcessType.NPC, FRAGMENT, 1, npc, true);
							int i0 = Rnd.get(100) + 1;
							if(i0 > 99)
							{
								player.addItem(ProcessType.NPC, FRAGMENT, 100, npc, true);
								makeNpcSay(npc, NpcStringId.AMAZING_S1_TOOK_100_OF_THESE_SOUL_STONE_FRAGMENTS_WHAT_A_COMPLETE_SWINDLER, player.getName(), null);
							}
							else
							{
								makeNpcSay(npc, NpcStringId.HMM_HEY_DID_YOU_GIVE_S1_SOMETHING_BUT_IT_WAS_JUST_1_HAHA, player.getName(), null);
							}

							startQuestTimer("TRICKERY_TIMER", 5000, npc, player);
							return null;
						}
						else
						{
							return "seyo002a.htm";
						}
					case 5:
						if(player.getItemsCount(FRAGMENT) >= 5)
						{
							npc.setCustomInt(1);
							player.destroyItemByItemId(ProcessType.NPC, FRAGMENT, 5, npc, true);
							int i0 = Rnd.get(100) + 1;
							if(i0 > 80)
							{
								int reward = Rnd.get(3) + 5 << 1;
								player.addItem(ProcessType.NPC, FRAGMENT, reward, npc, true);
								makeNpcSay(npc, NpcStringId.S1_PULLED_ONE_WITH_S2_DIGITS_LUCKY_NOT_BAD, player.getName(), String.valueOf(reward));
							}
							else if(i0 > 20 && i0 <= 80)
							{
								player.addItem(ProcessType.NPC, FRAGMENT, 1, npc, true);
								makeNpcSay(npc, NpcStringId.ITS_BETTER_THAN_LOSING_IT_ALL_RIGHT_OR_DOES_THIS_FEEL_WORSE, null, null);
							}
							else
							{
								makeNpcSay(npc, NpcStringId.AHEM_S1_HAS_NO_LUCK_AT_ALL_TRY_PRAYING, player.getName(), null);
							}

							startQuestTimer("TRICKERY_TIMER", 5000, npc, player);
							return null;
						}
						else
						{
							return "seyo002b.htm";
						}
					case 20:
						if(player.getItemsCount(FRAGMENT) >= 20)
						{
							npc.setCustomInt(1);
							player.destroyItemByItemId(ProcessType.NPC, FRAGMENT, 20, npc, true);
							int i0 = Rnd.get(10000) + 1;
							if(i0 == 10000)
							{
								player.addItem(ProcessType.NPC, FRAGMENT, 10000, npc, true);
								makeNpcSay(npc, NpcStringId.AH_ITS_OVER_WHAT_KIND_OF_GUY_IS_THAT_DAMN_FINE_YOU_S1_TAKE_IT_AND_GET_OUTTA_HERE, player.getName(), null);
							}
							else if(i0 > 10 && i0 <= 9999)
							{
								player.addItem(ProcessType.NPC, FRAGMENT, 1, npc, true);
								makeNpcSay(npc, NpcStringId.A_BIG_PIECE_IS_MADE_UP_OF_LITTLE_PIECES_SO_HERES_A_LITTLE_PIECE, null, null);
							}
							else if(i0 <= 10)
							{
								// here is probably typo in AI, they rand 100 but give 1 instead of i0 maybe was fixed later, dunno
								player.addItem(ProcessType.NPC, FRAGMENT, Rnd.get(100), npc, true);
								makeNpcSay(npc, NpcStringId.YOU_DONT_FEEL_BAD_RIGHT_ARE_YOU_SAD_BUT_DONT_CRY, null, null);
							}

							startQuestTimer("TRICKERY_TIMER", 5000, npc, player);
							return null;
						}
						else
						{
							return "seyo002c.htm";
						}
				}
			}
		}
		return null;
	}

	private void makeNpcSay(L2Npc npc, NpcStringId id, String param1, String param2)
	{
		NS ns = new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), id);

		if(param1 != null && !param1.isEmpty())
		{
			ns.addStringParameter(param1);
		}

		if(param2 != null && !param2.isEmpty())
		{
			ns.addStringParameter(param1);
		}

		npc.broadcastPacket(ns);
	}
}