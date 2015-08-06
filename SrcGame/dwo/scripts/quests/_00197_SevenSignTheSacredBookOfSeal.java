package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;

public class _00197_SevenSignTheSacredBookOfSeal extends Quest
{
	// Квестовые персонажи
	private static final int WOOD = 32593;
	private static final int ORVEN = 30857;
	private static final int LEOPARD = 32594;
	private static final int LAWRENCE = 32595;
	private static final int SOFIA = 32596;
	private static final int SHILENSEVIL = 27343;

	// Квестовые предметы
	private static final int TEXT = 13829;
	private static final int SCULPTURE = 14356;

	public _00197_SevenSignTheSacredBookOfSeal()
	{
		addStartNpc(WOOD);
		addTalkId(WOOD, ORVEN, LEOPARD, LAWRENCE, SOFIA);
		addKillId(SHILENSEVIL);
		questItemIds = new int[]{TEXT, SCULPTURE};
	}

	public static void main(String[] args)
	{
		new _00197_SevenSignTheSacredBookOfSeal();
	}

	@Override
	public int getQuestId()
	{
		return 197;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}

		switch(event)
		{
			case "32593-04.htm":
				st.startQuest();
				break;
			case "30857-04.htm":
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32594-03.htm":
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32595-04.htm":
				L2Npc monster = addSpawn(SHILENSEVIL, 152520, -57685, -3438, 0, false, 60000, true);
				monster.broadcastPacket(new NS(monster.getObjectId(), ChatType.NPC_ALL, monster.getNpcId(), NpcStringId.YOU_ARE_NOT_THE_OWNER_OF_THAT_ITEM));
				monster.setRunning();
				monster.getAttackable().attackCharacter(player);
				break;
			case "32595-08.htm":
				st.setCond(5);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32596-04.htm":
				st.setCond(6);
				st.giveItems(TEXT, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32593-08.htm":
				if(player.isSubClassActive())
				{
					return "subclass_forbidden.htm";
				}
				st.takeItems(TEXT, 1);
				st.takeItems(SCULPTURE, 1);
				st.addExpAndSp(10000000, 2500000);
				st.unset("cond");
				st.exitQuest(QuestType.ONE_TIME);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				break;
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}
		if(npc.getNpcId() == SHILENSEVIL && st.getCond() == 3)
		{
			npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.S1_YOU_MAY_HAVE_WON_THIS_TIME_BUT_NEXT_TIME_I_WILL_SURELY_CAPTURE_YOU).addStringParameter(player.getName()));
			st.giveItems(SCULPTURE, 1);
			st.setCond(4);
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getClass());
		if(st == null || player.getLevel() < 79)
		{
			return htmltext;
		}
		int cond = st.getCond();

		if(npc.getNpcId() == WOOD)
		{
			QuestState first = player.getQuestState(_00196_SevenSignSealOfTheEmperor.class);
			if(first != null && first.getState() == COMPLETED && st.getState() == CREATED && player.getLevel() >= 79)
			{
				htmltext = "32593-01.htm";
			}
			else if(cond == 0)
			{
				htmltext = "32593-00.htm";
				st.exitQuest(QuestType.REPEATABLE);
			}
			else if(cond == 1)
			{
				htmltext = "32593-05.htm";
			}
			else if(cond == 6)
			{
				htmltext = "32593-06.htm";
			}
		}
		else if(npc.getNpcId() == ORVEN)
		{
			if(cond == 1)
			{
				htmltext = "30857-01.htm";
			}
			else if(cond == 2)
			{
				htmltext = "30857-05.htm";
			}
		}
		else if(npc.getNpcId() == LEOPARD)
		{
			if(cond == 2)
			{
				htmltext = "32594-01.htm";
			}
			else if(cond == 3)
			{
				htmltext = "32594-04.htm";
			}
		}
		else if(npc.getNpcId() == LAWRENCE)
		{
			if(cond == 3)
			{
				htmltext = "32595-01.htm";
			}
			else if(cond == 4)
			{
				htmltext = "32595-05.htm";
			}
			else if(cond == 5)
			{
				htmltext = "32595-09.htm";
			}
		}
		else if(npc.getNpcId() == SOFIA)
		{
			if(cond == 5)
			{
				htmltext = "32596-01.htm";
			}
			else if(cond == 6)
			{
				htmltext = "32596-05.htm";
			}
		}
		return htmltext;
	}
}