package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _10290_LandDragonConqueror extends Quest
{
	// NPC
	private static final int Theodoric = 30755;
	private static final int[] Antharas = {29019, 29066, 29067, 29068}; //Old, Weak, Normal, Strong
	// Item
	private static final int PortalStone = 3865;
	private static final int ShabbyNecklace = 15522;
	private static final int MiracleNecklace = 15523;
	private static final int AntharaSlayerCirclet = 8568;

	public _10290_LandDragonConqueror()
	{
		addStartNpc(Theodoric);
		addTalkId(Theodoric);
		addKillId(Antharas);
		questItemIds = new int[]{MiracleNecklace, ShabbyNecklace};
	}

	public static void main(String[] args)
	{
		new _10290_LandDragonConqueror();
	}

	private void rewardPlayer(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st != null && st.getCond() == 1)
		{
			st.takeItems(ShabbyNecklace, 1);
			st.giveItems(MiracleNecklace, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			st.setCond(2);
		}
	}

	@Override
	public int getQuestId()
	{
		return 10290;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(event.equalsIgnoreCase("30755-07.htm"))
		{
			st.startQuest();
			st.giveItems(ShabbyNecklace, 1);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(player.isInParty())
		{
			player.getParty().getMembers().forEach(this::rewardPlayer);
		}
		else
		{
			rewardPlayer(player);
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
				if(player.getLevel() >= 83 && st.getQuestItemsCount(PortalStone) >= 1)
				{
					return "30755-01.htm";
				}
				else
				{
					return player.getLevel() < 83 ? "30755-02.htm" : "30755-04.htm";
				}
			case STARTED:
				if(st.getCond() == 1 && st.getQuestItemsCount(ShabbyNecklace) >= 1)
				{
					return st.hasQuestItems(ShabbyNecklace) ? "30755-08.htm" : "30755-09.htm";
				}
				if(st.getCond() == 2)
				{
					st.takeItems(MiracleNecklace, 1);
					st.giveItems(PcInventory.ADENA_ID, 131236);
					st.addExpAndSp(702557, 76334);
					st.giveItems(AntharaSlayerCirclet, 1);
					st.unset("cond");
					st.exitQuest(QuestType.ONE_TIME);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "30755-10.htm";
				}
				break;
			case COMPLETED:
				return "30755-03.htm";
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getInventory().getCountOf(PortalStone) > 0 && player.getLevel() >= 82;
	}
}