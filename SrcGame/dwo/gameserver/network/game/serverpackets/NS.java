/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.network.game.components.NpcStringId;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kerberos
 */
public class NS extends L2GameServerPacket
{
	// cddddS
	private int _objectId;
	private ChatType _textType;
	private int _npcId;
	private String _text;
	private int _npcString;
	private List<String> _parameters;

	public NS(L2Npc npc, ChatType messageType, NpcStringId npcString)
	{
		_objectId = npc.getObjectId();
		_textType = messageType;
		_npcId = 1000000 + npc.getNpcId();
		_npcString = npcString.getId();
	}

	public NS(L2Npc npc, ChatType messageType, int npcStringId)
	{
		_objectId = npc.getObjectId();
		_textType = messageType;
		_npcId = 1000000 + npc.getNpcId();
		_npcString = npcStringId;
	}

	public NS(L2Npc npc, ChatType messageType, String text)
	{
		_objectId = npc.getObjectId();
		_textType = messageType;
		_npcId = 1000000 + npc.getNpcId();
		_npcString = -1;
		_text = text;
	}

	public NS(int objectId, ChatType messageType, int npcId, String text)
	{
		_objectId = objectId;
		_textType = messageType;
		_npcId = 1000000 + npcId;
		_npcString = -1;
		_text = text;
	}

	public NS(int objectId, ChatType messageType, int npcId, NpcStringId npcString)
	{
		_objectId = objectId;
		_textType = messageType;
		_npcId = 1000000 + npcId;
		_npcString = npcString.getId();
	}

	public NS(int objectId, ChatType messageType, int npcId, int npcString)
	{
		_objectId = objectId;
		_textType = messageType;
		_npcId = 1000000 + npcId;
		_npcString = npcString;
	}

	/**
	 * String parameter for argument S1,S2,.. in npcstring-e.dat
	 * @param text
	 */
	public NS addStringParameter(String text)
	{
		if(_parameters == null)
		{
			_parameters = new ArrayList<>();
		}
		_parameters.add(text);
		return this;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objectId);
		writeD(_textType.ordinal());
		writeD(_npcId);
		writeD(_npcString);
		if(_npcString == -1)
		{
			writeS(_text);
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