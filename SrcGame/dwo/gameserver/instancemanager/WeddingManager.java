package dwo.gameserver.instancemanager;

import dwo.gameserver.datatables.sql.queries.mod.Wedding;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.npc.WeddingData;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author evill33t
 */

public class WeddingManager
{
	private static final Logger _log = LogManager.getLogger(WeddingManager.class);

	private static final List<WeddingData> _couples = new ArrayList<>();

	private WeddingManager()
	{
		_log.log(Level.INFO, "CoupleManager: Initializing...");
		load();
	}

	public static WeddingManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private void load()
	{
		_couples.clear();
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(Wedding.LOAD_INDEXES);
			rs = statement.executeQuery();

			while(rs.next())
			{
				_couples.add(new WeddingData(rs.getInt("id")));
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: CoupleManager.load(): " + e.getMessage(), e);
		}
		finally
		{
			_log.log(Level.INFO, "CoupleManager: Loaded " + _couples.size() + " couple(s)");
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	public WeddingData getCouple(int coupleId)
	{
		int index = getCoupleIndex(coupleId);
		if(index >= 0)
		{
			return _couples.get(index);
		}
		return null;
	}

	public void createCouple(L2PcInstance player1, L2PcInstance player2)
	{
		if(player1 != null && player2 != null)
		{
			if(player1.getPartnerId() == 0 && player2.getPartnerId() == 0)
			{
				int _player1id = player1.getObjectId();
				int _player2id = player2.getObjectId();

				WeddingData _new = new WeddingData(player1, player2);
				_couples.add(_new);
				player1.setPartnerId(_player2id);
				player2.setPartnerId(_player1id);
				player1.setCoupleId(_new.getId());
				player2.setCoupleId(_new.getId());
			}
		}
	}

	public void deleteCouple(int coupleId)
	{
		int index = getCoupleIndex(coupleId);
		WeddingData couple = _couples.get(index);
		if(couple != null)
		{
			L2PcInstance player1 = WorldManager.getInstance().getPlayer(couple.getPlayer1Id());
			L2PcInstance player2 = WorldManager.getInstance().getPlayer(couple.getPlayer2Id());
			if(player1 != null)
			{
				player1.setPartnerId(0);
				player1.setMarried(false);
				player1.setCoupleId(0);

			}
			if(player2 != null)
			{
				player2.setPartnerId(0);
				player2.setMarried(false);
				player2.setCoupleId(0);

			}
			couple.divorce();
			_couples.remove(index);
		}
	}

	public int getCoupleIndex(int coupleId)
	{
		int i = 0;
		for(WeddingData temp : _couples)
		{
			if(temp != null && temp.getId() == coupleId)
			{
				return i;
			}
			i++;
		}
		return -1;
	}

	public List<WeddingData> getCouples()
	{
		return _couples;
	}

	private static class SingletonHolder
	{
		protected static final WeddingManager _instance = new WeddingManager();
	}
}
