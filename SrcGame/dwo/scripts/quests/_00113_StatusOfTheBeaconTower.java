package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * User: Yukio
 * Date: 07.12.12
 * Time: 19:47:28
 */

public class _00113_StatusOfTheBeaconTower extends Quest
{
	// Квестовые персонажи
	private static final int MOIRA = 31979;
	private static final int TORANT = 32016;

	// Квестовые предметы
	private static final int FIRE_BOX = 8086;

	public _00113_StatusOfTheBeaconTower()
	{
		addStartNpc(MOIRA);
		addTalkId(MOIRA, TORANT);
		questItemIds = new int[]{FIRE_BOX};
	}

	public static void main(String[] args)
	{
		new _00113_StatusOfTheBeaconTower();
	}

	@Override
	public int getQuestId()
	{
		return 113;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(!st.isCompleted() && event.equals("quest_accept"))
		{
			st.startQuest();
			st.giveItems(FIRE_BOX, 1);
			return "seer_moirase_q0113_0104.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == TORANT)
		{
			if(reply == 3 && st.getCond() == 1)
			{
				st.takeItems(FIRE_BOX, 1);
				st.giveAdena(247600, true);
				st.addExpAndSp(1147830, 1352735);
				st.exitQuest(QuestType.ONE_TIME);
				return "torant_q0113_0201.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		int npcId = npc.getNpcId();

		switch(st.getState())
		{
			case COMPLETED:
				return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
			case CREATED:
				if(npcId == MOIRA)
				{
					if(player.getLevel() >= 80)
					{
						return "seer_moirase_q0113_0101.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "seer_moirase_q0113_0103.htm";
					}
				}
				break;
			case STARTED:
				if(npcId == MOIRA)
				{
					return "seer_moirase_q0113_0105.htm";
				}
				if(npcId == TORANT)
				{
					if(!st.hasQuestItems(FIRE_BOX))
					{
						return "torant_q0113_0101.htm";
					}
				}
				break;
		}
		return getNoQuestMsg(player);
	}
}