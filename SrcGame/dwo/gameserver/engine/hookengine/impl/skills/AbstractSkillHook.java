package dwo.gameserver.engine.hookengine.impl.skills;

import dwo.gameserver.engine.hookengine.AbstractHookImpl;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.L2Effect;

/**
 * Parent class for SkillHooks. Registers/Unregisters hooks from getDesiredHooks() when skill
 * is added/removed OR in case of effect bound, when effect is started/removed
 */

public abstract class AbstractSkillHook extends AbstractHookImpl
{
	private final SkillHookTemplate template;
	private final L2PcInstance owner;
	private final boolean isEffectBound;
	/** A little hack that solves the issue with effect overriding (with same effect)
	 *  For example when player uses frenzy, then uses frenzy befor the first one ends,
	 *  onEffect() is called before onExit() --> hooks would be replaced & then removed
	 *  This fixes it
	 */
	boolean activated;
	boolean doubleActivated;

	/**
	 * @param player owner of this hook
	 * @param temp Template for this hook
	 * @param isEffectBound If True this hook will triggered only when skill effect is "running", false for whole time
	 */
	protected AbstractSkillHook(L2PcInstance player, SkillHookTemplate temp, boolean isEffectBound)
	{
		owner = player;
		this.isEffectBound = isEffectBound;
		template = temp;

		if(isEffectBound)
		{
			player.getHookContainer().addHook(HookType.ON_EFFECT_START, this);
			player.getHookContainer().addHook(HookType.ON_EFFECT_STOP, this);
		}
		else
		{
			for(HookType hook : getDesiredHooks())
			{
				owner.getHookContainer().addHook(hook, this);
			}
		}

		player.getHookContainer().addHook(HookType.ON_SKILL_REMOVE, this);
	}

	protected abstract HookType[] getDesiredHooks();

	@Override
	public void onSkillRemove(L2PcInstance player, L2Skill skill)
	{
		if(skill.getId() == template.getSkillId())
		{
			player.getHookContainer().removeHook(HookType.ON_SKILL_REMOVE, this);

			if(isEffectBound)
			{
				player.getHookContainer().removeHook(HookType.ON_EFFECT_START, this);
				player.getHookContainer().removeHook(HookType.ON_EFFECT_STOP, this);
			}

			for(HookType hook : getDesiredHooks())
			{
				owner.getHookContainer().removeHook(hook, this);
			}
		}
	}

	@Override
	public void onEffectStart(L2Effect e)
	{
		if(e.getSkill().getId() == template.getSkillId())
		{
			if(activated)
			{
				doubleActivated = true;
				return;
			}

			activated = true;

			for(HookType hook : getDesiredHooks())
			{
				owner.getHookContainer().addHook(hook, this);
			}
		}
	}

	@Override
	public void onEffectStop(L2Effect e)
	{
		if(doubleActivated)
		{
			doubleActivated = false;
			return;
		}

		if(e.getSkill().getId() == template.getSkillId())
		{
			for(HookType hook : getDesiredHooks())
			{
				owner.getHookContainer().removeHook(hook, this);
			}

			activated = false;
		}
	}

	/**
	 * @return the template
	 */
	public SkillHookTemplate getTemplate()
	{
		return template;
	}

	/**
	 * @return the owner
	 */
	public L2PcInstance getOwner()
	{
		return owner;
	}

	/**
	 * @return the isEffectBound
	 */
	public boolean isEffectBound()
	{
		return isEffectBound;
	}
}
