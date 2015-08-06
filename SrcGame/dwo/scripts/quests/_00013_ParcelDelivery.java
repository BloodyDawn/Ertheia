package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00013_ParcelDelivery extends Quest
{
	// Квестовые персонажи
	private static final int FUNDIN = 31274;
	private static final int VULCAN = 31539;

	private static final int PACKAGE = 7263;

	public _00013_ParcelDelivery()
	{
		addStartNpc(FUNDIN);
		addTalkId(FUNDIN, VULCAN);
		questItemIds = new int[]{PACKAGE};
	}

	public static void main(String[] args)
	{
		new _00013_ParcelDelivery();
	}

	@Override
	public int getQuestId()
	{
		return 13;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept") && st.getPlayer().getLevel() >= 74 && !st.isCompleted())
		{
			st.startQuest();
			st.giveItems(PACKAGE, 1);
			return "mineral_trader_fundin_q0013_0104.htm";
		}
		return event;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(reply == 3 && npcId == VULCAN && cond == 1 && st.hasQuestItems(PACKAGE))
		{
			st.takeItems(PACKAGE, -1);
			st.addExpAndSp(589092, 58794);
			st.giveItems(PcInventory.ADENA_ID, 157834);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.unset("cond");
			st.exitQuest(QuestType.ONE_TIME);
			return "warsmith_vulcan_q0013_0201.htm";
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		if(st.isCompleted())
		{
			return getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
		}
		int cond = st.getCond();
		if(npc.getNpcId() == FUNDIN)
		{
			if(st.isCreated())
			{
				if(st.getPlayer().getLevel() >= 74)
				{
					return "mineral_trader_fundin_q0013_0101.htm";
				}
				else
				{
					st.exitQuest(QuestType.REPEATABLE);
					return "mineral_trader_fundin_q0013_0103.htm";
				}
			}
			else if(cond == 1)
			{
				return "mineral_trader_fundin_q0013_0105.htm";
			}
		}
		else if(npc.getNpcId() == VULCAN)
		{
			if(cond == 1 && st.hasQuestItems(PACKAGE))
			{
				return "warsmith_vulcan_q0013_0101.htm";
			}
		}
		return getNoQuestMsg(st.getPlayer());
	}
}