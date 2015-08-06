package dwo.gameserver.engine.hookengine.impl.skills;

import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

public class DayNightChangeHook extends AbstractSkillHook
{
	private static HookType[] desiredHooks = {HookType.ON_DAYNIGHT_CHANGE};

	public DayNightChangeHook(L2PcInstance player, SkillHookTemplate temp, boolean isEffectBound)
	{
		super(player, temp, isEffectBound);
	}

	@Override
	public void onDayNightChange(boolean isDay)
	{
		SystemMessage toSend = isDay ? getTemplate().getOn() : getTemplate().getOff();

		if(toSend != null)
		{
			getOwner().sendPacket(toSend);
		}

		getOwner().broadcastStatusUpdate();
	}

	@Override
	protected HookType[] getDesiredHooks()
	{
		return desiredHooks;
	}
}
