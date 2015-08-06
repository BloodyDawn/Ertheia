package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.ObsceneFilterTable;
import dwo.gameserver.handler.ChatCommandManager;
import dwo.gameserver.handler.ChatHandlerParams;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExWorldChatCnt;
import dwo.scripts.instances.ChaosFestival;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Say2c extends L2GameClientPacket
{
	private static Logger _logChat = LogManager.getLogger("chat");

	private String _text;
	private ChatType _type;
	private String _target;

	@Override
	protected void readImpl()
	{
		_text = readS();
		_type = ChatType.values()[readD()];
		_target = _type == ChatType.TELL ? readS() : null;
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(_type == null)
		{
			_log.log(Level.WARN, "Say2: Invalid type: " + _type + " Player : " + activeChar.getName() + " text: " + _text);
			activeChar.sendActionFailed();
			activeChar.logout();
			return;
		}

		if(_text.isEmpty())
		{
			_log.log(Level.WARN, activeChar.getName() + ": sending empty text. Possible packet hack!");
			activeChar.sendActionFailed();
			activeChar.logout();
			return;
		}

		// Even though the client can handle more characters than it's current limit allows, an overflow (critical error) happens if you pass a huge (1000+) message.
		// July 11, 2011 - Verified on High Five 4 official client as 105.
		// Allow higher limit if player shift some item (text is longer then).
		if(!activeChar.isGM() && (_text.indexOf(8) >= 0 && _text.length() > 500 || _text.indexOf(8) < 0 && _text.length() > 105))
		{
			activeChar.sendPacket(SystemMessageId.DONT_SPAM);
			return;
		}

		if(activeChar.isCursedWeaponEquipped() && (_type == ChatType.TRADE || _type == ChatType.SHOUT))
		{
			activeChar.sendPacket(SystemMessageId.SHOUT_AND_TRADE_CHAT_CANNOT_BE_USED_WHILE_POSSESSING_CURSED_WEAPON);
			return;
		}

        if(activeChar.isChatBanned())
        {
            if (_type == ChatType.ALL || _type == ChatType.SHOUT || _type == ChatType.TRADE || _type == ChatType.HERO_VOICE)
            {
                activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
                activeChar.sendActionFailed();
            }
            return;
        }

		if(activeChar.isInJail() && Config.JAIL_DISABLE_CHAT)
		{
			if(_type == ChatType.TELL || _type == ChatType.SHOUT || _type == ChatType.TRADE || _type == ChatType.HERO_VOICE)
			{
				activeChar.sendMessage("You can not chat with players outside of the jail.");
				return;
			}
		}

		if(ChaosFestival.getInstance().isFightingNow(activeChar))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(3741));
			return;
		}

		if(_type == ChatType.PETITION_PLAYER && activeChar.isGM())
		{
			_type = ChatType.PETITION_GM;
		}

		if(Config.LOG_CHAT)
		{
			if(_type == ChatType.TELL)
			{
				_logChat.log(Level.INFO, _type + " [" + activeChar.getName() + " to " + _target + "] " + _text);
			}
			else
			{
				_logChat.log(Level.INFO, _type + " [" + activeChar.getName() + "] " + _text);
			}
		}

		if(_text.indexOf(8) >= 0)
		{
			if(!parseAndPublishItem(activeChar))
			{
				return;
			}
		}

		// Say Filter implementation
		if(Config.USE_SAY_FILTER)
		{
			checkText();
		}

        int points = activeChar.getVariablesController().get("WORLD_CHAT_POINTS", Integer.class, 1);
        int newPoints = 1;

        if (_type == ChatType.GLOBAL)
        {
            if (activeChar.getWorldChatPoints() != 0)
            {
                activeChar.setWorldChatPoints(points - newPoints);
            }
        }

        if (_type == ChatType.GLOBAL && activeChar.getWorldChatPoints() == 0)
        {
            activeChar.sendPacket(new ExWorldChatCnt(activeChar));
            return;
        }

		ChatCommandManager.getInstance().execute(new ChatHandlerParams<>(activeChar, _type.ordinal(), _text, _target));
	}

	@Override
	public String getType()
	{
		return "[C] 38 Say2";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}

	private void checkText()
	{
		if(ObsceneFilterTable.getInstance().isObsceneWord(_text))
		{
			_text = Config.CHAT_FILTER_CHARS;
		}
	}

	private boolean parseAndPublishItem(L2PcInstance owner)
	{
		int pos1 = -1;
		while((pos1 = _text.indexOf(8, pos1)) > -1)
		{
			int pos = _text.indexOf("ID=", pos1);
			if(pos == -1)
			{
				return false;
			}
			StringBuilder result = new StringBuilder(9);
			pos += 3;
			while(Character.isDigit(_text.charAt(pos)))
			{
				result.append(_text.charAt(pos++));
			}
			int id = Integer.parseInt(result.toString());
			L2Object item = WorldManager.getInstance().findObject(id);
			if(item instanceof L2ItemInstance)
			{
				if(owner.getInventory().getItemByObjectId(id) == null)
				{
					_log.log(Level.INFO, getClient() + " trying publish item which doesnt own! ID:" + id);
					return false;
				}
				((L2ItemInstance) item).publish();
			}
			else
			{
				_log.log(Level.INFO, getClient() + " trying publish object which is not item! Object:" + item);
				return false;
			}
			pos1 = _text.indexOf(8, pos) + 1;
			if(pos1 == 0) // missing ending tag
			{
				_log.log(Level.INFO, getClient() + " sent invalid publish item msg! ID:" + id);
				return false;
			}
		}
		return true;
	}
}