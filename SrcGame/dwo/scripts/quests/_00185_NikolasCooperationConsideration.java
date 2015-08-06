package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 16.08.12
 * Time: 23:20
 */

public class _00185_NikolasCooperationConsideration extends Quest
{
	// Квестовые персонажи
	private static final int Lorain = 30673;
	private static final int Nikola = 30621;
	private static final int Device = 32366;
	private static final int Alarm = 32367;

	// Квестовые предметы
	private static final int Certificate = 10362;
	private static final int Metal = 10359;
	private static final int BrokenMetal = 10360;
	private static final int NicolasMap = 10361;

	public _00185_NikolasCooperationConsideration()
	{
		addTalkId(Nikola, Lorain, Device, Alarm);
		questItemIds = new int[]{Metal, BrokenMetal, NicolasMap};
	}

	public static void main(String[] args)
	{
		new _00185_NikolasCooperationConsideration();
	}

	@Override
	public int getQuestId()
	{
		return 185;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(event.equals("quest_accept") && !st.isCompleted())
		{
			if(st.getPlayer().getLevel() >= 40)
			{
				st.startQuest();
				st.giveItems(NicolasMap, 1);
				return "maestro_nikola_q0185_06.htm";
			}
			else
			{
				return "maestro_nikola_q0185_03a.htm";
			}
		}
		if(event.equals("1"))
		{
			npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.THE_ALARM_WILL_SELF_DESTRUCT_IN_60_SECONDS_ENTER_PASSCODE_TO_OVERRIDE));
			st.startQuestTimer("2", 30000);
			return null;
		}
		if(event.equals("2"))
		{
			npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.THE_ALARM_WILL_SELF_DESTRUCT_IN_30_SECONDS_ENTER_PASSCODE_TO_OVERRIDE));
			st.startQuestTimer("3", 20000);
			return null;
		}
		if(event.equals("3"))
		{
			npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.THE_ALARM_WILL_SELF_DESTRUCT_IN_10_SECONDS_ENTER_PASSCODE_TO_OVERRIDE));
			st.startQuestTimer("4", 10000);
			return null;
		}
		if(event.equals("4"))
		{
			npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.RECORDER_CRUSHED));
			npc.getLocationController().delete();
			st.set("step", "2");
			return null;
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		if(npcId == Nikola)
		{
			switch(reply)
			{
				case 1:
					if(st == null)
					{
						st = newQuestState(player);
						st.setState(CREATED);
					}
					return "maestro_nikola_q0185_03.htm";
				case 2:
					return "maestro_nikola_q0185_04.htm";
				case 3:
					return "maestro_nikola_q0185_05.htm";
			}
		}
		else if(npcId == Lorain)
		{
			switch(reply)
			{
				case 1:
					return "researcher_lorain_q0185_02.htm";
				case 2:
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(2);
					st.takeItems(NicolasMap, -1);
					return "researcher_lorain_q0185_03.htm";
				case 3:
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(3);
					return "researcher_lorain_q0185_05.htm";
				case 4:
					return "researcher_lorain_q0185_08.htm";
				case 5:
					String htmltext = "researcher_lorain_q0185_09.htm";
					if(st.getQuestItemsCount(BrokenMetal) == 1)
					{
						htmltext = "30673-10.htm";
					}
					else if(st.getQuestItemsCount(Metal) == 1)
					{
						st.giveItems(Certificate, 1);
					}
					if(player.getLevel() < 50)
					{
						st.addExpAndSp(203717, 14032);
					}
					st.giveAdena(72527, true);
					st.exitQuest(QuestType.ONE_TIME);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					return htmltext;
			}
		}
		else if(npcId == Device)
		{
			switch(reply)
			{
				case 1:
					L2Npc alarm = addSpawn(Alarm, new Location(16491, 113563, -9064));
					st.set("step", "1");
					st.playSound(QuestSound.ITEMSOUND_SIREN);
					st.startQuestTimer("1", 60000);
					alarm.broadcastPacket(new NS(alarm.getObjectId(), ChatType.NPC_ALL, alarm.getNpcId(), NpcStringId.INTRUDER_ALERT_THE_ALARM_WILL_SELF_DESTRUCT_IN_2_MINUTES));
					return "broken_controller_q0185_02.htm";
				case 2:
					st.unset("step");
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(4);
					st.giveItems(Metal, 1);
					return "broken_controller_q0185_06.htm";
				case 3:
					st.unset("step");
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(5);
					st.giveItems(BrokenMetal, 1);
					return "broken_controller_q0185_08.htm";
			}
		}
		else if(npcId == Alarm)
		{
			int pass = st.getInt("pass");
			switch(reply)
			{
				case 2:
					st.set("pass", "0");
					return "alarm_of_giant_q0184_q0185_02.htm";
				case 3:
					st.set("pass", String.valueOf(++pass));
					return "alarm_of_giant_q0184_q0185_03.htm";
				case 4:
					st.set("pass", String.valueOf(++pass));
					return "alarm_of_giant_q0184_q0185_05.htm";
				case 5:
					st.set("pass", String.valueOf(++pass));
					return "alarm_of_giant_q0184_q0185_07.htm";
				case 6:
					if(pass == 4)
					{
						st.set("step", "3");
						if(st.getQuestTimer("1") != null)
						{
							st.getQuestTimer("1").cancel();
						}
						if(st.getQuestTimer("2") != null)
						{
							st.getQuestTimer("2").cancel();
						}
						if(st.getQuestTimer("3") != null)
						{
							st.getQuestTimer("3").cancel();
						}
						if(st.getQuestTimer("4") != null)
						{
							st.getQuestTimer("4").cancel();
						}
						st.unset("pass");
						npc.getLocationController().delete();
						return "alarm_of_giant_q0184_q0185_09.htm";
					}
					else
					{
						return "alarm_of_giant_q0184_q0185_10.htm";
					}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Nikola)
		{
			switch(st.getState())
			{
				case CREATED:
					return st.getPlayer().getLevel() < 40 ? "maestro_nikola_q0185_03a.htm" : "maestro_nikola_q0185_03.htm";
				case STARTED:
					if(cond == 1)
					{
						return "maestro_nikola_q0185_07.htm";
					}
					break;
				case COMPLETED:
					return getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
			}
		}
		else if(npcId == Lorain)
		{
			if(st.isStarted())
			{
				switch(st.getCond())
				{
					case 1:
						return "researcher_lorain_q0185_01.htm";
					case 2:
						return "researcher_lorain_q0185_04.htm";
					case 3:
						return "researcher_lorain_q0185_06.htm";
					case 4:
					case 5:
						return "researcher_lorain_q0185_07.htm";
				}
			}
		}
		else if(npcId == Device)
		{
			if(st.isStarted())
			{
				if(cond == 3)
				{
					switch(st.getInt("step"))
					{
						case 0:
							return "broken_controller_q0185_01.htm";
						case 1:
							return "broken_controller_q0185_03.htm";
						case 2:
							return "broken_controller_q0185_05.htm";
						case 3:
							return "broken_controller_q0185_07.htm";
					}
				}
			}
		}
		else if(npcId == Alarm)
		{
			if(st.isStarted() && cond == 3)
			{
				return "alarm_of_giant_q0184_q0185_02.htm";
			}
		}
		return null;
	}
}
