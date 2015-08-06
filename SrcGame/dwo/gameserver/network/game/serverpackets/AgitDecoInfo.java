package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.world.residence.clanhall.ClanHall.ClanHallFunction;
import dwo.gameserver.model.world.residence.clanhall.type.AuctionableHall;
import dwo.gameserver.model.world.residence.function.FunctionType;

/**
 * @author Steuf
 */

public class AgitDecoInfo extends L2GameServerPacket
{
	private AuctionableHall _clanHall;

	public AgitDecoInfo(AuctionableHall ClanHall)
	{
		_clanHall = ClanHall;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_clanHall.getId()); // ClanHall ID

		ClanHallFunction function;

		function = _clanHall.getFunction(FunctionType.HP_REGEN);
		if(function != null)
		{
			writeC(function.getFunctionData().getDepth());
		}

		function = _clanHall.getFunction(FunctionType.MP_REGEN);
		if(function != null)
		{
			writeC(function.getFunctionData().getDepth());
		}

		function = _clanHall.getFunction(FunctionType.CP_REGEN);
		if(function != null)
		{
			writeC(function.getFunctionData().getDepth());
		}

		function = _clanHall.getFunction(FunctionType.XP_RESTORE);
		if(function != null)
		{
			writeC(function.getFunctionData().getDepth());
		}

		function = _clanHall.getFunction(FunctionType.TELEPORT);
		if(function != null)
		{
			writeC(function.getFunctionData().getDepth());
		}

		function = _clanHall.getFunction(FunctionType.BROADCAST);
		if(function != null)
		{
			writeC(function.getFunctionData().getDepth());
		}

		function = _clanHall.getFunction(FunctionType.CURTAIN);
		if(function != null)
		{
			writeC(function.getFunctionData().getDepth());
		}

		function = _clanHall.getFunction(FunctionType.HANGING);
		if(function != null)
		{
			writeC(function.getFunctionData().getDepth());
		}

		function = _clanHall.getFunction(FunctionType.BUFF);
		if(function != null)
		{
			writeC(function.getFunctionData().getDepth());
		}

		function = _clanHall.getFunction(FunctionType.OUTERPLATFORM);
		if(function != null)
		{
			writeC(function.getFunctionData().getDepth());
		}

		function = _clanHall.getFunction(FunctionType.PLATFORM);
		if(function != null)
		{
			writeC(function.getFunctionData().getDepth());
		}

		function = _clanHall.getFunction(FunctionType.ITEM_CREATE);
		if(function != null)
		{
			writeC(function.getFunctionData().getDepth());
		}

		writeD(0); // UNK
		writeD(0); // UNK
	}
}
