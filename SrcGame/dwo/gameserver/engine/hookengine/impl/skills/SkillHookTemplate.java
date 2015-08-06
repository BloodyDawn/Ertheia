package dwo.gameserver.engine.hookengine.impl.skills;

import dwo.gameserver.engine.hookengine.IHook;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;

/**
 * Serves as a template and class constructor for SkillHooks.
 */

public class SkillHookTemplate
{
	private static final Logger _log = LogManager.getLogger(SkillHookTemplate.class.getName());

	private SystemMessage on;
	private SystemMessage off;
	private String[] args;
	private Constructor<?> construct;
	private int skillId;
	private boolean isEffectBound;

	public SkillHookTemplate(int onMsgId, int offMsgId, int skillId, boolean isEffectBound, String hookClassName, String[] args)
	{
		this.skillId = skillId;
		this.isEffectBound = isEffectBound;
		this.args = args;

		try
		{
			Class<?> hookClass = Class.forName("dwo.gameserver.engine.hookengine.impl.skills." + hookClassName);
			construct = hookClass.getDeclaredConstructors()[0];
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
		}

		if(onMsgId != 0)
		{
			on = SystemMessage.getSystemMessage(onMsgId).addSkillName(skillId);
		}
		if(offMsgId != 0)
		{
			off = SystemMessage.getSystemMessage(offMsgId).addSkillName(skillId);
		}
	}

	/**
	 * @return the on
	 */
	public SystemMessage getOn()
	{
		return on;
	}

	/**
	 * @return the on
	 */
	public SystemMessage getOff()
	{
		return off;
	}

	/**
	 * @return the args
	 */
	public String[] getArgs()
	{
		return args;
	}

	public int getSkillId()
	{
		return skillId;
	}

	public IHook getNewHookInstance(L2PcInstance player)
	{
		try
		{
			return (IHook) construct.newInstance(player, this, isEffectBound);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
		}
		return null;
	}
}
