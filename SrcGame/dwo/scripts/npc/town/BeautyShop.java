package dwo.scripts.npc.town;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.olympiad.OlympiadManager;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.beautyshop.ExResponseBeautyList;
import dwo.gameserver.network.game.serverpackets.packet.beautyshop.ExResponseResetList;
import dwo.gameserver.network.game.serverpackets.packet.beautyshop.ExShowBeautyMenu;
import dwo.scripts.instances.ChaosFestival;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 19.11.12
 * Time: 19:03
 */

public class BeautyShop extends Quest
{
	private static final int beautyshop_manager = 33825;

	public BeautyShop()
	{
		addAskId(beautyshop_manager, -81588);
	}

	public static void main(String[] args)
	{
		new BeautyShop();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(OlympiadManager.getInstance().isRegistered(player))
		{
			player.sendPacket(SystemMessage.getSystemMessage(4127));
			return null;
		}
		if(ChaosFestival.getInstance().isRegistered(player))
		{
			player.sendPacket(SystemMessage.getSystemMessage(4126));
			return null;
		}
		switch(reply)
		{
			case 1:      // Покупка
				return "beautyshop_manager002.htm";
			case 2:      // Информация
				return "beautyshop_manager004.htm";
			case 3:      // Снятие
				return "beautyshop_manager003.htm";
			case 4:      // Посылка пакета покупки
				player.sendPacket(new ExShowBeautyMenu(ExShowBeautyMenu.ADD));
				player.sendPacket(new ExResponseBeautyList(player.getAdenaCount(), 0));
				break;
			case 5:      // Посылка пакета снятия
				player.sendPacket(new ExShowBeautyMenu(ExShowBeautyMenu.REMOVE));
				player.sendPacket(new ExResponseResetList(player.getAdenaCount(), player.getAppearance().getHairStyle(), player.getAppearance().getFace(), player.getAppearance().getHairColor()));
				break;
			case 6:     // Главная стр
				return "beautyshop_manager001.htm";
		}
		return null;
	}
}
