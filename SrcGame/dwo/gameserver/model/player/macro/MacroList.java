package dwo.gameserver.model.player.macro;

import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.L2ShortCut;
import dwo.gameserver.util.StringUtil;
import dwo.gameserver.util.arrays.L2TIntObjectHashMap;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.List;
import java.util.StringTokenizer;

public class MacroList
{
	private static Logger _log = LogManager.getLogger(MacroList.class);

	private L2PcInstance _owner;
	private int _macroId;
	private L2TIntObjectHashMap<L2Macro> _macroses = new L2TIntObjectHashMap<>();

	public MacroList(L2PcInstance owner)
	{
		_owner = owner;
		_macroId = 1000;
	}

	/* Возвращаем id макроса. */
	public int getId(L2Macro macro)
	{
		return macro.id;
	}

	public L2Macro[] getAllMacroses()
	{
		return _macroses.values(new L2Macro[0]);
	}

	public L2Macro getMacro(int id)
	{
		return _macroses.get(id - 1);
	}

	public void registerMacro(L2Macro macro)
	{
		if(macro.id == 0)
		{
			macro.id = _macroId++;
			while(_macroses.get(macro.id) != null)
			{
				macro.id = _macroId++;
			}
			_macroses.put(macro.id, macro);

			registerMacroInDb(macro);
		}
		else
		{
			L2Macro old = _macroses.get(macro.id);
			if(old != null)
			{
				deleteMacroFromDb(old);
			}
			registerMacroInDb(macro);
			sendUpdate(2, macro);
			return;
		}
		sendUpdate(1, macro);
	}

	public void deleteMacro(int id)
	{
		L2Macro toRemove = _macroses.get(id);
		if(toRemove != null)
		{
			deleteMacroFromDb(toRemove);
		}
		_macroses.remove(id);

		_owner.getShortcutController().removeShortcut(id, L2ShortCut.ShortcutType.MACRO);

		sendUpdate(0, toRemove);
	}

	public void sendUpdate(int macroAdd, L2Macro macro)
	{
		L2Macro[] all = getAllMacroses();
		if(all.length == 0)
		{
			_owner.sendPacket(new dwo.gameserver.network.game.serverpackets.MacroList(macroAdd, all.length, null));
		}
		else
		{
			_owner.sendPacket(new dwo.gameserver.network.game.serverpackets.MacroList(macroAdd, all.length, macro));
		}
	}

	/* При входе всегда macroAdd шлется "1" */
	public void sendAllMacro()
	{
		L2Macro[] all = getAllMacroses();
		if(all.length == 0)
		{
			_owner.sendPacket(new dwo.gameserver.network.game.serverpackets.MacroList(1, all.length, null));
		}
		else
		{
			for(L2Macro m : all)
			{
				_owner.sendPacket(new dwo.gameserver.network.game.serverpackets.MacroList(1, all.length, m));
			}
		}
	}

	private void registerMacroInDb(L2Macro macro)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("INSERT INTO character_macroses (charId,id,icon,name,descr,acronym,commands) values(?,?,?,?,?,?,?)");
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, macro.id);
			statement.setInt(3, macro.icon);
			statement.setString(4, macro.name);
			statement.setString(5, macro.descr);
			statement.setString(6, macro.acronym);
			StringBuilder sb = new StringBuilder(300);
			for(L2Macro.L2MacroCmd cmd : macro.commands)
			{
				StringUtil.append(sb, String.valueOf(cmd.type), ",", String.valueOf(cmd.d1), ",", String.valueOf(cmd.d2));

				if(cmd.cmd != null && !cmd.cmd.isEmpty())
				{
					StringUtil.append(sb, ",", cmd.cmd);
				}

				sb.append(';');
			}

			if(sb.length() > 255)
			{
				sb.setLength(255);
			}
			statement.setString(7, sb.toString());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "could not store macro:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * @param macro ?????? ?? ????????
	 */
	private void deleteMacroFromDb(L2Macro macro)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("DELETE FROM character_macroses WHERE charId=? AND id=?");
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, macro.id);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.WARN, "could not delete macro:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void restore()
	{
		_macroses.clear();
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT charId, id, icon, name, descr, acronym, commands FROM character_macroses WHERE charId=?");
			statement.setInt(1, _owner.getObjectId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				int id = rset.getInt("id");
				int icon = rset.getInt("icon");
				String name = rset.getString("name");
				String descr = rset.getString("descr");
				String acronym = rset.getString("acronym");
				List<L2Macro.L2MacroCmd> commands = FastList.newInstance();
				StringTokenizer st1 = new StringTokenizer(rset.getString("commands"), ";");
				while(st1.hasMoreTokens())
				{
					StringTokenizer st = new StringTokenizer(st1.nextToken(), ",");
					if(st.countTokens() < 3)
					{
						continue;
					}
					int type = Integer.parseInt(st.nextToken());
					int d1 = Integer.parseInt(st.nextToken());
					int d2 = Integer.parseInt(st.nextToken());
					String cmd = "";
					if(st.hasMoreTokens())
					{
						cmd = st.nextToken();
					}
					L2Macro.L2MacroCmd mcmd = new L2Macro.L2MacroCmd(commands.size(), type, d1, d2, cmd);
					commands.add(mcmd);
				}

				L2Macro m = new L2Macro(id, icon, name, descr, acronym, commands.toArray(new L2Macro.L2MacroCmd[commands.size()]));
				FastList.recycle((FastList<?>) commands);
				_macroses.put(m.id, m);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.WARN, "could not store shortcuts:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}
}
