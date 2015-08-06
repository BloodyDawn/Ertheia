package dwo.gameserver.model.world.zone.type;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.zone.AbstractZoneSettings;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.TaskZoneSettings;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExNotifyFlyMoveStart;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 15.06.2011
 * Time: 8:43:31
 */

public class L2JumpZone extends L2ZoneType
{
	private int _startTask;
	private int _reuseTask;
	private int _jumpId;

	public L2JumpZone(int id)
	{
		super(id);
		_startTask = 200;
		_reuseTask = 1000;
		setTargetType(L2PcInstance.class); // только для игроков
		AbstractZoneSettings settings = ZoneManager.getSettings(getName());
		if(settings == null)
		{
			settings = new TaskZoneSettings();
		}
		setSettings(settings);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if(name.equals("jumpId"))
		{
			_jumpId = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	@Override
	public TaskZoneSettings getSettings()
	{
		return (TaskZoneSettings) super.getSettings();
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if(getSettings().getTask() == null)
		{
			synchronized(this)
			{
				if(getSettings().getTask() == null && character instanceof L2PcInstance && !((L2PcInstance) character).isJumping())
				{
					getSettings().setTask(ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ApplyJump(this), _startTask, _reuseTask));
				}
			}
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if(_characterList.isEmpty() && getSettings().getTask() != null)
		{
			getSettings().clear();
		}
	}

	@Override
	public void onDieInside(L2Character character)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}

	public int getJumpId()
	{
		return _jumpId;
	}

	class ApplyJump implements Runnable
	{
		private final L2JumpZone _jmpZone;

		private ApplyJump(L2JumpZone zone)
		{
			_jmpZone = zone;
		}

		@Override
		public void run()
		{
			_jmpZone.getCharactersInside().stream().filter(characterInside -> characterInside != null && characterInside.isPlayer()).forEach(characterInside -> {
				if(!characterInside.isDead() && !((L2PcInstance) characterInside).isJumping() && !characterInside.isInCombat() && characterInside.getPets().isEmpty() && !((L2PcInstance) characterInside).isMounted() && !characterInside.isTransformed() && characterInside.isAwakened() && !((L2PcInstance) characterInside).isCursedWeaponEquipped())
				{
					characterInside.sendPacket(ExNotifyFlyMoveStart.STATIC_PACKET);
				}
			});
		}
	}
}