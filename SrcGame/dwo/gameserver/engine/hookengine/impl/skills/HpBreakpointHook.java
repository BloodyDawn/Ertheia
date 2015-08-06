package dwo.gameserver.engine.hookengine.impl.skills;

import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.effects.L2Effect;

/**
 * Hook used for managing system messages/user info broadcasts when player HP hits some breakpoint
 * for example Final Frenzy - when player goes over or under 30%, messages are sent + user info is updated
 */

public class HpBreakpointHook extends AbstractSkillHook
{
	/** Hooks that are registered when this skill is added, or skill effect is applied (in case of
	 * 	effectBound hooks
	 */
	private static HookType[] desiredHooks = {HookType.ON_HP_CHANGED};

	/** HP Breakpoint on which system messages might be sent + user info is broadcasted */
	private final double breakPoint;

	/** % HP during last check */
	private double lastPercent = -1;

	public HpBreakpointHook(L2PcInstance player, SkillHookTemplate temp, boolean isEffectBound)
	{
		super(player, temp, isEffectBound);

		if(!isEffectBound)
		{
			lastPercent = player.getCurrentHp() / player.getMaxHp();
		}

		breakPoint = Double.valueOf(temp.getArgs()[0]);
	}

	@Override
	public void onHpChange(L2Character player, double damage, double fullDamage)
	{
		if(!(player instanceof L2PcInstance))
		{
			return;
		}

		//TODO: If concurrency problems appear, sync this method (perhaps only partially) + change hp change hook
		// to be asynchronously called
		double currentPercent = player.getCurrentHp() / player.getMaxHp();

		//player was under breakpoint hp before, and now he is over breakpoint
		if(lastPercent < breakPoint && currentPercent > breakPoint)
		{
			if(getTemplate().getOff() != null)
			{
				player.sendPacket(getTemplate().getOff());
			}

			((L2PcInstance) player).broadcastUserInfo();
		}
		//vice versa situation
		else if(lastPercent > breakPoint && currentPercent < breakPoint)
		{
			if(getTemplate().getOn() != null)
			{
				player.sendPacket(getTemplate().getOn());
			}

			((L2PcInstance) player).broadcastUserInfo();
		}

		lastPercent = currentPercent;
	}

	@Override
	protected HookType[] getDesiredHooks()
	{
		return desiredHooks;
	}

	@Override
	public void onEffectStart(L2Effect e)
	{
		super.onEffectStart(e);

		//TODO: Duplicated check, see parent method
		if(e.getSkill().getId() == getTemplate().getSkillId())
		{
			lastPercent = getOwner().getCurrentHp() / getOwner().getMaxHp();
		}
	}
}
