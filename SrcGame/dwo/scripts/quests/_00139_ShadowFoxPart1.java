package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 12.03.12
 * Time: 8:51
 */

public class _00139_ShadowFoxPart1 extends Quest
{
	// Квестовые персонажи
	private static final int _Mia = 30896;

	// Квестовые предметы
	private static final int _fragment = 10345;
	private static final int _chest = 10346;

	// Квестовые монстры
	private static final int[] _mobs = {20784, 20785, 21639, 21640};

	public _00139_ShadowFoxPart1()
	{
		addFirstTalkId(_Mia);
		addTalkId(_Mia);
		addKillId(_mobs);
		questItemIds = new int[]{_fragment, _chest};
	}

	public static void main(String[] args)
	{
		new _00139_ShadowFoxPart1();
	}

	@Override
	public int getQuestId()
	{
		return 139;
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
			case "30896-03.htm":
				st.startQuest();
				break;
			case "30896-11.htm":
				st.setCond(2);
				st.setState(STARTED);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "30896-14.htm":
				st.takeItems(_fragment, -1);
				st.takeItems(_chest, -1);
				st.set("talk", "1");
				break;
			case "30896-16.htm":
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.giveAdena(14050, true);
				st.addExpAndSp(30000, 2000);
				st.exitQuest(QuestType.ONE_TIME);
				break;
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
		if(st.getCond() == 2)
		{
			st.giveItems(_fragment, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			if(Rnd.getChance(10))
			{
				st.giveItems(_chest, 1);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == _Mia)
		{
			switch(st.getState())
			{
				case CREATED:
					return player.getLevel() >= 37 ? "30896-01.htm" : "30896-00.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						return "30896-03.htm";
					}
					if(st.getCond() == 2)
					{
						if(st.getQuestItemsCount(_fragment) >= 10 && st.getQuestItemsCount(_chest) >= 1)
						{
							return "30896-13.htm";
						}
						else
						{
							return st.getInt("talk") == 1 ? "30896-14.htm" : "30896-12.htm";
						}
					}
					break;
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			st = newQuestState(player);
		}
		QuestState qs = player.getQuestState(_00138_TempleChampionPart2.class);
		if(qs != null && qs.isCompleted() && st.isCreated())
		{
			st.setState(STARTED);
		}
		npc.showChatWindow(player);
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_00138_TempleChampionPart2.class);
		QuestState st = player.getQuestState(getClass());
		return player.getLevel() >= 37 && previous != null && previous.isCompleted() && st != null && st.isCreated();

	}
}