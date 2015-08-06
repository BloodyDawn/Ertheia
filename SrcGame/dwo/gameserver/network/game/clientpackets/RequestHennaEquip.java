package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.HennaTable;
import dwo.gameserver.datatables.xml.HennaTreeTable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.L2Henna;
import dwo.gameserver.model.items.base.instance.L2HennaInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.util.Util;

public class RequestHennaEquip extends L2GameClientPacket
{
	private int _symbolId;

	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(!getClient().getFloodProtectors().getTransaction().tryPerformAction(FloodAction.HENNA_EQUIP))
		{
			return;
		}

		L2Henna template = HennaTable.getInstance().getTemplate(_symbolId);
		if(template == null)
		{
			return;
		}

		L2HennaInstance henna = new L2HennaInstance(template);
		long _count = 0;

		/**
		 *  Prevents henna drawing exploit:
		 * 1) talk to L2SymbolMakerInstance
		 * 2) RequestHennaList
		 * 3) Don't close the window and go to a GrandMaster and change your subclass
		 * 4) Get SymbolMaker range again and press draw
		 * You could draw any kind of henna just having the required subclass...
		 */
		boolean cheater = true;
		for(L2HennaInstance h : HennaTreeTable.getInstance().getAvailableHenna(activeChar.getClassId().getId()))
		{
			if(h.getSymbolId() == henna.getSymbolId())
			{
				cheater = false;
				break;
			}
		}
		try
		{
			_count = activeChar.getInventory().getItemByItemId(henna.getItemIdDye()).getCount();
		}
		catch(Exception ignored)
		{
			// Ignored
		}

		if(activeChar.getHennaEmptySlots() == 0)
		{
			activeChar.sendPacket(SystemMessageId.SYMBOLS_FULL);
			return;
		}

		if(!cheater && _count >= henna.getAmountDyeRequire() && activeChar.getAdenaCount() >= henna.getPrice() && activeChar.addHenna(henna))
		{
			activeChar.destroyItemByItemId(ProcessType.HENNA, henna.getItemIdDye(), henna.getAmountDyeRequire(), activeChar, true);

			activeChar.getInventory().reduceAdena(ProcessType.HENNA, henna.getPrice(), activeChar, activeChar.getLastFolkNPC());

			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(activeChar.getInventory().getAdenaInstance());
			activeChar.sendPacket(iu);

			activeChar.sendPacket(SystemMessageId.SYMBOL_ADDED);
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.CANT_DRAW_SYMBOL);
			if(!activeChar.isGM() && cheater)
			{
				Util.handleIllegalPlayerAction(activeChar, "Exploit attempt: Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " tryed to add a forbidden henna.", Config.DEFAULT_PUNISH);
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] BC RequestHennaEquip";
	}
}
