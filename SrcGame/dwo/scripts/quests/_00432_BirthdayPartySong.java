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
 * @author ANZO
 * Date: 24.04.12
 * Time: 15:15
 */

public class _00432_BirthdayPartySong extends Quest
{
	// Квестовые персонажи
	private static final int Октавия = 31043;

	// Квестовые монстры
	private static final int Голем = 21103;

	// Квестовые предметы
	private static final int КрасныйКристалл = 7541;
	private static final int МузыкальныйКристалл = 7061;

	public _00432_BirthdayPartySong()
	{
		addStartNpc(Октавия);
		addTalkId(Октавия);
		addKillId(Голем);
		questItemIds = new int[]{КрасныйКристалл};
	}

	public static void main(String[] args)
	{
		new _00432_BirthdayPartySong();
	}

	@Override
	public int getQuestId()
	{
		return 432;
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
			case "31043-02.htm":
				st.startQuest();
				break;
			case "31043-07.htm":
				st.giveItems(МузыкальныйКристалл, 25);
				st.exitQuest(QuestType.REPEATABLE);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
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
			if(npc.getNpcId() == Голем)
			{
				st.giveItem(КрасныйКристалл);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				if(st.getQuestItemsCount(КрасныйКристалл) >= 50)
				{
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		if(npc.getNpcId() == Октавия)
		{
			switch(st.getState())
			{
				case CREATED:
					return "31043-01.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						return !st.hasQuestItems(КрасныйКристалл) ? "31043-02.htm" : "31043-06.htm";
					}
					if(st.getCond() == 2)
					{
						return "31043-05.htm";
					}
					break;
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 31 && player.getLevel() <= 36;

	}
}