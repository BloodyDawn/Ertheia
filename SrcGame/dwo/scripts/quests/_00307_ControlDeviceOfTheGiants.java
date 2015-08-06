package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.RadarControl;

public class _00307_ControlDeviceOfTheGiants extends Quest
{
	// NPC
	private static final int DROPH = 32711;

	// RB
	private static final int GORGOLOS = 25681;
	private static final int LAST_TITAN_UTENUS = 25684;
	private static final int GIANT_MARPANAK = 25680;
	private static final int HEKATON_PRIME = 25687;

	// Items
	private static final int SUPPORT_ITEMS = 14850;
	private static final int CET_1_SHEET = 14851;
	private static final int CET_2_SHEET = 14852;
	private static final int CET_3_SHEET = 14853;

	private static final int respawnDelay = 3600000; // 1 hour

	public _00307_ControlDeviceOfTheGiants()
	{

		addStartNpc(DROPH);
		addTalkId(DROPH);
		addKillId(GORGOLOS, LAST_TITAN_UTENUS, GIANT_MARPANAK, HEKATON_PRIME);
	}

	public static void main(String[] args)
	{
		new _00307_ControlDeviceOfTheGiants();
	}

	private void rewardPlayer(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st != null && st.getInt("spawned") == 1)
		{
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			st.unset("spawned");
			st.setCond(2);
			st.saveGlobalQuestVar("Hekaton respawn", Long.toString(System.currentTimeMillis() + respawnDelay));
		}
	}

	@Override
	public int getQuestId()
	{
		return 307;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}

		if(event.equalsIgnoreCase("32711-04.htm"))
		{
			if(st.getPlayer().getLevel() >= 79)
			{
				st.startQuest();
				if(!st.hasQuestItems(CET_1_SHEET) || !st.hasQuestItems(CET_2_SHEET) || !st.hasQuestItems(CET_3_SHEET))
				{
					return "32711-04.htm";
				}
				else if(st.hasQuestItems(CET_1_SHEET) && st.hasQuestItems(CET_2_SHEET) && st.hasQuestItems(CET_3_SHEET))
				{
					return "32711-04a.htm";
				}
			}
		}
		else if(event.equalsIgnoreCase("32711-05a.htm"))
		{
			player.sendPacket(new RadarControl(0, 2, 186214, 61591, -4152));
		}
		else if(event.equalsIgnoreCase("32711-05b.htm"))
		{
			player.sendPacket(new RadarControl(0, 2, 187554, 60800, -4984));
		}
		else if(event.equalsIgnoreCase("32711-05c.htm"))
		{
			player.sendPacket(new RadarControl(0, 2, 193432, 53922, -4368));
		}
		// Hekaton Prime spawn
		else if(event.equalsIgnoreCase("spawn"))
		{
			String test = st.getGlobalQuestVar("Hekaton respawn");

			if(test.isEmpty())
			{
				st.takeItems(CET_1_SHEET, 1);
				st.takeItems(CET_2_SHEET, 1);
				st.takeItems(CET_3_SHEET, 1);
				addSpawn(HEKATON_PRIME, 191887, 56405, -7626, 1000, false, 0);
				st.set("spawned", "1");
				return "32711-09.htm";
			}
			else
			{
				long remain = Long.parseLong(test) - System.currentTimeMillis();

				if(remain > 0)
				{
					return "32711-09a.htm";
				}
				else
				{
					st.takeItems(CET_1_SHEET, 1);
					st.takeItems(CET_2_SHEET, 1);
					st.takeItems(CET_3_SHEET, 1);
					addSpawn(HEKATON_PRIME, 192062, 57357, -7650, 1000, false, 0);
					st.set("spawned", "1");
					return "32711-09.htm";
				}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, "1");
		if(partyMember == null)
		{
			return null;
		}
		QuestState st = partyMember.getQuestState(getClass());

		switch(npc.getNpcId())
		{
			case GORGOLOS:
				st.giveItems(CET_1_SHEET, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				break;
			case LAST_TITAN_UTENUS:
				st.giveItems(CET_2_SHEET, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				break;
			case GIANT_MARPANAK:
				st.giveItems(CET_3_SHEET, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				break;
			case HEKATON_PRIME:
				if(player.isInParty())
				{
					player.getParty().getMembers().forEach(this::rewardPlayer);
				}
				else
				{
					rewardPlayer(player);
				}
				break;
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
				return player.getLevel() >= 79 ? "32711-01.htm" : "32711-02.htm";
			case STARTED:
				if(st.getInt("spawned") == 1)
				{
					return "32711-09.htm";
				}
				if(st.getCond() == 1)
				{
					return !st.hasQuestItems(CET_1_SHEET) || !st.hasQuestItems(CET_2_SHEET) || !st.hasQuestItems(CET_3_SHEET) ? "32711-07.htm" : "32711-08.htm";
				}
				if(st.getCond() == 2)
				{
					st.giveItems(SUPPORT_ITEMS, 1);
					st.exitQuest(QuestType.REPEATABLE);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "32711-10.htm";
				}
				break;
		}
		return null;
	}
}