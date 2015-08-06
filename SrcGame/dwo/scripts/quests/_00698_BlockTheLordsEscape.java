package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 ** Note : Not Finished yet !
 */

public class _00698_BlockTheLordsEscape extends Quest
{
	// NPC's
	private static final int _tepios = 32603;

	public _00698_BlockTheLordsEscape()
	{

		addStartNpc(_tepios);
		addTalkId(_tepios);
	}

	public static void main(String[] args)
	{
		new _00698_BlockTheLordsEscape();
	}

	@Override
	public int getQuestId()
	{
		return 698;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}

		if(npc.getNpcId() == _tepios && event.equalsIgnoreCase("32603-02.htm"))
		{
			// if (GraciaSeedsManager.getInstance().getSoIState() < 6)
			return "32603-nosoilvl.htm";
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(st.isCompleted())
		{
			return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
		}

		if(npc.getNpcId() == _tepios)
		{
			if(player.getLevel() < 75)
			{
				return "32603-00.htm";
			}
			else if(st.isCreated())
			{
				return "32603-01.htm";
			}
		}
		return null;
	}
}