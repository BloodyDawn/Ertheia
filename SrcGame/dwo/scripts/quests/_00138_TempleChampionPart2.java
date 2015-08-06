package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00138_TempleChampionPart2 extends Quest
{
	// NPCs
	private static final int SYLVAIN = 30070;
	private static final int PUPINA = 30118;
	private static final int ANGUS = 30474;
	private static final int SLA = 30666;
	// ITEMs
	private static final int MANIFESTO = 10341;
	private static final int RELIC = 10342;
	private static final int ANGUS_REC = 10343;
	private static final int PUPINA_REC = 10344;
	// MONSTERs
	private final int[] mobs = {20176, 20550, 20551, 20552};

	public _00138_TempleChampionPart2()
	{
		addStartNpc(SYLVAIN);
		addFirstTalkId(SYLVAIN);
		addTalkId(SYLVAIN, PUPINA, ANGUS, SLA);
		addKillId(mobs);
	}

	public static void main(String[] args)
	{
		new _00138_TempleChampionPart2();
	}

	@Override
	public int getQuestId()
	{
		return 138;
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
				st.giveItems(MANIFESTO, 1);
				break;
			case "30070-05.htm":
				st.giveAdena(84593, true);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				if(st.getPlayer().getLevel() >= 36 && st.getPlayer().getLevel() <= 41)
				{
					st.addExpAndSp(187062, 11307);
				}
				break;
			case "30070-03.htm":
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "30118-06.htm":
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "30118-09.htm":
				st.setCond(6);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.set("talk", "0");
				st.giveItems(PUPINA_REC, 1);
				break;
			case "30474-02.htm":
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "30666-02.htm":
				if(!st.takeItemsAndConfirm(PUPINA_REC, -1))
				{
					return "<html><body>Incorrect item count.</body></html>";
				}
				st.set("talk", "1");
				break;
			case "30666-03.htm":
				if(!st.takeItemsAndConfirm(MANIFESTO, -1))
				{
					return "<html><body>Incorrect item count.</body></html>";
				}
				st.set("talk", "2");
				break;
			case "30666-08.htm":
				st.setCond(7);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.unset("talk");
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

		if(st.getCond() == 4)
		{
			if(st.getQuestItemsCount(RELIC) < 10)
			{
				st.giveItems(RELIC, 1);
				if(st.getQuestItemsCount(RELIC) >= 10)
				{
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

		int cond = st.getCond();
		switch(st.getState())
		{
			case CREATED:
				return getNoQuestMsg(player);
			case COMPLETED:
				return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
			case STARTED:
				if(npc.getNpcId() == SYLVAIN)
				{
					switch(cond)
					{
						case 0:
							if(st.getPlayer().getLevel() >= 36)
							{
								return "30070-01.htm";
							}
							else
							{
								st.exitQuest(QuestType.REPEATABLE);
								return "30070-00.htm";
							}
						case 1:
							return "30070-02.htm";
						case 2:
						case 3:
						case 4:
						case 5:
						case 6:
							return "30070-03.htm";
						case 7:
							return "30070-04.htm";
					}
				}
				else if(npc.getNpcId() == PUPINA)
				{
					switch(cond)
					{
						case 2:
							return "30118-01.htm";
						case 3:
						case 4:
							return "30118-07.htm";
						case 5:
							if(!st.takeItemsAndConfirm(ANGUS_REC, -1))
							{
								return "<html><body>Incorrect item count.</body></html>";
							}
							return "30118-08.htm";
						case 6:
							return "30118-10.htm";
					}
				}
				else if(npc.getNpcId() == ANGUS)
				{
					switch(cond)
					{
						case 3:
							return "30474-01.htm";
						case 4:
							if(st.getQuestItemsCount(RELIC) >= 10)
							{
								if(!st.takeItemsAndConfirm(RELIC, -1))
								{
									return "<html><body>Incorrect item count.</body></html>";
								}
								st.giveItems(ANGUS_REC, 1);
								st.setCond(5);
								st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
								return "30474-04.htm";
							}
							else
							{
								return "30474-03.htm";
							}
						case 5:
							return "30474-05.htm";
					}
				}
				else if(npc.getNpcId() == SLA)
				{
					if(cond == 6)
					{
						if(st.getInt("talk") == 0)
						{
							return "30666-01.htm";
						}
						else if(st.getInt("talk") == 1)
						{
							return "30666-02.htm";
						}
						else if(st.getInt("talk") == 2)
						{
							return "30666-03.htm";
						}
					}
					else if(cond == 7)
					{
						return "30666-09.htm";
					}
				}
				break;
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		QuestState qs = player.getQuestState(_00137_TempleChampionPart1.class);
		if(qs != null)
		{
			if(qs.isCompleted() && st.isCreated())
			{
				st.setState(STARTED);
			}
		}
		npc.showChatWindow(player);
		return "";
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState qs = player.getQuestState(_00137_TempleChampionPart1.class);
		return player.getLevel() >= 35 && qs != null && qs.isCompleted();

	}
}