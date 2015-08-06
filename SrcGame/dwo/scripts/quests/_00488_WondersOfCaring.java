package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

public class _00488_WondersOfCaring extends Quest
{
	private static final int ADVENTURER_HELPER = 33463;
	private static final int DOLPHREN = 32880;

	private static final int RELIC_BOX = 19500;

	private static final int DROP_CHANCE = 60;

	private static final int[] MOBS = {20965, 20966, 20967, 20968, 20969, 20970, 20971, 20972, 20973};

	public _00488_WondersOfCaring()
	{
		setMinMaxLevel(75, 79);
		addStartNpc(ADVENTURER_HELPER);
		addTalkId(ADVENTURER_HELPER, DOLPHREN);
		addKillId(MOBS);
	}

	public static void main(String[] args)
	{
		new _00488_WondersOfCaring();
	}

	@Override
	public int getQuestId()
	{
		return 488;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(npc.getNpcId() == ADVENTURER_HELPER && event.equalsIgnoreCase("33463-04.htm"))
		{
			st.startQuest();
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance randomPlayer = getRandomPartyMember(player, "1");

		if(randomPlayer != null && ArrayUtils.contains(MOBS, npc.getNpcId()) && Rnd.getChance(DROP_CHANCE))
		{
			QuestState st = randomPlayer.getQuestState(getClass());

			if(st != null)
			{
				st.giveItems(RELIC_BOX, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				if(st.getQuestItemsCount(RELIC_BOX) >= 50)
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

		if(npc.getNpcId() == ADVENTURER_HELPER)
		{
			switch(st.getState())
			{
				case CREATED:
					return "33463-01.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						return "33463-04.htm";
					}
					break;
				case COMPLETED:
					return getAlreadyCompletedMsg(player, QuestType.DAILY);
			}
		}
		else if(npc.getNpcId() == DOLPHREN)
		{
			if(st.isStarted() && st.getCond() == 2)
			{
				st.takeItems(RELIC_BOX, -1);
				st.addExpAndSp(22901550, 26024550);
				st.giveAdena(490545, true);
				st.unset("cond");
				st.setState(COMPLETED);
				st.exitQuest(QuestType.DAILY);
				return "32880.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 75 && player.getLevel() < 79;

	}
}