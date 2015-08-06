package dwo.scripts.quests;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.util.Rnd;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.util.List;

public class _00129_PailakaDevilsLegacy extends Quest
{
	private static final int MIN_LEVEL = 63;
	private static final int MAX_LEVEL = 67;
	private static final int EXIT_TIME = 5;
	private static final Location ENTRY_POINT = new Location(76428, -219038, -3752);
	private static final int ZONE = 20109;

	private static final int SURVIVOR = 32498;
	private static final int SUPPORTER = 32501;
	private static final int ADVENTURER1 = 32508;
	private static final int ADVENTURER2 = 32511;
	private static final int[] NPCS = {SURVIVOR, SUPPORTER, ADVENTURER1, ADVENTURER2};

	private static final int KAMS = 18629;
	private static final int HIKORO = 18630;
	private static final int ALKASO = 18631;
	private static final int GERBERA = 18632;
	private static final int LEMATAN = 18633;
	private static final int FOLLOWERS = 18634;
	private static final int TREASURE_BOX = 32495;
	private static final int POWDER_KEG = 18622;
	private static final int[] MONSTERS = {
		KAMS, HIKORO, ALKASO, GERBERA, LEMATAN, FOLLOWERS, TREASURE_BOX, POWDER_KEG, 18623, 18624, 18625, 18626, 18627
	};

	private static final int SWORD = 13042;
	private static final int ENH_SWORD1 = 13043;
	private static final int ENH_SWORD2 = 13044;
	private static final int SCROLL_1 = 13046;
	private static final int SCROLL_2 = 13047;
	private static final int HEALING_POTION = 13033;
	private static final int ANTIDOTE_POTION = 13048;
	private static final int DIVINE_POTION = 13049;
	private static final int DEFENCE_POTION = 13059;
	private static final int PAILAKA_KEY = 13150;
	private static final int[] ITEMS = {
		SWORD, ENH_SWORD1, ENH_SWORD2, SCROLL_1, SCROLL_2, HEALING_POTION, ANTIDOTE_POTION, DIVINE_POTION,
		DEFENCE_POTION, PAILAKA_KEY
	};
	private static final int[][] DROPLIST = {
		// must be sorted by npcId !
		// npcId, itemId, chance, max
		{TREASURE_BOX, HEALING_POTION, 20}, {TREASURE_BOX, DIVINE_POTION, 40}, {TREASURE_BOX, DEFENCE_POTION, 60},
		{TREASURE_BOX, PAILAKA_KEY, 80}, {TREASURE_BOX, ANTIDOTE_POTION, 100}
	};
	private static final int[][] HP_HERBS_DROPLIST = {
		// itemId, count, chance
		{8602, 1, 10}, {8601, 1, 40}, {8600, 1, 70}
	};
	private static final int[][] MP_HERBS_DROPLIST = {
		// itemId, count, chance
		{8605, 1, 10}, {8604, 1, 40}, {8603, 1, 70}
	};
	private static final int[] REWARDS = {13295, 13129};
	private static final int[][] FOLLOWERS_SPAWNS = {
		{85067, -208943, -3336, 20106, 60}, {84904, -208944, -3336, 10904, 60}, {85062, -208538, -3336, 44884, 60},
		{84897, -208542, -3336, 52973, 60}, {84808, -208633, -3339, 65039, 60}, {84808, -208856, -3339, 0, 60},
		{85144, -208855, -3341, 33380, 60}, {85139, -208630, -3339, 31777, 60}
	};
	private static boolean _isTeleportScheduled;
	private static boolean _isOnShip;
	private static L2Npc _lematanNpc;
	private static SkillHolder boom_skill = new SkillHolder(5714, 1);
	private static SkillHolder energy_skill = new SkillHolder(5712, 1);
	private List<L2Npc> _followerslist;

	public _00129_PailakaDevilsLegacy()
	{
		addStartNpc(SURVIVOR);
		addTalkId(SURVIVOR);
		addFirstTalkId(NPCS);
		addTalkId(NPCS);
		addAttackId(TREASURE_BOX, POWDER_KEG, LEMATAN);
		addKillId(MONSTERS);
		addEnterZoneId(ZONE);
		addSpawnId(FOLLOWERS);

		questItemIds = ITEMS;
	}

	private static void dropHerb(L2Npc mob, L2PcInstance player, int[][] drop)
	{
		int chance = Rnd.get(100);
		for(int[] aDrop : drop)
		{
			if(chance < aDrop[2])
			{
				((L2MonsterInstance) mob).dropItem(player, aDrop[0], aDrop[1]);
				return;
			}
		}
	}

	private static void dropItem(L2Npc mob, L2PcInstance player)
	{
		int npcId = mob.getNpcId();
		int chance = Rnd.get(100);
		for(int[] drop : DROPLIST)
		{
			if(npcId == drop[0])
			{
				if(chance < drop[2])
				{
					((L2MonsterInstance) mob).dropItem(player, drop[1], Rnd.get(1, 6));
					return;
				}
			}
			if(npcId < drop[0])
			{
				return; // not found
			}
		}
	}

	public static void main(String[] args)
	{
		new _00129_PailakaDevilsLegacy();
	}

	private void enterInstance(L2PcInstance player)
	{
		synchronized(this)
		{
			//check for existing instances for this player
			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			if(world != null)
			{
				if(player.isInParty())
				{
					player.leaveParty();
				}
				if(world.templateId != InstanceZoneId.PAILAKA_DEVILS_ISLE.getId())
				{
					player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
					return;
				}
				Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
				if(inst != null)
				{
					player.teleToInstance(ENTRY_POINT, world.instanceId);
				}
			}
			//New instance
			else
			{
				if(player.isInParty())
				{
					player.leaveParty();
				}

				int instanceId = InstanceManager.getInstance().createDynamicInstance("Pailaka_DevilsLegacy.xml");

				world = new InstanceWorld();
				world.instanceId = instanceId;
				world.templateId = InstanceZoneId.PAILAKA_DEVILS_ISLE.getId();
				InstanceManager.getInstance().addWorld(world);

				world.allowed.add(player.getObjectId());
				player.teleToInstance(ENTRY_POINT, instanceId);

				_lematanNpc = addSpawn(LEMATAN, 87881, -209137, -3747, 32525, false, 0, false, instanceId);
			}
		}
	}

	@Override
	public int getQuestId()
	{
		return 129;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(npc.getNpcId() == POWDER_KEG && !npc.isDead())
		{
			npc.doCast(boom_skill.getSkill());

			if(npc.getKnownList() != null)
			{
				for(L2Character target : npc.getKnownList().getKnownCharactersInRadius(900))
				{
					target.reduceCurrentHp(500 + Rnd.get(0, 200), npc, boom_skill.getSkill());

					if(target instanceof L2MonsterInstance)
					{
						if(((L2MonsterInstance) target).getNpcId() == POWDER_KEG)
						{
							startQuestTimer("keg_trigger", 500, (L2Npc) target, attacker);
						}
						else
						{
							if(isPet)
							{
								((L2Attackable) npc).attackCharacter(attacker.getPets().getFirst());
							}
							else
							{
								((L2Attackable) npc).attackCharacter(attacker);
							}
						}
					}
				}
			}
			if(!npc.isDead())
			{
				npc.doDie(attacker);
			}
		}
		else if(npc.getNpcId() == LEMATAN && npc.getCurrentHp() < npc.getMaxHp() / 2.0f && !_isTeleportScheduled)
		{
			startQuestTimer("lematan_teleport", 1000, npc, attacker);
		}
		else if(npc.getNpcId() == TREASURE_BOX)
		{
			dropItem(npc, attacker);
			npc.doDie(attacker);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		// Must be here, because of player == null
		if(npc.getNpcId() == FOLLOWERS && event.equals("follower_cast"))
		{
			if(!npc.isCastingNow() && !npc.isDead() && !_lematanNpc.isDead())
			{
				npc.setTarget(_lematanNpc);
				npc.doCast(energy_skill.getSkill());
			}
			startQuestTimer("follower_cast", 2000 + Rnd.get(100, 1000), npc, null);
			return null;
		}
		if(npc.getNpcId() == POWDER_KEG && event.equalsIgnoreCase("keg_trigger"))
		{
			onAttack(npc, player, 600, false);
		}

		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		int cond = st.getCond();

		if(event.equalsIgnoreCase("enter"))
		{
			enterInstance(player);
			if(cond == 1)
			{
				st.setCond(2);
				return "32498-07.htm";
			}
			else
			{
				return "32498-09.htm";
			}
		}
		if(event.equalsIgnoreCase("32498-05.htm"))
		{
			if(cond == 0)
			{
				st.startQuest();
			}
		}
		else if(event.equalsIgnoreCase("32501-03.htm"))
		{
			if(cond == 2)
			{
				st.giveItems(SWORD, 1);
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		else if(event.equalsIgnoreCase("32510-02.htm"))
		{
			st.unset("cond");
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.ONE_TIME);

			Instance inst = InstanceManager.getInstance().getInstance(npc.getInstanceId());
			inst.setDuration(EXIT_TIME * 60000);
			inst.setEmptyDestroyTime(0);

			if(inst.containsPlayer(player.getObjectId()))
			{
				st.addExpAndSp(810000, 50000);
				for(int id : REWARDS)
				{
					st.giveItems(id, 1);
				}
			}
		}
		else if(event.equalsIgnoreCase("lematan_teleport"))
		{
			if(npc.getNpcId() == LEMATAN && !npc.isMovementDisabled() && !_isOnShip)
			{
				// Reduce Hate
				((L2Attackable) npc).reduceHate(player, 9999);
				npc.abortAttack();
				npc.abortCast();
				// Broadcast Escape
				npc.broadcastPacket(new MagicSkillUse(npc, 2100, 1, 1000, 0));
				// Schedule telport - when Lematan Finish casting
				startQuestTimer("lematan_finish_teleport", 1500, npc, player);
			}
			else
			{
				_isTeleportScheduled = false;
			}
			return null;
		}
		else if(npc.getNpcId() == LEMATAN && event.equalsIgnoreCase("lematan_finish_teleport") && !_isOnShip)
		{
			// Teleport Lematan
			npc.teleToLocation(84973, -208721, -3300);
			// Set onShip
			_isOnShip = true;
			// Set Spawn loc to ship. If he loose aggro he should stay on board ;)
			npc.getSpawn().setLocx(84973);
			npc.getSpawn().setLocy(-208721);
			npc.getSpawn().setLocz(-3300);
			// To be sure, reduce again
			((L2Attackable) npc).reduceHate(player, 9999);
			// Spawn followers
			_followerslist = new FastList<>();
			for(int[] SPAWN : FOLLOWERS_SPAWNS)
			{
				L2Npc _follower = addSpawn(FOLLOWERS, SPAWN[0], SPAWN[1], SPAWN[2], SPAWN[3], false, 0, false, player.getInstanceId());
				if(_follower != null)
				{
					_followerslist.add(_follower);
				}
			}
			return null;
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null || st.getState() != STARTED)
		{
			return null;
		}

		switch(npc.getNpcId())
		{
			case KAMS:
				if(st.getQuestItemsCount(SWORD) > 0)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					st.giveItems(SCROLL_1, 1);
				}
				break;
			case ALKASO:
				if(st.getQuestItemsCount(ENH_SWORD1) > 0)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					st.giveItems(SCROLL_2, 1);
				}
				break;
			case LEMATAN:
				if(_followerslist != null && !_followerslist.isEmpty())
				{
					for(L2Npc _follower : _followerslist)
					{
						_follower.getLocationController().delete();
					}
					_followerslist.clear();
				}
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				addSpawn(ADVENTURER2, 84983, -208736, -3336, 49915, false, 0, false, npc.getInstanceId());
				break;
			case POWDER_KEG:
			case TREASURE_BOX:
			case FOLLOWERS:
				// do nothing
				break;
			default:
				// hardcoded herb drops
				dropHerb(npc, player, HP_HERBS_DROPLIST);
				dropHerb(npc, player, MP_HERBS_DROPLIST);
				break;
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		int cond = st.getCond();
		switch(npc.getNpcId())
		{
			case SURVIVOR:
				switch(st.getState())
				{
					case CREATED:
						if(player.getLevel() < MIN_LEVEL)
						{
							return "32498-11.htm";
						}
						if(player.getLevel() > MAX_LEVEL)
						{
							return "32498-12.htm";
						}
						return "32498-01.htm";
					case STARTED:
						return cond > 1 ? "32498-08.htm" : "32498-06.htm";
					case COMPLETED:
						return "32498-10.htm";
					default:
						return "32498-01.htm";
				}
			case SUPPORTER:
				return cond > 2 ? "32501-04.htm" : "32501-01.htm";
			case ADVENTURER1:
				if(player.getPets().isEmpty())
				{
					if(st.getQuestItemsCount(SWORD) > 0)
					{
						if(st.getQuestItemsCount(SCROLL_1) > 0)
						{
							st.takeItems(SWORD, -1);
							st.takeItems(SCROLL_1, -1);
							st.giveItems(ENH_SWORD1, 1);
							return "32508-03.htm";
						}
						else
						{
							return "32508-02.htm";
						}
					}
					else if(st.getQuestItemsCount(ENH_SWORD1) > 0)
					{
						if(st.getQuestItemsCount(SCROLL_2) > 0)
						{
							st.takeItems(ENH_SWORD1, -1);
							st.takeItems(SCROLL_2, -1);
							st.giveItems(ENH_SWORD2, 1);
							return "32508-05.htm";
						}
						else
						{
							return "32508-04.htm";
						}
					}
					else
					{
						return st.getQuestItemsCount(ENH_SWORD2) > 0 ? "32508-06.htm" : "32508-00.htm";
					}
				}
				else
				{
					return "32508-07.htm";
				}
			case ADVENTURER2:
				if(player.getPets().isEmpty())
				{
					st.unset("cond");
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);

					Instance inst = InstanceManager.getInstance().getInstance(npc.getInstanceId());
					inst.setDuration(EXIT_TIME * 60000);
					inst.setEmptyDestroyTime(0);

					if(inst.containsPlayer(player.getObjectId()))
					{
						st.addExpAndSp(10800000, 950000);
						for(int id : REWARDS)
						{
							st.giveItems(id, 1);
						}
					}
					return "32511-01.htm";
				}
				else
				{
					return "32511-02.htm";
				}
		}
		return getNoQuestMsg(player);
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		return st != null && npc.getNpcId() == ADVENTURER2 && st.getState() == COMPLETED ? "32511-03.htm" : npc.getNpcId() + ".htm";
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		startQuestTimer("follower_cast", 1000 + Rnd.get(100, 1000), npc, null);
		npc.disableCoreAI(true);
		return null;
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if(character instanceof L2PcInstance && !character.isDead() && !character.isTeleporting() && ((L2PcInstance) character).isOnline())
		{
			InstanceWorld world = InstanceManager.getInstance().getWorld(character.getInstanceId());
			if(world != null && world.templateId == InstanceZoneId.PAILAKA_DEVILS_ISLE.getId())
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new Teleport(character, world.instanceId), 1000);
			}
		}
		return super.onEnterZone(character, zone);
	}

	static class Teleport implements Runnable
	{
		private final L2Character _char;
		private final int _instanceId;

		public Teleport(L2Character c, int id)
		{
			_char = c;
			_instanceId = id;
		}

		@Override
		public void run()
		{
			try
			{
				_char.teleToInstance(ENTRY_POINT, _instanceId);
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, getClass().getSimpleName() + " Error while teleporting task! ", e);
			}
		}
	}
}