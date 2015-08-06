package dwo.gameserver.model.actor.stat;

import dwo.config.Config;
import dwo.config.scripts.ConfigWorldStatistic;
import dwo.gameserver.datatables.xml.ExperienceTable;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.network.game.masktypes.UserInfoType;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.worldstat.CategoryType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ability.ExAcquireAPSkillList;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExVitalityEffectInfo;
import dwo.gameserver.network.game.serverpackets.packet.info.UI;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowMemberListUpdate;
import dwo.gameserver.util.Util;
import dwo.scripts.services.Tutorial;

public class PcStat extends PlayableStat
{
	public static final int MAX_VITALITY_POINTS = 140000;
	public static final int MIN_VITALITY_POINTS = 0;
	private int _oldMaxHp; // stats watch
	private int _oldMaxMp; // stats watch
	private int _oldMaxCp; // stats watch

	public PcStat(L2PcInstance activeChar)
	{
		super(activeChar);
	}

	@Override
	public boolean addExp(long value)
	{
		L2PcInstance activeChar = getActiveChar();

		// Может-ли персонаж получать опыт?
		if(!getActiveChar().getAccessLevel().canGainExp())
		{
			return false;
		}

		// Персонажи в состоянии ПК не могут получать опыт
		// На мобах можно только восстанавливать репутацию
		if(!activeChar.isCursedWeaponEquipped() && activeChar.hasBadReputation())
		{
			int reputationRestore = activeChar.calculateReputationRestore(value);
			if(reputationRestore > 0)
			{
				activeChar.setReputation(activeChar.getReputation() + reputationRestore);

				// Если персонаж восстановил свою репутацию, сбрасываем ему флаг о убийстве
				// (который не дает получить репутацию за килл ПК более чем одному персонажу)
				if(activeChar.getReputation() >= 0)
				{
					activeChar.getVariablesController().set("pkKilled", false);
				}
			}
			return false;
		}

		if(!super.addExp(value))
		{
			return false;
		}

		activeChar.sendUserInfo();
		if(value > 0)
		{
			if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
			{
				activeChar.updateWorldStatistic(CategoryType.EXP_ADDED, null, (long) (value / Config.RATE_XP));
			}
		}
		return true;
	}

	/**
	 * Add Experience and SP rewards to the L2PcInstance, remove its Karma (if necessary) and Launch increase level task.<BR><BR>
	 * <p/>
	 * <B><U> Actions </U> :</B><BR><BR>
	 * <li>Remove Karma when the player kills L2MonsterInstance</li>
	 * <li>Send a ServerMode->Client packet StatusUpdate to the L2PcInstance</li>
	 * <li>Send a ServerMode->Client System Message to the L2PcInstance </li>
	 * <li>If the L2PcInstance increases it's level, send a ServerMode->Client packet SocialAction (broadcast) </li>
	 * <li>If the L2PcInstance increases it's level, manage the increase level task (Max MP, Max MP, Recommandation, Expertise and beginner skills...) </li>
	 * <li>If the L2PcInstance increases it's level, send a ServerMode->Client packet UserInfo to the L2PcInstance </li><BR><BR>
	 *
	 * @param addToExp The Experience value to add
	 * @param addToSp  The SP value to add
	 */
	@Override
	public boolean addExpAndSp(long addToExp, int addToSp)
	{
		return addExpAndSp(addToExp, addToSp, false);
	}

	@Override
	public boolean removeExpAndSp(long addToExp, int addToSp)
	{
		return removeExpAndSp(addToExp, addToSp, true);
	}

	@Override
	public boolean addLevel(byte value)
	{
		if(getLevel() + value > ExperienceTable.getInstance().getMaxLevel() - 1)
		{
			return false;
		}

		boolean levelIncreased = super.addLevel(value);

		if(levelIncreased)
		{
			if(!Config.DISABLE_TUTORIAL)
			{
				QuestState qs = getActiveChar().getQuestState(Tutorial.class);
				if(qs != null)
				{
					qs.getQuest().notifyEvent("CE40", null, getActiveChar());
				}
			}

			getActiveChar().setCurrentCp(getMaxCp());
			getActiveChar().broadcastPacket(new SocialAction(getActiveChar().getObjectId(), SocialAction.LEVEL_UP));
			getActiveChar().sendPacket(SystemMessageId.YOU_INCREASED_YOUR_LEVEL);

			// TODO: Запрос на пробуждение в хук
			HookManager.getInstance().notifyEvent(HookType.ON_LEVEL_INCREASE, getActiveChar().getHookContainer(), getActiveChar());
		}

		getActiveChar().rewardSkills(); // Give Expertise skill of this level
		if(getActiveChar().getClan() != null)
		{
			getActiveChar().getClan().updateClanMember(getActiveChar());
			getActiveChar().getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(getActiveChar()));
		}
		if(getActiveChar().isInParty())
		{
			getActiveChar().getParty().recalculatePartyLevel(); // Recalculate the party level
		}

		if(getActiveChar().isTransformed() || getActiveChar().isInStance())
		{
			getActiveChar().getTransformation().onLevelUp();
		}

		StatusUpdate su = new StatusUpdate(getActiveChar());
		su.addAttribute(StatusUpdate.LEVEL, getLevel());
		su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		getActiveChar().sendPacket(su);

		// Update the overloaded status of the L2PcInstance
		getActiveChar().refreshOverloaded();
		// Update the expertise status of the L2PcInstance
		getActiveChar().refreshExpertisePenalty();
		// Send a ServerMode->Client packet UserInfo to the L2PcInstance
		getActiveChar().sendUserInfo();
		// Check pet and broadcast his status if level should be synced
		if(!getActiveChar().getPets().isEmpty())
		{
			getActiveChar().getPets().stream().filter(pet -> pet instanceof L2PetInstance).forEach(pet -> {
				L2PetInstance _pet = (L2PetInstance) pet;
				if(_pet.getPetData().syncLevel())
				{
					_pet.getStat().setLevel(getLevel());
					_pet.getStat().setExp(_pet.getStat().getExpForLevel(getLevel()));
					_pet.broadcastPacket(new SocialAction(getActiveChar().getObjectId(), SocialAction.LEVEL_UP));
					_pet.updateAndBroadcastStatus(1);
				}
			});
		}

        if ((getLevel() == ExperienceTable.getInstance().getMaxLevel()) && getActiveChar().isNoble())
        {
            getActiveChar().sendPacket(new ExAcquireAPSkillList(getActiveChar()));
        }
		return levelIncreased;
	}

	@Override
	public boolean addSp(int value)
	{
		if(!super.addSp(value))
		{
			return false;
		}

        UI ui = new UI(getActiveChar(), false);
        ui.addComponentType(UserInfoType.CURRENT_HPMPCP_EXP_SP);
        getActiveChar().sendPacket(ui);

		return true;
	}

	@Override
	public long getExpForLevel(int level)
	{
		return ExperienceTable.getInstance().getExpForLevel(level);
	}

	@Override
	public L2PcInstance getActiveChar()
	{
		return (L2PcInstance) super.getActiveChar();
	}

	@Override
	public int getRunSpeed()
	{
		if(getActiveChar() == null)
		{
			return 1;
		}

		int val;

		L2PcInstance player = getActiveChar();
		if(player.isMounted())
		{
			float baseRunSpd = NpcTable.getInstance().getTemplate(getActiveChar().getMountNpcId()).getBaseRunSpd();
			val = (int) Math.round(calcStat(Stats.RUN_SPEED, baseRunSpd, null, null));
		}
		else if(player.isFlying())
		{
			val = player.getBaseTemplate().getBaseCharTemplate().getStats().getFlyRunSpd();
		}
		else
		{
			val = player.isInWater() ? player.getBaseTemplate().getBaseCharTemplate().getStats().getWaterRunSpd() : super.getRunSpeed();
		}

		val += Config.RUN_SPD_BOOST;

		// Apply max run speed cap.
		if(val > Config.MAX_RUN_SPEED && !getActiveChar().isGM())
		{
			return Config.MAX_RUN_SPEED;
		}

		return val;
	}

	public boolean addExpAndSp(long addToExp, int addToSp, boolean useBonuses)
	{
		L2PcInstance activeChar = getActiveChar();

		// Allowed to gain exp/sp?
		if(!activeChar.getAccessLevel().canGainExp())
		{
			return false;
		}

		if(activeChar.isAbleToGainExp())
		{
			return false;
		}

		long baseExp = addToExp;
		int baseSp = addToSp;

		double bonus = 1.0;

		if(useBonuses)
		{
			bonus = activeChar.getExpBonusMultiplier();
		}

		addToExp *= bonus;
		addToSp *= bonus;

		float ratioTakenByPlayer = 0;

		// if this player has a pet that takes from the owner's Exp, give the pet Exp now
		if(!activeChar.getPets().isEmpty())
		{
			for(L2Summon pet : activeChar.getPets())
			{
				if(pet.isPet() && Util.checkIfInShortRadius(Config.ALT_PARTY_RANGE, activeChar, pet, false))
				{
					ratioTakenByPlayer = +((L2PetInstance) pet).getPetLevelData().getOwnerExpTaken() / 100.0f;
					// only give exp/sp to the pet by taking from the owner if the pet has a non-zero, positive ratio
					// allow possible customizations that would have the pet earning more than 100% of the owner's exp/sp
					if(ratioTakenByPlayer > 1)
					{
						ratioTakenByPlayer = 1;
					}
					if(!pet.isDead())
					{
						pet.addExpAndSp((long) (addToExp * (1 - ratioTakenByPlayer)), (int) (addToSp * (1 - ratioTakenByPlayer)));
					}

					// now adjust the max ratio to avoid the owner earning negative exp/sp
					addToExp = +(long) (addToExp * ratioTakenByPlayer);
					addToSp = +(int) (addToSp * ratioTakenByPlayer);
				}
			}
		}

		if(!super.addExpAndSp(addToExp, addToSp))
		{
			return false;
		}

		SystemMessage sm;
		if(addToExp == 0 && addToSp != 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_SP);
			sm.addNumber(addToSp);
		}
		else if(addToSp == 0 && addToExp != 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_EXPERIENCE);
			sm.addNumber((int) addToExp);
		}
		else
		{
			if(addToExp - baseExp > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_EARNED_S1_EXP_BONUS_S2_AND_S3_SP_BONUS_S4);
				if(addToExp > Integer.MAX_VALUE)
				{
					sm.addNumber(Integer.MAX_VALUE);
					sm.addNumber(Integer.MAX_VALUE - (int) baseExp);
				}
				else
				{
					sm.addNumber((int) addToExp);
					sm.addNumber((int) (addToExp - baseExp));
				}
				sm.addNumber(addToSp);
				sm.addNumber(addToSp - baseSp);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_EARNED_S1_EXP_AND_S2_SP);
				if(addToExp > Integer.MAX_VALUE)
				{
					sm.addNumber(Integer.MAX_VALUE);
				}
				else
				{
					sm.addNumber((int) addToExp);
				}
				sm.addNumber(addToSp);
			}
		}
		activeChar.sendPacket(sm);
		return true;
	}

	public boolean removeExpAndSp(long addToExp, int addToSp, boolean sendMessage)
	{
		byte level = getLevel();
		if(!super.removeExpAndSp(addToExp, addToSp))
		{
			return false;
		}

		if(sendMessage)
		{
			getActiveChar().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EXP_DECREASED_BY_S1).addNumber((int) addToExp));
			getActiveChar().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1).addNumber(addToSp));
			if(getLevel() < level)
			{
				getActiveChar().broadcastStatusUpdate();
			}
		}
		return true;
	}

	public long getExpBaseClass()
	{
		return super.getExp();
	}

	public byte getLevelBaseClass()
	{
		return super.getLevel();
	}

	public int getSpBaseClass()
	{
		return super.getSp();
	}

	@Override
	public int getEvasionRate(L2Character target)
	{
		int val = super.getEvasionRate(target);

		if(val > Config.MAX_EVASION_RATE && !getActiveChar().isGM())
		{
			return Config.MAX_EVASION_RATE;
		}

		return val;
	}

	@Override
	public int getMagicalEvasionRate(L2Character target)
	{
		int val = super.getMagicalEvasionRate(target);

		if(val > Config.MAX_EVASION_RATE && !getActiveChar().isGM())
		{
			val = Config.MAX_EVASION_RATE;
		}
		return val;
	}

	@Override
	public long getExp()
	{
		if(getActiveChar().isSubClassActive())
		{
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getExp();
		}

		return super.getExp();
	}

	@Override
	public void setExp(long value)
	{
		if(getActiveChar().isSubClassActive())
		{
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setExp(value);
		}
		else
		{
			super.setExp(value);
		}
	}

	@Override
	public byte getLevel()
	{
		if(getActiveChar().isSubClassActive())
		{
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getLevel();
		}

		return super.getLevel();
	}

	@Override
	public void setLevel(byte value)
	{
		if(value > ExperienceTable.getInstance().getMaxLevel() - 1)
		{
			value = (byte) (ExperienceTable.getInstance().getMaxLevel() - 1);
		}

		if(getActiveChar().isSubClassActive())
		{
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setLevel(value);
		}
		else
		{
			super.setLevel(value);
		}
	}

	@Override
	public int getMaxCp()
	{
		// Get the Max CP (base+modifier) of the L2PcInstance
		int val = super.getMaxCp();
		if(val != _oldMaxCp)
		{
			_oldMaxCp = val;

			// Launch a regen task if the new Max CP is higher than the old one
			if(getActiveChar().getStatus().getCurrentCp() != val)
			{
				getActiveChar().getStatus().setCurrentCp(getActiveChar().getStatus().getCurrentCp()); // trigger start of regeneration
			}
		}
		return val;
	}

	@Override
	public int getMaxHp()
	{
		// Get the Max HP (base+modifier) of the L2PcInstance
		int val = super.getMaxHp();
		if(val != _oldMaxHp)
		{
			_oldMaxHp = val;

			// Launch a regen task if the new Max HP is higher than the old one
			if(getActiveChar().getStatus().getCurrentHp() != val)
			{
				getActiveChar().getStatus().setCurrentHp(getActiveChar().getStatus().getCurrentHp()); // trigger start of regeneration
			}
		}

		return val;
	}

	@Override
	public int getMaxMp()
	{
		// Get the Max MP (base+modifier) of the L2PcInstance
		int val = super.getMaxMp();

		if(val != _oldMaxMp)
		{
			_oldMaxMp = val;

			// Launch a regen task if the new Max MP is higher than the old one
			if(getActiveChar().getStatus().getCurrentMp() != val)
			{
				getActiveChar().getStatus().setCurrentMp(getActiveChar().getStatus().getCurrentMp()); // trigger start of regeneration
			}
		}

		return val;
	}

	@Override
	public int getMAtkSpd()
	{
		int val = super.getMAtkSpd();

		if(val > Config.MAX_MATK_SPEED && !getActiveChar().isGM())
		{
			return Config.MAX_MATK_SPEED;
		}

		return val;
	}

	@Override
	public float getMovementSpeedMultiplier()
	{
		if(getActiveChar() == null)
		{
			return 1;
		}

		if(getActiveChar().isMounted())
		{
			return getRunSpeed() * 1.0f / NpcTable.getInstance().getTemplate(getActiveChar().getMountNpcId()).getBaseRunSpd();
		}

		return super.getMovementSpeedMultiplier();
	}

	@Override
	public int getPAtkSpd()
	{
		int val = super.getPAtkSpd();

		if(val > Config.MAX_PATK_SPEED && !getActiveChar().isGM())
		{
			return Config.MAX_PATK_SPEED;
		}

		return val;
	}

	@Override
	public int getSp()
	{
		if(getActiveChar().isSubClassActive())
		{
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getSp();
		}

		return super.getSp();
	}

	@Override
	public void setSp(int value)
	{
		if(getActiveChar().isSubClassActive())
		{
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setSp(value);
		}
		else
		{
			super.setSp(value);
		}
	}

	@Override
	public int getWalkSpeed()
	{
		if(getActiveChar() == null)
		{
			return 1;
		}

		return getRunSpeed() * 70 / 100;
	}

	/**
	 * @return current vitality points in integer format
	 */
	public int getVitalityPoints()
	{
		return getActiveChar().getVitalityDataForCurrentClassIndex().getVitalityPoints();
	}

	/**
	 * Устанавливает указанное количество очков виталити игроку
	 *
	 * @param points количество очков виталити
	 */
	public void setVitalityPoints(int points)
	{
		points = Math.min(Math.max(points, MIN_VITALITY_POINTS), MAX_VITALITY_POINTS);
		if(points == getVitalityPoints())
		{
			return;
		}
		getActiveChar().getVitalityDataForCurrentClassIndex().setVitalityPoints(points);
		getActiveChar().sendPacket(new ExVitalityEffectInfo(getActiveChar()));
	}

	public void updateVitalityPoints(float points, int killedlevel, boolean useRates, boolean quiet)
	{
		synchronized(this)
		{
			if(points == 0 || !Config.ENABLE_VITALITY)
			{
				return;
			}

			if(useRates)
			{
				byte level = getLevel();
				if(level < 10)
				{
					return;
				}

				if(points < 0) // vitality consumed
				{
					int stat = (int) calcStat(Stats.VITALITY_CONSUME_RATE, 1, getActiveChar(), null);

					// is vitality consumption stopped ?
					if(stat == 0)
					{
						return;
					}
					// is vitality gained ?
					if(stat < 0)
					{
						points = -points;
					}
				}

				if(points > 0)
				{
					double lvldiff = 1;
					if(Config.DIVIDE_VITALITY_GAIN_BYLEVEL && killedlevel > 0)
					{
						// Allow 5 lvl difference.
						// Mob with 5 lvl less than player will give still * 1.0
						// Higher difference will make vitality gain lower
						lvldiff = Math.min(1, Math.pow((double) (killedlevel + 5) / level, 3));

						points *= lvldiff;
					}
					// vitality increased
					points *= Config.RATE_VITALITY_GAIN;

					if(getActiveChar().isDebug())
					{
						getActiveChar().sendDebugMessage("Gained " + String.format("%1.2f", points) + " vit points, level diff " + String.format("%1.2f", lvldiff));
					}
				}
				else
				{
					// vitality decreased
					points *= Config.RATE_VITALITY_LOST;

					if(getActiveChar().isDebug())
					{
						getActiveChar().sendDebugMessage("Loosed " + String.format("%1.2f", Math.abs(points)) + " vitality points");
					}
				}
			}

			points = points > 0 ? Math.min(getVitalityPoints() + points, MAX_VITALITY_POINTS) : Math.max(getVitalityPoints() + points, MIN_VITALITY_POINTS);

			if(points == getVitalityPoints())
			{
				return;
			}

			setVitalityPoints((int) points);
		}
	}

	public double getVitalityMultiplier()
	{
		double vitality = 1.0;

		if(Config.ENABLE_VITALITY)
		{
			vitality = Config.RATE_VITALITY;
		}
		return vitality;
	}

	public double getExpBonusMultiplier()
	{
		double bonus = 1.0;
		double bonusExp = 1.0;

		// Bonus exp from skills
		bonusExp = 1 + calcStat(Stats.BONUS_EXP, 0, null, null) / 100;

		if(bonusExp > 1)
		{
			bonus += bonusExp - 1;
		}

		// Check for abnormal bonuses
		bonus = Math.max(bonus, 1);
		bonus = Math.min(bonus, Config.MAX_BONUS_EXP);
		return bonus;
	}

	public double getSpBonusMultiplier()
	{
		double bonus = 1.0;
		double bonusSp = 1.0;

		// Bonus exp from skills
		bonusSp = 1 + calcStat(Stats.BONUS_SP, 0, null, null) / 100;

		if(bonusSp > 1)
		{
			bonus += bonusSp - 1;
		}

		// Check for abnormal bonuses
		bonus = Math.max(bonus, 1);
		bonus = Math.min(bonus, Config.MAX_BONUS_EXP);

		return bonus;
	}
}