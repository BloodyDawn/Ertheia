package dwo.gameserver.network.game.clientpackets.packet.Commission;

import dwo.gameserver.instancemanager.CommissionManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.commission.ExCloseCommission;
import org.apache.log4j.Level;

import static dwo.gameserver.instancemanager.CommissionManager.Window.Main;
import static dwo.gameserver.instancemanager.CommissionManager.Window.MainSubsection;

/**
 * L2GOD Team
 * User: Keiichi, Bacek
 * Date: 24.07.2011
 * Time: 0:08:12
 */

public class RequestCommissionList extends L2GameClientPacket
{
	private int _depth;
	private int _depthType;
	private int _nameCalss;
	private int _grade;
	private String _searchString;

	@Override
	protected void readImpl()
	{
		_depth = readD(); // 0 все 1 основной раздел 2 под раздел
		_depthType = readD();
		_nameCalss = readD();  // -1 все 0 общие 1 редкие
		_grade = readD();  //  -1 любой грейд 0 нг 1 д 2 с 3 в 4 а 5 s 6 s80 7 s84 8 r 9 r95 10 r99
		_searchString = readS();  // имя итема в поиске
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance cha = getClient().getActiveChar();

		if(cha == null)
		{
			return;
		}

		L2Npc manager = cha.getLastFolkNPC();
		if(manager == null || !manager.canInteract(cha))
		{
			cha.sendPacket(new ExCloseCommission());
			return;
		}

		if(_depth == 0 && _depthType == 0)
		{
			CommissionManager.getInstance().showPlayerLots(cha, Main, -1, _nameCalss, _grade, _searchString);
		}
		else if(_depth == 1)
		{
			CommissionManager.getInstance().showPlayerLots(cha, Main, _depthType, _nameCalss, _grade, _searchString);
		}
		else if(_depth == 2)
		{
			CommissionManager.getInstance().showPlayerLots(cha, MainSubsection, _depthType, _nameCalss, _grade, _searchString);
		}
		else
		{
			_log.log(Level.ERROR, "RequestCommissionList new id(readD): " + _depth);
		}
	}

	@Override
	public String getType()
	{
		return "[C] DO:A0 RequestCommissionList";
	}
}