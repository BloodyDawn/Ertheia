package dwo.gameserver.network.game.serverpackets.packet.recipeshop;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.RecipeData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.L2RecipeList;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import org.apache.log4j.Level;

public class RecipeItemMakeInfo extends L2GameServerPacket
{
	private int _id;
	private L2PcInstance _activeChar;
	private boolean _success;

	public RecipeItemMakeInfo(int id, L2PcInstance player, boolean success)
	{
		_id = id;
		_activeChar = player;
		_success = success;
	}

	public RecipeItemMakeInfo(int id, L2PcInstance player)
	{
		_id = id;
		_activeChar = player;
		_success = true;
	}

	@Override
	protected void writeImpl()
	{
		L2RecipeList recipe = RecipeData.getInstance().getRecipeList(_id);

		if(recipe != null)
		{
			writeD(_id);
			writeD(recipe.isDwarvenRecipe() ? 0 : 1); // 0 = Dwarven - 1 = Common
			writeD((int) _activeChar.getCurrentMp());
			writeD(_activeChar.getMaxMp());
			writeD(_success ? 1 : 0); // item creation success/failed
		}
		else if(Config.DEBUG)
		{
			_log.log(Level.DEBUG, "No recipe found with ID = " + _id);
		}
	}
}
