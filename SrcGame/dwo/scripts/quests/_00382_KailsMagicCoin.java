package dwo.scripts.quests;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

import java.util.HashMap;
import java.util.Map;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 17.06.12
 * Time: 17:32
 */

public class _00382_KailsMagicCoin extends Quest
{
	// Квестовые монстры и шансы
	private static final Map<Integer, int[]> MOBS = new HashMap<>(4);

	static
	{
		MOBS.put(21017, new int[]{5961});
		MOBS.put(21019, new int[]{5962});
		MOBS.put(21020, new int[]{5963});
		MOBS.put(21022, new int[]{5961, 5962, 5963});
	}

	// Квестовые предметы
	private static int ROYAL_MEMBERSHIP = 5898;
	// Квестовые персонажи
	private static int VERGARA = 30687;

	public _00382_KailsMagicCoin()
	{
		addStartNpc(VERGARA);
		addTalkId(VERGARA);
		MOBS.keySet().forEach(this::addKillId);

		questItemIds = new int[]{5961, 5962, 5963};
	}

	public static void main(String[] args)
	{
		new _00382_KailsMagicCoin();
	}

	@Override
	public int getQuestId()
	{
		return 382;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		if(event.equalsIgnoreCase("30687-03.htm"))
		{
			if(st.getPlayer().getLevel() >= 55 && st.getQuestItemsCount(ROYAL_MEMBERSHIP) > 0)
			{
				st.startQuest();
			}
			else
			{
				event = "30687-01.htm";
				st.exitQuest(QuestType.REPEATABLE);
			}
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

		int npcId = npc.getNpcId();

		if(Rnd.getChance((int) Math.min(10 * Config.RATE_QUEST_REWARD, 100)) && st.getQuestItemsCount(ROYAL_MEMBERSHIP) > 0)
		{
			st.giveItems(MOBS.get(npcId)[Rnd.get(MOBS.get(npcId).length)], (int) Config.RATE_QUEST_REWARD);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int cond = st.getCond();

		if(st.getQuestItemsCount(ROYAL_MEMBERSHIP) == 0 || st.getPlayer().getLevel() < 55)
		{
			st.exitQuest(QuestType.REPEATABLE);
			return "30687-01.htm";
		}
		else
		{
			return cond == 0 ? "30687-02.htm" : "30687-04.htm";
		}
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return !(player.getLevel() < 55 && player.getInventory().getCountOf(ROYAL_MEMBERSHIP) < 1);
	}
}