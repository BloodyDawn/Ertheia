package dwo.scripts.quests;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import org.apache.commons.lang3.ArrayUtils;

//@author ANZO

public class _00701_ProofOfExistence extends Quest
{
	// Квестовые персонажи
	private static final int ARTIUS = 32559;

	// Квестовые предметы
	private static final int DEADMANS_REMAINS = 13875;

	// Квестовые мобы
	private static final int[] MOBS = {22606, 22607, 22608, 22609};
	// Шансы
	private static final int DROP_CHANCE = 80;

	public _00701_ProofOfExistence()
	{
		addStartNpc(ARTIUS);
		addTalkId(ARTIUS);
		addKillId(MOBS);
		questItemIds = new int[]{DEADMANS_REMAINS};
	}

	public static void main(String[] args)
	{
		new _00701_ProofOfExistence();
	}

	@Override
	public int getQuestId()
	{
		return 701;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(event.equalsIgnoreCase("32559-03.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("32559-quit.htm"))
		{
			st.unset("cond");
			st.exitQuest(QuestType.REPEATABLE);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}

		if(st.getCond() == 1 && ArrayUtils.contains(MOBS, npc.getNpcId()))
		{
			int chance = (int) (DROP_CHANCE * Config.RATE_QUEST_DROP);
			int numItems = chance / 1000;
			chance %= 1000;
			if(st.getRandom(1000) < chance)
			{
				numItems++;
			}
			if(numItems > 0)
			{
				st.giveItems(DEADMANS_REMAINS, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return null;
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

		if(npc.getNpcId() == ARTIUS)
		{
			QuestState first = player.getQuestState(_10273_GoodDayToFly.class);
			if(first != null && first.getState() == COMPLETED && st.getState() == CREATED && player.getLevel() >= 78)
			{
				htmltext = "32559-01.htm";
			}
			else
			{
				switch(st.getCond())
				{
					case 0:
						htmltext = "32559-00.htm";
						break;
					case 1:
						long count = st.getQuestItemsCount(DEADMANS_REMAINS);
						if(count > 0)
						{
							st.takeItems(DEADMANS_REMAINS, -1);
							st.giveAdena(count * 2500, true);
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
							htmltext = "32559-06.htm";
						}
						else
						{
							htmltext = "32559-04.htm";
						}
						break;
				}
			}
		}
		return htmltext;
	}
}