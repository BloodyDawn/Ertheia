package dwo.gameserver.model.world.residence.fort;

import dwo.config.Config;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Vice - 2008
 * Class managing periodical events with fort
 */

public class FortUpdater implements Runnable
{
	protected static Logger _log = LogManager.getLogger(FortUpdater.class);
	private L2Clan _clan;
	private Fort _fort;
	private int _runCount;
	private UpdaterType _updaterType;

	public FortUpdater(Fort fort, L2Clan clan, int runCount, UpdaterType ut)
	{
		_fort = fort;
		_clan = clan;
		_runCount = runCount;
		_updaterType = ut;
	}

	@Override
	public void run()
	{
		try
		{
			switch(_updaterType)
			{
				case PERIODIC_UPDATE:
					_runCount++;
					if(_fort.getOwnerClan() == null || !_fort.getOwnerClan().equals(_clan))
					{
						return;
					}

					_fort.getOwnerClan().increaseBloodOathCount();

					if(_fort.getFortState() == FortState.CONTRACTED)
					{
						if(_clan.getWarehouse().getAdenaCount() >= Config.FS_FEE_FOR_CASTLE)
						{
							_clan.getWarehouse().destroyItemByItemId(ProcessType.FORT, PcInventory.ADENA_ID, Config.FS_FEE_FOR_CASTLE, null, null);
							CastleManager.getInstance().getCastleById(_fort.getCastleId()).addToTreasuryNoTax(Config.FS_FEE_FOR_CASTLE);
							_fort.raiseSupplyLvL();
						}
						else
						{
							_fort.setFortState(FortState.INDEPENDENT, 0);
						}
					}
					_fort.save();
					break;
				case MAX_OWN_TIME:
					if(_fort.getOwnerClan() == null || !_fort.getOwnerClan().equals(_clan))
					{
						return;
					}
					if(_fort.getOwnedTime() > Config.FS_MAX_OWN_TIME * 3600)
					{
						_fort.removeOwner();
						_fort.setFortState(FortState.NOT_DECIDED, 0);
					}
					_fort.save();
					break;
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error while running FortUpdater()", e);
		}
	}

	public enum UpdaterType
	{
		MAX_OWN_TIME, // gives fort back to NPC clan
		PERIODIC_UPDATE // raise blood oath/supply level
	}
}