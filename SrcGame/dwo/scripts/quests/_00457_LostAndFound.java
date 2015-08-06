package dwo.scripts.quests;

import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.lang3.ArrayUtils;

public class _00457_LostAndFound extends Quest
{
	private static final int GUMIEL = 32759;
	private static final int BOX = 15716;
	private static final int SPAWN_CHANCE = 10; // per 1k
	private static final int[] SPAWNERS = {22789, 22790, 22791, 22793};
	private static TIntIntHashMap players = new TIntIntHashMap();

	public _00457_LostAndFound()
	{

		addStartNpc(GUMIEL);
		addTalkId(GUMIEL);
		addKillId(SPAWNERS);
	}

	public static void main(String[] args)
	{
		new _00457_LostAndFound();
	}

	private QuestState getStateFromNpc(L2Npc npc)
	{
		if(players != null && players.containsKey(npc.getObjectId()))
		{
			int playerId = players.get(npc.getObjectId());
			L2PcInstance player = WorldManager.getInstance().getPlayer(playerId);
			if(player != null && player.isOnline())
			{
				return player.getQuestState(getClass());
			}
		}
		return null;
	}

	private void deleteNpc(L2Npc npc)
	{
		// Remove from list
		players.remove(npc.getObjectId());
		// Despawn NPC
		npc.getLocationController().delete();
	}

	@Override
	public int getQuestId()
	{
		return 457;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("checker"))
		{
			QuestState st = getStateFromNpc(npc);
			if(st != null)
			{
				L2PcInstance pl = st.getPlayer();
				if(pl != null && pl.isOnline())
				{
					if(npc.isInsideRadius(122985, -74841, 500, false) || npc.isInsideRadius(108457, -87835, 500, false))
					{
						pl.sendPacket(new NS(npc.getObjectId(), ChatType.TELL, npc.getNpcId(), NpcStringId.AH_FRESH_AIR));
						npc.getAI().stopFollow();
						npc.setIsRunning(false);
						startQuestTimer("success", 100, npc, pl);
						return null;
					}

					int dist = (int) Math.sqrt(npc.getDistanceSq(pl));

					if(dist > 600)
					{
						pl.sendPacket(new NS(npc.getObjectId(), ChatType.TELL, npc.getNpcId(), NpcStringId.HUFF_HUFF_YOURE_TOO_FAST_I_CANT_FOLLOW_ANYMORE));
						startQuestTimer("fail", 100, npc, pl);
						return null;
					}
					if(dist > 300)
					{
						npc.setIsRunning(true);
						int rnd = Rnd.get(5);
						if(rnd == 0)
						{
							pl.sendPacket(new NS(npc.getObjectId(), ChatType.TELL, npc.getNpcId(), NpcStringId.HEY_DONT_GO_SO_FAST));
						}
						else if(rnd == 1)
						{
							pl.sendPacket(new NS(npc.getObjectId(), ChatType.TELL, npc.getNpcId(), NpcStringId.ITS_HARD_TO_FOLLOW));
						}
					}
					else
					{
						npc.setIsRunning(false);
						int rnd = Rnd.get(30);
						if(rnd == 0)
						{
							pl.sendPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.AH_I_THINK_I_REMEMBER_THIS_PLACE));
						}
						else if(rnd == 1)
						{
							pl.sendPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.WHAT_WERE_YOU_DOING_HERE));
						}
						else if(rnd == 2)
						{
							pl.sendPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.I_GUESS_YOURE_THE_SILENT_TYPE_THEN_ARE_YOU_LOOKING_FOR_TREASURE_LIKE_ME));
						}
					}
					startQuestTimer("checker", 1000, npc, player);
				}
				else
				{
					// Player is null or he isnt online
					deleteNpc(npc);
				}
			}
			else
			{
				// State is null so smth goes wrong unspawn Npc
				deleteNpc(npc);
			}
			return null;
		}
		if(event.equalsIgnoreCase("fail"))
		{
			QuestState st = getStateFromNpc(npc);
			if(st != null)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.unset("cond");
				st.unset("escape");
				st.exitQuest(QuestType.REPEATABLE);
			}
			deleteNpc(npc);
			cancelQuestTimer("timeout", npc, player);
			return null;
		}
		if(event.equalsIgnoreCase("timeout"))
		{
			QuestState st = getStateFromNpc(npc);
			if(st != null)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.unset("cond");
				st.unset("escape");
				st.exitQuest(QuestType.REPEATABLE);
			}
			deleteNpc(npc);
			return null;
		}
		if(event.equalsIgnoreCase("success"))
		{
			QuestState st = getStateFromNpc(npc);
			if(st != null)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			cancelQuestTimer("timeout", npc, player);
			return null;
		}
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}

		if(npc.getNpcId() == GUMIEL)
		{
			if(event.equalsIgnoreCase("lost_villager_q0457_04.htm"))
			{
				if(players != null)
				{
					int playerId = players.get(npc.getObjectId());
					L2PcInstance escort = WorldManager.getInstance().getPlayer(playerId);
					if(escort == null)
					{
						players.remove(npc.getObjectId());
						st.startQuest();
						st.unset("escape"); // Player can have it from previous quest
						players.put(npc.getObjectId(), player.getObjectId());
					}
					else
					{
						return "lost_villager_q0457_01a.htm";
					}
				}
			}
			else if(event.equalsIgnoreCase("lost_villager_q0457_06.htm"))
			{
				st.setCond(1);
				st.set("escape", "1");
				npc.getAI().stopFollow();
				npc.setIsRunning(false);
				npc.getAI().startFollow(player);
				startQuestTimer("checker", 2000, npc, player);
				startQuestTimer("timeout", 600000, npc, player);
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(ArrayUtils.contains(SPAWNERS, npc.getNpcId()) && Rnd.get(1000) < SPAWN_CHANCE)
		{
			L2Npc gumiel = addSpawn(GUMIEL, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 1800000, false);
			if(gumiel != null)
			{
				players.put(gumiel.getObjectId(), 0);
			}
		}

		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npc.getNpcId() == GUMIEL)
		{
			if(st.isCompleted())
			{
				return "lost_villager_q0457_02.htm";
			}
			else if(player.getLevel() < 82)
			{
				return "lost_villager_q0457_03.htm";
			}
			else if(st.isStarted())
			{
				if(st.getCond() == 1)
				{
					return st.getInt("escape") == 1 ? "lost_villager_q0457_08.htm" : "lost_villager_q0457_05.htm";
				}
				else if(st.getCond() == 2)
				{
					st.giveItems(BOX, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.unset("cond");
					st.unset("escape");
					st.exitQuest(QuestType.DAILY);
					deleteNpc(npc);
					return "lost_villager_q0457_09.htm";
				}
			}
			else if(st.isCreated())
			{
				if(players != null)
				{
					int playerId = players.get(npc.getObjectId());
					L2PcInstance escort = WorldManager.getInstance().getPlayer(playerId);
					return escort != null ? "lost_villager_q0457_01a.htm" : "lost_villager_q0457_01.htm";
				}
			}
		}
		return null;
	}
}