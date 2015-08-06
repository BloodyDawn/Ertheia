package dwo.scripts.quests;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.math.NumberUtils;

public class _00643_RiseandFalloftheElrokiTribe extends Quest
{
	//Settings: drop chance in %
	private static final int DROP_CHANCE = 75;

	private static final int BONES_OF_A_PLAINS_DINOSAUR = 8776;
	private static final int[] PLAIN_DINOSAURS = {22208, 22209, 22210, 22211, 22212, 22213, 22221, 22222, 22226, 22227};
	private static final int[] REWARDS = {8712, 8713, 8714, 8715, 8716, 8717, 8718, 8719, 8720, 8721, 8722, 8723};

	public _00643_RiseandFalloftheElrokiTribe()
	{
		addStartNpc(32106);
		addTalkId(32106, 32117);
		addKillId(PLAIN_DINOSAURS);
		questItemIds = new int[]{BONES_OF_A_PLAINS_DINOSAUR};
	}

	public static void main(String[] args)
	{
		new _00643_RiseandFalloftheElrokiTribe();
	}

	@Override
	public int getQuestId()
	{
		return 643;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}
		long countI = st.getQuestItemsCount(BONES_OF_A_PLAINS_DINOSAUR);
		if(event.equals("None"))
		{
			return null;
		}
		if(event.equals("32106-03.htm"))
		{
			st.startQuest();
		}
		else if(event.equals("32117-03.htm"))
		{
			if(countI >= 300)
			{
				st.takeItems(BONES_OF_A_PLAINS_DINOSAUR, 300);
				st.rewardItems(REWARDS[st.getRandom(REWARDS.length)], 5);
			}
			else
			{
				return "32117-04.htm";
			}
		}
		else if(NumberUtils.isDigits(event))
		{
			int count = 0;
			int id = Integer.parseInt(event);
			switch(id)
			{
				case 9492: //tunic
					count = 400;
					break;
				case 9493: // legs
					count = 250;
					break;
				case 9494: // circlet
					count = 200;
					break;
				case 9495: //gloves
					count = 134;
					break;
				case 9496: // shoes
					count = 134;
					break;
				case 10115: // sigil
					count = 287;
					break;
			}
			if(count > 0)
			{
				if(countI >= count)
				{
					st.giveItems(id, 1);
					st.takeItems(BONES_OF_A_PLAINS_DINOSAUR, count);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "32117-02.htm";
				}
				return "32117-06.htm";
			}
		}
		else if(event.equals("Quit"))
		{
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.REPEATABLE);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null || !st.isStarted())
		{
			return null;
		}
		long count = st.getQuestItemsCount(BONES_OF_A_PLAINS_DINOSAUR);
		if(st.getCond() == 1 && Rnd.getChance(DROP_CHANCE))
		{
			if(count + 1 >= 300)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			st.giveItems(BONES_OF_A_PLAINS_DINOSAUR, (int) Config.RATE_QUEST_DROP);
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		String htmltext = getNoQuestMsg(player);
		if(st != null)
		{
			int cond = st.getCond();
			long count = st.getQuestItemsCount(BONES_OF_A_PLAINS_DINOSAUR);
			if(cond == 0 && npc.getNpcId() == 32106)
			{
				if(player.getLevel() >= 75)
				{
					htmltext = "32106-01.htm";
				}
				else
				{
					htmltext = "32106-00.htm";
					st.exitQuest(QuestType.REPEATABLE);
				}
			}
			else if(st.getState() == STARTED)
			{
				if(npc.getNpcId() == 32106)
				{
					if(count == 0)
					{
						htmltext = "32106-05.htm";
					}
					else
					{
						htmltext = "32106-06.htm";
						st.takeItems(BONES_OF_A_PLAINS_DINOSAUR, -1);
						st.giveAdena(count * 1374, true);
					}
				}
				else if(npc.getNpcId() == 32117)
				{
					htmltext = "32117-01.htm";
				}
			}
		}
		return htmltext;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 75;
	}
}
