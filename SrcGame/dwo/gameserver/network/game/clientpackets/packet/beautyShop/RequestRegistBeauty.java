package dwo.gameserver.network.game.clientpackets.packet.beautyShop;

import dwo.gameserver.datatables.xml.BeautyShopData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.beautyshop.ExResponseBeautyList;
import dwo.gameserver.network.game.serverpackets.packet.beautyshop.ExResponseBeautyRegistReset;

/**
 * User: Bacek
 * Date: 19.11.12
 * Time: 19:38
 */
public class RequestRegistBeauty extends L2GameClientPacket
{
	// chddd
	private int _hairId;
	private int _faceId;
	private int _colorId;

	@Override
	protected void readImpl()
	{
		_hairId = readD();
		_faceId = readD();
		_colorId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(player.getPrivateStoreType() != PlayerPrivateStoreType.NONE || player.isInCrystallize())
		{
			return;
		}

		int type = 0;
		int adenaCount = 0;
		if(_hairId > 10000 && player.getAppearance().getHairStyle() != _hairId)
		{
			type = 0;
			BeautyShopData.BeautyShopList st = BeautyShopData.getInstance().getBeautyById(type, _hairId);
			if(st != null)
			{
				adenaCount += st._cost;
				adenaCount += st._colorList.get(_colorId);

				if(player.getAdenaCount() > adenaCount)
				{
					player.getAppearance().setCustomHairStyle(_hairId);
					player.getAppearance().setCustomHairColor(_colorId);
				}
			}
		}
		else
		{
			if(_colorId > 100 && player.getAppearance().getHairColor() != _colorId)
			{
				type = 0;
				BeautyShopData.BeautyShopList st = BeautyShopData.getInstance().getBeautyById(type, _hairId);
				if(st != null)
				{
					adenaCount += st._colorList.get(_colorId);
					if(player.getAdenaCount() > adenaCount)
					{
						player.getAppearance().setCustomHairColor(_colorId);
					}
				}
			}
		}

		if(_faceId > 20000 && player.getAppearance().getFace() != _faceId)
		{
			type = 1;
			BeautyShopData.BeautyShopList st = BeautyShopData.getInstance().getBeautyById(type, _faceId);
			if(st != null)
			{
				adenaCount += st._cost;
				if(player.getAdenaCount() > adenaCount)
				{
					player.getAppearance().setCustomFace(_faceId);
				}
			}
		}

		if(adenaCount > 0 && player.getAdenaCount() > adenaCount)
		{
			player.reduceAdena(ProcessType.NPC, adenaCount, player, true);
			player.sendPacket(new ExResponseBeautyRegistReset(player.getAdenaCount(), 1, player.getAppearance().getHairStyle(), player.getAppearance().getFace(), player.getAppearance().getHairColor(), 0));
			player.sendPacket(SystemMessage.getSystemMessage(4004));
			player.sendPacket(new ExResponseBeautyList(player.getAdenaCount(), type));
			player.broadcastUserInfo();
			return;
		}
		player.sendPacket(new ExResponseBeautyRegistReset(player.getAdenaCount(), 0, player.getAppearance().getHairStyle(), player.getAppearance().getFace(), player.getAppearance().getHairColor(), 0));
		player.sendPacket(SystemMessage.getSystemMessage(4018));
	}

	@Override
	public String getType()
	{
		return "[C] D0:D8 RequestRegistBeauty";
	}
}
