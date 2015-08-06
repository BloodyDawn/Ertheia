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
 * User: ANZO
 * Date: 05.11.12
 * Time: 4:56
 */

public class _00344_1000YearsEndofLamentation extends Quest
{
	// Квестовые персонажи
	private static final int GILMORE = 30754;
	private static final int RODEMAI = 30756;
	private static final int ORVEN = 30857;
	private static final int KAIEN = 30623;
	private static final int GARVARENTZ = 30704;

	// Квестовые предметы
	private static final int ARTICLES_DEAD_HEROES = 4269;
	private static final int OLD_KEY = 4270;
	private static final int OLD_HILT = 4271;
	private static final int OLD_TOTEM = 4272;
	private static final int CRUCIFIX = 4273;

	// Квестовые монстры
	private static final int[] MOBs = {20236, 20237, 20238, 20239, 20240};

	public _00344_1000YearsEndofLamentation()
	{
		addStartNpc(GILMORE);
		addTalkId(GILMORE, RODEMAI, ORVEN, GARVARENTZ, KAIEN);
		addKillId(MOBs);
		questItemIds = new int[]{
			ARTICLES_DEAD_HEROES, OLD_KEY, OLD_HILT, OLD_TOTEM, CRUCIFIX
		};
	}

	public static void main(String[] args)
	{
		new _00344_1000YearsEndofLamentation();
	}

	@Override
	public int getQuestId()
	{
		return 344;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept"))
		{
			qs.startQuest();
			return "watcher_antaras_gilmore_q0344_03.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == GILMORE)
		{
			switch(reply)
			{
				case 1:
					st.set("memoState", "1");
					return "watcher_antaras_gilmore_q0344_04.htm";
				case 2:
					if(st.hasQuestItems(ARTICLES_DEAD_HEROES))
					{
						int chance = Rnd.get(1000);
						if(chance >= st.getQuestItemsCount(ARTICLES_DEAD_HEROES))
						{
							st.giveAdena(st.getQuestItemsCount(ARTICLES_DEAD_HEROES) * 60, true);
							st.takeItems(ARTICLES_DEAD_HEROES, -1);
							return "watcher_antaras_gilmore_q0344_07.htm";
						}
						else
						{
							st.giveAdena(st.getQuestItemsCount(ARTICLES_DEAD_HEROES), true);
							st.set("memoState", "3");
							st.set("memoStateEx", String.valueOf(st.getQuestItemsCount(ARTICLES_DEAD_HEROES)));
							st.takeItems(ARTICLES_DEAD_HEROES, -1);
							return "watcher_antaras_gilmore_q0344_08.htm";
						}
					}
					else
					{
						return "watcher_antaras_gilmore_q0344_06t.htm";
					}
				case 3:
					st.set("memoState", "1");
					return "watcher_antaras_gilmore_q0344_15.htm";
				case 4:
					st.exitQuest(QuestType.REPEATABLE);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					return "watcher_antaras_gilmore_q0344_16.htm";
				case 5:
					if(st.getCond() == 1)
					{
						st.setCond(2);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						st.set("memoState", "4");

						int chance = Rnd.get(100);
						if(chance <= 24)
						{
							st.giveItem(OLD_KEY);
							return "watcher_antaras_gilmore_q0344_09.htm";
						}
						else if(chance <= 49)
						{
							st.giveItem(OLD_HILT);
							return "watcher_antaras_gilmore_q0344_10.htm";
						}
						else if(chance <= 74)
						{
							st.giveItem(OLD_TOTEM);
							return "watcher_antaras_gilmore_q0344_11.htm";
						}
						else
						{
							st.giveItem(CRUCIFIX);
							return "watcher_antaras_gilmore_q0344_12.htm";
						}
					}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}

		if(ArrayUtils.contains(MOBs, npc.getNpcId()))
		{
			if(Rnd.getChance(58))
			{
				st.giveItem(ARTICLES_DEAD_HEROES);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				st.set("memoState", "2");
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == GILMORE)
		{
			switch(st.getState())
			{
				case COMPLETED:
				case CREATED:
					return player.getLevel() < 48 ? "watcher_antaras_gilmore_q0344_01.htm" : "watcher_antaras_gilmore_q0344_02.htm";
				case STARTED:
					int memoState = st.getInt("memoState");
					if(memoState == 0)
					{
						return "watcher_antaras_gilmore_q0344_04.htm";
					}
					else if(memoState == 1 || memoState == 2 && !st.hasQuestItems(ARTICLES_DEAD_HEROES))
					{
						return "watcher_antaras_gilmore_q0344_05.htm";
					}
					else if(memoState == 2 && st.hasQuestItems(ARTICLES_DEAD_HEROES))
					{
						return "watcher_antaras_gilmore_q0344_06.htm";
					}
					else if(memoState == 3)
					{
						st.setCond(2);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						st.set("memoState", "4");

						int chance = Rnd.get(100);
						if(chance <= 24)
						{
							st.giveItem(OLD_KEY);
							return "watcher_antaras_gilmore_q0344_09.htm";
						}
						else if(chance <= 49)
						{
							st.giveItem(OLD_HILT);
							return "watcher_antaras_gilmore_q0344_10.htm";
						}
						else if(chance <= 74)
						{
							st.giveItem(OLD_TOTEM);
							return "watcher_antaras_gilmore_q0344_11.htm";
						}
						else
						{
							st.giveItem(CRUCIFIX);
							return "watcher_antaras_gilmore_q0344_12.htm";
						}
					}
					else if(memoState == 4)
					{
						return "watcher_antaras_gilmore_q0344_13.htm";
					}
					else if(memoState >= 5 && memoState <= 8)
					{
						int memoStateEx = st.getInt("memoStateEx", 1);
						if(memoState == 5)
						{
							st.giveAdena(memoStateEx * 50 + 1500, true);
						}
						else if(memoState == 6)
						{
							st.giveAdena(memoStateEx * 50, true);
							st.giveItem(4044);
						}
						else if(memoState == 7)
						{
							st.giveAdena(memoStateEx * 50, true);
							st.giveItem(4043);
						}
						else if(memoState == 8)
						{
							st.giveAdena(memoStateEx * 50, true);
							st.giveItem(4042);
						}
						st.unset("memoStateEx");
						st.set("memoState", "1");
						return "watcher_antaras_gilmore_q0344_14.htm";
					}
			}
		}
		else if(npc.getNpcId() == RODEMAI)
		{
			if(st.isStarted())
			{
				if(st.hasQuestItems(OLD_KEY))
				{
					int chance = Rnd.get(100);
					if(chance <= 39)
					{
						st.giveItems(1879, 55);
					}
					else if(chance <= 89)
					{
						st.giveItems(951, 1);
					}
					else
					{
						st.giveItems(885, 1);
					}
					st.takeItems(OLD_KEY, -1);
					st.set("memoState", "5");
					return "sir_kristof_rodemai_q0344_01.htm";
				}
				else if(st.getInt("memoState") == 5)
				{
					return "sir_kristof_rodemai_q0344_02.htm";
				}
			}
		}
		else if(npc.getNpcId() == ORVEN)
		{
			if(st.isStarted())
			{
				if(st.hasQuestItems(CRUCIFIX))
				{
					int chance = Rnd.get(100);
					if(chance <= 49)
					{
						st.giveItems(1875, 19);
					}
					else if(chance <= 69)
					{
						st.giveItems(952, 5);
					}
					else
					{
						st.giveItems(2437, 1);
					}
					st.takeItems(CRUCIFIX, -1);
					st.set("memoState", "8");
					return "highpriest_orven_q0344_01.htm";
				}
				else if(st.getInt("memoState") == 8)
				{
					return "highpriest_orven_q0344_02.htm";
				}
			}
		}
		else if(npc.getNpcId() == KAIEN)
		{
			if(st.isStarted())
			{
				if(st.hasQuestItems(OLD_HILT))
				{
					int chance = Rnd.get(100);
					if(chance <= 52)
					{
						st.giveItems(1874, 25);
					}
					else if(chance <= 76)
					{
						st.giveItems(1887, 10);
					}
					else if(chance <= 98)
					{
						st.giveItems(951, 1);
					}
					else
					{
						st.giveItems(133, 1);
					}
					st.takeItems(OLD_HILT, -1);
					st.set("memoState", "6");
					return "duelist_kaien_q0344_01.htm";
				}
				else if(st.getInt("memoState") == 6)
				{
					return "duelist_kaien_q0344_02.htm";
				}
			}
		}
		else if(npc.getNpcId() == GARVARENTZ)
		{
			if(st.isStarted())
			{
				if(st.hasQuestItems(OLD_TOTEM))
				{
					int chance = Rnd.get(100);
					if(chance <= 47)
					{
						st.giveItems(1882, 70);
					}
					else if(chance <= 97)
					{
						st.giveItems(1881, 50);
					}
					else
					{
						st.giveItems(191, 1);
					}
					st.takeItems(CRUCIFIX, -1);
					st.set("memoState", "7");
					return "high_prefect_garvarentz_q0344_01.htm";
				}
				else if(st.getInt("memoState") == 7)
				{
					return "high_prefect_garvarentz_q0344_02.htm";
				}
			}
		}
		return getNoQuestMsg(player);
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 48;

	}
}