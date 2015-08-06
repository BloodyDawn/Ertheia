package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;

public class _00192_SevenSignSeriesOfDoubt extends Quest
{
	// Квестовые персонажи
	private static final int Croop = 30676;
	private static final int Hector = 30197;
	private static final int Stan = 30200;
	private static final int CorpseOfDoubt = 32568;
	private static final int Hollint = 30191;

	// Квестовые итемы
	private static final int CroopsIntroduction = 13813;
	private static final int JacobsNecklace = 13814;
	private static final int CroopsLetterRequesting = 13815;

	public _00192_SevenSignSeriesOfDoubt()
	{
		addStartNpc(Croop);
		addTalkId(Croop, Hector, Stan, CorpseOfDoubt, Hollint);
		questItemIds = new int[]{CroopsIntroduction, JacobsNecklace, CroopsLetterRequesting};
	}

	public static void main(String[] args)
	{
		new _00192_SevenSignSeriesOfDoubt();
	}

	@Override
	public int getQuestId()
	{
		return 192;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}
		switch(event)
		{
			case "30676-2.htm":
				st.startQuest();
				break;
			case "30676-3.htm":
				st.setCond(2);
				player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ_SUSPICIOUS_DEATH);
				return null;
			case "30197-2.htm":
				st.takeItems(CroopsIntroduction, -1);
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "30200-3.htm":
				st.setCond(5);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32568-1.htm":
				st.giveItems(JacobsNecklace, 1);
				st.setCond(6);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "30676-9.htm":
				st.takeItems(JacobsNecklace, 1);
				st.giveItems(CroopsLetterRequesting, 1);
				st.setCond(7);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "30191-2.htm":
				if(player.isSubClassActive())
				{
					return "subclass_forbidden.htm";
				}
				st.takeItems(CroopsLetterRequesting, 1);
				st.addExpAndSp(10000000, 2500000);
				st.unset("cond");
				st.exitQuest(QuestType.ONE_TIME);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				break;
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		String htmltext = getNoQuestMsg(player);
		if(st == null || player.getLevel() < 79)
		{
			return htmltext;
		}
		int cond = st.getCond();
		if(st.isCompleted())
		{
			return getNoQuestMsg(player);
		}
		switch(npc.getNpcId())
		{
			case Croop:
				if(cond == 0)
				{
					if(player.getLevel() >= 79)
					{
						htmltext = "30676-0.htm";
					}
					else
					{
						htmltext = "30676-0a.htm";
						st.exitQuest(QuestType.REPEATABLE);
					}
				}
				else if(cond == 1)
				{
					htmltext = "30676-2.htm";
				}
				else if(cond == 2)
				{
					htmltext = "30676-3.htm";
					st.giveItems(CroopsIntroduction, 1);
					st.setCond(3);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				else if(cond > 2 && cond < 6)
				{
					htmltext = "30676-3a.htm";
				}
				else if(cond == 6)
				{
					htmltext = "30676-4.htm";
				}
				break;
			case Hector:
				if(cond == 3)
				{
					htmltext = "30197-0.htm";
				}
				else if(cond >= 4)
				{
					htmltext = "30197-2a.htm";
				}
				break;
			case Stan:
				if(cond == 4)
				{
					htmltext = "30200-0.htm";
				}
				else if(cond >= 5)
				{
					htmltext = "30200-3a.htm";
				}
				break;
			case CorpseOfDoubt:
				if(cond == 5)
				{
					htmltext = "32568-0.htm";
				}
				break;
			case Hollint:
				if(cond == 7)
				{
					htmltext = "30191-0.htm";
				}
				break;
		}
		return htmltext;
	}
}