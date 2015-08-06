package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2-GodWorld Team
 * User: Yukio
 * Date: 19.08.12
 * Time: 12:11:05
 */

public class _00622_DeliveryOfSpecialLiquor extends Quest
{
	//NPCs
	private static final int JEREMY = 31521;
	private static final int BEOLIN = 31547;
	private static final int KUBER = 31546;
	private static final int CROCUS = 31545;
	private static final int NAFF = 31544;
	private static final int PULIN = 31543;
	private static final int LIETTA = 31267;

	//QUEST ITEMs
	private static final int SPECIAL_DRINK = 7197;
	private static final int FEE_OF_DRINK = 7198;

	public _00622_DeliveryOfSpecialLiquor()
	{
		addStartNpc(JEREMY);
		addTalkId(JEREMY, BEOLIN, KUBER, CROCUS, NAFF, PULIN, LIETTA);
		questItemIds = new int[]{SPECIAL_DRINK, FEE_OF_DRINK};
	}

	public static void main(String[] args)
	{
		new _00622_DeliveryOfSpecialLiquor();
	}

	@Override
	public int getQuestId()
	{
		return 622;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && qs.getPlayer().getLevel() >= 68)
		{
			qs.startQuest();
			qs.giveItems(SPECIAL_DRINK, 5);
			return "jeremy_q0622_0104.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int cond = st.getCond();
		switch(npc.getNpcId())
		{
			case JEREMY:
				if(reply == 1 && st.getQuestItemsCount(FEE_OF_DRINK) == 5 && cond == 6)
				{
					st.setCond(7);
					return "jeremy_q0622_0701.htm";
				}
			case BEOLIN:
				if(st.getQuestItemsCount(SPECIAL_DRINK) == 0)
				{
					return "beolin_q0622_0202.htm";
				}
				else
				{
					if(reply == 1 && cond == 1)
					{
						st.takeItems(SPECIAL_DRINK, 1);
						st.giveItems(FEE_OF_DRINK, 1);
						st.setCond(2);
						return "beolin_q0622_0201.htm";
					}
					else
					{
						return "beolin_q0622_0203.htm";
					}
				}
			case KUBER:
				if(st.getQuestItemsCount(SPECIAL_DRINK) == 0)
				{
					return "kuber_q0622_0302.htm";
				}
				else
				{
					if(reply == 1 && cond == 2)
					{
						st.takeItems(SPECIAL_DRINK, 1);
						st.giveItems(FEE_OF_DRINK, 1);
						st.setCond(3);
						return "kuber_q0622_0301.htm";
					}
					else
					{
						return "kuber_q0622_0303.htm";
					}
				}
			case CROCUS:
				if(st.getQuestItemsCount(SPECIAL_DRINK) == 0)
				{
					return "crocus_q0622_0403.htm";
				}
				else
				{
					if(reply == 1 && cond == 3)
					{
						st.takeItems(SPECIAL_DRINK, 1);
						st.giveItems(FEE_OF_DRINK, 1);
						st.setCond(4);
						return "crocus_q0622_0401.htm";
					}
					else
					{
						return "crocus_q0622_0402.htm";
					}
				}
			case NAFF:
				if(st.getQuestItemsCount(SPECIAL_DRINK) == 0)
				{
					return "naff_q0622_0502.htm";
				}
				else
				{
					if(reply == 1 && cond == 4)
					{
						st.takeItems(SPECIAL_DRINK, 1);
						st.giveItems(FEE_OF_DRINK, 1);
						st.setCond(5);
						return "naff_q0622_0501.htm";
					}
					else
					{
						return "naff_q0622_0503.htm";
					}
				}
			case PULIN:
				if(st.getQuestItemsCount(SPECIAL_DRINK) == 0)
				{
					return "pulin_q0622_0602.htm";
				}
				else
				{
					if(reply == 1 && cond == 5)
					{
						st.takeItems(SPECIAL_DRINK, 1);
						st.giveItems(FEE_OF_DRINK, 1);
						st.setCond(6);
						return "pulin_q0622_0601.htm";
					}
					else
					{
						return "pulin_q0622_0603.htm";
					}
				}
			case LIETTA:
				if(reply == 3 && st.getQuestItemsCount(FEE_OF_DRINK) == 5)
				{
					st.takeItems(FEE_OF_DRINK, -1);
					int random = st.getRandom(1000);
					if(random < 800)
					{
						st.giveAdena(18800, true);
						st.rewardItems(734, 1);
					}
					else if(random < 880)
					{
						st.giveItems(6849, 1);
					}
					else if(random < 960)
					{
						st.giveItems(6847, 1);
					}
					else if(random < 1000)
					{
						st.giveItems(6851, 1);
					}
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.unset("cond");
					st.exitQuest(QuestType.REPEATABLE);
					return "warehouse_keeper_lietta_q0622_0801.htm";
				}
		}
		return getNoQuestMsg(st.getPlayer());
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		switch(st.getState())
		{
			case CREATED:
				if(cond == 0 && npcId == JEREMY)
				{
					if(st.getPlayer().getLevel() >= 68)
					{
						return "jeremy_q0622_0101.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "jeremy_q0622_0103.htm";

					}
				}
				break;
			case STARTED:
				switch(npcId)
				{
					case JEREMY:
						if(cond == 1)
						{
							return "jeremy_q0622_0105.htm";
						}
						if(cond >= 2 && cond <= 5)
						{
							return "jeremy_q0622_0702.htm";
						}
						if(cond == 6 && st.getQuestItemsCount(FEE_OF_DRINK) == 5)
						{
							return "jeremy_q0622_0601.htm";
						}
						if(cond == 7 && st.getQuestItemsCount(FEE_OF_DRINK) == 5)
						{
							return "jeremy_q0622_0703.htm";
						}
					case BEOLIN:
						if(cond == 1)
						{
							return "beolin_q0622_0101.htm";
						}
					case KUBER:
						if(cond == 2)
						{
							return "kuber_q0622_0201.htm";
						}
					case CROCUS:
						if(cond == 3)
						{
							return "crocus_q0622_0301.htm";
						}
					case NAFF:
						if(cond == 4)
						{
							return "naff_q0622_0401.htm";
						}
					case PULIN:
						if(cond == 5)
						{
							return "pulin_q0622_0501.htm";
						}
					case LIETTA:
						if(cond == 7 && st.getQuestItemsCount(FEE_OF_DRINK) == 5)
						{
							return "warehouse_keeper_lietta_q0622_0701.htm";
						}
				}
		}
		return getNoQuestMsg(st.getPlayer());
	}
}