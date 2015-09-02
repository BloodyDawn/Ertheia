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
package dwo.gameserver.model.actor.templates;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.StatsSet;

import java.util.Map;

public class L2CharTemplate
{
	private float _baseHpMax;
	private float _baseCpMax;
	private float _baseMpMax;
	private float _baseHpReg;
	private float _baseMpReg;
	private float _baseMReuseRate;
	private int _baseShldDef;
	private int _baseShldRate;
	private int _baseAggression;
	private int _baseBleed;
	private int _basePoison;
	private int _baseStun;
	private int _baseRoot;
	private int _baseMovement;
	private int _baseConfusion;
	private int _baseSleep;
	private double _baseAggressionVuln;
	private double _baseBleedVuln;
	private double _basePoisonVuln;
	private double _baseStunVuln;
	private double _baseRootVuln;
	private double _baseMovementVuln;
	private double _baseSleepVuln;
	private double _baseCritVuln;
	private double _baseMCritVuln;
	// C4-характеристики
	private int _baseMpConsumeRate;
	private int _baseHpConsumeRate;
	// Коллизии (для ClientInfo)
	private int _collisionRadius;
	private int _collisionHeight;
	private double _fCollisionRadius;
	private double _fCollisionHeight;
	private double _npcSizeMultiplier;
	// Базовые характеристики
	private int _baseSTR;
	private int _baseCON;
	private int _baseDEX;
	private int _baseINT;
	private int _baseWIT;
	private int _baseMEN;
	private int _baseCHA;
	private int _baseLUC;
	private float _basePAtk;
	private float _baseMAtk;
	private float _basePDef;
	private float _baseMDef;
	private int _basePAtkSpd;
	private int _baseMAtkSpd;
	private int _baseAtkRange;
	private int _baseCritRate;
	private int _baseMCritRate;
	private float _baseWalkSpd;
	private float _baseRunSpd;
	// Специальные характеристики
	private int _baseBreath;
	private int _baseFire;
	private int _baseWind;
	private int _baseWater;
	private int _baseEarth;
	private int _baseHoly;
	private int _baseDark;
	private double _baseFireRes;
	private double _baseWindRes;
	private double _baseWaterRes;
	private double _baseEarthRes;
	private double _baseHolyRes;
	private double _baseDarkRes;

    public L2CharTemplate(StatsSet set)
    {
        updateL2CharTemplate(set);
    }

    public void updateL2CharTemplate(StatsSet set)
    {
        // Базовые характеристики
        _baseSTR = set.getInteger("str", 99);
        _baseCON = set.getInteger("con", 99);
        _baseDEX = set.getInteger("dex", 99);
        _baseINT = set.getInteger("int", 99);
        _baseWIT = set.getInteger("wit", 99);
        _baseMEN = set.getInteger("men", 99);
        _baseLUC = set.getInteger("luc", 99);
        _baseCHA = set.getInteger("cha", 99);
        _baseHpMax = set.getFloat("org_hp", 0);
        _baseCpMax = set.getFloat("baseCpMax", 0);
        _baseMpMax = set.getFloat("org_mp", 0);
        _baseHpReg = set.getFloat("org_hp_regen", 3.0e-3f);
        _baseMpReg = set.getFloat("org_mp_regen", 3.0e-3f);
        _basePAtk = set.getFloat("base_physical_attack", 99999);
        _baseMAtk = set.getFloat("base_magic_attack", 99999);
        _basePDef = set.getFloat("base_defend", 99999);
        _baseMDef = set.getFloat("base_magic_defend", 99999);
        _basePAtkSpd = set.getInteger("base_attack_speed", 300);
        _baseMAtkSpd = set.getInteger("baseMAtkSpd", 333);            // -- Статична для всех нпц
        _baseMReuseRate = set.getFloat("baseMReuseDelay", 1.0f);
        _baseShldDef = set.getInteger("baseShldDef", 0);
        _baseAtkRange = set.getInteger("base_attack_range", 0);
        _baseShldRate = set.getInteger("baseShldRate", 0);
        _baseCritRate = set.getInteger("base_critical", 4);
        _baseMCritRate = set.getInteger("baseMCritRate", 5);        // -- Статична для всех нпц
        _baseWalkSpd = set.getFloat("ground_low", 0);
        _baseRunSpd = set.getFloat("ground_high", 0);
        _npcSizeMultiplier = set.getFloat("size_multiplier", 1.0f);

        // Специальные характеристики
        _baseBreath = set.getInteger("baseBreath", 100);
        _baseAggression = set.getInteger("baseAggression", 0);
        _baseBleed = set.getInteger("baseBleed", 0);
        _basePoison = set.getInteger("basePoison", 0);
        _baseStun = set.getInteger("baseStun", 0);
        _baseRoot = set.getInteger("baseRoot", 0);
        _baseMovement = set.getInteger("baseMovement", 0);
        _baseConfusion = set.getInteger("baseConfusion", 0);
        _baseSleep = set.getInteger("baseSleep", 0);
        _baseFire = set.getInteger("baseFire", 0);
        _baseWind = set.getInteger("baseWind", 0);
        _baseWater = set.getInteger("baseWater", 0);
        _baseEarth = set.getInteger("baseEarth", 0);
        _baseHoly = set.getInteger("baseHoly", 0);
        _baseDark = set.getInteger("baseDark", 0);
        _baseAggressionVuln = set.getInteger("baseAggressionVuln", 0);
        _baseBleedVuln = set.getInteger("baseBleedVuln", 0);
        _basePoisonVuln = set.getInteger("basePoisonVuln", 0);
        _baseStunVuln = set.getInteger("baseStunVuln", 0);
        _baseRootVuln = set.getInteger("baseRootVuln", 0);
        _baseMovementVuln = set.getInteger("baseMovementVuln", 0);
        _baseSleepVuln = set.getInteger("baseSleepVuln", 0);
        _baseFireRes = set.getInteger("baseFireRes", 0);
        _baseCritVuln = set.getInteger("baseCritVuln", 1); // TODO: В ДП нет такого поля в темплейтах
        _baseMCritVuln = set.getInteger("baseMCritVuln", 1); // TODO: Возможно не существует
        // C4-характеристики
        _baseMpConsumeRate = set.getInteger("baseMpConsumeRate", 0);
        _baseHpConsumeRate = set.getInteger("baseHpConsumeRate", 0);

        // Коллизии
        _fCollisionHeight = set.getDouble("collision_height", 0);
        _fCollisionRadius = set.getDouble("collision_radius", 0);
        _collisionRadius = (int) _fCollisionRadius;
        _collisionHeight = (int) _fCollisionHeight;

        // Характеристики элементалей
        setBasicElementals(set);
    }

    public void setBasicElementals(StatsSet set)
    {
        _baseFireRes = set.getInteger("baseFireRes", 0);
        _baseWindRes = set.getInteger("baseWindRes", 0);
        _baseWaterRes = set.getInteger("baseWaterRes", 0);
        _baseEarthRes = set.getInteger("baseEarthRes", 0);
        _baseHolyRes = set.getInteger("baseHolyRes", 0);
        _baseDarkRes = set.getInteger("baseDarkRes", 0);
        _baseFire = set.getInteger("baseFire", 0);
        _baseWind = set.getInteger("baseWind", 0);
        _baseWater = set.getInteger("baseWater", 0);
        _baseEarth = set.getInteger("baseEarth", 0);
        _baseHoly = set.getInteger("baseHoly", 0);
        _baseDark = set.getInteger("baseDark", 0);
    }

	/**
	 * @return the org_hp
	 */
	public float getBaseHpMax()
	{
		return _baseHpMax;
	}

	/**
	 * @return the _baseFire
	 */
	public int getBaseFire()
	{
		return _baseFire;
	}

	/**
	 * @param baseFire the baseFire to set
	 */
	public void setBaseFire(int baseFire)
	{
		_baseFire = baseFire;
	}

	/**
	 * @return the _baseWind
	 */
	public int getBaseWind()
	{
		return _baseWind;
	}

	/**
	 * @param baseWind the baseWind to set
	 */
	public void setBaseWind(int baseWind)
	{
		_baseWind = baseWind;
	}

	/**
	 * @return the _baseWater
	 */
	public int getBaseWater()
	{
		return _baseWater;
	}

	/**
	 * @param baseWater the baseWater to set
	 */
	public void setBaseWater(int baseWater)
	{
		_baseWater = baseWater;
	}

	/**
	 * @return the _baseEarth
	 */
	public int getBaseEarth()
	{
		return _baseEarth;
	}

	/**
	 * @param baseEarth the baseEarth to set
	 */
	public void setBaseEarth(int baseEarth)
	{
		_baseEarth = baseEarth;
	}

	/**
	 * @return the _baseHoly
	 */
	public int getBaseHoly()
	{
		return _baseHoly;
	}

	/**
	 * @param baseHoly the baseHoly to set
	 */
	public void setBaseHoly(int baseHoly)
	{
		_baseHoly = baseHoly;
	}

	/**
	 * @return the _baseDark
	 */
	public int getBaseDark()
	{
		return _baseDark;
	}

	/**
	 * @param baseDark the baseDark to set
	 */
	public void setBaseDark(int baseDark)
	{
		_baseDark = baseDark;
	}

	/**
	 * @return the _baseFireRes
	 */
	public double getBaseFireRes()
	{
		return _baseFireRes;
	}

	/**
	 * @param baseFireRes the baseFireRes to set
	 */
	public void setBaseFireRes(double baseFireRes)
	{
		_baseFireRes = baseFireRes;
	}

	/**
	 * @return the _baseWindRes
	 */
	public double getBaseWindRes()
	{
		return _baseWindRes;
	}

	/**
	 * @param baseWindRes the baseWindRes to set
	 */
	public void setBaseWindRes(double baseWindRes)
	{
		_baseWindRes = baseWindRes;
	}

	/**
	 * @return the _baseWaterRes
	 */
	public double getBaseWaterRes()
	{
		return _baseWaterRes;
	}

	/**
	 * @param baseWaterRes the baseWaterRes to set
	 */
	public void setBaseWaterRes(double baseWaterRes)
	{
		_baseWaterRes = baseWaterRes;
	}

	/**
	 * @return the _baseEarthRes
	 */
	public double getBaseEarthRes()
	{
		return _baseEarthRes;
	}

	/**
	 * @param baseEarthRes the baseEarthRes to set
	 */
	public void setBaseEarthRes(double baseEarthRes)
	{
		_baseEarthRes = baseEarthRes;
	}

	/**
	 * @return the _baseHolyRes
	 */
	public double getBaseHolyRes()
	{
		return _baseHolyRes;
	}

	/**
	 * @param baseHolyRes the baseHolyRes to set
	 */
	public void setBaseHolyRes(double baseHolyRes)
	{
		_baseHolyRes = baseHolyRes;
	}

	/**
	 * @return the _baseDarkRes
	 */
	public double getBaseDarkRes()
	{
		return _baseDarkRes;
	}

	/**
	 * @param baseDarkRes the baseDarkRes to set
	 */
	public void setBaseDarkRes(double baseDarkRes)
	{
		_baseDarkRes = baseDarkRes;
	}

	/**
	 * @return the baseSTR
	 */
	public int getBaseSTR()
	{
		return _baseSTR;
	}

	public void setBaseSTR(int str)
	{
		_baseSTR = str;
	}

	/**
	 * @return the baseCON
	 */
	public int getBaseCON()
	{
		return _baseCON;
	}

	public void setBaseCON(int con)
	{
		_baseCON = con;
	}

	/**
	 * @return the baseDEX
	 */
	public int getBaseDEX()
	{
		return _baseDEX;
	}

	public void setBaseDEX(int dex)
	{
		_baseDEX = dex;
	}

	/**
	 * @return the baseINT
	 */
	public int getBaseINT()
	{
		return _baseINT;
	}

	public void setBaseINT(int _int)
	{
		_baseINT = _int;
	}

	/**
	 * @return the baseWIT
	 */
	public int getBaseWIT()
	{
		return _baseWIT;
	}

	public void setBaseWIT(int wit)
	{
		_baseWIT = wit;
	}

	/**
	 * @return the baseMEN
	 */
	public int getBaseMEN()
	{
		return _baseMEN;
	}

	public void setBaseMEN(int men)
	{
		_baseMEN = men;
	}

	public int getBaseLUC()
	{
			return _baseLUC;
	}

	public void setBaseLUC(int luc)
	{
			_baseLUC = luc;
	}

	public int getBaseCHA()
	{
			return _baseCHA;
	}

	public void setBaseCHA(int cha)
	{
			_baseCHA = cha;
	}
	/**
	 * @return the baseCpMax
	 */
	public float getBaseCpMax()
	{
		return _baseCpMax;
	}

	/**
	 * @return the baseMpMax
	 */
	public float getBaseMpMax()
	{
		return _baseMpMax;
	}

	/**
	 * @return the baseHpReg
	 */
	public float getBaseHpReg()
	{
		return _baseHpReg;
	}

	/**
	 * @return the baseMpReg
	 */
	public float getBaseMpReg()
	{
		return _baseMpReg;
	}

	/**
	 * @return the basePAtk
	 */
	public float getBasePAtk()
	{
		return _basePAtk;
	}

	public void setBasePAtk(int patk)
	{
		_basePAtk = patk;
	}

	/**
	 * @return the baseMAtk
	 */
	public float getBaseMAtk()
	{
		return _baseMAtk;
	}

	/**
	 * @return the basePDef
	 */
	public float getBasePDef()
	{
		return _basePDef;
	}

	public void setBasePDef(int pdef)
	{
		_basePDef = pdef;
	}

	/**
	 * @return the baseMDef
	 */
	public float getBaseMDef()
	{
		return _baseMDef;
	}

	public void setBaseMDef(int mdef)
	{
		_baseMDef = mdef;
	}

	/**
	 * @return the basePAtkSpd
	 */
	public int getBasePAtkSpd()
	{
		return _basePAtkSpd;
	}

	public void setBasePAtkSpd(int atkSpd)
	{
		_basePAtkSpd = atkSpd;
	}

	/**
	 * @return the baseMAtkSpd
	 */
	public int getBaseMAtkSpd()
	{
		return _baseMAtkSpd;
	}

	public void setBaseMAtkSpd(int mAtkSpd)
	{
		_baseMAtkSpd = mAtkSpd;
	}

	/**
	 * @return the baseMReuseRate
	 */
	public float getBaseMReuseRate()
	{
		return _baseMReuseRate;
	}

	/**
	 * @return the baseShldDef
	 */
	public int getBaseShldDef()
	{
		return _baseShldDef;
	}

	/**
	 * @return the baseAtkRange
	 */
	public int getBaseAtkRange()
	{
		return _baseAtkRange;
	}

	/**
	 * @return the baseShldRate
	 */
	public int getBaseShldRate()
	{
		return _baseShldRate;
	}

	/**
	 * @return the baseCritRate
	 */
	public int getBaseCritRate()
	{
		return _baseCritRate;
	}

	public void setBaseCritRate(int critRate)
	{
		_baseCritRate = critRate;
	}

	/**
	 * @return the baseMCritRate
	 */
	public int getBaseMCritRate()
	{
		return _baseMCritRate;
	}

	public void setBaseMCritRate(int mCritRate)
	{
		_baseMCritRate = mCritRate;
	}

	/**
	 * @return the baseWalkSpd
	 */
	public float getBaseWalkSpd()
	{
		return _baseWalkSpd;
	}

	public void setBaseWalkSpd(int speed)
	{
		_baseWalkSpd = speed;
	}

	/**
	 * @return the baseRunSpd
	 */
	public float getBaseRunSpd()
	{
		return _baseRunSpd;
	}

	public void setBaseRunSpd(int speed)
	{
		_baseRunSpd = speed;
	}

	/**
	 * @return the baseBreath
	 */
	public int getBaseBreath()
	{
		return _baseBreath;
	}

	public void setBaseBreath(int breath)
	{
		_baseBreath = breath;
	}

	/**
	 * @return the baseAggression
	 */
	public int getBaseAggression()
	{
		return _baseAggression;
	}

	/**
	 * @return the baseBleed
	 */
	public int getBaseBleed()
	{
		return _baseBleed;
	}

	/**
	 * @return the basePoison
	 */
	public int getBasePoison()
	{
		return _basePoison;
	}

	/**
	 * @return the baseStun
	 */
	public int getBaseStun()
	{
		return _baseStun;
	}

	/**
	 * @return the baseRoot
	 */
	public int getBaseRoot()
	{
		return _baseRoot;
	}

	/**
	 * @return the baseMovement
	 */
	public int getBaseMovement()
	{
		return _baseMovement;
	}

	/**
	 * @return the baseConfusion
	 */
	public int getBaseConfusion()
	{
		return _baseConfusion;
	}

	/**
	 * @return the baseSleep
	 */
	public int getBaseSleep()
	{
		return _baseSleep;
	}

	/**
	 * @return the baseAggressionVuln
	 */
	public double getBaseAggressionVuln()
	{
		return _baseAggressionVuln;
	}

	/**
	 * @return the baseBleedVuln
	 */
	public double getBaseBleedVuln()
	{
		return _baseBleedVuln;
	}

	/**
	 * @return the basePoisonVuln
	 */
	public double getBasePoisonVuln()
	{
		return _basePoisonVuln;
	}

	/**
	 * @return the baseStunVuln
	 */
	public double getBaseStunVuln()
	{
		return _baseStunVuln;
	}

	/**
	 * @return the baseRootVuln
	 */
	public double getBaseRootVuln()
	{
		return _baseRootVuln;
	}

	/**
	 * @return the baseMovementVuln
	 */
	public double getBaseMovementVuln()
	{
		return _baseMovementVuln;
	}

	/**
	 * @return the baseSleepVuln
	 */
	public double getBaseSleepVuln()
	{
		return _baseSleepVuln;
	}

	/**
	 * @return the baseCritVuln
	 */
	public double getBaseCritVuln()
	{
		return _baseCritVuln;
	}

	/**
	 * @return the baseMCritVuln
	 */
	public double getBaseMCritVuln()
	{
		return _baseMCritVuln;
	}

	/**
	 * @return the baseMpConsumeRate
	 */
	public int getBaseMpConsumeRate()
	{
		return _baseMpConsumeRate;
	}

	/**
	 * @return the baseHpConsumeRate
	 */
	public int getBaseHpConsumeRate()
	{
		return _baseHpConsumeRate;
	}

	/**
	 * @return the collisionRadius
	 */
	public int getCollisionRadius(L2Character activeChar)
	{
		return _collisionRadius;
	}

	/**
	 * @return the collisionHeight
	 */
	public int getCollisionHeight(L2Character activeChar)
	{
		return _collisionHeight;
	}

    /**
     * @return the collisionRadius
     */
    public int getCollisionRadius()
    {
        return _collisionRadius;
    }

    /**
     * @return the collisionHeight
     */
    public int getCollisionHeight()
    {
        return _collisionHeight;
    }


    /**
	 * @return the fCollisionRadius
	 */
	public double getFCollisionRadius(L2Character activeChar)
	{
		return _fCollisionRadius;
	}

	/**
	 * @return the fCollisionHeight
	 */
	public double getFCollisionHeight(L2Character activeChar)
	{
		return _fCollisionHeight;
	}

	public double getNpcSizeMultiplier()
	{
		return _npcSizeMultiplier;
	}

	public void setBaseMatk(int matk)
	{
		_baseMAtk = matk;
	}

	public void setBaseAttackRange(int attackRange)
	{
		_baseAtkRange = attackRange;
	}

	/**
	 * Overridden in L2NpcTemplate
	 * @return
	 */
	public Map<Integer, L2Skill> getSkills()
	{
		return null;
	}
}