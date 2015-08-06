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
 * Date: 20.08.12
 * Time: 2:34
 */

public class _00357_WarehouseKeeperAmbition extends Quest
{
	// Различные значения
	private static final int DROPRATE = 50;
	private static final int REWARD1 = 900; // Награда за один предмет
	private static final int REWARD2 = 10000; // Награда если предметов > 100

	// Квестовые персонажи
	private static final int SILVA = 30686;

	// Квестовые монстры
	private static final int MOB1 = 20594;
	private static final int MOB2 = 20595;
	private static final int MOB3 = 20596;
	private static final int MOB4 = 20597;
	private static final int MOB5 = 20598;

	// Квестовые предметы
	private static final int JADE_CRYSTAL = 5867;

	public _00357_WarehouseKeeperAmbition()
	{
		addStartNpc(SILVA);
		addTalkId(SILVA);
		addKillId(MOB1, MOB2, MOB3, MOB4, MOB5);

		questItemIds = new int[]{JADE_CRYSTAL};
	}

	public static void main(String[] args)
	{
		new _00357_WarehouseKeeperAmbition();
	}

	@Override
	public int getQuestId()
	{
		return 357;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "warehouse_keeper_silva_q0357_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();

		if(npcId == SILVA)
		{
			if(reply == 1)
			{
				return "warehouse_keeper_silva_q0357_03.htm";
			}
			else if(reply == 2)
			{
				return "warehouse_keeper_silva_q0357_04.htm";
			}
			else if(reply == 3 && st.isStarted())
			{
				long count = st.getQuestItemsCount(JADE_CRYSTAL);
				if(count > 0)
				{
					long reward = count * REWARD1;
					if(count >= 100)
					{
						reward += REWARD2;
					}
					st.takeItems(JADE_CRYSTAL, -1);
					st.giveAdena(reward, true);
					return count >= 100 ? "warehouse_keeper_silva_q0357_09.htm" : "warehouse_keeper_silva_q0357_08.htm";
				}
				else
				{
					return "warehouse_keeper_silva_q0357_06.htm";
				}
			}
			else if(reply == 4 && st.isStarted())
			{
				return "warehouse_keeper_silva_q0357_10.htm";
			}
			else if(reply == 5 && st.isStarted())
			{
				st.exitQuest(QuestType.REPEATABLE);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return "warehouse_keeper_silva_q0357_11.htm";
			}
		}

		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == SILVA)
		{
			switch(st.getState())
			{
				case CREATED:
					if(st.getPlayer().getLevel() >= 47)
					{
						return "warehouse_keeper_silva_q0357_02.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "warehouse_keeper_silva_q0357_01.htm";
					}
				case STARTED:
					long jadeCount = st.getQuestItemsCount(JADE_CRYSTAL);
					if(jadeCount == 0)
					{
						return "warehouse_keeper_silva_q0357_06.htm";
					}
					else if(jadeCount > 0)
					{
						return "warehouse_keeper_silva_q0357_07.htm";
					}
			}
		}
		return getNoQuestMsg(st.getPlayer());
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 47;
	}
}