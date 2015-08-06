package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.instancemanager.MentorManager;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;

import java.util.ArrayList;
import java.util.List;

public class Say2 extends L2GameServerPacket
{
	// ddSS
	private int _objectId;
	private ChatType _textType;
	private String _charName;
	private int _charId;
	private String _text;
	private int _npcString = -1;
    private int _mask;
    private int _charLevel = -1;
	private List<String> _parameters;

    public Say2(L2PcInstance sender, L2PcInstance receiver, String name, ChatType messageType, String text)
    {
        _objectId = sender.getObjectId();
        _charName = name;
        _charLevel = sender.getLevel();
        _textType = messageType;
        _text = text;
        if (RelationListManager.getInstance().getFriendList(sender.getObjectId()).size() > 0)
        {
            _mask |= 0x01;
        }
        if ((receiver.getClanId() > 0) && (receiver.getClanId() == sender.getClanId()))
        {
            _mask |= 0x02;
        }
        if ((MentorManager.getInstance().getMentee(receiver.getObjectId(), sender.getObjectId()) != null) || (MentorManager.getInstance().getMentee(sender.getObjectId(), receiver.getObjectId()) != null))
        {
            _mask |= 0x04;
        }
        if ((receiver.getAllyId() > 0) && (receiver.getAllyId() == sender.getAllyId()))
        {
            _mask |= 0x08;
        }

        if (sender.isGM())
        {
            _mask |= 0x10;
        }
    }

    public Say2(int objectId, ChatType messageType, String charName, String text)
	{
		_objectId = objectId;
		_textType = messageType;
		_charName = charName;
		_text = text;
	}

	public Say2(int objectId, ChatType messageType, int charId, NpcStringId npcString)
	{
		_objectId = objectId;
		_textType = messageType;
		_charId = charId;
		_npcString = npcString.getId();
	}

	public Say2(int objectId, ChatType messageType, String charName, NpcStringId npcString)
	{
		_objectId = objectId;
		_textType = messageType;
		_charName = charName;
		_npcString = npcString.getId();
	}

	public Say2(int objectId, ChatType messageType, int charId, SystemMessageId sysString)
	{
		_objectId = objectId;
		_textType = messageType;
		_charId = charId;
		_npcString = sysString.getId();
	}

	/**
	 * String parameter for argument S1,S2,.. in npcstring-e.dat
	 * @param text
	 */
	public void addStringParameter(String text)
	{
		if(_parameters == null)
		{
			_parameters = new ArrayList<>();
		}
		_parameters.add(text);
	}

	@Override
	public void runImpl()
	{
		L2PcInstance _pci = getClient().getActiveChar();
		if(_pci != null)
		{
			_pci.broadcastSnoop(_textType, _charName, _text);
		}
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objectId);
		writeD(_textType.ordinal());
		if(_charName != null)
		{
			writeS(_charName);
		}
		else
		{
			writeD(_charId);
		}
		writeD(_npcString); // High Five NPCString ID
		if(_text != null)
		{
			writeS(_text);
            if ((_charLevel > 0) && (_textType == ChatType.TELL))
            {
                writeC(_mask);
                if ((_mask & 0x10) == 0)
                {
                    writeC(_charLevel);
                }
            }
		}
		else
		{
			if(_parameters != null)
			{
				_parameters.forEach(this::writeS);
			}
		}
	}
}
