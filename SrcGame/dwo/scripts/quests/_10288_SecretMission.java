package dwo.scripts.quests;

import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _10288_SecretMission extends Quest
{
	// NPC's
	private static final int _dominic = 31350;
	private static final int _aquilani = 32780;
	private static final int _greymore = 32757;

	// Items
	private static final int _letter = 15529;

	public _10288_SecretMission()
	{
		addStartNpc(_dominic, _aquilani);
		addTalkId(_dominic, _greymore, _aquilani);
		addFirstTalkId(_aquilani);
	}

	public static void main(String[] args)
	{
		new _10288_SecretMission();
	}

	@Override
	public int getQuestId()
	{
		return 10288;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(npc.getNpcId() == _dominic)
		{
			if(event.equalsIgnoreCase("31350-05.htm"))
			{
				st.startQuest();
				st.giveItems(_letter, 1);
			}
		}
		else if(npc.getNpcId() == _greymore && event.equalsIgnoreCase("32757-03.htm"))
		{
			st.unset("cond");
			st.takeItems(_letter, -1);
			st.giveItems(PcInventory.ADENA_ID, 106583);
			st.addExpAndSp(417788, 46320);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.ONE_TIME);
		}
		else if(npc.getNpcId() == _aquilani)
		{
			if(st.getState() == STARTED)
			{
				if(event.equalsIgnoreCase("32780-05.html"))
				{
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
			else if(st.getState() == COMPLETED && event.equalsIgnoreCase("teleport"))
			{
				player.teleToLocation(118833, -80589, -2688);
				return null;
			}
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == _dominic)
		{
			switch(st.getState())
			{
				case CREATED:
					return player.getLevel() >= 82 ? "31350-01.htm" : "31350-00.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						return "31350-06.htm";
					}
					if(st.getCond() == 2)
					{
						return "31350-07.htm";
					}
					break;
				case COMPLETED:
					return "31350-08.htm";
			}
		}
		else if(npc.getNpcId() == _aquilani)
		{
			if(st.getCond() == 1)
			{
				return "32780-03.html";
			}
			else if(st.getCond() == 2)
			{
				return "32780-06.html";
			}
		}
		else if(npc.getNpcId() == _greymore && st.getCond() == 2)
		{
			return "32757-01.htm";
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(getClass());
			st = q.newQuestState(player);
		}
		if(npc.getNpcId() == _aquilani)
		{
			return st.getState() == COMPLETED ? "32780-01.html" : "32780-00.html";
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 82;
	}
}