/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.model.skills.effects;

import dwo.gameserver.datatables.xml.BuffStackGroupData;
import dwo.gameserver.handler.EffectHandler;
import dwo.gameserver.model.skills.ChanceCondition;
import dwo.gameserver.model.skills.base.conditions.Condition;
import dwo.gameserver.model.skills.base.funcs.FuncTemplate;
import dwo.gameserver.model.skills.base.funcs.Lambda;
import dwo.gameserver.model.skills.base.proptypes.L2EffectStopCond;
import dwo.gameserver.model.skills.base.proptypes.L2SkillComboType;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.StatsSet;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mkizub
 */

public class EffectTemplate
{
	static Logger _log = LogManager.getLogger(EffectTemplate.class);
	public final Condition attachCond;
	public final Condition applayCond;
	public final Lambda lambda;
	public final int totalTickCount;
	public final int abnormalTime; // in seconds
	public final boolean icon;
	public final String funcName;
	public final double effectPower; // to thandle chance
	public final int triggeredId;
	public final int triggeredLevel;
	public final int comboId;
	public final List<L2EffectStopCond> onRemovedEffect;
	public final Byte hitToRemove;
	public final ChanceCondition chanceCondition;
	public final String additional;
	public final L2SkillType effectType; // to handle resistences etc...
	private final Class<?> _func;
	private final Constructor<?> _constructor;
	private final StatsSet _parameters;
    private final int activationChanceEffect;
    public FuncTemplate[] funcTemplates;
    private AbnormalEffect[] _abnormalVisualEffect;
    private String[] _lockedStackTypes;
    private String _abnormalType;
    private byte _abnormalLevel;
    private boolean selfEffectType;

	public EffectTemplate(Condition pAttachCond, Condition pApplayCond, Lambda pLambda, StatsSet set, StatsSet params)
	{
		lambda = pLambda;
		applayCond = pApplayCond;
		attachCond = pAttachCond;
		funcName = set.getString("name");
		totalTickCount = set.getInteger("count", 0);
		abnormalTime = set.getInteger("abnormalTime", 1);
		icon = set.getInteger("noicon", 0) == 0;
		hitToRemove = set.getByte("hitCount", (byte) 0);
		comboId = set.getEnum("comboType", L2SkillComboType.class, L2SkillComboType.NO_COMBO).getId();
		effectPower = set.getInteger("effectPower", -1); // TODO Выпилить!!
		effectType = set.getEnum("effectType", L2SkillType.class, null); // TODO Выпилить!!
		triggeredId = set.getInteger("triggeredId", 0);
		triggeredLevel = set.getInteger("triggeredLevel", 1);
		chanceCondition = ChanceCondition.parse(set.getString("chanceType", null), set.getInteger("activationChance", -1), set.getInteger("activationMinDamage", -1), set.getString("activationElements", null), set.getString("activationSkills", null), set.getBool("pvpChanceOnly", false), set.getInteger("triggeredById", 0));
		additional = set.getString("additional", null);
        activationChanceEffect = set.getInteger("activationChanceEffect", 100);
		_func = EffectHandler.getInstance().getHandler(funcName);
		_parameters = params;
		onRemovedEffect = new ArrayList<>();
		onRemovedEffect.add(L2EffectStopCond.ON_NONE);
		if(set.getString("stopCond", null) != null)
		{
			for(String str : set.getString("stopCond").split(","))
			{
				onRemovedEffect.add(L2EffectStopCond.valueOf(str));
			}
		}

		if(_func == null)
		{
			_log.log(Level.ERROR, "EffectTemplate: Requested Unexistent effect: " + funcName);
			throw new RuntimeException();
		}

		try
		{
			_constructor = _func.getConstructor(Env.class, EffectTemplate.class);
		}
		catch(NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}

        String abnormalVisualEffect = set.getString("abnormalVisualEffect", null);
        if(abnormalVisualEffect != null)
        {
            String[] abnormalVisualEffects = abnormalVisualEffect.split(",");
            _abnormalVisualEffect = new AbnormalEffect[abnormalVisualEffects.length];
            for(int s = 0; s < abnormalVisualEffects.length; s++)
            {
                _abnormalVisualEffect[s] = AbnormalEffect.getByName(abnormalVisualEffects[s]);
            }
        }

        _abnormalType = set.getString("abnormalType", "none");
        _abnormalLevel = set.getByte("abnormalLevel", (byte) 0);

        _lockedStackTypes = set.getString("lockedStacktypes", null) != null ? set.getString("lockedStacktypes", null).split(",") : BuffStackGroupData.getInstance().getLockedAbnormalsList(_abnormalType);
        selfEffectType = set.getBool("selfEffectType", false);
	}

	public L2Effect getEffect(Env env)
	{
		if(attachCond != null && !attachCond.test(env))
		{
			return null;
		}
		try
		{
			return (L2Effect) _constructor.newInstance(env, this);
		}
		catch(IllegalAccessException e)
		{
			_log.log(Level.ERROR, "", e);
			return null;
		}
		catch(InstantiationException e)
		{
			_log.log(Level.ERROR, "", e);
			return null;
		}
		catch(InvocationTargetException e)
		{
			_log.log(Level.ERROR, "Error creating new instance of Class " + _func + " Exception was: " + e.getTargetException().getMessage(), e.getTargetException());
			return null;
		}

	}

	/*
	 * Creates an L2Effect instance from an existing one and an Env object.
	 */
	public L2Effect getStolenEffect(Env env, L2Effect stolen)
	{
		Class<?> func = EffectHandler.getInstance().getHandler(funcName);
		if(func == null)
		{
			throw new RuntimeException();
		}

		Constructor<?> stolenCons;
		try
		{
			stolenCons = func.getConstructor(Env.class, L2Effect.class);
		}
		catch(NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
		try
		{
			return (L2Effect) stolenCons.newInstance(env, stolen);
		}
		catch(IllegalAccessException e)
		{
			_log.log(Level.ERROR, "", e);
			return null;
		}
		catch(InstantiationException e)
		{
			_log.log(Level.ERROR, "", e);
			return null;
		}
		catch(InvocationTargetException e)
		{
			_log.log(Level.ERROR, "Error creating new instance of Class " + func + " Exception was: " + e.getTargetException().getMessage(), e.getTargetException());
			return null;
		}
	}

	public void attach(FuncTemplate f)
	{
		if(funcTemplates == null)
		{
			funcTemplates = new FuncTemplate[]{f};
		}
		else
		{
			int len = funcTemplates.length;
			FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			funcTemplates = tmp;
		}
	}

	public int getComboAbnormal()
	{
		return comboId;
	}

	/**
	 * Get the parameters.
	 * @return the parameters of this effect template
	 */
	public StatsSet getParameters()
	{
		return _parameters;
	}

	/**
	 * Verify if this effect template has parameters.
	 * @return {@code true} if this effect template has parameters, {@code false} otherwise
	 */
	public boolean hasParameters()
	{
		return _parameters != null;
	}

	public int getTotalTickCount()
	{
		return totalTickCount;
	}

    public final String[] getAbnormalLockedStackTypes()
    {
        return _lockedStackTypes;
    }

    public final AbnormalEffect[] getAbnormalVisualEffect()
    {
        return _abnormalVisualEffect;
    }

    public final byte getAbnormalLevel()
    {
        return _abnormalLevel;
    }

    public final String getAbnormalType()
    {
        return _abnormalType;
    }

    public boolean isSelfEffectType()
    {
        return selfEffectType;
    }

    public int getActivationChanceEffect() {
        return activationChanceEffect;
    }
}