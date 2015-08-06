package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 05.03.12
 * Time: 2:47
 */

public class _00137_TempleChampionPart1 extends Quest
{
	// Квестовые персонажи
	private static final int SYLVAIN = 30070;

	// Квестовые предметы
	private static final int FRAGMENT = 10340;
	private static final int BadgeTempleExecutor = 10334;
	private static final int BadgeTempleMissionary = 10339;

	// Квестовые монстры
	private static final int GraniteGolem = 20083;
	private static final int HangmanTree = 20144;
	private static final int AmberBasilisk = 20199;
	private static final int Strain = 20200;
	private static final int Ghoul = 20201;
	private static final int DeadSeeker = 20202;

	public _00137_TempleChampionPart1()
	{
		addStartNpc(SYLVAIN);
		addTalkId(SYLVAIN);
		addKillId(GraniteGolem, HangmanTree, AmberBasilisk, Strain, Ghoul, DeadSeeker);
		questItemIds = new int[]{FRAGMENT};
	}

	public static void main(String[] args)
	{
		new _00137_TempleChampionPart1();
	}

	@Override
	public int getQuestId()
	{
		return 137;
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
			case "30070-02.htm":
				st.startQuest();
				st.set("talk", "0");
				break;
			case "30070-05.htm":
				st.set("talk", "1");
				break;
			case "30070-06.htm":
				st.set("talk", "2");
				break;
			case "30070-08.htm":
				st.unset("talk");
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "30070-16.htm":
				st.takeItems(10334, -1);
				st.takeItems(10339, -1);
				st.giveAdena(69146, true);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.unset("talk");
				st.exitQuest(QuestType.ONE_TIME);
				if(st.getPlayer().getLevel() >= 35 && st.getPlayer().getLevel() <= 40)
				{
					st.addExpAndSp(219975, 13047);
				}
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

		if(st.getCond() == 2)
		{
			if(st.getQuestItemsCount(FRAGMENT) < 30)
			{
				st.giveItems(FRAGMENT, 1);
				if(st.getQuestItemsCount(FRAGMENT) >= 30)
				{
					st.setCond(3);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				else
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == SYLVAIN)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
				case CREATED:
					if(player.getLevel() >= 35 && st.hasQuestItems(BadgeTempleExecutor) && st.hasQuestItems(BadgeTempleMissionary))
					{
						return "30070-01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "30070-00.htm";
					}
				case STARTED:
					switch(st.getCond())
					{
						case 1:
							if(st.getInt("talk") == 0)
							{
								return "30070-03.htm";
							}
							if(st.getInt("talk") == 1)
							{
								return "30070-05.htm";
							}
							if(st.getInt("talk") == 2)
							{
								return "30070-06.htm";
							}
							break;
						case 2:
							return "30070-08.htm";
						case 3:
							if(st.getQuestItemsCount(FRAGMENT) >= 30)
							{
								st.set("talk", "1");
								st.takeItems(FRAGMENT, -1);
								return "30070-09.htm";
							}
							if(st.getInt("talk") == 1)
							{
								return "30070-10.htm";
							}
							break;
					}
					break;
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 35 && player.getInventory().getItemByItemId(BadgeTempleExecutor) != null && player.getInventory().getItemByItemId(BadgeTempleMissionary) != null;

	}
}
