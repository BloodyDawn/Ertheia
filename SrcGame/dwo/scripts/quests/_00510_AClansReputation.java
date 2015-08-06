package dwo.scripts.quests;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2ClanMember;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowInfoUpdate;

public class _00510_AClansReputation extends Quest
{
	private static final int[] RAIDS = {
		22215, 22216, 22217, 22218
	};

	// Quest NPC
	private static final int VALDIS = 31331;

	// Quest Items
	private static final int CLAW = 8767;

	// Reward
	private static final int CLAN_POINTS_REWARD = (int) (12 * Config.RATE_QUEST_REWARD); // Rep Points Per Tyrannosaurus Item

	public _00510_AClansReputation()
	{

		addStartNpc(VALDIS);
		addTalkId(VALDIS);
		addKillId(RAIDS);
	}

	public static void main(String[] args)
	{
		new _00510_AClansReputation();
	}

	@Override
	public int getQuestId()
	{
		return 510;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = null;
		if(player.isClanLeader())
		{
			st = player.getQuestState(getClass());
		}
		else
		{
			L2Clan clan = player.getClan();
			if(clan != null)
			{
				L2ClanMember leader = clan.getLeader();
				if(leader != null)
				{
					L2PcInstance pleader = leader.getPlayerInstance();
					if(pleader != null)
					{
						if(player.isInsideRadius(pleader, 1600, true, false))
						{
							st = pleader.getQuestState(getClass());
						}
					}
				}
			}
		}

		if(st == null)
		{
			return null;
		}

		if(st.getState() == STARTED)
		{
			int npcId = npc.getNpcId();

			for(int id : RAIDS)
			{
				if(id == npcId)
				{
					st.giveItems(CLAW, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					break;
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

		if(clan == null || !player.isClanLeader() || clan.getLevel() < 5)
		{
			st.exitQuest(QuestType.REPEATABLE);
			return "31331-0.htm";
		}
		int cond = st.getCond();
		QuestStateType id = st.getState();
		if(id == CREATED && cond == 0)
		{
			st.startQuest();
			return "31331-1.htm";
		}
		if(id == STARTED && cond == 1)
		{
			int count = (int) st.getQuestItemsCount(CLAW);
			if(count <= 0)
			{
				return "31331-4.htm";
			}
			if(count >= 1)
			{
				return "31331-7.htm";
			}
			st.takeItems(CLAW, -1);
			int reward = CLAN_POINTS_REWARD * count;
			clan.addReputationScore(reward, true);
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_QUEST_COMPLETED_AND_S1_POINTS_GAINED).addNumber(reward));
			clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
		}
		return null;
	}
}
