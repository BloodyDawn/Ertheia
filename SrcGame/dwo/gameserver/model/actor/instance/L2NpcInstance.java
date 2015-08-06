package dwo.gameserver.model.actor.instance;

import dwo.gameserver.handler.effects.Buff;
import dwo.gameserver.handler.effects.Debuff;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.status.FolkStatus;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.skills.effects.L2Effect;

import java.util.List;

public class L2NpcInstance extends L2Npc
{
	private final List<ClassId> _classesToTeach;

	public L2NpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setIsInvul(false);
		_classesToTeach = template.getTeachInfo();
	}

	@Override
	public FolkStatus getStatus()
	{
		return (FolkStatus) super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		setStatus(new FolkStatus(this));
	}

	@Override
	public void addEffect(L2Effect newEffect)
	{
		if(newEffect instanceof Debuff || newEffect instanceof Buff)
		{
			super.addEffect(newEffect);
		}
		else if(newEffect != null)
		{
			newEffect.stopEffectTask();
		}
	}

	public List<ClassId> getClassesToTeach()
	{
		return _classesToTeach;
	}
}

