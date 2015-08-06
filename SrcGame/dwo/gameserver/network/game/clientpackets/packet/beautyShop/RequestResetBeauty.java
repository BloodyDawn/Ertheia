package dwo.gameserver.network.game.clientpackets.packet.beautyShop;

import dwo.gameserver.datatables.xml.BeautyShopData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.beautyshop.ExResponseBeautyRegistReset;

/**
 * User: Bacek
 * Date: 19.11.12
 * Time: 19:38
 */
public class RequestResetBeauty extends L2GameClientPacket
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

		int adenaCount = 0;
		if(_hairId > 10000 && player.getAppearance().getHairStyle() == _hairId)
		{
			BeautyShopData.BeautyShopList st = BeautyShopData.getInstance().getBeautyById(0, _hairId);
			if(st != null)
			{
				adenaCount += st._resetCost;

				if(player.getAdenaCount() > adenaCount)
				{
					player.getAppearance().setCustomHairStyle(0);
					player.getAppearance().setCustomHairColor(0);
				}
			}
		}

		if(_faceId > 20000 && player.getAppearance().getFace() == _faceId)
		{
			BeautyShopData.BeautyShopList st = BeautyShopData.getInstance().getBeautyById(1, _faceId);
			if(st != null)
			{
				adenaCount += st._resetCost;
				if(player.getAdenaCount() > adenaCount)
				{
					player.getAppearance().setCustomFace(0);
				}
			}
		}

		if(adenaCount > 0)
		{
			player.reduceAdena(ProcessType.NPC, adenaCount, player, true);
			player.sendPacket(new ExResponseBeautyRegistReset(player.getAdenaCount(), 1, player.getAppearance().getHairStyle(), player.getAppearance().getFace(), player.getAppearance().getHairColor(), 1));
			player.sendPacket(SystemMessage.getSystemMessage(4017));
			player.broadcastUserInfo();
			return;
		}
		player.sendPacket(new ExResponseBeautyRegistReset(player.getAdenaCount(), 0, player.getAppearance().getHairStyle(), player.getAppearance().getFace(), player.getAppearance().getHairColor(), 1));
		player.sendPacket(SystemMessage.getSystemMessage(4018));
	}

	@Override
	public String getType()
	{
		return "[C] D0:DA RequestResetBeauty";
	}
}
