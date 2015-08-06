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
 * Date: 18.08.12
 * Time: 14:53
 */

public class _00141_ShadowFox3 extends Quest
{
	// Квестовые персонажи
	private static final int NATOOLS = 30894;

	// Квестовые предметы
	private static final int REPORT = 10350;

	// Квестовые монстры
	private final int[] MOBS = {20791, 20792, 20135};

	public _00141_ShadowFox3()
	{
		addStartNpc(NATOOLS);
		addTalkId(NATOOLS);
		addKillId(MOBS);
	}

	public static void main(String[] args)
	{
		new _00141_ShadowFox3();
	}

	@Override
	public int getQuestId()
	{
		return 141;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "warehouse_chief_natools_q0141_03.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == NATOOLS)
		{
			if(reply == 1 && cond == 1)
			{
				return "warehouse_chief_natools_q0141_05.htm";
			}
			else if(reply == 2 && cond == 1)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "warehouse_chief_natools_q0141_06.htm";
			}
			else if(reply == 3 && cond == 3)
			{
				return "warehouse_chief_natools_q0141_10.htm";
			}
			else if(reply == 4 && cond == 3)
			{
				return "warehouse_chief_natools_q0141_11.htm";
			}
			else if(reply == 5 && cond == 3)
			{
				return "warehouse_chief_natools_q0141_12.htm";
			}
			else if(reply == 6 && cond == 3)
			{
				return "warehouse_chief_natools_q0141_13.htm";
			}
			else if(reply == 7 && cond == 3)
			{
				return "warehouse_chief_natools_q0141_14.htm";
			}
			else if(reply == 8 && cond == 3)
			{
				return "warehouse_chief_natools_q0141_15.htm";
			}
			else if(reply == 9 && cond == 3)
			{
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.unset("talk");
				return "warehouse_chief_natools_q0141_16.htm";
			}
			else if(reply == 10 && cond == 4)
			{
				return "warehouse_chief_natools_q0141_18.htm";
			}
			else if(reply == 11 && cond == 4)
			{
				return "warehouse_chief_natools_q0141_19.htm";
			}
			else if(reply == 12 && cond == 4)
			{
				return "warehouse_chief_natools_q0141_20.htm";
			}
			else if(reply == 13 && cond == 4)
			{
				return "warehouse_chief_natools_q0141_22.htm";
			}
			else if(reply == 14 && cond == 4)
			{
				return "warehouse_chief_natools_q0141_23.htm";
			}
			else if(reply == 15)
			{
				if(cond == 4 && st.isStarted())
				{
					if(player.getLevel() < 38)
					{
						return "warehouse_chief_natools_q0141_24.htm";
					}
					else
					{
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.ONE_TIME);
						st.giveAdena(88888, true);
						if(player.getLevel() >= 37 && player.getLevel() <= 42)
						{
							st.addExpAndSp(278005, 17058);
						}
						return "warehouse_chief_natools_q0141_25.htm";
					}
				}
				else
				{
					return "warehouse_chief_natools_q0141_25.htm";
				}
			}
			else if(reply == 16)
			{
				return "warehouse_chief_natools_q0141_26.htm";
			}
			else if(reply == 17)
			{
				return "warehouse_chief_natools_q0141_27.htm";
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

		if(st.getCond() == 2 && Rnd.getChance(80) && st.getQuestItemsCount(REPORT) < 30)
		{
			st.giveItems(REPORT, 1);
			if(st.getQuestItemsCount(REPORT) >= 30)
			{
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == NATOOLS)
		{
			switch(st.getState())
			{
				case CREATED:
					QuestState pqs = st.getPlayer().getQuestState(_00140_ShadowFoxPart2.class);
					if(pqs == null || !pqs.isCompleted())
					{
						return "warehouse_chief_natools_q0141_02a.htm";
					}
					else
					{
						return st.getPlayer().getLevel() < 38 ? "warehouse_chief_natools_q0141_02.htm" : "warehouse_chief_natools_q0141_01.htm";
					}
				case STARTED:
					switch(cond)
					{
						case 1:
							return "warehouse_chief_natools_q0141_04.htm";
						case 2:
							return "warehouse_chief_natools_q0141_07.htm";
						case 3:
							if(st.getInt("talk") == 0)
							{
								st.takeItems(REPORT, -1);
								st.set("talk", "1");
								return "warehouse_chief_natools_q0141_08.htm";
							}
							else
							{
								return "warehouse_chief_natools_q0141_09.htm";
							}
						case 4:
							return "warehouse_chief_natools_q0141_17.htm";
					}
				case COMPLETED:
					QuestState p142 = st.getPlayer().getQuestState(_00142_FallenAngelRequestofDawn.class);
					QuestState p143 = st.getPlayer().getQuestState(_00143_FallenAngelRequestofDusk.class);
					if(p142 != null && !p142.isStarted() && p143 != null && !p143.isStarted())
					{
						return "warehouse_chief_natools_q0141_25.htm";
					}
			}
		}
		return getNoQuestMsg(st.getPlayer());
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState qs = player.getQuestState(_00140_ShadowFoxPart2.class);
		return player.getLevel() >= 37 && qs != null && qs.isCompleted();

	}
}