package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 09.04.12
 * Time: 11:54
 */

public class _00061_LawEnforcement extends Quest
{
	private static int LIANE = 32222;
	private static int KEKROPUS = 32138;
	private static int EINDBURGH = 32469;
	private static short JUDGE = 136;
	private static int PATHEON = 32972;

	public _00061_LawEnforcement()
	{
		addStartNpc(LIANE);
		addTalkId(KEKROPUS, LIANE, PATHEON, EINDBURGH);
		addFirstTalkId(PATHEON);
	}

	public static void main(String[] args)
	{
		new _00061_LawEnforcement();
	}

	@Override
	public int getQuestId()
	{
		return 61;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		switch(event)
		{
			case "32222-03.htm":
				st.startQuest();
				break;
			case "teleport":
				player.teleToLocation(-114711, 243911, -7968);
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return null;
			case "32138-08.htm":
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32469-07.htm":
			case "32469-08.htm":
				if(st.getPlayer().getClassId() == ClassId.inspector)
				{
					st.giveItems(PcInventory.ADENA_ID, 26000);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					player.setClassId(JUDGE);
					player.broadcastUserInfo();
					st.exitQuest(QuestType.ONE_TIME);
					player.store();
				}
				else
				{
					return "Я могу сделать из Вас арбитра, только при условии что Вы являетесь сейчас Инспектором!";
				}
				break;
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == LIANE)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
				case CREATED:
					if(player.getLevel() < 76 || player.getClassId() != ClassId.inspector)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "32222-00.htm";
					}
					else
					{
						return "32222-01.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "32222-04.htm";
					}
					break;
			}
		}
		else if(npc.getNpcId() == KEKROPUS)
		{
			if(st.getCond() == 2)
			{
				return "32138-00.htm";
			}
			else if(st.getCond() == 3)
			{
				return "32138-09.htm";
			}
		}
		else if(npc.getNpcId() == EINDBURGH)
		{
			if(st.getCond() == 3)
			{
				return "32469-00.htm";
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(npc.getNpcId() == PATHEON && st != null)
		{
			if(st.getState() == STARTED && st.getCond() == 1)
			{
				return "32972-00.htm";
			}
		}
		npc.showChatWindow(player);
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 76 || player.getClassId() == ClassId.inspector;

	}
}