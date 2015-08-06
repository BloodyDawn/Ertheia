package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.RadarControl;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowInfoUpdate;
import gnu.trove.map.hash.TIntObjectHashMap;

public class _00509_TheClansPrestige extends Quest
{
	// Квестовые персонажи
	private static final int VALDIS = 31331;

	private static final TIntObjectHashMap<int[]> REWARD_POINTS = new TIntObjectHashMap<>();

	static
	{
		REWARD_POINTS.put(1, new int[]{25290, 8489, 1378}); // Daimon The White-Eyed
		REWARD_POINTS.put(2, new int[]{25293, 8490, 1378}); // Hestia, Guardian Deity Of The Hot Springs
		REWARD_POINTS.put(3, new int[]{25523, 8491, 1070}); // Plague Golem
		REWARD_POINTS.put(4, new int[]{25322, 8492, 782}); // Demon's Agent Falston
	}

	private static final int[] RAID_BOSS = {
		25290, 25293, 25523, 25322
	};

	public _00509_TheClansPrestige()
	{
		addStartNpc(VALDIS);
		addTalkId(VALDIS);
		addKillId(RAID_BOSS);
	}

	public static void main(String[] args)
	{
		new _00509_TheClansPrestige();
	}

	@Override
	public int getQuestId()
	{
		return 509;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		switch(event)
		{
			case "31331-0.html":
				st.startQuest();
				break;
			case "31331-1.html":
				st.set("raid", "1");
				player.sendPacket(new RadarControl(0, 2, 186304, -43744, -3193));
				break;
			case "31331-2.html":
				st.set("raid", "2");
				player.sendPacket(new RadarControl(0, 2, 134672, -115600, -1216));
				break;
			case "31331-3.html":
				st.set("raid", "3");
				player.sendPacket(new RadarControl(0, 2, 170000, -60000, -3500));
				break;
			case "31331-4.html":
				st.set("raid", "4");
				player.sendPacket(new RadarControl(0, 2, 93296, -75104, -1824));
				break;
			case "31331-5.html":
				st.exitQuest(QuestType.REPEATABLE);
				break;
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(player.getClan() == null)
		{
			return null;
		}

		QuestState st = null;

		if(player.isClanLeader())
		{
			st = player.getQuestState(getClass());
		}
		else
		{
			L2PcInstance pleader = player.getClan().getLeader().getPlayerInstance();
			if(pleader != null && player.isInsideRadius(pleader, 1500, true, false))
			{
				st = pleader.getQuestState(getClass());
			}
		}

		if(st != null && st.isStarted())
		{
			int raid = st.getInt("raid");
			if(REWARD_POINTS.containsKey(raid))
			{
				if(npc.getNpcId() == REWARD_POINTS.get(raid)[0] && !st.hasQuestItems(REWARD_POINTS.get(raid)[1]))
				{
					st.rewardItems(REWARD_POINTS.get(raid)[1], 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		L2Clan clan = player.getClan();

		switch(st.getState())
		{
			case CREATED:
				return clan == null || !player.isClanLeader() || clan.getLevel() < 6 ? "31331-0a.htm" : "31331-0b.htm";
			case STARTED:
				if(clan == null || !player.isClanLeader())
				{
					st.exitQuest(QuestType.REPEATABLE);
					return "31331-6.html";
				}

				int raid = st.getInt("raid");

				if(REWARD_POINTS.containsKey(raid))
				{
					if(st.hasQuestItems(REWARD_POINTS.get(raid)[1]))
					{
						st.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_1);
						st.takeItems(REWARD_POINTS.get(raid)[1], -1);
						clan.addReputationScore(REWARD_POINTS.get(raid)[2], true);
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_QUEST_COMPLETED_AND_S1_POINTS_GAINED).addNumber(REWARD_POINTS.get(raid)[2]));
						clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
						return "31331-" + raid + "b.html";
					}
					else
					{
						return "31331-" + raid + "a.html";
					}
				}
				else
				{
					return "31331-0.html";
				}
		}
		return null;
	}
}