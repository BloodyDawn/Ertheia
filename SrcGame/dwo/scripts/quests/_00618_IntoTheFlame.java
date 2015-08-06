package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import dwo.scripts.npc.teleporter.HeartOfVolcano;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 24.12.12
 * Time: 16:07
 */

public class _00618_IntoTheFlame extends Quest
{
	// Квестовые персонажи
	private static final int KLEIN = 31540;
	private static final int HILDA = 31271;

	// Квестовые предметы
	private static final int VACUALITE_ORE = 7265;
	private static final int VACUALITE = 7266;
	private static final int FLOATING_STONE = 7267;

	public _00618_IntoTheFlame()
	{
		addStartNpc(KLEIN);
		addTalkId(KLEIN, HILDA);
		for(int i = 0; i <= 5; i++)
		{
			addKillId(21274 + i);
			addKillId(21282 + i);
			addKillId(21290 + i);
		}
		questItemIds = new int[]{VACUALITE_ORE, VACUALITE};
	}

	public static void main(String[] args)
	{
		new _00618_IntoTheFlame();
	}

	@Override
	public int getQuestId()
	{
		return 618;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			qs.setMemoState(11);
			return "watcher_valakas_klein_q0618_0104.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();

		if(npcId == KLEIN)
		{
			if(reply == 3)
			{
				if(player.getItemsCount(VACUALITE) >= 1)
				{
					st.takeItems(VACUALITE, -1);
					st.giveItem(FLOATING_STONE);
					st.exitQuest(QuestType.REPEATABLE);
					return "watcher_valakas_klein_q0618_0401.htm";
				}
			}
			else if(reply == 101)
			{
				int playersEntered = HeartOfVolcano.getInstance().getEnteredToValakasPlayersCount();
				if(playersEntered < 50)
				{
					return "watcher_valakas_klein003.htm";
				}
				else if(playersEntered < 100)
				{
					return "watcher_valakas_klein004.htm";
				}
				else if(playersEntered < 150)
				{
					return "watcher_valakas_klein005.htm";
				}
				else
				{
					return playersEntered < 200 ? "watcher_valakas_klein006.htm" : "watcher_valakas_klein007.htm";
				}
			}
		}
		else if(npc.getNpcId() == HILDA)
		{
			if(reply == 1)
			{
				if(st.getMemoState() == 11)
				{
					st.takeItems(VACUALITE, -1);
					st.giveItem(VACUALITE);
					st.setCond(2);
					st.setMemoState(21);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "blacksmith_hilda_q0618_0201.htm";
				}
				else if(st.getMemoState() == 22)
				{
					if(player.getItemsCount(VACUALITE_ORE) >= 50 && st.getCond() == 3)
					{
						st.takeItems(VACUALITE_ORE, -1);
						st.giveItem(VACUALITE);
						st.setCond(4);
						st.setMemoState(31);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "blacksmith_hilda_q0618_0301.htm";
					}
					else
					{
						return "blacksmith_hilda_q0618_0203.htm";
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		if(Util.checkIfInRange(1500, npc, st.getPlayer(), true))
		{
			int i4 = Rnd.get(1000);
			if(i4 < 630 && st.getCond() == 2)
			{
				st.giveItems(VACUALITE_ORE, 1);
				if(st.getQuestItemsCount(VACUALITE_ORE) == 50)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(3);
					st.setMemoState(22);
				}
				else
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == KLEIN)
		{
			switch(st.getState())
			{
				case CREATED:
					return player.getLevel() >= 60 ? "watcher_valakas_klein_q0618_0101.htm" : "watcher_valakas_klein_q0618_0103.htm";
				case STARTED:
					if(st.getMemoState() == 11)
					{
						return "watcher_valakas_klein_q0618_0105.htm";
					}
					else if(st.getMemoState() == 31)
					{
						if(player.getItemsCount(VACUALITE) >= 1)
						{
							return "watcher_valakas_klein_q0618_0301.htm";
						}
					}
			}
		}
		else if(npc.getNpcId() == HILDA)
		{
			if(st.isStarted())
			{
				if(st.getMemoState() == 11)
				{
					return "blacksmith_hilda_q0618_0101.htm";
				}
				else if(st.getMemoState() <= 22 && st.getMemoState() >= 21)
				{
					return st.getMemoState() == 22 && player.getItemsCount(VACUALITE_ORE) >= 50 ? "blacksmith_hilda_q0618_0202.htm" : "blacksmith_hilda_q0618_0203.htm";
				}
				else if(st.getMemoState() == 31)
				{
					return "blacksmith_hilda_q0618_0303.htm";
				}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 60;
	}
}