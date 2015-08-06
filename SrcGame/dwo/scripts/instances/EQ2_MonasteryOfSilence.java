package dwo.scripts.instances;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import dwo.gameserver.util.Rnd;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastList;

public class EQ2_MonasteryOfSilence extends Quest
{
	private static final String qn = "EQ2_MonasteryOfSilence";

	// NPC's
	private static final int Odd_Globe = 32815;
	private static final int Elcadia_Support = 32787;
	private static final int ErisEvilThoughts = 32792;
	private static final int RelicGuardian = 32803;
	private static final int TeleportControlDevice = 32817;
	private static final int TeleportControlDevice1 = 32818;
	private static final int TeleportControlDevice2 = 32819;
	private static final int TeleportControlDevice3 = 32820;
	private static final int RelicWatcher = 32804;
	private static final int RelicWatcher1 = 32805;
	private static final int RelicWatcher2 = 32806;
	private static final int RelicWatcher3 = 32807;

	// Teleports
	private static final Location ENTRY_POINT = new Location(120710, -86971, -3392);
	private static final Location EXIT_POINT = new Location(115599, -81415, -3400);
	private static final Location CENTRAL_POINT = new Location(85794, -249788, -8320);
	private static final Location SOUTH_POINT = new Location(85798, -246566, -8320);
	private static final Location WEST_POINT = new Location(82531, -249405, -8320);
	private static final Location EAST_POINT = new Location(88665, -249784, -8320);
	private static final Location NORTH_POINT = new Location(85792, -252336, -8320);
	private static final Location BACK_POINT = new Location(120710, -86971, -3392);

	private static final NpcStringId[] spam = {
		NpcStringId.IT_SEEMS_THAT_YOU_CANNOT_REMEMBER_TO_THE_ROOM_OF_THE_WATCHER_WHO_FOUND_THE_BOOK,
		NpcStringId.WE_MUST_SEARCH_HIGH_AND_LOW_IN_EVERY_ROOM_FOR_THE_READING_DESK_THAT_CONTAINS_THE_BOOK_WE_SEEK,
		NpcStringId.REMEMBER_THE_CONTENT_OF_THE_BOOKS_THAT_YOU_FOUND_YOU_CANT_TAKE_THEM_OUT_WITH_YOU
	};
	private static final int[] skill = {6725, 6728, 6730};
	private final TIntObjectHashMap<InstanceHolder> instanceWorlds = new TIntObjectHashMap<>();

	public EQ2_MonasteryOfSilence()
	{

		addStartNpc(Odd_Globe);
		addTalkId(Odd_Globe);
		addStartNpc(RelicGuardian);
		addTalkId(RelicGuardian);
		addStartNpc(ErisEvilThoughts);
		addTalkId(ErisEvilThoughts);
		addStartNpc(TeleportControlDevice);
		addTalkId(TeleportControlDevice);
		addStartNpc(TeleportControlDevice1);
		addTalkId(TeleportControlDevice1);
		addStartNpc(TeleportControlDevice2);
		addTalkId(TeleportControlDevice2);
		addStartNpc(TeleportControlDevice3);
		addTalkId(TeleportControlDevice3);
		addTalkId(Elcadia_Support);
		addStartNpc(RelicWatcher);
		addTalkId(RelicWatcher);
		addStartNpc(RelicWatcher1);
		addTalkId(RelicWatcher1);
		addStartNpc(RelicWatcher2);
		addTalkId(RelicWatcher2);
		addStartNpc(RelicWatcher3);
		addTalkId(RelicWatcher3);
	}

	public static void main(String[] args)
	{
		new EQ2_MonasteryOfSilence();
	}

	private void checkFollower(L2Npc npc, L2PcInstance player)
	{
		InstanceHolder holder = instanceWorlds.get(player.getInstanceId());
		if(holder == null && player.getInstanceId() > 0)
		{
			holder = new InstanceHolder();
			instanceWorlds.put(player.getInstanceId(), holder);
		}
		player.stopAllEffectsExceptThoseThatLastThroughDeath();
		cancelQuestTimer("check_follow", npc, player);
		if(holder != null)
		{
			for(L2Npc h : holder.mobs)
			{
				h.getLocationController().delete();
			}
			holder.mobs.clear();

			if(player.getInstanceId() > 0)
			{
				L2Npc support = addSpawn(Elcadia_Support, player.getX(), player.getY(), player.getZ(), 0, false, 0, false, player.getInstanceId());
				holder.mobs.add(support);
				startQuestTimer("check_follow", 3000, support, player);
			}
		}
	}

	protected void enterInstance(L2Npc npc, L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if(world != null)
		{
			if(!(world instanceof MonasteryOfSilenceWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if(inst != null)
			{
				player.teleToInstance(ENTRY_POINT, world.instanceId);
				checkFollower(npc, player);
			}
		}
		else
		{
			int instanceId = InstanceManager.getInstance().createDynamicInstance("EQ2_MonasteryOfSilence.xml");

			world = new MonasteryOfSilenceWorld();
			world.instanceId = instanceId;
			world.templateId = InstanceZoneId.MONASTERY_OF_SILENCE_1.getId();
			world.status = 0;
			InstanceManager.getInstance().addWorld(world);

			world.allowed.add(player.getObjectId());

			player.teleToInstance(ENTRY_POINT, instanceId);
			checkFollower(npc, player);
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);

		if(event.equalsIgnoreCase("check_follow"))
		{
			npc.getAI().stopFollow();
			npc.setIsRunning(true);
			npc.getAI().startFollow(player);
			if(player.isInCombat())
			{
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.YOUR_WORK_HERE_IS_DONE_SO_RETURN_TO_THE_CENTRAL_GUARDIAN));
				L2Skill skilluse = SkillTable.getInstance().getInfo(skill[Rnd.get(0, skill.length - 1)], 1);
				npc.setTarget(player);
				npc.doCast(skilluse);
			}
			else
			{
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), spam[Rnd.get(0, spam.length - 1)]));
			}
			startQuestTimer("check_follow", 20000, npc, player);
			return null;
		}
		if(event.equalsIgnoreCase("start_movie"))
		{
			player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ2_HOLY_BURIAL_GROUND_OPENING);
			return null;
		}
		if(npc.getNpcId() == Odd_Globe)
		{
			if(event.equalsIgnoreCase("enter"))
			{
				enterInstance(npc, player);
				return null;
			}
		}
		else if(npc.getNpcId() == ErisEvilThoughts)
		{
			if(event.equalsIgnoreCase("tele2"))
			{
				player.teleToInstance(CENTRAL_POINT, player.getInstanceId());
				checkFollower(npc, player);
				startQuestTimer("start_movie", 2000, npc, player);
				return null;
			}
			else if(event.equalsIgnoreCase("exit"))
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
		else if(npc.getNpcId() == RelicGuardian)
		{
			if(event.equalsIgnoreCase("back"))
			{
				player.teleToInstance(BACK_POINT, player.getInstanceId());
				checkFollower(npc, player);
				return null;
			}
		}
		else if(npc.getNpcId() == TeleportControlDevice)
		{
			if(event.equalsIgnoreCase("east"))
			{
				player.teleToInstance(EAST_POINT, player.getInstanceId());
				checkFollower(npc, player);
				return null;
			}
		}
		else if(npc.getNpcId() == TeleportControlDevice1)
		{
			if(event.equalsIgnoreCase("west"))
			{
				player.teleToInstance(WEST_POINT, player.getInstanceId());
				checkFollower(npc, player);
				return null;
			}
		}
		else if(npc.getNpcId() == TeleportControlDevice2)
		{
			if(event.equalsIgnoreCase("north"))
			{
				player.teleToInstance(NORTH_POINT, player.getInstanceId());
				checkFollower(npc, player);
				return null;
			}
		}
		else if(npc.getNpcId() == TeleportControlDevice3)
		{
			if(event.equalsIgnoreCase("south"))
			{
				player.teleToInstance(SOUTH_POINT, player.getInstanceId());
				checkFollower(npc, player);
				return null;
			}
		}
		else if(npc.getNpcId() == RelicWatcher)
		{
			if(event.equalsIgnoreCase("tocenter"))
			{
				player.teleToInstance(CENTRAL_POINT, player.getInstanceId());
				checkFollower(npc, player);
				return null;
			}
		}
		else if(npc.getNpcId() == RelicWatcher1)
		{
			if(event.equalsIgnoreCase("tocenter1"))
			{
				player.teleToInstance(CENTRAL_POINT, player.getInstanceId());
				checkFollower(npc, player);
				return null;
			}
		}
		else if(npc.getNpcId() == RelicWatcher2)
		{
			if(event.equalsIgnoreCase("tocenter2"))
			{
				player.teleToInstance(CENTRAL_POINT, player.getInstanceId());
				checkFollower(npc, player);
				return null;
			}
		}
		else if(npc.getNpcId() == RelicWatcher3)
		{
			if(event.equalsIgnoreCase("tocenter3"))
			{
				player.teleToInstance(CENTRAL_POINT, player.getInstanceId());
				checkFollower(npc, player);
				return null;
			}
		}
		return htmltext;
	}

	private static class InstanceHolder
	{
		FastList<L2Npc> mobs = new FastList<>();
	}

	private class MonasteryOfSilenceWorld extends InstanceWorld
	{
		public MonasteryOfSilenceWorld()
		{
		}
	}
}
