package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 09.01.13
 * Time: 16:17
 * TODO: Диалоги
 * TODO: addQuestItemId(int[]) из л2ж
 * TODO: Призыв рандомного НПЦ из массива FlowersNPC
 * TODO: Призыв должен работать только в специальной зоне спауна мандрагор
 */

public class _00750_SevenFlowers extends Quest
{
	// Квестовые персонажи
	private static final int Dadfena = 33697;

	// Квестовые монстры
	private static final int[] Mandragoras = {23210, 23211};
	private static final int[] FlowersNPC = {33720, 33721, 33722, 33723, 33724, 33725, 33726};

	// Квестовые предметы
	private static final int StrangeSeed = 34963; // TODO
	private static final int[] Flowers = {34964, 34965, 34966, 34967, 34968, 34969, 34970};

	// Квестовые награды
	private static final int DeadChest = 35546; // TODO

	public _00750_SevenFlowers()
	{
		addStartNpc(Dadfena);
		addTalkId(Dadfena);
		addKillId(Mandragoras);
		addKillId(FlowersNPC);
		// TODO: addQuestItems(Flowers) - перенести из л2ж
		questItemIds = new int[]{StrangeSeed};
	}

	public static void main(String[] args)
	{
		new _00750_SevenFlowers();
	}

	/***
	 * @param player проверяемый игрок
	 * @return количество собранных разных цветков
	 */
	private int checkFlowersUniqueCount(L2PcInstance player)
	{
		int uniqueFlowersCount = 0;
		for(int id : Flowers)
		{
			if(player.getItemsCount(id) > 0)
			{
				uniqueFlowersCount++;
			}
		}
		return uniqueFlowersCount;
	}

	@Override
	public int getQuestId()
	{
		return 750;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept"))
		{
			qs.startQuest();
			return "";
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(ArrayUtils.contains(Mandragoras, npc.getNpcId()))
		{
			QuestState st;
			L2Party party = player.getParty();
			if(party != null)
			{
				for(L2PcInstance member : party.getMembersInRadius(player, 900))
				{
					st = member.getQuestState(getClass());
					if(st != null && st.getCond() >= 1)
					{
						if(st.getCond() == 1)
						{
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							st.setCond(2);
						}
						st.giveItem(StrangeSeed);
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}
			}
			else
			{
				st = player.getQuestState(getClass());
				if(st != null && st.getCond() >= 1)
				{
					if(st.getCond() == 1)
					{
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						st.setCond(2);
					}
					st.giveItem(StrangeSeed);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		else if(Rnd.getChance(50) && ArrayUtils.contains(FlowersNPC, npc.getNpcId()))
		{
			// Даем цветок только убившему, не даем группе
			int flowerId = npc.getNpcId() + 244;
			QuestState st = player.getQuestState(getClass());
			if(st != null && st.getCond() >= 2)
			{
				if(st.getCond() == 2)
				{
					st.setCond(3);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				st.giveItem(flowerId);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == Dadfena)
		{
			switch(st.getState())
			{
				case CREATED:
					return player.getLevel() >= 95 ? "001.htm" : "np-level.htm";
				case STARTED:
					switch(st.getCond())
					{
						case 1:
						case 2:
						case 3:
							break;
					}
			}
		}

		return getNoQuestMsg(player);
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 95;
	}
}
