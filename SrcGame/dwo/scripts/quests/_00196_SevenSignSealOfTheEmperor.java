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
import dwo.gameserver.network.game.serverpackets.SystemMessage;

public class _00196_SevenSignSealOfTheEmperor extends Quest
{
	private static boolean spawned;

	public _00196_SevenSignSealOfTheEmperor()
	{
		addStartNpc(30969);
		addTalkId(30969, 32584, 32586, 32593);
	}

	public static void main(String[] args)
	{
		new _00196_SevenSignSealOfTheEmperor();
	}

	@Override
	public int getQuestId()
	{
		return 196;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("spawnAllow"))
		{
			spawned = false;
			return null;
		}
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}
		switch(event)
		{
			case "30969-04.htm":
				st.startQuest();
				break;
			case "30969-05.htm":
				if(spawned)
				{
					return "30969-06.htm";
				}
				// Спауним дядьку маммона
				spawned = true;
				L2Npc mammon = addSpawn(32584, 109743, 219975, -3512, 0, false, 30000);
				mammon.broadcastPacket(new NS(mammon.getObjectId(), ChatType.NPC_ALL, mammon.getNpcId(), NpcStringId.WHO_DARES_SUMMON_THE_MERCHANT_OF_MAMMON));
				startQuestTimer("spawnAllow", 30000, null, null);
				break;
			case "32584-04.htm":
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32586-06.htm":
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				player.sendPacket(SystemMessage.getSystemMessage(3031));
				player.sendPacket(SystemMessage.getSystemMessage(3039));
				st.giveItem(13808);
				st.giveItem(15310);
				break;
			case "32586-12.htm":
				st.setCond(5);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.takeItems(13846, -1);
				st.takeItems(13809, -1);
				st.takeItems(13808, -1);
				st.takeItems(15310, -1);
				break;
			case "30969-11.htm":
				st.setCond(6);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32593-01.htm":
				if(player.isSubClassActive())
				{
					return "subclass_forbidden.htm";
				}
				st.addExpAndSp(10000000, 2500000);
				st.unset("cond");
				st.exitQuest(QuestType.ONE_TIME);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				break;
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		QuestState st1 = player.getQuestState(_00195_SevenSignSecretRitualOfThePriests.class);
		String htmltext = getNoQuestMsg(player);
		if(st == null || player.getLevel() < 79)
		{
			return htmltext;
		}
		int cond = st.getCond();
		if(st.isCompleted())
		{
			htmltext = getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
		}
		switch(npc.getNpcId())
		{
			case 30969:
				if(st1 != null && st1.isCompleted())
				{
					if(cond == 0)
					{
						if(player.getLevel() >= 79)
						{
							htmltext = "30969-01.htm";
						}
						else
						{
							htmltext = "30969-0a.htm";
							st.exitQuest(QuestType.REPEATABLE);
						}
					}
					else if(cond == 1)
					{
						htmltext = "30969-04.htm";
					}
					else if(cond == 2)
					{
						st.setCond(3);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						htmltext = "30969-07.htm";
					}
					else if(cond < 5)
					{
						htmltext = "30969-08.htm";
					}
					else if(cond == 5)
					{
						htmltext = "30969-09.htm";
					}
					else if(cond > 5)
					{
						htmltext = "30969-12.htm";
					}
				}
				else
				{
					htmltext = "30969-0a.htm";
				}
				break;
			case 32584:
				if(cond == 1)
				{
					htmltext = "32584-00.htm";
				}
				break;
			case 32586:
				if(cond == 3)
				{
					htmltext = "32586-00.htm";
				}
				else if(cond == 4)
				{
					htmltext = player.getItemsCount(13846) >= 4 ? "32586-08.htm" : "32586-07.htm";
				}
				else if(cond == 5)
				{
					htmltext = "32586-13.htm";
				}
				break;
			case 32593:
				if(cond == 6)
				{
					htmltext = "32593-00.htm";
				}
				break;
		}
		return htmltext;
	}
}