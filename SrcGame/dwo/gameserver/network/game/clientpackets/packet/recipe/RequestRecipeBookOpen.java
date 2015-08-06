package dwo.gameserver.network.game.clientpackets.packet.recipe;

import dwo.gameserver.datatables.xml.RecipeData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;

public class RequestRecipeBookOpen extends L2GameClientPacket
{
	private boolean _isDwarvenCraft;

	@Override
	protected void readImpl()
	{
		_isDwarvenCraft = readD() == 0;
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
		{
			return;
		}

		if(activeChar.isCastingNow() || activeChar.isCastingSimultaneouslyNow())
		{
			activeChar.sendPacket(SystemMessageId.NO_RECIPE_BOOK_WHILE_CASTING);
			return;
		}

		if(activeChar.getActiveRequester() != null)
		{
			activeChar.sendMessage("Вы не можете использовать книгу рецептов в режиме торговли.");
			return;
		}

		RecipeData.getInstance().requestBookOpen(activeChar, _isDwarvenCraft);
	}

	@Override
	public String getType()
	{
		return "[C] AC RequestRecipeBookOpen";
	}
}
