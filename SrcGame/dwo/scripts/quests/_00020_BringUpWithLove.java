package dwo.scripts.quests;

import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00020_BringUpWithLove extends Quest
{
	// Npc
	private static final int _tunatun = 31537;

	// Item
	private static final int _beast_whip = 15473;
	private static final int _crystal = 9553;
	private static final int _jewel = 7185;

	public _00020_BringUpWithLove()
	{
		addStartNpc(_tunatun);
		addTalkId(_tunatun);
		addFirstTalkId(_tunatun);
	}

	public static void main(String[] args)
	{
		new _00020_BringUpWithLove();
	}

	@Override
	public int getQuestId()
	{
		return 20;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(npc.getNpcId() == _tunatun)
		{
			switch(event)
			{
				case "31537-12.htm":
					st.startQuest();
					break;
				case "31537-03.htm":
					if(st.hasQuestItems(_beast_whip))
					{
						return "31537-03a.htm";
					}
					st.giveItems(_beast_whip, 1);
					break;
				case "31537-15.htm":
					st.unset("cond");
					st.takeItems(_jewel, -1);
					st.giveItems(_crystal, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.exitQuest(QuestType.ONE_TIME);
					break;
				case "31537-21.html":
					if(player.getLevel() < 82)
					{
						return "31537-23.html";
					}
					if(st.hasQuestItems(_beast_whip))
					{
						return "31537-22.html";
					}
					st.giveItems(_beast_whip, 1);
					break;
			}
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == _tunatun)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 82)
					{
						return "31537-01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "31537-00.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "31537-13.htm";
					}
					if(st.getCond() == 2)
					{
						return "31537-14.htm";
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
			Quest q = QuestManager.getInstance().getQuest(getClass());
			st = q.newQuestState(player);
		}
		return "31537-20.html";
	}
}