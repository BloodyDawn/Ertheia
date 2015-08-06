package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

public class _00490_DutyOfTheSurvivor extends Quest
{
	private static final int VOLLODOS = 30137;

	private static final int EXTRACT = 34059;
	private static final int BLOOD = 34060;

	private static final int DROP_CHANCE = 60;

	private static final int[] EXTRACT_MOBS = {23162, 23163, 23164, 23165, 23166, 23167};
	private static final int[] BLOOD_MOBS = {23168, 23169, 23170, 23171, 23172, 23173};

	public _00490_DutyOfTheSurvivor()
	{
		addStartNpc(VOLLODOS);
		addTalkId(VOLLODOS);
		addKillId(EXTRACT_MOBS);
		addKillId(BLOOD_MOBS);
		questItemIds = new int[]{EXTRACT, BLOOD};
	}

	public static void main(String[] args)
	{
		new _00490_DutyOfTheSurvivor();
	}

	@Override
	public int getQuestId()
	{
		return 490;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(npc.getNpcId() == VOLLODOS && event.equalsIgnoreCase("30137-05.htm"))
		{
			st.startQuest();
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance randomPlayer = getRandomPartyMember(player, "1");

		if(randomPlayer != null && Rnd.getChance(DROP_CHANCE))
		{
			QuestState st = randomPlayer.getQuestState(getClass());
			if(st != null)
			{
				if(ArrayUtils.contains(EXTRACT_MOBS, npc.getNpcId()) && st.getQuestItemsCount(EXTRACT) < 20 && Rnd.getChance(DROP_CHANCE))
				{
					st.giveItems(EXTRACT, 1);
				}
				else if(ArrayUtils.contains(BLOOD_MOBS, npc.getNpcId()) && st.getQuestItemsCount(BLOOD) < 20 && Rnd.getChance(DROP_CHANCE))
				{
					st.giveItems(BLOOD, 1);
				}
				if(st.getQuestItemsCount(EXTRACT) == 20 && st.getQuestItemsCount(BLOOD) == 20)
				{
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npc.getNpcId() == VOLLODOS)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() < 85 || player.getLevel() > 89)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "30137-00.htm";
					}
					else
					{
						return "30137-01.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return st.hasQuestItems(EXTRACT) || st.hasQuestItems(BLOOD) ? "30137-09.htm" : "30137-06.htm";
					}
					if(st.getCond() == 2)
					{
						st.takeItems(EXTRACT, -1);
						st.takeItems(BLOOD, -1);
						st.addExpAndSp(145557000, 58119840);
						st.unset("cond");
						st.setState(COMPLETED);
						st.exitQuest(QuestType.DAILY);
						return "30137-07.htm";
					}
					break;
				case COMPLETED:
					return "30137-08.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 85 && player.getLevel() < 89;

	}
}