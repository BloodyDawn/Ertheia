package dwo.scripts.quests;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.List;

public class _00625_TheFinestIngredientsPart2 extends Quest
{
	private static final int JEREMY = 31521;
	private static final int TABLE = 31542;

	private static final int BUMPALUMP = 25296;

	private static final int SAUCE = 7205;
	private static final int FOOD = 7209;
	private static final int MEAT = 7210;

	private static final int[] REWARDS = {
		4589, 4590, 4591, 4592, 4593, 4594, 4595
	};

	public _00625_TheFinestIngredientsPart2()
	{
		int[] questItems = {
			FOOD, MEAT, SAUCE
		};
		questItemIds = questItems;

		addStartNpc(JEREMY);
		addTalkId(JEREMY);
		addTalkId(TABLE);
		addKillId(BUMPALUMP);

		String var = loadGlobalQuestVar("625_respawn");
		long respawn = 0;
		try
		{
			if(!var.isEmpty())
			{
				respawn = Long.parseLong(var);
			}

			if(respawn <= 0)
			{
				addSpawn(31542, 157136, -121456, -2363, 40000, false, 0, true);
			}
			else
			{
				startQuestTimer("spawn_npc", respawn, null, null);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.WARN, "TheFinestIngredientsPart2: Couldn't load RespawnTime.", e);
			addSpawn(31542, 157136, -121456, -2363, 40000, false, 0, true);
		}
	}

	public static void main(String[] args)
	{
		new _00625_TheFinestIngredientsPart2();
	}

	@Override
	public int getQuestId()
	{
		return 625;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("despawn_bumpalump"))
		{
			npc.reduceCurrentHp(9999999, npc, null);
			addSpawn(31542, 157136, -121456, -2363, 40000, false, 0, true);
			autoChat(npc, "The good fragrant flavor...");
			return null;
		}
		if(event.equalsIgnoreCase("spawn_npc"))
		{
			addSpawn(31542, 157136, -121456, -2363, 40000, false, 0, true);
			return null;
		}

		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}

		if(event.equalsIgnoreCase("31521-02.htm"))
		{
			if(st.getPlayer().getLevel() < 73)
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "31521-00b.htm";
			}
			else
			{
				st.startQuest();
				st.takeItems(SAUCE, 1);
				st.giveItems(FOOD, 1);
			}
		}
		else if(event.equalsIgnoreCase("31542-02.htm"))
		{
			if(st.hasQuestItems(FOOD))
			{
				L2Npc spawnId = st.addSpawn(BUMPALUMP, 158240, -121536, -2253);
				st.takeItems(FOOD, 1);
				npc.getLocationController().delete();
				st.setCond(2);
				startQuestTimer("despawn_bumpalump", 1200000, spawnId, null);
				autoChat(spawnId, "not!");
			}
			else
			{
				return "31542-04.htm";
			}
		}
		else if(event.equalsIgnoreCase("31521-04.htm"))
		{
			if(st.hasQuestItems(MEAT))
			{
				st.takeItems(MEAT, 1);
				st.giveItems(REWARDS[st.getRandom(REWARDS.length - 1)], 5);
				st.exitQuest(QuestType.REPEATABLE);
				return "31521-04.htm";
			}
			else
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "31521-05.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if(npcId == BUMPALUMP)
		{
			long respawnMinDelay = 43200000 * (long) Config.RAID_MIN_RESPAWN_MULTIPLIER;
			long respawnMaxDelay = 129600000 * (long) Config.RAID_MAX_RESPAWN_MULTIPLIER;
			long respawn_delay = Rnd.get(respawnMinDelay, respawnMaxDelay);
			saveGlobalQuestVar("625_respawn", String.valueOf(System.currentTimeMillis() + respawn_delay));
			startQuestTimer("spawn_npc", respawn_delay, null, null);
			cancelQuestTimer("despawn_bumpalump", npc, null);
			L2Party party = player.getParty();
			if(party != null)
			{
				List<QuestState> pT = new ArrayList<>();
				for(L2PcInstance ptMember : party.getMembersInRadius(player, 900))
				{
					QuestState pSt = ptMember.getQuestState(getClass());
					if(pSt != null)
					{
						if(pSt.getState() == STARTED && (pSt.getCond() == 1 || pSt.getCond() == 2))
						{
							pT.add(pSt);
						}
					}
				}
				if(pT.isEmpty())
				{
					return null;
				}
				else
				{
					QuestState st = pT.get(Rnd.get(pT.size() - 1));
					st.takeItems(FOOD, 1);
					st.giveItems(MEAT, 1);
					st.setCond(3);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
			else
			{
				QuestState st = player.getQuestState(getClass());
				if(st == null)
				{
					return null;
				}
				if(st.isStarted() && (st.getCond() == 1 || st.getCond() == 2))
				{
					if(st.hasQuestItems(FOOD))
					{
						st.takeItems(FOOD, 1);
					}
					st.giveItems(MEAT, 1);
					st.setCond(3);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
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

		switch(cond)
		{
			case 0:
				if(npcId == JEREMY)
				{
					return st.getQuestItemsCount(SAUCE) >= 1 ? "31521-01.htm" : "31521-00a.htm";
				}
				break;
			case 1:
				if(npcId == JEREMY)
				{
					return "31521-02a.htm";
				}
				if(npcId == TABLE)
				{
					return "31542-01.htm";
				}
				break;
			case 2:
				if(npcId == JEREMY)
				{
					return "31521-03a.htm";
				}
				if(npcId == TABLE)
				{
					return "31542-01.htm";
				}
				break;
			case 3:
				if(npcId == JEREMY)
				{
					return "31521-03.htm";
				}
				if(npcId == TABLE)
				{
					return "31542-05.htm";
				}
				break;
		}
		return null;
	}

	private void autoChat(L2Npc npc, String text)
	{
		npc.getKnownList().getKnownPlayers().values().stream().filter(player -> player != null).forEach(player -> player.sendPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), text)));
	}
}