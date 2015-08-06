package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.type.L2ActionType;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExAutoSoulShot;

/***************************************
 * Project: godworld.ru
 * Date:	09.08.12 16:17
 * Name:    Bacek ( created / edited )
 **************************************/
public class RequestAutoSoulShot extends L2GameClientPacket
{
	// format cd
	private int _itemId;
	private int _type; // 1 = on : 0 = off;

	@Override
	protected void readImpl()
	{
		_itemId = readD();
		_type = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(activeChar.getPrivateStoreType() == PlayerPrivateStoreType.NONE && activeChar.getActiveRequester() == null && !activeChar.isDead())
		{
			L2ItemInstance item = activeChar.getInventory().getItemByItemId(_itemId);
			if(item == null)
			{
				return;
			}

			if(_type == 1)
			{
				if(!activeChar.getInventory().canManipulateWithItemId(item.getItemId()))
				{
					activeChar.sendMessage("Cannot use this item.");
					return;
				}

				// Fishingshots are not automatic on retail
				if(item.isEtcItem() && item.getEtcItem().getDefaultAction() != L2ActionType.fishingshot)
				{
					// Attempt to charge first shot on activation
					// Шоты для петов...
					if(item.getEtcItem().isSummonSpiritshot() || item.getEtcItem().isSummonSoulshot())
					{
						if(activeChar.getPets().isEmpty())
						{
							activeChar.sendPacket(SystemMessageId.NO_SERVITOR_CANNOT_AUTOMATE_USE);
						}
						else
						{
							int soulShotCount = 0;
							int spiritShotCount = 0;
							for(L2Summon pet : activeChar.getPets())
							{
								soulShotCount = +pet.getSoulShotsPerHit();
								spiritShotCount = +pet.getSoulShotsPerHit();
							}
							if(item.getEtcItem().getHandlerName().equals("BeastSoulShot"))
							{
								if(soulShotCount > item.getCount())
								{
									activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET);
									return;
								}
							}
							else
							{
								if(spiritShotCount > item.getCount())
								{
									activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET);
									return;
								}
							}
							activeChar.addAutoSoulShot(_itemId);
							activeChar.sendPacket(new ExAutoSoulShot(_itemId, _type));

							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO).addItemName(item));

							activeChar.rechargeAutoSoulShot(true, true, true);
						}
					}
					else
					{
						if(activeChar.getActiveWeaponItem() != activeChar.getFistsWeaponItem() && item.getItem().getCrystalType() == activeChar.getActiveWeaponItem().getItemGradeSPlus())
						{
							activeChar.addAutoSoulShot(_itemId);
							activeChar.sendPacket(new ExAutoSoulShot(_itemId, _type));
						}
						else
						{
							if(item.getEtcItem().isCharSpiritshot())
							{
								activeChar.sendPacket(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH);
							}
							else
							{
								activeChar.sendPacket(SystemMessageId.SOULSHOTS_GRADE_MISMATCH);
							}

							activeChar.addAutoSoulShot(_itemId);
							activeChar.sendPacket(new ExAutoSoulShot(_itemId, _type));
						}

						// start the auto soulshot use
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO).addItemName(item));
						activeChar.rechargeAutoSoulShot(true, true, false);
					}
				}
			}
			else if(_type == 0)
			{
				activeChar.removeAutoSoulShot(_itemId);
				activeChar.sendPacket(new ExAutoSoulShot(_itemId, _type));

				// cancel the auto soulshot use
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addItemName(item));
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:0D RequestAutoSoulShot";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}