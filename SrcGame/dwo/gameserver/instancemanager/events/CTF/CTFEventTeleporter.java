package dwo.gameserver.instancemanager.events.CTF;

import dwo.config.events.ConfigEventCTF;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.duel.DuelState;
import dwo.gameserver.util.Rnd;

public class CTFEventTeleporter implements Runnable
{
	private L2PcInstance _player;
	private int[] _coordinates = new int[3];
	private boolean _adminRemove;

	public CTFEventTeleporter(L2PcInstance player, int[] coordinates, boolean fastSchedule, boolean adminRemove)
	{
		_player = player;
		_coordinates = coordinates;
		_adminRemove = adminRemove;

		long delay = (CTFEvent.isStarted() ? ConfigEventCTF.CTF_EVENT_RESPAWN_DELAY : ConfigEventCTF.CTF_EVENT_START_LEAVE_TELEPORT_DELAY) * 1000;

		ThreadPoolManager.getInstance().scheduleGeneral(this, fastSchedule ? 0 : delay);
	}

	@Override
	public void run()
	{
		if(_player == null)
		{
			return;
		}

		if(!_player.getPets().isEmpty() && ConfigEventCTF.CTF_EVENT_DESPAWN_SUMMON_ON_TELEPORT)
		{
			for(L2Summon pet : _player.getPets())
			{
				pet.getLocationController().decay();
			}
		}
		if(_player.isInDuel())
		{
			_player.setDuelState(DuelState.INTERRUPTED);
		}

		if(ConfigEventCTF.CTF_EVENT_EFFECTS_REMOVAL == 0 || ConfigEventCTF.CTF_EVENT_EFFECTS_REMOVAL == 1 && _player.getTeam() == 0 || _player.isInDuel() && _player.getDuelState() != DuelState.INTERRUPTED)
		{
			_player.stopAllEffectsExceptThoseThatLastThroughDeath();
		}

		int CTFInstance = CTFEvent.getCTFEventInstance();
		if(CTFInstance == 0)
		{
			_player.getInstanceController().setInstanceId(0);
		}
		else
		{
			if(CTFEvent.isStarted() && !_adminRemove)
			{
				_player.getInstanceController().setInstanceId(CTFInstance);
			}
			else
			{
				_player.getInstanceController().setInstanceId(0);
			}
		}

		_player.doRevive();
		_player.teleToLocation(_coordinates[0] + Rnd.get(101) - 50, _coordinates[1] + Rnd.get(101) - 50, _coordinates[2], false);

		if(CTFEvent.isStarted() && !_adminRemove)
		{
			_player.setTeam(CTFEvent.getParticipantTeamId(_player.getObjectId()) + 1);
		}
		else
		{
			_player.setTeam(0);
		}

		_player.setCurrentCp(_player.getMaxCp());
		_player.setCurrentHp(_player.getMaxHp());
		_player.setCurrentMp(_player.getMaxMp());

		_player.broadcastStatusUpdate();
		_player.broadcastUserInfo();
	}
}