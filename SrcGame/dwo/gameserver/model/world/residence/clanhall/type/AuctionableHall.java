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
package dwo.gameserver.model.world.residence.clanhall.type;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.AuctionManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallManager;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.residence.clanhall.ClanHall;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;

public class AuctionableHall extends ClanHall
{
	protected static final int _chRate = 604800000;
	private long _paidUntil;
	private int _grade;
	private boolean _isPaid;
	private int _lease;

	public AuctionableHall(StatsSet set)
	{
		super(set);
		_paidUntil = set.getLong("paidUntil");
		_grade = set.getInteger("grade");
		_isPaid = set.getBool("paid");
		_lease = set.getInteger("lease");

		if(getOwnerId() != 0)
		{
			_isFree = false;
			initializeTask(false);
			loadFunctions();
		}
	}

	/** Return if clanHall is paid or not */
	public boolean isPaid()
	{
		return _isPaid;
	}

	@Override
	public void free()
	{
		super.free();
		_paidUntil = 0;
		_isPaid = false;
	}

	@Override
	public void setOwner(L2Clan clan)
	{
		super.setOwner(clan);
		_paidUntil = System.currentTimeMillis();
		initializeTask(true);
	}

	/** Return Grade */
	@Override
	public int getGrade()
	{
		return _grade;
	}

	/** Return PaidUntil */
	@Override
	public long getPaidUntil()
	{
		return _paidUntil;
	}

	/** Return lease*/
	@Override
	public int getLease()
	{
		return _lease;
	}

	/***
	 * К получаемому значению добавляется 1 день, т.к. таск FeeTask забирает КЗ именно так
	 * @return сколько дней осталось до очередной оплаты КХ
	 */
	@Override
	public int getDaysToFree()
	{
		return (int) (_paidUntil + _chRate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24) + 1;
	}

	@Override
	public void updateDb()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clanhall SET ownerId=?, paidUntil=?, paid=? WHERE id=?");
			statement.setInt(1, getOwnerId());
			statement.setLong(2, _paidUntil);
			statement.setInt(3, _isPaid ? 1 : 0);
			statement.setInt(4, getId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: updateOwnerInDB(L2Clan clan): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/** Initialize Fee Task */
	private void initializeTask(boolean forced)
	{
		long currentTime = System.currentTimeMillis();
		if(_paidUntil > currentTime)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _paidUntil - currentTime);
		}
		else if(!_isPaid && !forced)
		{
			if(System.currentTimeMillis() + 1000 * 60 * 60 * 24 <= _paidUntil + _chRate)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), System.currentTimeMillis() + 1000 * 60 * 60 * 24);
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _paidUntil + _chRate - System.currentTimeMillis());
			}
		}
		else
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), 0);
		}
	}

	/** Fee Task */
	private class FeeTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				long _time = System.currentTimeMillis();

				if(_isFree)
				{
					return;
				}

				if(_paidUntil > _time)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _paidUntil - _time);
					return;
				}

				L2Clan Clan = ClanTable.getInstance().getClan(getOwnerId());
				if(ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdenaCount() >= getLease())
				{
					if(_paidUntil == 0)
					{
						_paidUntil = _time + _chRate;
					}
					else
					{
						while(_paidUntil <= _time)
						{
							_paidUntil += _chRate;
						}
					}
					ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().destroyItemByItemId(ProcessType.CLAN, PcInventory.ADENA_ID, getLease(), null, null);

					ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _paidUntil - _time);
					_isPaid = true;
					updateDb();
				}
				else
				{
					_isPaid = false;
					if(_time > _paidUntil + _chRate)
					{
						if(ClanHallManager.getInstance().loaded())
						{
							AuctionManager.getInstance().initNPC(getId());
							ClanHallManager.getInstance().setFree(getId());
							Clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED));
						}
						else
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), 3000);
						}
					}
					else
					{
						updateDb();
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
						sm.addNumber(getLease());
						Clan.broadcastToOnlineMembers(sm);
						if(_time + 1000 * 60 * 60 * 24 <= _paidUntil + _chRate)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _time + 1000 * 60 * 60 * 24);
						}
						else
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _paidUntil + _chRate - _time);
						}

					}
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
	}
}
