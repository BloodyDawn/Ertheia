package dwo.scripts.ai.individual.raidbosses;

import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.GlobalVariablesManager;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;

import java.util.ArrayList;
import java.util.List;

/**
 * L2GOD Team
 * User: nonom, ANZO
 * Date: 19.12.12
 * Time: 16:52
 */

public class Benom extends Quest
{
	private static final int CASTLE = 8; // Rune

	private static final int VENOM = 29054;
	private static final int TELEPORT_CUBE = 29055;
	private static final int DUNGEON_KEEPER = 35506;

	private static final byte ALIVE = 0;
	private static final byte DEAD = 1;

	private static final int HOURS_BEFORE = 24;

	private static final Location[] TARGET_TELEPORTS = {
		new Location(12860, -49158, 976), new Location(14878, -51339, 1024), new Location(15674, -49970, 864),
		new Location(15696, -48326, 864), new Location(14873, -46956, 1024), new Location(12157, -49135, -1088),
		new Location(12875, -46392, -288), new Location(14087, -46706, -288), new Location(14086, -51593, -288),
		new Location(12864, -51898, -288), new Location(15538, -49153, -1056), new Location(17001, -49149, -1064)
	};

	private static final Location TRHONE = new Location(11025, -49152, -537);
	private static final Location DUNGEON = new Location(11882, -49216, -3008);
	private static final Location TELEPORT_ENTER = new Location(12589, -49044, -3008);
	private static final Location[] TELEPORT_EXIT = {
		new Location(11913, -48851, -1088), new Location(11918, -49447, -1088)
	};
	private static final Location CUBE = new Location(12589, -49044, -3008);

	private static final SkillHolder VENOM_STRIKE = new SkillHolder(4993, 1);
	private static final SkillHolder SONIC_STORM = new SkillHolder(4994, 1);
	private static final SkillHolder VENOM_TELEPORT = new SkillHolder(4995, 1);
	private static final SkillHolder RANGE_TELEPORT = new SkillHolder(4996, 1);
	private static final int[] TARGET_TELEPORTS_OFFSET = {
		650, 100, 100, 100, 100, 650, 200, 200, 200, 200, 200, 650
	};
	private static List<L2PcInstance> _targets = new ArrayList<>();
	private final L2Npc _massymore;
	private L2Npc _venom;
	private int _venomX;
	private int _venomY;
	private int _venomZ;
	private boolean _aggroMode;
	private boolean _prisonIsOpen;

	private Benom()
	{
		addFirstTalkId(DUNGEON_KEEPER);
		addTeleportRequestId(TELEPORT_CUBE);
		addAskId(DUNGEON_KEEPER, 0);
		addSpawnId(VENOM);
		addSpellFinishedId(VENOM);
		addAttackId(VENOM);
		addKillId(VENOM);
		addAggroRangeEnterId(VENOM);
		addEventId(HookType.ON_SIEGE_START);
		addEventId(HookType.ON_SIEGE_END);

		_massymore = SpawnTable.getInstance().getFirstSpawn(DUNGEON_KEEPER).getLastSpawn();
        if (SpawnTable.getInstance().getFirstSpawn(VENOM) != null) {
            _venom = SpawnTable.getInstance().getFirstSpawn(VENOM).getLastSpawn();
            _venomX = _venom.getX();
            _venomY = _venom.getY();
            _venomZ = _venom.getZ();
            _venom.disableSkill(VENOM_TELEPORT.getSkill(), 0);
            _venom.disableSkill(RANGE_TELEPORT.getSkill(), 0);
            _venom.doRevive();
            ((L2Attackable) _venom).setCanReturnToSpawnPoint(false);
            if (checkStatus() == DEAD) {
                _venom.getLocationController().delete();
            }
        }

		long currentTime = System.currentTimeMillis();
		long startSiegeDate = CastleManager.getInstance().getCastleById(CASTLE).getSiegeDate().getTimeInMillis();
		long openingDungeonDate = startSiegeDate - HOURS_BEFORE * 360000;

		if(currentTime > openingDungeonDate && currentTime < startSiegeDate)
		{
			_prisonIsOpen = true;
		}
	}

	public static void main(String[] args)
	{
		new Benom();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		double distance = Math.sqrt(npc.getPlanDistanceSq(attacker.getX(), attacker.getY()));
		if(_aggroMode && Rnd.getChance(25))
		{
			npc.setTarget(attacker);
			npc.doCast(VENOM_TELEPORT.getSkill());
		}
		else if(_aggroMode && npc.getCurrentHp() < npc.getMaxHp() / 3 && Rnd.getChance(25) && !npc.isCastingNow())
		{
			npc.setTarget(attacker);
			npc.doCast(RANGE_TELEPORT.getSkill());
		}
		else if(distance > 300 && Rnd.getChance(10) && !npc.isCastingNow())
		{
			npc.setTarget(attacker);
			npc.doCast(VENOM_STRIKE.getSkill());
		}
		else if(Rnd.getChance(10) && !npc.isCastingNow())
		{
			npc.setTarget(attacker);
			npc.doCast(SONIC_STORM.getSkill());
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch(event)
		{
			case "tower_check":
				if(CastleManager.getInstance().getCastleById(CASTLE).getSiege().getControlTowerCount() <= 1)
				{
					changeLocation(MoveTo.THRONE);
					_massymore.broadcastPacket(new NS(_massymore.getObjectId(), ChatType.SHOUT, _massymore.getNpcId(), NpcStringId.OH_NO_THE_DEFENSES_HAVE_FAILED_IT_IS_TOO_DANGEROUS_TO_REMAIN_INSIDE_THE_CASTLE_FLEE_EVERY_MAN_FOR_HIMSELF));
					cancelQuestTimer("tower_check", npc, null);
					startQuestTimer("raid_check", 10000, npc, null, true);
				}
				break;
			case "raid_check":
				if(!npc.isInsideZone(L2Character.ZONE_SIEGE) && !npc.isTeleporting())
				{
					npc.teleToLocation(new Location(_venomX, _venomY, _venomZ), false);
				}
				break;
			case "cube_despawn":
				if(npc != null)
				{
					npc.getLocationController().delete();
				}
				break;
		}
		return event;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == 0)
		{
			if(_prisonIsOpen)
			{
				player.teleToLocation(TELEPORT_ENTER, 0);
				return null;
			}
			else
			{
				return "rune_massymore_teleporter002.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		updateStatus(DEAD);
		npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.SHOUT, npc.getNpcId(), NpcStringId.ITS_NOT_OVER_YET_IT_WONT_BE_OVER_LIKE_THIS_NEVER));
		if(!CastleManager.getInstance().getCastleById(CASTLE).getSiege().isInProgress())
		{
			L2Npc cube = addSpawn(TELEPORT_CUBE, CUBE, 0, false, 0);
			startQuestTimer("cube_despawn", 120000, cube, null);
		}
		cancelQuestTimer("raid_check", npc, null);
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTeleportRequest(L2Npc npc, L2PcInstance player)
	{
		player.teleToLocation(TELEPORT_EXIT[Rnd.get(TELEPORT_EXIT.length)], 0);
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(npc.getCastle().getSiege().isInProgress())
		{
			return "rune_massymore_teleporter002.htm";
		}
		return "rune_massymore_teleporter001.htm";
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		switch(skill.getId())
		{
			case 4222:
				npc.teleToLocation(new Location(_venomX, _venomY, _venomZ), false);
				break;
			case 4995:
				teleportTarget(player);
				((L2Attackable) npc).stopHating(player);
				break;
			case 4996:
				teleportTarget(player);
				((L2Attackable) npc).stopHating(player);
				if(_targets != null && !_targets.isEmpty())
				{
					for(L2PcInstance target : _targets)
					{
						long x = player.getX() - target.getX();
						long y = player.getY() - target.getY();
						long z = player.getZ() - target.getZ();
						long range = 250;
						if(x * x + y * y + z * z <= range * range)
						{
							teleportTarget(target);
							((L2Attackable) npc).stopHating(target);
						}
					}
					_targets.clear();
				}
				break;
		}
		return super.onSpellFinished(npc, player, skill);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(!npc.isTeleporting())
		{
			if(checkStatus() == DEAD)
			{
				npc.getLocationController().delete();
			}
			else
			{
				npc.doRevive();
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.SHOUT, npc.getNpcId(), NpcStringId.WHO_DARES_TO_COVET_THE_THRONE_OF_OUR_CASTLE_LEAVE_IMMEDIATELY_OR_YOU_WILL_PAY_THE_PRICE_OF_YOUR_AUDACITY_WITH_YOUR_VERY_OWN_BLOOD));
			}
		}
		return super.onSpawn(npc);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(isPet)
		{
			return super.onAggroRangeEnter(npc, player, isPet);
		}

		if(_aggroMode && _targets.size() < 10 && Rnd.get(3) < 1 && !player.isDead())
		{
			_targets.add(player);
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}

	@Override
	public void onSiegeStart(Castle castle)
	{
		if(castle.getCastleId() == CASTLE)
		{
			if(castle.getIsTimeRegistrationOver() && !castle.getSiege().getAttackerClans().isEmpty())
			{
				_prisonIsOpen = true;
				changeLocation(MoveTo.PRISON);
			}

			_aggroMode = true;
			_prisonIsOpen = false;
			if(_venom != null && !_venom.isDead())
			{
				_venom.setCurrentHp(_venom.getMaxHp());
				_venom.setCurrentMp(_venom.getMaxMp());
				_venom.enableSkill(VENOM_TELEPORT.getSkill());
				_venom.enableSkill(RANGE_TELEPORT.getSkill());
				startQuestTimer("tower_check", 30000, _venom, null, true);
			}
		}
	}

	@Override
	public void onSiegeEnd(Castle castle)
	{
		if(castle.getCastleId() == CASTLE)
		{
			if(castle.getIsTimeRegistrationOver() && !castle.getSiege().getAttackerClans().isEmpty())
			{
				_prisonIsOpen = true;
				changeLocation(MoveTo.PRISON);
			}

			_aggroMode = false;
			if(_venom != null && !_venom.isDead())
			{
				changeLocation(MoveTo.PRISON);
				_venom.disableSkill(VENOM_TELEPORT.getSkill(), 0);
				_venom.disableSkill(RANGE_TELEPORT.getSkill(), 0);
			}
			updateStatus(ALIVE);
			cancelQuestTimer("tower_check", _venom, null);
			cancelQuestTimer("raid_check", _venom, null);
		}
	}

	/**
	 * Alters the Venom location
	 * @param loc enum
	 */
	private void changeLocation(MoveTo loc)
	{
		switch(loc)
		{
			case THRONE:
				_venom.teleToLocation(TRHONE, false);
				break;
			case PRISON:
				if(_venom == null || _venom.isDead() || _venom.isDecayed())
				{
					_venom = addSpawn(VENOM, DUNGEON, 0, false, 0);
				}
				else
				{
					_venom.teleToLocation(DUNGEON, false);
				}
				cancelQuestTimer("raid_check", _venom, null);
				cancelQuestTimer("tower_check", _venom, null);
				break;
		}
		_venomX = _venom.getX();
		_venomY = _venom.getY();
		_venomZ = _venom.getZ();
	}

	private void teleportTarget(L2PcInstance player)
	{
		if(player != null && !player.isDead())
		{
			int rnd = Rnd.get(11);
			player.teleToLocation(TARGET_TELEPORTS[rnd], TARGET_TELEPORTS_OFFSET[rnd]);
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
	}

	/**
	 * Checks if Venom is Alive or Dead
	 * @return status
	 */
	private int checkStatus()
	{
		int checkStatus = ALIVE;
		if(GlobalVariablesManager.getInstance().isVariableStored("VenomStatus"))
		{
			checkStatus = Integer.parseInt(GlobalVariablesManager.getInstance().getStoredVariable("VenomStatus"));
		}
		else
		{
			GlobalVariablesManager.getInstance().storeVariable("VenomStatus", "0");
		}
		return checkStatus;
	}

	/**
	 * Update the Venom status
	 * @param status the new status. 0 = ALIVE, 1 = DEAD.
	 */
	private void updateStatus(int status)
	{
		GlobalVariablesManager.getInstance().storeVariable("VenomStatus", Integer.toString(status));
	}

	private enum MoveTo
	{
		THRONE,
		PRISON
	}
}