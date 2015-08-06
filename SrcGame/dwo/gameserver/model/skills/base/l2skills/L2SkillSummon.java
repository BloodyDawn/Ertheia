package dwo.gameserver.model.skills.base.l2skills;

import dwo.gameserver.datatables.sql.CharSummonTable;
import dwo.gameserver.datatables.xml.ExperienceTable;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SummonPointsTable;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2CubicInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2SiegeSummonInstance;
import dwo.gameserver.model.actor.instance.L2SummonInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.util.Rnd;
import org.apache.log4j.Level;

public class L2SkillSummon extends L2Skill
{
	private final boolean _isCubic;
	// cubic AI
	// Power for a cubic
	private final int _cubicPower;
	// Activation time for a cubic
	private final int _activationtime;
	// Activation chance for a cubic.
	private final int _activationchance;
	// Maximum casts made by the cubic until it goes idle.
	private final int _maxcount;
	// What is the total lifetime of summons (in millisecs)
	private final int _summonTotalLifeTime;
	// How much lifetime is lost per second of idleness (non-fighting)
	private final int _summonTimeLostIdle;
	// How much time is lost per second of activity (fighting)
	private final int _summonTimeLostActive;
	// Inherit elementals from master
	private final boolean _inheritElementals;
	private final double _elementalSharePercent;
	private final int _skillToCast;
	private final int _skillToCastLevel;
	private final String _summonGroupReplace;
	// Разрешить или запретить передвижение нпц
	private final boolean _isMovementDisable;
	private float _expPenalty;

	public L2SkillSummon(StatsSet set)
	{
		super(set);
		_expPenalty = set.getFloat("expPenalty", 0.0f);
		_isCubic = set.getBool("isCubic", false);
		_cubicPower = set.getInteger("cubicPower", 0);
		_activationtime = set.getInteger("activationtime", 8);
		_activationchance = set.getInteger("activationChance", 30);
		_maxcount = set.getInteger("maxcount", -1);
		_summonTotalLifeTime = set.getInteger("summonTotalLifeTime", 1200000);  // 20 minutes default
		_summonTimeLostIdle = set.getInteger("summonTimeLostIdle", 0);
		_summonTimeLostActive = set.getInteger("summonTimeLostActive", 0);
		_inheritElementals = set.getBool("inheritElementals", false);
		_elementalSharePercent = set.getDouble("inheritPercent", 1);
		_skillToCast = set.getInteger("skillToCast", -1);
		_skillToCastLevel = set.getInteger("skillToCastLevel", 1);
		_summonGroupReplace = set.getString("summonGroupReplace", null);
		_isMovementDisable = set.getBool("isMovementDisable", false);
	}

	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if(caster.isAlikeDead() || !caster.isPlayer())
		{
			return;
		}

		L2PcInstance activeChar = (L2PcInstance) caster;

		if(getNpcId() == 0)
		{
			activeChar.sendMessage("Summon skill " + getId() + " not described yet");
			return;
		}

		if(_isCubic)
		{
			// Gnacik :
			// If skill is enchanted calculate cubic skill level based on enchant
			// 8 at 101 (+1 Power)
			// 12 at 130 (+30 Power)
			// Because 12 is max 5115-5117 skills
			// TODO: make better method of calculation, dunno how its calculated on offi
			int _cubicSkillLevel = getLevel();
			if(_cubicSkillLevel > 100)
			{
				_cubicSkillLevel = (getLevel() - 100) / 7 + 8;
			}

			L2PcInstance player = null;
			int mastery = 0;
			L2CubicInstance cubic = null;
			switch(getTargetType())
			{
				case TARGET_ONE:
					L2Object target = targets[0];

					if(!(target instanceof L2PcInstance))
					{
						return;
					}

					player = (L2PcInstance) target;
					mastery = player.getCubicMastery();

					// Если такой кубик есть уже на чаре - удаляем его
					cubic = player.getCubic(getNpcId());
					if(cubic != null)
					{
						cubic.stopAction();
						cubic.cancelDisappear();
						player.getCubics().remove(cubic);
					}

					if(mastery > 0)
					{
						if(player.getCubics().size() == mastery)
						{
							cubic = player.getCubics().remove(0);
							if(cubic != null)
							{
								cubic.stopAction();
								cubic.cancelDisappear();
							}
						}
						else if(player.getCubics().size() > mastery)
						{
							_log.log(Level.WARN, "Player " + player + " has more cubic than mastery allowes.");
						}
					}
					if(player.equals(activeChar))
					{
						player.addCubic(getNpcId(), _cubicSkillLevel, _cubicPower, _activationtime, _activationchance, _maxcount, _summonTotalLifeTime, false);
					}
					else // given by other player
					{
						player.addCubic(getNpcId(), _cubicSkillLevel, _cubicPower, _activationtime, _activationchance, _maxcount, _summonTotalLifeTime, true);
					}
					player.broadcastUserInfo();
					break;

				case TARGET_SELF:
					for(L2Object obj : targets)
					{
						if(!(obj instanceof L2PcInstance))
						{
							continue;
						}

						player = (L2PcInstance) obj;
						mastery = player.getCubicMastery();

						// Если такой кубик есть уже на чаре - удаляем его
						cubic = player.getCubic(getNpcId());
						if(cubic != null)
						{
							cubic.stopAction();
							cubic.cancelDisappear();
							player.getCubics().remove(cubic);
						}

						if(mastery > 0)
						{
							if(player.getCubics().size() == mastery)
							{
								cubic = player.getCubics().remove(0);
								if(cubic != null)
								{
									cubic.stopAction();
									cubic.cancelDisappear();
								}
							}
							else if(player.getCubics().size() > mastery)
							{
								_log.log(Level.WARN, "Player " + player + " has more cubic than mastery allowes.");
							}
						}

						if(player.equals(activeChar))
						{
							activeChar.addCubic(getNpcId(), _cubicSkillLevel, _cubicPower, _activationtime, _activationchance, _maxcount, _summonTotalLifeTime, false);
							activeChar.broadcastUserInfo();
						}
						else
						{
							player.addCubic(getNpcId(), _cubicSkillLevel, _cubicPower, _activationtime, _activationchance, _maxcount, _summonTotalLifeTime, true);
							player.broadcastUserInfo();
						}
					}
					break;

				default: // Например TARGET_PARTY
					for(L2Object obj : targets)
					{
						if(!(obj instanceof L2PcInstance))
						{
							continue;
						}

						player = (L2PcInstance) obj;
						mastery = player.getCubicMastery();

						// Если такой кубик есть уже на чаре - удаляем его
						cubic = player.getCubic(getNpcId());
						if(cubic != null)
						{
							cubic.stopAction();
							cubic.cancelDisappear();
							player.getCubics().remove(cubic);
						}

						if(mastery > 0)
						{
							if(player.getCubics().size() == mastery)
							{
								cubic = player.getCubics().remove(0);
								if(cubic != null)
								{
									cubic.stopAction();
									cubic.cancelDisappear();
								}
							}
							else if(player.getCubics().size() > mastery)
							{
								_log.log(Level.WARN, "Player " + player + " has more cubic than mastery allowes.");
							}
						}
						if(player.equals(activeChar))
						{
							player.addCubic(getNpcId(), _cubicSkillLevel, _cubicPower, _activationtime, _activationchance, _maxcount, _summonTotalLifeTime, false);
						}
						else // given by other player
						{
							player.addCubic(getNpcId(), _cubicSkillLevel, _cubicPower, _activationtime, _activationchance, _maxcount, _summonTotalLifeTime, true);
						}
						player.broadcastUserInfo();
					}
					break;
			}
			// Скил, вызывающий кубик хиллера имеет так-же и эффекты
			if(hasEffects())
			{
				for(L2Object obj : targets)
				{
					if(obj instanceof L2Character)
					{
						getEffects(caster, (L2Character) obj);
					}
				}
			}
			return;
		}

		if(activeChar.isMounted())
		{
			return;
		}

		if(activeChar.getObserverController().isObserving())
		{
			return;
		}

		int summonReqPoints = SummonPointsTable.getInstance().getPointsForSummonId(getNpcId());

		CharSummonTable.getInstance().restoreSummon(activeChar, 0);

		// Если призван старый пет (поглащает все очки призыва), то нельзя призвать любого другого
		if(summonReqPoints == -1)
		{
			if(!activeChar.getPets().isEmpty())
			{
				boolean replace = false;
				// Быстрая замена пета ( используется для замены дерева и огонька )
				if(_summonGroupReplace != null)
				{
					for(L2Summon pets : activeChar.getPets())
					{
						if(pets instanceof L2SummonInstance)
						{
							String group = ((L2SummonInstance) pets).getSummonGroupReplace();
							if(group != null && _summonGroupReplace.equals(group))
							{
								pets.getLocationController().delete();
								replace = true;
							}
						}
					}
				}

				if(!replace)
				{
					activeChar.sendPacket(SystemMessageId.YOU_ALREADY_HAVE_A_PET);
					return;
				}
			}
		}

		// Запрещаем вызов, если восстановленные саммоны захавали все саммон поинты :)
		if(activeChar.getMaxSummonPoints() - activeChar.getUsedSummonPoints() < summonReqPoints)
		{
			activeChar.sendPacket(SystemMessageId.YOU_ALREADY_HAVE_A_PET);
			return;
		}

		L2SummonInstance summon;
		L2NpcTemplate summonTemplate = NpcTable.getInstance().getTemplate(getNpcId());
		if(summonTemplate == null)
		{
			_log.log(Level.WARN, "Summon attempt for nonexisting NPC ID:" + getNpcId() + ", skill ID:" + getId());
			return; // npcID doesn't exist
		}
		summon = summonTemplate.isType("L2SiegeSummon") ? new L2SiegeSummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, this) : new L2SummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, this);

		summon.setName(summonTemplate.getName());
		summon.setTitle(activeChar.getName());
		summon.setExpPenalty(_expPenalty);
		summon.setSharedElementals(_inheritElementals);
		summon.setSharedElementalsValue(_elementalSharePercent);
		summon.setSummonGroupReplace(_summonGroupReplace);

		if(summon.getLevel() >= ExperienceTable.getInstance().getMaxPetLevel())
		{
			summon.getStat().setExp(ExperienceTable.getInstance().getExpForLevel(ExperienceTable.getInstance().getMaxPetLevel() - 1));
			_log.log(Level.WARN, "Summon (" + summon.getName() + ") NpcID: " + summon.getNpcId() + " has a level above " + ExperienceTable.getInstance().getMaxPetLevel() + ". Please rectify.");
		}
		else
		{
			summon.getStat().setExp(ExperienceTable.getInstance().getExpForLevel(summon.getLevel() % ExperienceTable.getInstance().getMaxPetLevel()));
		}

		summon.setHeading(activeChar.getHeading());
		summon.setRunning();
		activeChar.addPet(summon);
		summon.getLocationController().spawn(activeChar.getX() + Rnd.get(-120, 120), activeChar.getY() + Rnd.get(-120, 120), activeChar.getZ());

		// Необходима для восстоновления 100% после переноса бафов от хозяина
		summon.setCurrentHp(summon.getMaxHp());
		summon.setCurrentMp(summon.getMaxMp());

		/**
		 * Для новых саммонов типа Дерева Жизни, которые ничего не умеют, кроме как кастовать скилл на автомате.
		 */
		if(_skillToCast > 0)
		{
			summon.startSkillCastingTask(_skillToCast, _skillToCastLevel);
		}

		activeChar.ssUncharge(this);
	}

	public boolean isCubic()
	{
		return _isCubic;
	}

	public int getSkillToCast()
	{
		return _skillToCast;
	}

	public int getSkillToCastLevel()
	{
		return _skillToCastLevel;
	}

	public int getTotalLifeTime()
	{
		return _summonTotalLifeTime;
	}

	public int getTimeLostIdle()
	{
		return _summonTimeLostIdle;
	}

	public int getTimeLostActive()
	{
		return _summonTimeLostActive;
	}

	public float getExpPenalty()
	{
		return _expPenalty;
	}

	public boolean getInheritElementals()
	{
		return _inheritElementals;
	}

	public double getElementalSharePercent()
	{
		return _elementalSharePercent;
	}

	public String getSummonGroupReplace()
	{
		return _summonGroupReplace;
	}

	public boolean isMovementDisable()
	{
		return _isMovementDisable;
	}
}