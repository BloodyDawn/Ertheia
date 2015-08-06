package dwo.scripts.instances.ChamberOfDelusion;

import dwo.config.Config;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.EarthQuake;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.util.List;

public class South extends Quest
{
	private static final String qn = "South";
	//NPCs
	private static final int ENTRANCE_GATEKEEPER = 32660;
	private static final int ROOM_GATEKEEPER_FIRST = 32674;
	private static final int ROOM_GATEKEEPER_LAST = 32678;
	private static final int AENKINEL = 25692;
	private static final int BOX = 18838;
	//Items
	private static final int ENRIA = 4042;
	private static final int ASOFE = 4043;
	private static final int THONS = 4044;
	private static final int LEONARD = 9628;
	private static final int DELUSION_MARK = 15311;
	//Skills
	private static final SkillHolder SUCCESS_SKILL = new SkillHolder(5758, 1);
	private static final SkillHolder FAIL_SKILL = new SkillHolder(5376, 4);
	private static final int[][] ROOM_ENTER_POINTS = {
		{-122368, -207820, -6720}, {-122368, -206940, -6720}, {-122368, -209116, -6720}, {-121456, -207356, -6720},
		{-121440, -209004, -6720} //Raid room
	};
	private static final int[] AENKINEL_SPAWN = {-121292, -208690, -6720, 0};
	private static final int[] RETURN_POINT = {-114592, -152509, -6723};
	private static final long ROOM_CHANGE_INTERVAL = 480000; //8 min
	private static final int ROOM_CHANGE_RANDOM_TIME = 120; //2 min
	//Managers spawn coordinates (npcId, x, y, z, heading)
	private int[][] MANAGER_SPAWN_POINTS = {
		{32674, -122368, -207920, -6720, 0}, {32675, -122368, -207040, -6720, 0}, {32676, -122368, -209216, -6720, 0},
		{32677, -121456, -207456, -6720, 0}, {32678, -121440, -209104, -6720, 0}

	};
	private int[][] BOX_SPAWN_POINTS = {
		{-121292, -208690, -6720, 0}, {-121605, -208850, -6720, 0}, {-121622, -209341, -6720, 0},
		{-121200, -209348, -6720, 0}
	};

	public South()
	{

		addStartNpc(ENTRANCE_GATEKEEPER);
		addTalkId(ENTRANCE_GATEKEEPER);
		for(int i = ROOM_GATEKEEPER_FIRST; i <= ROOM_GATEKEEPER_LAST; i++)
		{
			addStartNpc(i);
			addTalkId(i);
		}
		addKillId(AENKINEL);
		addAttackId(BOX);
		addSpellFinishedId(BOX);
	}

	public static void main(String[] args)
	{
		new South();
	}

	private boolean checkConditions(L2PcInstance player)
	{
		L2Party party = player.getParty();
		if(party == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}
		if(!party.getLeader().equals(player))
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}
		for(L2PcInstance partyMember : party.getMembers())
		{
			if(partyMember.getLevel() < 80)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
			if(!Util.checkIfInRange(1000, player, partyMember, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
		}

		return true;
	}

	private void teleportplayer(L2PcInstance player, TeleCoord teleto)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.getInstanceController().setInstanceId(teleto.instanceId);
		player.teleToLocation(teleto.x, teleto.y, teleto.z);
	}

	private void banishStrangers(CDWorld world)
	{
		L2Party party = world.getPartyInside();

		L2Npc manager = world.managers[world.currentRoom - 1];
		TeleCoord tele = new TeleCoord();

		tele.x = RETURN_POINT[0];
		tele.y = RETURN_POINT[1];
		tele.z = RETURN_POINT[2];

		manager.getKnownList().getKnownPlayersInRadius(1000).stream().filter(player -> party == null || player.getParty() == null || !player.getParty().equals(world.getPartyInside())).forEach(player -> {
			world.allowed.remove(world.allowed.indexOf(player.getObjectId()));
			exitInstance(player);
		});

		Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);

		//Schedule next banish task only if remaining time is enough
		if(inst.getInstanceEndTime() - System.currentTimeMillis() > 60000)
		{
			startQuestTimer("banish_strangers", 60000, world.managers[world.currentRoom - 1], null);
		}
	}

	private void changeRoom(CDWorld world)
	{
		L2Party party = world.getPartyInside();

		if(party == null)
		{
			return;
		}

		int newRoom = world.currentRoom;

		if(world.currentRoom != ROOM_ENTER_POINTS.length && Rnd.get(100) < 10) //10% chance for teleport to raid room if not here already
		{
			newRoom = ROOM_ENTER_POINTS.length;
		}
		else
		{
			while(newRoom == world.currentRoom) //otherwise teleport to another room, except current
			{
				newRoom = Rnd.get(ROOM_ENTER_POINTS.length - 1) + 1;
			}
		}

		cancelQuestTimer("banish_strangers", world.managers[world.currentRoom - 1], null);
		banishStrangers(world);

		for(L2PcInstance partyMember : party.getMembers())
		{
			if(world.instanceId == partyMember.getInstanceId())
			{
				partyMember.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				partyMember.teleToLocation(ROOM_ENTER_POINTS[newRoom - 1][0] - 50 + Rnd.get(100), ROOM_ENTER_POINTS[newRoom - 1][1] - 50 + Rnd.get(100), ROOM_ENTER_POINTS[newRoom - 1][2]);
			}
		}

		Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
		long nextInterval = ROOM_CHANGE_INTERVAL + Rnd.get(ROOM_CHANGE_RANDOM_TIME) * 1000;

		//Schedule next room change only if remaining time is enough and here is no raid room
		if(inst.getInstanceEndTime() - System.currentTimeMillis() > nextInterval && newRoom != ROOM_ENTER_POINTS.length)
		{
			startQuestTimer("prepare_change_room", nextInterval, world.managers[newRoom - 1], null);
		}

		if(inst.getInstanceEndTime() - System.currentTimeMillis() > 60000)
		{
			startQuestTimer("banish_strangers", 60000, world.managers[newRoom - 1], null);
		}

		world.currentRoom = newRoom;
	}

	private void enter(CDWorld world)
	{
		L2Party party = world.getPartyInside();

		if(party == null)
		{
			return;
		}

		int newRoom = Rnd.get(ROOM_ENTER_POINTS.length - 1) + 1;

		for(L2PcInstance partyMember : party.getMembers())
		{
			QuestState st = partyMember.getQuestState(qn);
			if(st == null)
			{
				st = newQuestState(partyMember);
			}

			if(st.hasQuestItems(DELUSION_MARK))
			{
				st.takeItems(DELUSION_MARK, -1);
			}

			if(partyMember.getObjectId() == party.getLeaderObjectId())
			{
				st.giveItems(DELUSION_MARK, 1);
			}

			//Save location for teleport back into main hall
			st.set("return_point", Integer.toString(partyMember.getX()) + ';' + Integer.toString(partyMember.getY()) + ';' + Integer.toString(partyMember.getZ()));

			partyMember.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			partyMember.getInstanceController().setInstanceId(world.instanceId);
			world.allowed.add(partyMember.getObjectId());
			partyMember.teleToLocation(ROOM_ENTER_POINTS[newRoom - 1][0] - 50 + Rnd.get(100), ROOM_ENTER_POINTS[newRoom - 1][1] - 50 + Rnd.get(100), ROOM_ENTER_POINTS[newRoom - 1][2]);
		}

		startQuestTimer("prepare_change_room", ROOM_CHANGE_INTERVAL + Rnd.get(ROOM_CHANGE_RANDOM_TIME - 10) * 1000, world.managers[newRoom - 1], null); //schedule room change
		startQuestTimer("banish_strangers", 60000, world.managers[newRoom - 1], null); //schedule checkup for player without party or another party

		world.currentRoom = newRoom;
	}

	private void earthQuake(CDWorld world)
	{
		L2Party party = world.getPartyInside();

		if(party == null)
		{
			return;
		}

		party.getMembers().stream().filter(partyMember -> world.instanceId == partyMember.getInstanceId()).forEach(partyMember -> partyMember.sendPacket(new EarthQuake(partyMember.getX(), partyMember.getY(), partyMember.getZ(), 20, 10)));

		startQuestTimer("change_room", 5000, world.managers[world.currentRoom - 1], null);
	}

	protected void spawnState(CDWorld world)
	{
		addSpawn(AENKINEL, AENKINEL_SPAWN[0], AENKINEL_SPAWN[1], AENKINEL_SPAWN[2], AENKINEL_SPAWN[3], false, 0, false, world.instanceId);

		for(int i = 0; i < MANAGER_SPAWN_POINTS.length; i++)
		{
			world.managers[i] = addSpawn(MANAGER_SPAWN_POINTS[i][0], MANAGER_SPAWN_POINTS[i][1], MANAGER_SPAWN_POINTS[i][2], MANAGER_SPAWN_POINTS[i][3], MANAGER_SPAWN_POINTS[i][4], false, 0, false, world.instanceId);
		}
	}

	protected int enterInstance(L2PcInstance player, String template)
	{
		int instanceId = 0;
		//check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		//existing instance
		if(world != null)
		{
			if(!(world instanceof CDWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			CDWorld currentWorld = (CDWorld) world;
			TeleCoord tele = new TeleCoord();
			tele.x = ROOM_ENTER_POINTS[currentWorld.currentRoom - 1][0];
			tele.y = ROOM_ENTER_POINTS[currentWorld.currentRoom - 1][1];
			tele.z = ROOM_ENTER_POINTS[currentWorld.currentRoom - 1][2];
			tele.instanceId = world.instanceId;
			teleportplayer(player, tele);
			return instanceId;
		}
		//New instance
		else
		{
			if(!checkConditions(player))
			{
				return 0;
			}
			L2Party party = player.getParty();
			instanceId = InstanceManager.getInstance().createDynamicInstance(template);
			world = new CDWorld(party);
			world.instanceId = instanceId;
			world.templateId = InstanceZoneId.DELUSION_CHAMBER_SOUTH_SEAL.getId();
			world.status = 0;
			InstanceManager.getInstance().addWorld(world);
			_log.log(Level.INFO, "Chamber Of Delusion started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
			spawnState((CDWorld) world);
			enter((CDWorld) world);
			return instanceId;
		}
	}

	private void exitInstance(L2PcInstance player)
	{
		int x = RETURN_POINT[0];
		int y = RETURN_POINT[1];
		int z = RETURN_POINT[2];

		QuestState st = player.getQuestState(qn);

		if(st != null)
		{
			String return_point = st.get("return_point");
			if(return_point != null)
			{
				String[] coords = return_point.split(";");
				if(coords.length == 3)
				{
					try
					{
						x = Integer.parseInt(coords[0]);
						y = Integer.parseInt(coords[1]);
						z = Integer.parseInt(coords[2]);
					}
					catch(Exception e)
					{
						x = RETURN_POINT[0];
						y = RETURN_POINT[1];
						z = RETURN_POINT[2];
					}
				}
			}
		}

		player.getInstanceController().setInstanceId(0);
		player.teleToLocation(x, y, z);
		if(!player.getPets().isEmpty())
		{
			for(L2Summon pet : player.getPets())
			{
				pet.getInstanceController().setInstanceId(0);
				pet.teleToLocation(x, y, z);
			}
		}
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(attacker);
		if(tmpworld instanceof CDWorld)
		{
			CDWorld world = (CDWorld) tmpworld;

			if(npc.getNpcId() == BOX && !world.rewardedBoxes.contains(npc.getObjectId()) && npc.getCurrentHp() < npc.getMaxHp() / 10)
			{
				L2MonsterInstance box = (L2MonsterInstance) npc;
				ItemHolder item;
				if(Rnd.get(100) < 25) //25% chance to reward
				{
					world.rewardedBoxes.add(box.getObjectId());
					if(Rnd.get(100) < 33)
					{
						item = new ItemHolder(ENRIA, (int) (3 * Config.RATE_DROP_ITEMS));
						box.dropItem(attacker, item);
					}
					if(Rnd.get(100) < 50)
					{
						item = new ItemHolder(THONS, (int) (4 * Config.RATE_DROP_ITEMS));
						box.dropItem(attacker, item);
					}
					if(Rnd.get(100) < 50)
					{
						item = new ItemHolder(ASOFE, (int) (4 * Config.RATE_DROP_ITEMS));
						box.dropItem(attacker, item);
					}
					if(Rnd.get(100) < 16)
					{
						item = new ItemHolder(LEONARD, (int) (2 * Config.RATE_DROP_ITEMS));
						box.dropItem(attacker, item);
					}

					box.doCast(SUCCESS_SKILL.getSkill());
				}
				else
				{
					box.doCast(FAIL_SKILL.getSkill());
				}
			}
		}

		return super.onAttack(npc, attacker, damage, isPet, skill);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());

		if(tmpworld != null && tmpworld instanceof CDWorld && npc.getNpcId() >= ROOM_GATEKEEPER_FIRST && npc.getNpcId() <= ROOM_GATEKEEPER_LAST)
		{
			CDWorld world = (CDWorld) tmpworld;

			switch(event)
			{
				case "prepare_change_room":
					earthQuake(world);
					break;
				case "change_room":
					changeRoom(world);
					break;
				case "banish_strangers":
					banishStrangers(world);
					break;
			}

			//Timers part ends here, further player cannot be null
			if(player == null)
			{
				return null;
			}

			QuestState st = player.getQuestState(qn);

			if(st == null)
			{
				st = newQuestState(player);
			}//Change room from dialog
			else if(event.equals("next_room"))
			{
				if(player.getParty() == null)
				{
					htmltext = HtmCache.getInstance().getHtmQuest(player.getLang(), "instances/ChambersOfDelusion/no_party.htm");
				}
				else if(player.getParty().getLeaderObjectId() != player.getObjectId())
				{
					htmltext = HtmCache.getInstance().getHtmQuest(player.getLang(), "instances/ChambersOfDelusion/no_leader.htm");
				}
				else if(st.hasQuestItems(DELUSION_MARK))
				{
					st.takeItems(DELUSION_MARK, 1);
					cancelQuestTimer("prepare_change_room", npc, null);
					cancelQuestTimer("change_room", npc, null);
					changeRoom(world);
				}

				else
				{
					htmltext = HtmCache.getInstance().getHtmQuest(player.getLang(), "instances/ChambersOfDelusion/no_item.htm");
				}
			}

			else if(event.equals("go_out"))
			{
				if(player.getParty() == null)
				{
					htmltext = HtmCache.getInstance().getHtmQuest(player.getLang(), "instances/ChambersOfDelusion/no_party.htm");
				}
				else if(player.getParty().getLeaderObjectId() != player.getObjectId())
				{
					htmltext = HtmCache.getInstance().getHtmQuest(player.getLang(), "instances/ChambersOfDelusion/no_leader.htm");
				}
				else
				{
					Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);

					cancelQuestTimer("prepare_change_room", npc, null);
					cancelQuestTimer("change_room", npc, null);
					cancelQuestTimer("banish_strangers", npc, null);

					for(L2PcInstance partyMember : player.getParty().getMembers())
					{
						exitInstance(partyMember);
						world.allowed.remove(world.allowed.indexOf(partyMember.getObjectId()));
					}

					inst.setEmptyDestroyTime(0);
				}
			}

			else if(event.equals("look_party"))
			{
				if(player.getParty() != null && player.getParty().equals(world.getPartyInside()))
				{
					player.teleToLocation(ROOM_ENTER_POINTS[world.currentRoom - 1][0], ROOM_ENTER_POINTS[world.currentRoom - 1][1], ROOM_ENTER_POINTS[world.currentRoom - 1][2]);
				}
			}
		}

		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if(tmpworld instanceof CDWorld)
		{
			if(npc.getNpcId() == AENKINEL)
			{
				CDWorld world = (CDWorld) tmpworld;
				Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);

				if(inst.getInstanceEndTime() - System.currentTimeMillis() > 60000)
				{
					cancelQuestTimer("prepare_change_room", world.managers[MANAGER_SPAWN_POINTS.length - 1], null);
					cancelQuestTimer("change_room", world.managers[MANAGER_SPAWN_POINTS.length - 1], null);
					startQuestTimer("change_room", 60000, world.managers[MANAGER_SPAWN_POINTS.length - 1], null);
				}

				for(int[] BOX_SPAWN_POINT : BOX_SPAWN_POINTS)
				{
					L2MonsterInstance box = (L2MonsterInstance) addSpawn(BOX, BOX_SPAWN_POINT[0], BOX_SPAWN_POINT[1], BOX_SPAWN_POINT[2], BOX_SPAWN_POINT[3], false, 0, false, world.instanceId);
					box.setIsNoRndWalk(true);
				}
			}
		}

		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();
		QuestState st = player.getQuestState(qn);

		if(st == null)
		{
			st = newQuestState(player);
		}

		if(npcId == ENTRANCE_GATEKEEPER)
		{
			enterInstance(player, "COD_South.xml");
		}

		return "";
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if(npc.getNpcId() == BOX && skill.getId() == 5376 || skill.getId() == 5758 && !npc.isDead())
		{
			npc.doDie(player);
		}

		return super.onSpellFinished(npc, player, skill);
	}

	private class CDWorld extends InstanceWorld
	{
		protected L2Npc[] managers = new L2Npc[5];

		protected int currentRoom;
		protected List<Integer> rewardedBoxes;
		private L2Party partyInside;

		public CDWorld(L2Party party)
		{
			partyInside = party;
			rewardedBoxes = new FastList<>();
		}

		protected L2Party getPartyInside()
		{
			return partyInside;
		}
	}

	private class TeleCoord
	{
		int instanceId;
		int x;
		int y;
		int z;
	}
}