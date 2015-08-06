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
package dwo.gameserver.instancemanager.castle;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.datatables.xml.ManorData;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.ClanWarehouse;
import dwo.gameserver.model.items.itemcontainer.ItemContainer;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

/**
 * Class For Castle Manor Manager Load manor data from DB Update/Reload/Delete
 * Handles all schedule for manor
 *
 * @author l3x
 */

public class CastleManorManager
{
	public static final int PERIOD_CURRENT = 0;
	public static final int PERIOD_NEXT = 1;
	protected static final Logger _log = LogManager.getLogger(CastleManorManager.class);
	protected static final long MAINTENANCE_PERIOD = Config.ALT_MANOR_MAINTENANCE_PERIOD; // 6 mins
	private static final String CASTLE_MANOR_LOAD_PROCURE = "SELECT * FROM castle_manor_procure WHERE castle_id=?";
	private static final String CASTLE_MANOR_LOAD_PRODUCTION = "SELECT * FROM castle_manor_production WHERE castle_id=?";
	private static final int NEXT_PERIOD_APPROVE = Config.ALT_MANOR_APPROVE_TIME; // 6:00
	private static final int NEXT_PERIOD_APPROVE_MIN = Config.ALT_MANOR_APPROVE_MIN; //
	private static final int MANOR_REFRESH = Config.ALT_MANOR_REFRESH_TIME; // 20:00
	private static final int MANOR_REFRESH_MIN = Config.ALT_MANOR_REFRESH_MIN; //
	protected ScheduledFuture<?> _scheduledManorRefresh;
	protected ScheduledFuture<?> _scheduledMaintenanceEnd;
	protected ScheduledFuture<?> _scheduledNextPeriodapprove;
	private Calendar _manorRefresh;
	private Calendar _periodApprove;
	private boolean _underMaintenance;
	private boolean _disabled;

	private CastleManorManager()
	{
		_log.log(Level.INFO, "CastleManorManager: Initializing.");
		load(); // load data from database
		init(); // schedule all manor related events
		_underMaintenance = false;
		_disabled = !Config.ALLOW_MANOR;

		boolean isApproved;
		isApproved = _periodApprove.getTimeInMillis() > _manorRefresh.getTimeInMillis() ? _manorRefresh.getTimeInMillis() > Calendar.getInstance().getTimeInMillis() : _periodApprove.getTimeInMillis() < Calendar.getInstance().getTimeInMillis() && _manorRefresh.getTimeInMillis() > Calendar.getInstance().getTimeInMillis();

		for(Castle castle : CastleManager.getInstance().getCastles())
		{
			castle.setNextPeriodApproved(isApproved);
		}
	}

	public static CastleManorManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private void load()
	{
		ThreadConnection con = null;
		try
		{
			// Get ThreadConnection
			con = L2DatabaseFactory.getInstance().getConnection();
			FiltredPreparedStatement statementProduction = con.prepareStatement(CASTLE_MANOR_LOAD_PRODUCTION);
			FiltredPreparedStatement statementProcure = con.prepareStatement(CASTLE_MANOR_LOAD_PROCURE);
			for(Castle castle : CastleManager.getInstance().getCastles())
			{
				FastList<SeedProduction> production = new FastList<>();
				FastList<SeedProduction> productionNext = new FastList<>();
				FastList<CropProcure> procure = new FastList<>();
				FastList<CropProcure> procureNext = new FastList<>();

				// restore seed production info
				statementProduction.setInt(1, castle.getCastleId());
				ResultSet rs = statementProduction.executeQuery();
				statementProduction.clearParameters();
				while(rs.next())
				{
					int seedId = rs.getInt("seed_id");
					int canProduce = rs.getInt("can_produce");
					int startProduce = rs.getInt("start_produce");
					int price = rs.getInt("seed_price");
					int period = rs.getInt("period");
					if(period == PERIOD_CURRENT)
					{
						production.add(new SeedProduction(seedId, canProduce, price, startProduce));
					}
					else
					{
						productionNext.add(new SeedProduction(seedId, canProduce, price, startProduce));
					}
				}
				rs.close();

				castle.setSeedProduction(production, PERIOD_CURRENT);
				castle.setSeedProduction(productionNext, PERIOD_NEXT);

				// restore procure info
				statementProcure.setInt(1, castle.getCastleId());
				rs = statementProcure.executeQuery();
				statementProcure.clearParameters();
				while(rs.next())
				{
					int cropId = rs.getInt("crop_id");
					int canBuy = rs.getInt("can_buy");
					int startBuy = rs.getInt("start_buy");
					int rewardType = rs.getInt("reward_type");
					int price = rs.getInt("price");
					int period = rs.getInt("period");
					if(period == PERIOD_CURRENT)
					{
						procure.add(new CropProcure(cropId, canBuy, rewardType, startBuy, price));
					}
					else
					{
						procureNext.add(new CropProcure(cropId, canBuy, rewardType, startBuy, price));
					}
				}
				rs.close();

				castle.setCropProcure(procure, PERIOD_CURRENT);
				castle.setCropProcure(procureNext, PERIOD_NEXT);

				if(!procure.isEmpty() || !procureNext.isEmpty() || !production.isEmpty() || !productionNext.isEmpty())
				{
					_log.log(Level.INFO, castle.getName() + ": Data loaded");
				}
			}
			statementProduction.close();
			statementProcure.close();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "CastleManorManager: Error restoring manor data: " + e.getMessage());
		}
		finally
		{
			DatabaseUtils.closeConnection(con);
		}
	}

	private void init()
	{
		_manorRefresh = Calendar.getInstance();
		_manorRefresh.set(Calendar.HOUR_OF_DAY, MANOR_REFRESH);
		_manorRefresh.set(Calendar.MINUTE, MANOR_REFRESH_MIN);

		_periodApprove = Calendar.getInstance();
		_periodApprove.set(Calendar.HOUR_OF_DAY, NEXT_PERIOD_APPROVE);
		_periodApprove.set(Calendar.MINUTE, NEXT_PERIOD_APPROVE_MIN);

		updateManorRefresh();
		updatePeriodApprove();
	}

	public void updateManorRefresh()
	{
		_log.log(Level.INFO, "CastleManorManager: Manor refresh updated");

		_scheduledManorRefresh = ThreadPoolManager.getInstance().scheduleGeneral(() -> {
			if(!_disabled)
			{
				_underMaintenance = true;
				_log.log(Level.INFO, "CastleManorManager: Under maintenance mode started");

				_scheduledMaintenanceEnd = ThreadPoolManager.getInstance().scheduleGeneral(() -> {
					_log.log(Level.INFO, "CastleManorManager: Next period started");
					setNextPeriod();
					try
					{
						save();
					}
					catch(Exception e)
					{
						_log.log(Level.WARN, "Manor System: Failed to save manor data: " + e.getMessage(), e);
					}
					_underMaintenance = false;
				}, MAINTENANCE_PERIOD);
			}
			updateManorRefresh();
		}, getMillisToManorRefresh());
	}

	public void updatePeriodApprove()
	{
		_log.log(Level.INFO, "CastleManorManager: Manor period approve updated");

		_scheduledNextPeriodapprove = ThreadPoolManager.getInstance().scheduleGeneral(() -> {
			if(!_disabled)
			{
				approveNextPeriod();
				_log.log(Level.INFO, "CastleManorManager: Next period approved");
			}
			updatePeriodApprove();
		}, getMillisToNextPeriodApprove());
	}

	public long getMillisToManorRefresh()
	{
		// use safe interval 120s to prevent double run
		if(_manorRefresh.getTimeInMillis() - Calendar.getInstance().getTimeInMillis() < 120000)
		{
			setNewManorRefresh();
		}

		_log.log(Level.INFO, "CastleManorManager: New Schedule for manor refresh @ " + _manorRefresh.getTime());

		return _manorRefresh.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
	}

	public void setNewManorRefresh()
	{
		_manorRefresh = Calendar.getInstance();
		_manorRefresh.set(Calendar.HOUR_OF_DAY, MANOR_REFRESH);
		_manorRefresh.set(Calendar.MINUTE, MANOR_REFRESH_MIN);
		_manorRefresh.set(Calendar.SECOND, 0);
		_manorRefresh.add(Calendar.HOUR_OF_DAY, 24);
	}

	public long getMillisToNextPeriodApprove()
	{
		// use safe interval 120s to prevent double run
		if(_periodApprove.getTimeInMillis() - Calendar.getInstance().getTimeInMillis() < 120000)
		{
			setNewPeriodApprove();
		}

		_log.log(Level.INFO, "CastleManorManager: New Schedule for period approve @ " + _periodApprove.getTime());

		return _periodApprove.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
	}

	public void setNewPeriodApprove()
	{
		_periodApprove = Calendar.getInstance();
		_periodApprove.set(Calendar.HOUR_OF_DAY, NEXT_PERIOD_APPROVE);
		_periodApprove.set(Calendar.MINUTE, NEXT_PERIOD_APPROVE_MIN);
		_periodApprove.set(Calendar.SECOND, 0);
		_periodApprove.add(Calendar.HOUR_OF_DAY, 24);
	}

	public void setNextPeriod()
	{
		for(Castle castle : CastleManager.getInstance().getCastles())
		{
			if(castle.getOwnerId() <= 0)
			{
				continue;
			}
			L2Clan clan = ClanTable.getInstance().getClan(castle.getOwnerId());
			if(clan == null)
			{
				continue;
			}

			ItemContainer cwh = clan.getWarehouse();
			if(!(cwh instanceof ClanWarehouse))
			{
				_log.log(Level.INFO, "CastleManorManager: Can't get clan warehouse for clan " + ClanTable.getInstance().getClan(castle.getOwnerId()));
				continue;
			}

			for(CropProcure crop : castle.getCropProcure(PERIOD_CURRENT))
			{
				if(crop.getStartAmount() == 0)
				{
					continue;
				}
				// adding bought crops to clan warehouse
				if(crop.getStartAmount() - crop.getAmount() > 0)
				{
					long count = crop.getStartAmount() - crop.getAmount();
					count = count * 90 / 100;
					if(count < 1)
					{
						if(Rnd.getChance(90))
						{
							count = 1;
						}
					}
					if(count > 0)
					{
						cwh.addItem(ProcessType.MANOR, ManorData.getInstance().getMatureCrop(crop.getId()), count, null, null);
					}
				}
				// reserved and not used money giving back to treasury
				if(crop.getAmount() > 0)
				{
					castle.addToTreasuryNoTax(crop.getAmount() * crop.getPrice());
				}
			}

			castle.setSeedProduction(castle.getSeedProduction(PERIOD_NEXT), PERIOD_CURRENT);
			castle.setCropProcure(castle.getCropProcure(PERIOD_NEXT), PERIOD_CURRENT);

			if(castle.getTreasury() < castle.getManorCost(PERIOD_CURRENT))
			{
				castle.setSeedProduction(getNewSeedsList(castle.getCastleId()), PERIOD_NEXT);
				castle.setCropProcure(getNewCropsList(castle.getCastleId()), PERIOD_NEXT);
			}
			else
			{
				FastList<SeedProduction> production = new FastList<>();
				for(SeedProduction s : castle.getSeedProduction(PERIOD_CURRENT))
				{
					s.setCanProduce(s.getStartProduce());
					production.add(s);
				}
				castle.setSeedProduction(production, PERIOD_NEXT);

				FastList<CropProcure> procure = new FastList<>();
				for(CropProcure cr : castle.getCropProcure(PERIOD_CURRENT))
				{
					cr.setAmount(cr.getStartAmount());
					procure.add(cr);
				}
				castle.setCropProcure(procure, PERIOD_NEXT);
			}
			if(Config.ALT_MANOR_SAVE_ALL_ACTIONS)
			{
				castle.saveCropData();
				castle.saveSeedData();
			}

			// Sending notification to a clan leader
			L2PcInstance clanLeader = null;
			clanLeader = WorldManager.getInstance().getPlayer(clan.getLeader().getName());
			if(clanLeader != null)
			{
				clanLeader.sendPacket(SystemMessageId.THE_MANOR_INFORMATION_HAS_BEEN_UPDATED);
			}

			castle.setNextPeriodApproved(false);
		}
	}

	public void approveNextPeriod()
	{
		for(Castle castle : CastleManager.getInstance().getCastles())
		{
			boolean notFunc = false;

			if(castle.getOwnerId() <= 0) // Castle has no owner
			{
				castle.setCropProcure(new FastList<>(), PERIOD_NEXT);
				castle.setSeedProduction(new FastList<>(), PERIOD_NEXT);
			}
			else if(castle.getTreasury() < castle.getManorCost(PERIOD_NEXT))
			{
				notFunc = true;
				_log.log(Level.INFO, "CastleManorManager: Manor for castle " + castle.getName() + " disabled, not enough adena in treasury: " + castle.getTreasury() + ", " + castle.getManorCost(PERIOD_NEXT) + " required.");
				castle.setSeedProduction(getNewSeedsList(castle.getCastleId()), PERIOD_NEXT);
				castle.setCropProcure(getNewCropsList(castle.getCastleId()), PERIOD_NEXT);
			}
			else
			{
				ItemContainer cwh = ClanTable.getInstance().getClan(castle.getOwnerId()).getWarehouse();
				if(!(cwh instanceof ClanWarehouse))
				{
					_log.log(Level.INFO, "CastleManorManager: Can't get clan warehouse for clan " + ClanTable.getInstance().getClan(castle.getOwnerId()));
					continue;
				}
				int slots = 0;
				for(CropProcure crop : castle.getCropProcure(PERIOD_NEXT))
				{
					if(crop.getStartAmount() > 0)
					{
						if(cwh.getItemByItemId(ManorData.getInstance().getMatureCrop(crop.getId())) == null)
						{
							slots++;
						}
					}
				}
				if(!cwh.validateCapacity(slots))
				{
					notFunc = true;
					_log.log(Level.INFO, "CastleManorManager: Manor for castle " + castle.getName() + " disabled, not enough free slots in clan warehouse: " + (Config.WAREHOUSE_SLOTS_CLAN - cwh.getSize()) + ", but " + slots + " required.");
					castle.setSeedProduction(getNewSeedsList(castle.getCastleId()), PERIOD_NEXT);
					castle.setCropProcure(getNewCropsList(castle.getCastleId()), PERIOD_NEXT);
				}
			}
			castle.setNextPeriodApproved(true);
			castle.addToTreasuryNoTax(-1 * castle.getManorCost(PERIOD_NEXT));

			if(notFunc)
			{
				L2Clan clan = ClanTable.getInstance().getClan(castle.getOwnerId());
				L2PcInstance clanLeader = null;
				if(clan != null)
				{
					clanLeader = WorldManager.getInstance().getPlayer(clan.getLeaderId());
				}
				if(clanLeader != null)
				{
					clanLeader.sendPacket(SystemMessageId.THE_AMOUNT_IS_NOT_SUFFICIENT_AND_SO_THE_MANOR_IS_NOT_IN_OPERATION);
				}
			}
		}
	}

	private FastList<SeedProduction> getNewSeedsList(int castleId)
	{
		FastList<SeedProduction> seeds = new FastList<>();
		FastList<Integer> seedsIds = ManorData.getInstance().getSeedsForCastle(castleId);
		seeds.addAll(seedsIds.stream().map(SeedProduction::new).collect(Collectors.toList()));
		return seeds;
	}

	private FastList<CropProcure> getNewCropsList(int castleId)
	{
		FastList<CropProcure> crops = new FastList<>();
		FastList<Integer> cropsIds = ManorData.getInstance().getCropsForCastle(castleId);
		crops.addAll(cropsIds.stream().map(CropProcure::new).collect(Collectors.toList()));

		return crops;
	}

	public boolean isUnderMaintenance()
	{
		return _underMaintenance;
	}

	public void setUnderMaintenance(boolean mode)
	{
		_underMaintenance = mode;
	}

	public boolean isDisabled()
	{
		return _disabled;
	}

	public void setDisabled(boolean mode)
	{
		_disabled = mode;
	}

	public SeedProduction getNewSeedProduction(int id, long amount, long price, long sales)
	{
		return new SeedProduction(id, amount, price, sales);
	}

	public CropProcure getNewCropProcure(int id, long amount, int type, long price, long buy)
	{
		return new CropProcure(id, amount, type, buy, price);
	}

	public void save()
	{
		for(Castle castle : CastleManager.getInstance().getCastles())
		{
			castle.saveSeedData();
			castle.saveCropData();
		}
	}

	public static class CropProcure
	{
		final int _cropId;
		final int _rewardType;
		final long _buy;
		final long _price;
		long _buyResidual;

		public CropProcure(int id)
		{
			_cropId = id;
			_buyResidual = 0;
			_rewardType = 0;
			_buy = 0;
			_price = 0;
		}

		public CropProcure(int id, long amount, int type, long buy, long price)
		{
			_cropId = id;
			_buyResidual = amount;
			_rewardType = type;
			_buy = buy;
			_price = price;
		}

		public int getReward()
		{
			return _rewardType;
		}

		public int getId()
		{
			return _cropId;
		}

		public long getAmount()
		{
			return _buyResidual;
		}

		public void setAmount(long amount)
		{
			_buyResidual = amount;
		}

		public long getStartAmount()
		{
			return _buy;
		}

		public long getPrice()
		{
			return _price;
		}
	}

	public static class SeedProduction
	{
		final int _seedId;
		final long _price;
		final long _sales;
		long _residual;

		public SeedProduction(int id)
		{
			_seedId = id;
			_residual = 0;
			_price = 0;
			_sales = 0;
		}

		public SeedProduction(int id, long amount, long price, long sales)
		{
			_seedId = id;
			_residual = amount;
			_price = price;
			_sales = sales;
		}

		public int getId()
		{
			return _seedId;
		}

		public long getCanProduce()
		{
			return _residual;
		}

		public void setCanProduce(long amount)
		{
			_residual = amount;
		}

		public long getPrice()
		{
			return _price;
		}

		public long getStartProduce()
		{
			return _sales;
		}
	}

	private static class SingletonHolder
	{
		protected static final CastleManorManager _instance = new CastleManorManager();
	}
}
