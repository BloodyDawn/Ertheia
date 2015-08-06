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
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.util.Rnd;

public class _00450_GraveRobberMemberRescue extends Quest
{
	private static final int KANEMIKA = 32650;
	private static final int WARRIOR_NPC = 32651;

	private static final int WARRIOR_MON = 22741;

	private static final int EVIDENCE_OF_MIGRATION = 14876;

	public _00450_GraveRobberMemberRescue()
	{
		addStartNpc(KANEMIKA);
		addTalkId(KANEMIKA);
		addTalkId(WARRIOR_NPC);
		questItemIds = new int[]{EVIDENCE_OF_MIGRATION};
	}

	public static void main(String[] args)
	{
		new _00450_GraveRobberMemberRescue();
	}

	@Override
	public int getQuestId()
	{
		return 450;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}

		if(event.equals("32650-05.htm"))
		{
			st.startQuest();
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		int cond = st.getCond();
		if(npc.getNpcId() == KANEMIKA)
		{
			if(cond == 0)
			{
				if(st.isNowAvailable())
				{
					if(player.getLevel() >= 80)
					{
						st.setState(CREATED);
						return "32650-01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "32650-00.htm";
					}
				}
				else
				{
					return "32650-09.htm";
				}
			}
			else if(cond == 1)
			{
				return st.hasQuestItems(EVIDENCE_OF_MIGRATION) ? "32650-07.htm" : "32650-06.htm";
			}
			else if(cond == 2 && st.getQuestItemsCount(EVIDENCE_OF_MIGRATION) == 10)
			{
				st.addExpAndSp(6886980, 8116410);
				st.giveAdena(371400, true);
				st.takeItems(EVIDENCE_OF_MIGRATION, 10);
				st.unset("cond");
				st.exitQuest(QuestType.DAILY);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return "32650-08.htm";
			}
		}
		else if(cond == 1 && npc.getNpcId() == WARRIOR_NPC)
		{
			if(Rnd.getChance(50))
			{
				st.giveItems(EVIDENCE_OF_MIGRATION, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(npc.getX() + 100, npc.getY() + 100, npc.getZ(), 0));
				npc.getSpawn().decreaseCount(npc);
				npc.getLocationController().delete();
				if(st.getQuestItemsCount(EVIDENCE_OF_MIGRATION) == 10)
				{
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				return "32651-01.htm";
			}
			else
			{
				player.sendPacket(new ExShowScreenMessage(NpcStringId.THE_GRAVE_ROBBER_WARRIOR_HAS_BEEN_FILLED_WITH_DARK_ENERGY_AND_IS_ATTACKING_YOU, ExShowScreenMessage.MIDDLE_CENTER, 4000));
				L2Npc warrior = st.addSpawn(WARRIOR_MON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 600000);
				warrior.setRunning();
				((L2Attackable) warrior).addDamageHate(player, 0, 999);
				warrior.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
				if(Rnd.getChance(50))
				{
					npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.GRUNT_OH));
				}
				else
				{
					npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.GRUNT_WHATS_WRONG_WITH_ME));
				}
				npc.getSpawn().decreaseCount(npc);
				npc.getLocationController().delete();
				return null;
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 80;

	}
}