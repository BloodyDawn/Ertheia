package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

public class _00452_FindingtheLostSoldiers extends Quest
{
	private static final int JAKAN = 32773;
	private static final int TAG_ID = 15513;
	private static final int[] SOLDIER_CORPSES = {32769, 32770, 32771, 32772};

	public _00452_FindingtheLostSoldiers()
	{

		questItemIds = new int[]{TAG_ID};
		addStartNpc(JAKAN);
		addTalkId(JAKAN);
		addTalkId(SOLDIER_CORPSES);
	}

	public static void main(String[] args)
	{
		new _00452_FindingtheLostSoldiers();
	}

	@Override
	public int getQuestId()
	{
		return 452;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return htmltext;
		}

		if(event.equalsIgnoreCase("32773-3.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("corpse-2.htm"))
		{
			if(st.getCond() == 1)
			{
				int i0 = Rnd.get(10);
				if(i0 < 5)
				{
					st.giveItems(TAG_ID, 1);
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				else
				{
					htmltext = "corpse-3.htm";
				}
			}
			else
			{
				htmltext = "corpse-3.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return htmltext;
		}

		if(npc.getNpcId() == JAKAN)
		{
			switch(st.getState())
			{
				case CREATED:
					if(st.getPlayer().getLevel() >= 85)
					{
						htmltext = "32773-1.htm";
					}
					else
					{
						htmltext = "32773-0.htm";
						st.exitQuest(QuestType.REPEATABLE);
					}
					break;
				case STARTED:
					if(st.getCond() == 1)
					{
						htmltext = "32773-4.htm";
					}
					else if(st.getCond() == 2)
					{
						htmltext = "32773-5.htm";
						st.takeItems(TAG_ID, 1);
						st.giveAdena(95200, true);
						st.addExpAndSp(435024, 50366);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.DAILY);
					}
					break;
				case COMPLETED:
					if(st.isNowAvailable())
					{
						if(st.getPlayer().getLevel() >= 85)
						{
							htmltext = "32773-1.htm";
							st.setState(CREATED);
						}
						else
						{
							htmltext = "32773-0.htm";
							st.exitQuest(QuestType.REPEATABLE);
						}
					}
					else
					{
						htmltext = "32773-6.htm";
					}
					break;
			}
		}
		else if(ArrayUtils.contains(SOLDIER_CORPSES, npc.getNpcId()))
		{
			if(st.getCond() == 1)
			{
				htmltext = "corpse-1.htm";
			}
		}
		return htmltext;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 85;

	}
}