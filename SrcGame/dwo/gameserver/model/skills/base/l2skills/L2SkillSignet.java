package dwo.gameserver.model.skills.base.l2skills;

import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2EffectPointInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.util.geometry.Point3D;
import org.apache.log4j.Level;

public class L2SkillSignet extends L2Skill
{
	public int _effectNpcId;
	public int _effectId;
	public int _effectLevel;

	public L2SkillSignet(StatsSet set)
	{
		super(set);
		_effectNpcId = set.getInteger("effectNpcId", -1);
		_effectId = set.getInteger("effectId", -1);
		// Для новых сигнетов, которые точаться для эффекта скилл берется из поля effectLevel иначе в триггер будут приходить знаечния > 100
		_effectLevel = set.getInteger("effectLevel", getLevel());
	}

	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if(caster == null || caster.isAlikeDead())
		{
			return;
		}

		L2NpcTemplate template = NpcTable.getInstance().getTemplate(_effectNpcId);

		try
		{
			L2EffectPointInstance effectPoint = new L2EffectPointInstance(IdFactory.getInstance().getNextId(), template, caster);
			effectPoint.setCurrentHp(effectPoint.getMaxHp());
			effectPoint.setCurrentMp(effectPoint.getMaxMp());
			//L2World.getInstance().storeObject(effectPoint);

			int x = caster.getX();
			int y = caster.getY();
			int z = caster.getZ();

			if(caster instanceof L2PcInstance && getTargetType() == L2TargetType.TARGET_GROUND)
			{
				Point3D wordPosition = caster.getActingPlayer().getCurrentSkillWorldPosition();

				if(wordPosition != null)
				{
					x = wordPosition.getX();
					y = wordPosition.getY();
					z = wordPosition.getZ();
				}
			}

			getEffects(caster, effectPoint);
			effectPoint.setIsInvul(true);
			effectPoint.getLocationController().spawn(x, y, z);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Trouble with EffectPointId: " + _effectNpcId + " and SkillId: " + getId(), e);
		}
	}
}
