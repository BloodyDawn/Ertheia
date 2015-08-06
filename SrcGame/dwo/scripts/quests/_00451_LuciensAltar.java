package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00451_LuciensAltar extends Quest
{
	private static final int DAICHIR = 30537;

	private static final int REPLENISHED_BEAD = 14877;
	private static final int DISCHARGED_BEAD = 14878;

	private static final int[][] ALTARS = {
		{32706, 1}, {32707, 2}, {32708, 4}, {32709, 8}, {32710, 16}
	};

	public _00451_LuciensAltar()
	{
		addStartNpc(DAICHIR);
		addTalkId(DAICHIR);

		for(int[] i : ALTARS)
		{
			addTalkId(i[0]);
		}
		questItemIds = new int[]{REPLENISHED_BEAD, DISCHARGED_BEAD};
	}

	public static void main(String[] args)
	{
		new _00451_LuciensAltar();
	}

	@Override
	public int getQuestId()
	{
		return 451;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}
		if(event.equals("30537-03.htm"))
		{
			st.set("altars_state", "0");
			st.giveItems(REPLENISHED_BEAD, 5);
			st.startQuest();
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == DAICHIR)
		{
			if(st.getCond() == 0)
			{
				if(st.isCompleted())
				{
					return "30537-06.htm";
				}
				else
				{
					if(player.getLevel() >= 80)
					{
						return "30537-01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "30537-00.htm";
					}
				}
			}
			else if(st.getCond() == 1)
			{
				return "30537-04.htm";
			}
			else if(st.getCond() == 2)
			{
				st.giveAdena(742800, true);
				st.addExpAndSp(13773960, 16232820);
				st.takeItems(DISCHARGED_BEAD, 5);
				st.unset("cond");
				st.unset("altars_state");
				st.exitQuest(QuestType.ONE_TIME);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return "30537-05.htm";
			}
		}
		else if(st.getCond() == 1)
		{
			int idx = 0;
			for(int[] i : ALTARS)
			{
				if(i[0] == npc.getNpcId())
				{
					idx = i[1];
					break;
				}
			}
			if(idx != 0)
			{
				int state = st.getInt("altars_state");
				if((state & idx) == 0)
				{
					st.set("altars_state", String.valueOf(state | idx));
					st.takeItems(REPLENISHED_BEAD, 1);
					st.giveItems(DISCHARGED_BEAD, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					if(st.getQuestItemsCount(DISCHARGED_BEAD) == 5)
					{
						st.setCond(2);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					return "recharge.htm";
				}
				else
				{
					return "findother.htm";
				}
			}
		}
		return null;
	}
}