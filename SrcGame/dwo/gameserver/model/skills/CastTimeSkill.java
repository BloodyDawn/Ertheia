package dwo.gameserver.model.skills;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.L2Effect;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * L2GOD Team
 * @author Keiichi, Yorie
 * Date: 25.03.12
 * Time: 18:19
 */

public class CastTimeSkill extends FusionSkill
{
	protected int _skillId;
	protected int _skillLevel;
	protected List<WeakReference<L2Effect>> _effects = new FastList<>();

	public CastTimeSkill(L2Character caster, L2Character target, L2Skill skill)
	{
		_skillCastRange = skill.getCastRange();
		_caster = caster;
		_target = target;
		_skillId = skill.getTriggeredId();
		_skillLevel = skill.getTriggeredLevel();

		// Если нету тригера
		if(_skillId == 0)
		{
			for(L2Object nextTarget : skill.getTargetList(caster))
			{
				if(nextTarget instanceof L2Character)
				{
					for(L2Effect effect : skill.getEffects(_caster, (L2Character) nextTarget, null))
					{
						_effects.add(new WeakReference(effect));
					}
				}
			}
		}
		else
		{
			L2Skill force = SkillTable.getInstance().getInfo(_skillId, _skillLevel);
			if(force != null)
			{
				for(L2Object nextTarget : skill.getTargetList(caster))
				{
					if(nextTarget instanceof L2Character)
					{
						for(L2Effect effect : force.getEffects(_caster, (L2Character) nextTarget, null))
						{
							_effects.add(new WeakReference(effect));
						}
					}
				}
			}
			else
			{
				_log.log(Level.WARN, "Triggered skill: [sk: " + skill.getId() + ';' + skill.getLevel() + "]  [trig: " + _skillId + ';' + _skillLevel + "] not found!");
			}
		}

		_geoCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new GeoCheckTask(), 1000, 1000);
	}

	@Override
	public void onCastAbort()
	{
		_caster.setCastTimeSkill(null);
		_geoCheckTask.cancel(true);
		// Снимаем эффекты
		_effects.stream().filter(effect -> effect.get() != null).forEach(effect -> effect.get().exit());
	}
}
