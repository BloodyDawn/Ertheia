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
package dwo.gameserver.datatables.sql;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.datatables.sql.queries.Servitors;
import dwo.gameserver.datatables.xml.ExperienceTable;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SummonItemsData;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.actor.instance.L2SiegeSummonInstance;
import dwo.gameserver.model.actor.instance.L2SummonInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.L2SummonItem;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.l2skills.L2SkillSummon;
import dwo.gameserver.network.game.serverpackets.packet.pet.PetItemList;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Nyaran
 */
public class CharSummonTable
{
	private static final String SAVE_SUMMON = "REPLACE INTO character_summons (owner_id, summon_object_id, summon_skill_id, cur_hp, cur_mp) VALUES (?,?,?,?,?)";
	private static final String REMOVE_SUMMONS = "DELETE FROM character_summons WHERE owner_id = ?";
	private static Logger _log = LogManager.getLogger(CharSummonTable.class);

	public static CharSummonTable getInstance()
	{
		return SingletonHolder._instance;
	}

	public void saveSummon(L2SummonInstance summon)
	{
		if(summon == null || summon.getTimeRemaining() <= 0 || summon.isDead())
		{
			return;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SAVE_SUMMON);

			statement.setInt(1, summon.getOwner().getObjectId());
			statement.setInt(2, summon.getObjectId());
			statement.setInt(3, summon.getReferenceSkill());
			statement.setDouble(4, summon.getCurrentHp());
			statement.setDouble(5, summon.getCurrentMp());

			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Failed to store summon [SummonId: " + summon.getNpcId() + "] from Char [CharId: " + summon.getOwner().getObjectId() + "] data", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Restores summons from database.
	 *
	 * @param activeChar Current character.
	 * @param spawnDelay Delay before summon will be spawned.
	 */
	public void restoreSummon(L2PcInstance activeChar, long spawnDelay)
	{
		if(!activeChar.getPets().isEmpty())
		{
			return;
		}

		int maxRestoreSummonCount = 1;
		if(activeChar.getMaxSummonPoints() > 0)
		{
			maxRestoreSummonCount = 2;  // Макс количество петов 2 ( у сумов )
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.SELECT_CHARACTER_SUMMONS);
			statement.setInt(1, activeChar.getObjectId());
			statement.setInt(2, maxRestoreSummonCount);
			rset = statement.executeQuery();

			L2NpcTemplate summonTemplate;
			L2SummonInstance summon;
			L2SkillSummon skill;

			List<L2SummonInstance> allPets = new FastList<>();

			while(rset.next())
			{
				int skillId = rset.getInt("summon_skill_id");
				int curHp = rset.getInt("cur_hp");
				int curMp = rset.getInt("cur_mp");
				int summonObjectId = rset.getInt("summon_object_id");

				skill = (L2SkillSummon) SkillTable.getInstance().getInfo(skillId, activeChar.getSkillLevel(skillId));
				if(skill == null)
				{
					removeResummon(activeChar);
					return;
				}

				summonTemplate = NpcTable.getInstance().getTemplate(skill.getNpcId());
				if(summonTemplate == null)
				{
					_log.log(Level.WARN, "[CharSummonTable] Summon attemp for nonexisting Skill ID:" + skillId);
					return;
				}

				summon = summonTemplate.isType("L2SiegeSummon") ? new L2SiegeSummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, skill) : new L2SummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, skill, summonObjectId);

				summon.setName(summonTemplate.getName());
				summon.setTitle(activeChar.getName());
				summon.setExpPenalty(skill.getExpPenalty());
				summon.setSharedElementals(skill.getInheritElementals());
				summon.setSharedElementalsValue(skill.getElementalSharePercent());
				summon.setSummonGroupReplace(skill.getSummonGroupReplace());

				if(summon.getLevel() >= ExperienceTable.getInstance().getMaxPetLevel())
				{
					summon.getStat().setExp(ExperienceTable.getInstance().getExpForLevel(ExperienceTable.getInstance().getMaxPetLevel() - 1));
					_log.log(Level.WARN, "Summon (" + summon.getName() + ") NpcID: " + summon.getNpcId() + " has a level above " + ExperienceTable.getInstance().getMaxPetLevel() + ". Please rectify.");
				}
				else
				{
					summon.getStat().setExp(ExperienceTable.getInstance().getExpForLevel(summon.getLevel() % ExperienceTable.getInstance().getMaxPetLevel()));
				}
				summon.setCurrentHp(curHp);
				summon.setCurrentMp(curMp);
				summon.setHeading(activeChar.getHeading());
				summon.setRunning();
				summon.setRestoredObjectId(summonObjectId);

				allPets.add(summon);

				if(skill.getSkillToCast() > 0)
				{
					summon.startSkillCastingTask(skill.getSkillToCast(), skill.getSkillToCastLevel());
				}
			}

			statement = con.prepareStatement(Servitors.CLEAN_CHARACTER_SUMMON_EFFECTS);
			statement.setInt(1, activeChar.getObjectId());
			statement.executeUpdate();

			for(L2SummonInstance pet : allPets)
			{
				activeChar.addPet(pet);
				pet.setShowSummonAnimation(true);
				pet.broadcastNpcInfo(2);
				ThreadPoolManager.getInstance().scheduleGeneral(() -> pet.getLocationController().spawn(activeChar.getX() + Rnd.get(-120, 120), activeChar.getY() + Rnd.get(-120, 120), activeChar.getZ()), spawnDelay);
			}
			removeResummon(activeChar);
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "[CharSummonTable]: Summon cannot be restored: ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public void removeResummon(L2PcInstance activeChar)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(REMOVE_SUMMONS);
			statement.setInt(1, activeChar.getObjectId());
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "[CharSummonTable]: Summon cannot be removed: ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void restorePet(L2PcInstance activeChar)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.SELECT_PETS);
			statement.setInt(1, activeChar.getObjectId());

			rset = statement.executeQuery();

			if(rset.next())
			{
				L2ItemInstance item = activeChar.getInventory().getItemByObjectId(rset.getInt("item_obj_id"));
				if(item == null)
				{
					return;
				}

				L2SummonItem sitem = SummonItemsData.getInstance().getSummonItem(item.getItemId());
				L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(sitem.getNpcId());

				if(npcTemplate == null)
				{
					return;
				}

				L2PetInstance petSummon = L2PetInstance.spawnPet(npcTemplate, activeChar, item);
				if(petSummon == null)
				{
					return;
				}

				petSummon.setShowSummonAnimation(true);
				petSummon.setTitle(activeChar.getName());

				if(!petSummon.isRespawned())
				{
					petSummon.setCurrentHp(petSummon.getMaxHp());
					petSummon.setCurrentMp(petSummon.getMaxMp());
					petSummon.getStat().setExp(petSummon.getExpForThisLevel());
					petSummon.setCurrentFed(petSummon.getMaxFed());
				}

				petSummon.setRunning();

				if(!petSummon.isRespawned())
				{
					petSummon.store();
				}

				activeChar.addPet(petSummon);

				petSummon.getLocationController().spawn(activeChar.getX() + 50, activeChar.getY() + 100, activeChar.getZ());
				petSummon.startFeed();
				item.setEnchantLevel(petSummon.getLevel());

				if(petSummon.getCurrentFed() <= 0)
				{
					petSummon.getLocationController().decay();
				}
				else
				{
					petSummon.startFeed();
				}

				petSummon.setFollowStatus(true);

				petSummon.getOwner().sendPacket(new PetItemList(petSummon));
				petSummon.broadcastStatusUpdate();
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "[CharSummonTable]: Pet cannot be restored: ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	private static class SingletonHolder
	{
		protected static final CharSummonTable _instance = new CharSummonTable();
	}
}
