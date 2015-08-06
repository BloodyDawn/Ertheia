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
package dwo.gameserver.model.actor.stat;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.ExperienceTable;
import dwo.gameserver.datatables.xml.PetDataTable;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.world.zone.type.L2SwampZone;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class PlayableStat extends CharStat
{
	protected static final Logger _log = LogManager.getLogger(PlayableStat.class);

	public PlayableStat(L2Playable activeChar)
	{
		super(activeChar);
	}

	public boolean addExp(long value)
	{
		if(getExp() + value < 0L || value > 0L && getExp() == getExpForLevel(getMaxLevel()) - 1L)
		{
			return true;
		}

		if(getExp() + value >= getExpForLevel(getMaxLevel()))
		{
			value = getExpForLevel(getMaxLevel()) - 1L - getExp();
		}

		setExp(getExp() + value);

		byte minimumLevel = 1;
		if(getActiveChar().isPet())
		{
			// get minimum level from L2NpcTemplate
			minimumLevel = (byte) PetDataTable.getInstance().getPetMinLevel(((L2PetInstance) getActiveChar()).getTemplate().getNpcId());
		}

		byte level = minimumLevel; // minimum level

		for(byte tmp = level; tmp <= getMaxLevel(); tmp++)
		{
			if(getExp() >= getExpForLevel(tmp))
			{
				continue;
			}
			level = --tmp;
			break;
		}
		if(level != getLevel() && level >= minimumLevel)
		{
			addLevel((byte) (level - getLevel()));
		}

		return true;
	}

	public boolean removeExp(long value)
	{
		if(getExp() - value < 0)
		{
			value = getExp() - 1;
		}

		setExp(getExp() - value);

		byte minimumLevel = 1;
		if(getActiveChar().isPet())
		{
			// get minimum level from L2NpcTemplate
			minimumLevel = (byte) PetDataTable.getInstance().getPetMinLevel(((L2PetInstance) getActiveChar()).getTemplate().getNpcId());
		}
		byte level = minimumLevel;

		for(byte tmp = level; tmp <= getMaxLevel(); tmp++)
		{
			if(getExp() >= getExpForLevel(tmp))
			{
				continue;
			}
			level = --tmp;
			break;
		}
		if(level != getLevel() && level >= minimumLevel)
		{
			addLevel((byte) (level - getLevel()));
		}
		return true;
	}

	public boolean addExpAndSp(long addToExp, int addToSp)
	{
		boolean expAdded = false;
		boolean spAdded = false;
		if(addToExp >= 0)
		{
			expAdded = addExp(addToExp);
		}
		if(addToSp >= 0)
		{
			spAdded = addSp(addToSp);
		}

		return expAdded || spAdded;
	}

	public boolean removeExpAndSp(long removeExp, int removeSp)
	{
		boolean expRemoved = false;
		boolean spRemoved = false;
		if(removeExp > 0)
		{
			expRemoved = removeExp(removeExp);
		}
		if(removeSp > 0)
		{
			spRemoved = removeSp(removeSp);
		}

		return expRemoved || spRemoved;
	}

	public boolean addLevel(byte value)
	{
		if(getLevel() + value > getMaxLevel() - 1)
		{
			if(getLevel() < getMaxLevel() - 1)
			{
				value = (byte) (getMaxLevel() - 1 - getLevel());
			}
			else
			{
				return false;
			}
		}

		boolean levelIncreased = getLevel() + value > getLevel();

		// Добавляет CRP для членов клана 40+ уровня (клан уровня 5+)
		if(levelIncreased && value + getLevel() >= Config.REPUTATION_BONUS_MIN_LEVEL &&
			getActiveChar() instanceof L2PcInstance &&
			((L2PcInstance) getActiveChar()).getClan() != null &&
			((L2PcInstance) getActiveChar()).getClan().getLevel() >= 5 && !((L2PcInstance) getActiveChar()).isAcademyMember())
		{
			String prevRewardLevel = ((L2PcInstance) getActiveChar()).getVariablesController().get("clanLvlAddCrpBonus_ci" + ((L2PcInstance) getActiveChar()).getClassIndex());
			if(prevRewardLevel != null)
			{
				int prevLevelDate = Integer.parseInt(prevRewardLevel);
				if(prevLevelDate < getLevel() + value)
				{
					for(byte i = 0; i < value; ++i)
					{
						int newLevel = getLevel() + i + 1;
						int crpToAdd = 0;

						if(newLevel >= 40 && newLevel <= 67)
						{
							crpToAdd = (newLevel - 40) / 3 + 5;
						}
						else if(newLevel >= 68 && newLevel <= 84)
						{
							crpToAdd = (newLevel - 68) / 2 + 15;
						}
						else if(newLevel >= 85 && newLevel <= 88)
						{
							crpToAdd = (newLevel - 85 << 1) + 90;
						}
						else if(newLevel == 89)
						{
							crpToAdd = 99;
						}
						else if(newLevel == 90)
						{
							crpToAdd = 115;
						}
						else if(newLevel == 91)
						{
							crpToAdd = 118;
						}
						else if(newLevel >= 92 && newLevel <= 94)
						{
							crpToAdd = (getLevel() - 92) * 3 + 120;
						}
						else if(newLevel >= 95 && newLevel <= 98)
						{
							crpToAdd = (getLevel() - 95 << 2) + 180;
						}
						else if(newLevel == 99)
						{
							crpToAdd = 745;
						}

						if(crpToAdd > 0)
						{
							// Добавляем репутацию клану и сохраняем в базу
							((L2PcInstance) getActiveChar()).getClan().addReputationScore(crpToAdd, false);
							((L2PcInstance) getActiveChar()).getClan().updateClanScoreInDB();

							// Шлем соответсвующее сообщение членам клана
							SystemMessage sm = SystemMessage.getSystemMessage(4028);
							sm.addPcName((L2PcInstance) getActiveChar());
							sm.addNumber(crpToAdd);
							((L2PcInstance) getActiveChar()).getClan().broadcastToOnlineMembers(sm);
						}
					}
				}
			}
		}

		value += getLevel();
		setLevel(value);

		// Sync up exp with current level
		if(getExp() >= getExpForLevel(getLevel() + 1) || getExpForLevel(getLevel()) > getExp())
		{
			setExp(getExpForLevel(getLevel()));
		}

		if(!levelIncreased && getActiveChar() instanceof L2PcInstance && !getActiveChar().isGM() && Config.DECREASE_SKILL_LEVEL)
		{
			((L2PcInstance) getActiveChar()).checkPlayerSkills();
		}

		if(!levelIncreased)
		{
			return false;
		}

		getActiveChar().getStatus().setCurrentHp(getActiveChar().getStat().getMaxHp());
		getActiveChar().getStatus().setCurrentMp(getActiveChar().getStat().getMaxMp());
		//TODO: AwakeningManager.handleAwake((L2PcInstance)getActiveChar(),true);
		return true;
	}

	public boolean addSp(int value)
	{
		if(value < 0)
		{
			_log.log(Level.WARN, "wrong usage");
			return false;
		}
		int currentSp = getSp();
		if(currentSp == Integer.MAX_VALUE)
		{
			return false;
		}

		if(currentSp > Integer.MAX_VALUE - value)
		{
			value = Integer.MAX_VALUE - currentSp;
		}

		setSp(currentSp + value);
		return true;
	}

	public boolean removeSp(int value)
	{
		int currentSp = getSp();
		if(currentSp < value)
		{
			value = currentSp;
		}
		setSp(getSp() - value);
		return true;
	}

	public long getExpForLevel(int level)
	{
		return level;
	}

	@Override
	public L2Playable getActiveChar()
	{
		return (L2Playable) super.getActiveChar();
	}

	@Override
	public int getRunSpeed()
	{
		int val = super.getRunSpeed();

		if(getActiveChar().isInsideZone(L2Character.ZONE_WATER))
		{
			val /= 2;
		}

		if(getActiveChar().isInsideZone(L2Character.ZONE_SWAMP))
		{
			L2SwampZone zone = ZoneManager.getInstance().getZone(getActiveChar(), L2SwampZone.class);
			int bonus = zone == null ? 0 : zone.getMoveBonus();
			double dbonus = bonus / 100.0; //%
			val += val * dbonus;
		}

		return val;
	}

	public int getMaxLevel()
	{
		return ExperienceTable.getInstance().getMaxLevel();
	}
}
