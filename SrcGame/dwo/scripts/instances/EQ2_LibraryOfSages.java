package dwo.scripts.instances;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastList;

public class EQ2_LibraryOfSages extends Quest
{
	private static final String qn = "EQ2_LibraryOfSages";

	// NPC's
	private static final int Sophia = 32596;
	private static final int Sophia2 = 32861;
	private static final int Sophia3 = 32863;
	private static final int Elcadia_Support = 32785;

	// Teleports
	private static final Location ENTRY_POINT = new Location(37063, -49813, -1128);
	private static final Location EXIT_POINT = new Location(37063, -49813, -1128);
	private static final Location ROOM_POINT = new Location(37355, -50065, -1127);

	private static final NpcStringId[] spam = {
		NpcStringId.I_MUST_ASK_LIBRARIAN_SOPHIA_ABOUT_THE_BOOK,
		NpcStringId.THIS_LIBRARY_ITS_HUGE_BUT_THERE_ARENT_MANY_USEFUL_BOOKS_RIGHT,
		NpcStringId.AN_UNDERGROUND_LIBRARY_I_HATE_DAMP_AND_SMELLY_PLACES,
		NpcStringId.THE_BOOK_THAT_WE_SEEK_IS_CERTAINLY_HERE_SEARCH_INCH_BY_INCH
	};
	private final TIntObjectHashMap<InstanceHolder> instanceWorlds = new TIntObjectHashMap<>();

	public EQ2_LibraryOfSages()
	{

		addStartNpc(Sophia);
		addStartNpc(Sophia2);
		addTalkId(Sophia);
		addTalkId(Sophia2);
		addTalkId(Sophia3);
		addTalkId(Elcadia_Support);
	}

	public static void main(String[] args)
	{
		new EQ2_LibraryOfSages();
	}

	protected void enterInstance(L2Npc npc, L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if(world != null)
		{
			if(!(world instanceof LibraryOfSagesWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if(inst != null)
			{
				player.teleToInstance(ENTRY_POINT, world.instanceId);
				player.stopAllEffectsExceptThoseThatLastThroughDeath();
				cancelQuestTimer("check_follow", npc, player);
				InstanceHolder holder = instanceWorlds.get(world.instanceId);
				if(holder != null)
				{
					for(L2Npc h : holder.mobs)
					{
						h.getLocationController().delete();
					}
					holder.mobs.clear();

					if(world.instanceId > 0)
					{
						L2Npc support = addSpawn(Elcadia_Support, player.getX(), player.getY(), player.getZ(), 0, false, 0, false, player.getInstanceId());
						holder.mobs.add(support);
						startQuestTimer("check_follow", 3000, support, player);
					}
				}
			}
		}
		else
		{
			int instanceId = InstanceManager.getInstance().createDynamicInstance("EQ2_LibraryOfSages.xml");

			world = new LibraryOfSagesWorld();
			world.instanceId = instanceId;
			world.templateId = InstanceZoneId.LIBRARY_OF_SAGES.getId();
			world.status = 0;
			InstanceManager.getInstance().addWorld(world);

			world.allowed.add(player.getObjectId());

			player.teleToInstance(ENTRY_POINT, instanceId);

			player.stopAllEffectsExceptThoseThatLastThroughDeath();
			cancelQuestTimer("check_follow", npc, player);
			InstanceHolder holder = instanceWorlds.get(world.instanceId);
			if(holder != null)
			{
				for(L2Npc h : holder.mobs)
				{
					h.getLocationController().delete();
				}
				holder.mobs.clear();

				if(world.instanceId > 0)
				{
					L2Npc support = addSpawn(Elcadia_Support, player.getX(), player.getY(), player.getZ(), 0, false, 0, false, player.getInstanceId());
					holder.mobs.add(support);
					startQuestTimer("check_follow", 3000, support, player);
				}
			}
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if(st == null)
		{
			st = newQuestState(player);
		}

		if(event.equalsIgnoreCase("check_follow"))
		{
			npc.getAI().stopFollow();
			npc.setIsRunning(true);
			npc.getAI().startFollow(player);
			npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), spam[Rnd.get(0, spam.length - 1)]));
			startQuestTimer("check_follow", 20000, npc, player);
			return "";
		}
		if(npc.getNpcId() == Sophia)
		{
			if(event.equalsIgnoreCase("tele1"))
			{
				enterInstance(npc, player);
				return null;
			}
		}
		else if(npc.getNpcId() == Sophia2)
		{
			if(event.equalsIgnoreCase("tele2"))
			{
				player.teleToInstance(ROOM_POINT, player.getInstanceId());
				return null;
			}
			else if(event.equalsIgnoreCase("tele3"))
			{
				InstanceHolder holder = instanceWorlds.get(player.getInstanceId());
				if(holder != null)
				{
					for(L2Npc h : holder.mobs)
					{
						h.getLocationController().delete();
					}
					holder.mobs.clear();
				}
				player.teleToInstance(EXIT_POINT, 0);
				return null;
			}
		}
		else if(npc.getNpcId() == Sophia3)
		{
			if(event.equalsIgnoreCase("tele4"))
			{
				player.teleToInstance(ENTRY_POINT, player.getInstanceId());
				return null;
			}
		}
		return htmltext;
	}

	private static class InstanceHolder
	{
		FastList<L2Npc> mobs = new FastList<>();
	}

	private class LibraryOfSagesWorld extends InstanceWorld
	{
		public LibraryOfSagesWorld()
		{
		}
	}
}
