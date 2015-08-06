package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _10291_FireDragonDestroyer extends Quest
{
	// NPC
	private static final int Klein = 31540;
	private static final int Valakas = 29028;
	// Item
	private static final int FloatingStone = 7267;
	private static final int PoorNecklace = 15524;
	private static final int ValorNecklace = 15525;
	private static final int ValakaSlayerCirclet = 8567;

	public _10291_FireDragonDestroyer()
	{
		addStartNpc(Klein);
		addTalkId(Klein);
		addKillId(Valakas);
		questItemIds = new int[]{PoorNecklace, ValorNecklace};
	}

	public static void main(String[] args)
	{
		new _10291_FireDragonDestroyer();
	}

	private void rewardPlayer(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st != null && st.getCond() == 1)
		{
			st.takeItems(PoorNecklace, 1);
			st.giveItems(ValorNecklace, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			st.setCond(2);
		}
	}

	@Override
	public int getQuestId()
	{
		return 10291;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(event.equalsIgnoreCase("31540-07.htm"))
		{
			st.startQuest();
			st.giveItems(PoorNecklace, 1);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			if(player.isInParty())
			{
				player.getParty().getMembers().forEach(this::rewardPlayer);
			}
			else
			{
				rewardPlayer(player);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		switch(st.getState())
		{
			case CREATED:
				if(player.getLevel() >= 83 && st.getQuestItemsCount(FloatingStone) >= 1)
				{
					return "31540-01.htm";
				}
				else
				{
					return player.getLevel() < 83 ? "31540-02.htm" : "31540-04.htm";
				}
			case STARTED:
				if(st.getCond() == 1)
				{
					return st.hasQuestItems(PoorNecklace) ? "31540-08.htm" : "31540-09.htm";
				}
				if(st.getCond() == 2)
				{
					st.takeItems(ValorNecklace, 1);
					st.giveItems(PcInventory.ADENA_ID, 126549);
					st.addExpAndSp(717291, 77397);
					st.giveItems(ValakaSlayerCirclet, 1);
					st.unset("cond");
					st.exitQuest(QuestType.ONE_TIME);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "31540-10.htm";
				}
				break;
			case COMPLETED:
				return "31540-03.htm";
		}

		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getInventory().getCountOf(FloatingStone) > 0 && player.getLevel() >= 83;
	}
}