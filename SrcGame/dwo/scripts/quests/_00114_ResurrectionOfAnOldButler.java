package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;

/**
 * @author Threat
 */

public class _00114_ResurrectionOfAnOldButler extends Quest
{
	// NPCs
	private static final int Newyear = 31961;
	private static final int Yumi = 32041;
	private static final int Stones = 32046;
	private static final int Wendy = 32047;
	private static final int Box = 32050;

	// Mobs
	private static final int Guardian = 27318;

	// Items
	private static final int Detector = 8090;
	private static final int Detector2 = 8091;
	private static final int Starstone = 8287;
	private static final int Letter = 8288;
	private static final int Starstone2 = 8289;

	private static boolean isSpawned;

	public _00114_ResurrectionOfAnOldButler()
	{
		addStartNpc(Yumi);
		addFirstTalkId(Stones);
		addTalkId(Yumi, Wendy, Box, Stones, Newyear);
		addKillId(Guardian);
		questItemIds = new int[]{Starstone, Detector, Detector2, Letter, Starstone2};
	}

	public static void main(String[] args)
	{
		new _00114_ResurrectionOfAnOldButler();
	}

	@Override
	public int getQuestId()
	{
		return 114;
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
			case "31961-02.htm":
				st.setCond(22);
				st.takeItems(Letter, 1);
				st.giveItems(Starstone2, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32041-02.htm":
				st.startQuest();
				st.set("talk", "0");
				break;
			case "32041-06.htm":
				st.set("talk", "1");
				break;
			case "32041-07.htm":
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.set("talk", "0");
				break;
			case "32041-10.htm":
			{
				int choice = st.getInt("choice");
				if(choice == 1)
				{
					return "32041-10.htm";
				}
				if(choice == 2)
				{
					return "32041-10a.htm";
				}
				if(choice == 3)
				{
					return "32041-10b.htm";
				}
				break;
			}
			case "32041-11.htm":
				st.set("talk", "1");
				break;
			case "32041-18.htm":
				st.set("talk", "2");
				break;
			case "32041-20.htm":
				st.setCond(6);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.set("talk", "0");
				break;
			case "32041-25.htm":
				st.setCond(17);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.giveItems(Detector, 1);
				break;
			case "32041-28.htm":
				st.takeItems(Detector2, 1);
				st.set("talk", "1");
				break;
			case "32041-31.htm":
				int choice = st.getInt("choice");
				if(choice > 1)
				{
					return "32041-37.htm";
				}
				break;
			case "32041-32.htm":
				st.setCond(21);
				st.giveItems(Letter, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32041-36.htm":
				st.setCond(20);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32046-02.htm":
				st.setCond(19);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32046-06.htm":
				st.addExpAndSp(1846611, 144270);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				break;
			case "32047-01.htm":
				if(st.getInt("talk") + st.getInt("talk1") == 2)
				{
					return "32047-04.htm";
				}
				if(st.getInt("talk") + st.getInt("talk1") + st.getInt("talk2") == 6)
				{
					return "32047-08.htm";
				}
				break;
			case "32047-02.htm":
				if(st.getInt("talk") == 0)
				{
					st.set("talk", "1");
				}
				break;
			case "32047-03.htm":
				if(st.getInt("talk1") == 0)
				{
					st.set("talk1", "1");
				}
				break;
			case "32047-05.htm":
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.set("talk", "0");
				st.set("choice", "1");
				st.unset("talk1");
				break;
			case "32047-06.htm":
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.set("talk", "0");
				st.set("choice", "2");
				st.unset("talk1");
				break;
			case "32047-07.htm":
				st.setCond(5);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.set("talk", "0");
				st.set("choice", "3");
				st.unset("talk1");
				break;
			case "32047-13.htm":
				st.setCond(7);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32047-13a.htm":
				st.setCond(10);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32047-15.htm":
				if(st.getInt("talk") == 0)
				{
					st.set("talk", "1");
				}
				break;
			case "32047-15a.htm":
				if(isSpawned)
				{
					return "32047-19a.htm";
				}
				L2Npc golem = st.addSpawn(Guardian, 96977, -110625, -3280, 0, false, 900000);
				golem.broadcastPacket(new NS(golem, ChatType.NPC_ALL, NpcStringId.YOU_S1_YOU_ATTACKED_WENDY_PREPARE_TO_DIE).addStringParameter(player.getName()));
				golem.setRunning();
				((L2Attackable) golem).addDamageHate(player, 0, 999);
				golem.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
				isSpawned = true;
				break;
			case "32047-17a.htm":
				st.setCond(12);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32047-20.htm":
				st.set("talk", "2");
				break;
			case "32047-23.htm":
				st.setCond(13);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.set("talk", "0");
				break;
			case "32047-25.htm":
				st.setCond(15);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.takeItems(Starstone, 1);
				break;
			case "32047-30.htm":
				st.set("talk", "2");
				break;
			case "32047-33.htm":
				if(st.getCond() == 7)
				{
					st.setCond(8);
					st.set("talk", "0");
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				else if(st.getCond() == 8)
				{
					st.setCond(9);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "32047-34.htm";
				}
				break;
			case "32047-34.htm":
				st.setCond(9);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32047-38.htm":
				st.giveItems(Starstone2, 1);
				st.takeAdena(3000);
				st.setCond(26);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32050-02.htm":
				st.playSound(QuestSound.ITEMSOUND_ARMOR_WOOD);
				st.set("talk", "1");
				break;
			case "32050-04.htm":
				st.setCond(14);
				st.giveItems(Starstone, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.set("talk", "0");
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
			return super.onKill(npc, player, isPet);
		}
		if(st.getCond() == 10)
		{
			npc.broadcastPacket(new NS(npc, ChatType.NPC_ALL, NpcStringId.THIS_ENEMY_IS_FAR_TOO_POWERFUL_FOR_ME_TO_FIGHT_I_MUST_WITHDRAW));
			st.setCond(11);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return htmltext;
		}
		if(st.isCompleted())
		{
			return getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
		}
		switch(npc.getNpcId())
		{
			case Yumi:
				switch(st.getCond())
				{
					case 0:
						QuestState Pavel = player.getQuestState(_00121_PavelTheGiants.class);
						if(Pavel != null && Pavel.isCompleted() && st.getPlayer().getLevel() >= 70)
						{
							htmltext = "32041-01.htm";
						}
						else
						{
							htmltext = "32041-00.htm";
							st.exitQuest(QuestType.REPEATABLE);
						}
						break;
					case 1:
						htmltext = st.getInt("talk") == 0 ? "32041-02.htm" : "32041-06.htm";
						break;
					case 2:
						htmltext = "32041-08.htm";
						break;
					case 3:
					case 4:
					case 5:
						if(st.getInt("talk") == 0)
						{
							htmltext = "32041-09.htm";
						}
						else
						{
							htmltext = st.getInt("talk") == 1 ? "32041-11.htm" : "32041-18.htm";
						}
						break;
					case 6:
						htmltext = "32041-21.htm";
						break;
					case 9:
					case 12:
					case 16:
						htmltext = "32041-22.htm";
						break;
					case 17:
						htmltext = "32041-26.htm";
						break;
					case 19:
						htmltext = st.getInt("talk") == 0 ? "32041-27.htm" : "32041-28.htm";
						break;
					case 20:
						htmltext = "32041-36.htm";
						break;
					case 21:
						htmltext = "32041-33.htm";
						break;
					case 22:
					case 26:
						htmltext = "32041-34.htm";
						st.setCond(27);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						break;
					case 27:
						htmltext = "32041-35.htm";
						break;
				}
				break;
			case Wendy:
				switch(st.getCond())
				{
					case 2:
						if(st.getInt("talk") + st.getInt("talk1") < 2)
						{
							htmltext = "32047-01.htm";
						}
						else if(st.getInt("talk") + st.getInt("talk1") == 2)
						{
							htmltext = "32047-04.htm";
						}
						break;
					case 3:
						htmltext = "32047-09.htm";
						break;
					case 4:
					case 5:
						htmltext = "32047-09a.htm";
						break;
					case 6:
						int choice = st.getInt("choice");
						if(choice == 1)
						{
							if(st.getInt("talk") == 0)
							{
								htmltext = "32047-10.htm";
							}
							else
							{
								htmltext = st.getInt("talk") == 1 ? "32047-20.htm" : "32047-30.htm";
							}
						}
						else if(choice == 2)
						{
							htmltext = "32047-10a.htm";
						}
						else if(choice == 3)
						{
							if(st.getInt("talk") == 0)
							{
								htmltext = "32047-14.htm";
							}
							else
							{
								htmltext = st.getInt("talk") == 1 ? "32047-15.htm" : "32047-20.htm";
							}
						}
						break;
					case 7:
						if(st.getInt("talk") == 0)
						{
							htmltext = "32047-14.htm";
						}
						else
						{
							htmltext = st.getInt("talk") == 1 ? "32047-15.htm" : "32047-20.htm";
						}
						break;
					case 8:
						htmltext = "32047-30.htm";
						break;
					case 9:
						htmltext = "32047-27.htm";
						break;
					case 10:
						htmltext = "32047-14a.htm";
						break;
					case 11:
						htmltext = "32047-16a.htm";
						break;
					case 12:
						htmltext = "32047-18a.htm";
						break;
					case 13:
						htmltext = "32047-23.htm";
						break;
					case 14:
						htmltext = "32047-24.htm";
						break;
					case 15:
						htmltext = "32047-26.htm";
						st.setCond(16);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						break;
					case 16:
						htmltext = "32047-27.htm";
						break;
					case 20:
						htmltext = "32047-35.htm";
						break;
					case 26:
						htmltext = "32047-40.htm";
						break;
				}
				break;
			case Box:
				switch(st.getCond())
				{
					case 13:
						htmltext = st.getInt("talk") == 0 ? "32050-01.htm" : "32050-03.htm";
						break;
					case 14:
						htmltext = "32050-05.htm";
						break;
				}
				break;
			case Stones:
				switch(st.getCond())
				{
					case 18:
						htmltext = "32046-01.htm";
						break;
					case 19:
						htmltext = "32046-02.htm";
						break;
					case 27:
						htmltext = "32046-03.htm";
						break;
				}
				break;
			case Newyear:
				switch(st.getCond())
				{
					case 21:
						htmltext = "31961-01.htm";
						break;
					case 22:
						htmltext = "31961-03.htm";
						break;
				}
				break;
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			if(npc.getNpcId() == Stones && st.getCond() == 17)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.takeItems(Detector, 1);
				st.giveItems(Detector2, 1);
				st.setCond(18);
				player.sendPacket(new ExShowScreenMessage(NpcStringId.THE_RADIO_SIGNAL_DETECTOR_IS_RESPONDING_A_SUSPICIOUS_PILE_OF_STONES_CATCHES_YOUR_EYE, ExShowScreenMessage.TOP_CENTER, 4500));
			}
		}
		npc.showChatWindow(player);
		return null;
	}
}