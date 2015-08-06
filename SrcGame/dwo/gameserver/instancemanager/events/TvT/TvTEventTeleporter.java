package dwo.gameserver.instancemanager.events.TvT;

import dwo.config.events.ConfigEventTvT;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.duel.DuelState;
import dwo.gameserver.model.skills.effects.AbnormalEffect;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class TvTEventTeleporter implements Runnable
{
	protected static final Logger _log = LogManager.getLogger(TvTEventTeleporter.class);
	/** The instance of the player to teleport */
	private L2PcInstance _playerInstance;
	/** Coordinates of the spot to teleport to */
	private int[] _coordinates = new int[3];
	/** Admin removed this player from event */
	private boolean _adminRemove;
	private int _teamid;

	/**
	 * Initialize the teleporter and start the delayed task<br><br>
	 *
	 * @param playerInstance as L2PcInstance<br>
	 * @param coordinates as int[]<br>
	 * @param fastSchedule as boolean<br>
	 * @param adminRemove as boolean<br>
	 */
	public TvTEventTeleporter(L2PcInstance playerInstance, int[] coordinates, boolean fastSchedule, boolean adminRemove)
	{
		_playerInstance = playerInstance;
		_coordinates = coordinates;
		_adminRemove = adminRemove;
		_teamid = TvTEvent.getParticipantTeamId(playerInstance.getObjectId());
		long delay = (TvTEvent.isStarted() ? ConfigEventTvT.TVT_EVENT_RESPAWN_TELEPORT_DELAY : ConfigEventTvT.TVT_EVENT_START_LEAVE_TELEPORT_DELAY) * 1000;

		ThreadPoolManager.getInstance().scheduleGeneral(this, fastSchedule ? 0 : delay);
	}

	/**
	 * The task method to teleport the player<br>
	 * 1. Unsummon pet if there is one<br>
	 * 2. Remove all effects<br>
	 * 3. Revive and full heal the player<br>
	 * 4. Teleport the player<br>
	 * 5. Broadcast status and user info<br><br>
	 *
	 */
	@Override
	public void run()
	{
		if(_playerInstance == null)
		{
			return;
		}

		if(!_playerInstance.getPets().isEmpty())
		{
			for(L2Summon pet : _playerInstance.getPets())
			{
				pet.getLocationController().decay();
			}
		}

		if(_playerInstance.isInDuel())
		{
			_playerInstance.setDuelState(DuelState.INTERRUPTED);
		}

		if(ConfigEventTvT.TVT_EVENT_EFFECTS_REMOVAL == 0 || ConfigEventTvT.TVT_EVENT_EFFECTS_REMOVAL == 1 && _playerInstance.getTeam() == 0)
		{
			_playerInstance.stopAllEffectsExceptThoseThatLastThroughDeath();
		}

		int TvTInstance = TvTEvent.getTvTEventInstance();
		if(TvTInstance == 0)
		{
			_playerInstance.getInstanceController().setInstanceId(0);
		}
		else
		{
			if(TvTEvent.isStarted() && !_adminRemove)
			{
				_playerInstance.getInstanceController().setInstanceId(TvTInstance);
			}

			else
			{
				_playerInstance.getInstanceController().setInstanceId(0);
			}
		}

		_playerInstance.TvTSetKillsWithoutDie(0);
		_playerInstance.stopAbnormalEffect(AbnormalEffect.REAL_TARGET);
		_playerInstance.doRevive();

		try
		{
			if(ConfigEventTvT.TVT_EVENT_PARTICIPATION_NPC_COORDINATES == _coordinates)
			{
				_playerInstance.teleToLocation(_coordinates[0] + Rnd.get(101) - 50, _coordinates[1] + Rnd.get(101) - 50, _coordinates[2], false);
			}
			else
			{
				Location loc = TvTLocationManager.getInstance().getLocation(TvTEvent.getCurrentMapId()).getRandomLoc(_teamid);
				_playerInstance.teleToLocation(loc, false);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		if(TvTEvent.isStarted() && !_adminRemove)
		{
			_playerInstance.setTeam(TvTEvent.getParticipantTeamId(_playerInstance.getObjectId()) + 1);
		}
		else
		{
			_playerInstance.setTeam(0);
		}

		_playerInstance.setCurrentCp(_playerInstance.getMaxCp());
		_playerInstance.setCurrentHp(_playerInstance.getMaxHp());
		_playerInstance.setCurrentMp(_playerInstance.getMaxMp());

		_playerInstance.broadcastStatusUpdate();
		_playerInstance.broadcastUserInfo();
	}
}