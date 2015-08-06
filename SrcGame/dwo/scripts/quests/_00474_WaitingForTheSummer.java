package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 24.04.12
 * Time: 12:16
 */

public class _00474_WaitingForTheSummer extends Quest
{
	// Квестовые персонажи
	private static final int Гид = 33463;
	private static final int Вишотский = 31981;

	// Квестовые предметы
	private static final int МясоБуйвола = 19490;
	private static final int МясоУрсуса = 19491;
	private static final int МясоЙети = 19492;

	// Квестовые монстры
	private static final int[] Буйволы = {22093, 22094};
	private static final int[] Урсусы = {22095, 22096};
	private static final int[] Йети = {22097, 22098};

	public _00474_WaitingForTheSummer()
	{
		setMinMaxLevel(60, 64);
		addStartNpc(Гид);
		addTalkId(Гид, Вишотский);
		addKillId(Буйволы);
		addKillId(Урсусы);
		addKillId(Йети);
		questItemIds = new int[]{МясоБуйвола, МясоЙети, МясоУрсуса};
	}

	public static void main(String[] args)
	{
		new _00474_WaitingForTheSummer();
	}

	@Override
	public int getQuestId()
	{
		return 474;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		switch(event)
		{
			case "33463-04.htm":
				st.startQuest();
				break;
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}

		if(st.getCond() == 1 && Rnd.getChance(50))
		{
			int npcId = npc.getNpcId();
			if(ArrayUtils.contains(Йети, npcId))
			{
				if(st.getQuestItemsCount(МясоЙети) < 30)
				{
					st.giveItem(МясоЙети);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
			else if(ArrayUtils.contains(Урсусы, npcId))
			{
				if(st.getQuestItemsCount(МясоУрсуса) < 30)
				{
					st.giveItem(МясоУрсуса);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
			else if(ArrayUtils.contains(Буйволы, npcId))
			{
				if(st.getQuestItemsCount(МясоБуйвола) < 30)
				{
					st.giveItem(МясоБуйвола);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
			if(st.getQuestItemsCount(МясоЙети) >= 30 && st.getQuestItemsCount(МясоУрсуса) >= 30 && st.getQuestItemsCount(МясоБуйвола) >= 30)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npc.getNpcId() == Гид)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(player, QuestType.DAILY);
				case CREATED:
					return "33463-01.htm";
				case STARTED:
					return st.getCond() == 1 ? "33463-05.htm" : "33463-06.htm";
			}
		}
		else if(npc.getNpcId() == Вишотский)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 1)
				{
					return "31981-02.htm";
				}
				else if(st.getCond() == 2)
				{
					st.addExpAndSp(1879400, 1782000);
					st.giveAdena(194000, true);
					st.exitQuest(QuestType.DAILY);
					return "31981-01.htm";
				}
			}
			else if(st.isCompleted())
			{
				return "31981-03.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 60 && player.getLevel() <= 64;

	}
}