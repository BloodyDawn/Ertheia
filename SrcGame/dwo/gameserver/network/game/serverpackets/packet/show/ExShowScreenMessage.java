package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExShowScreenMessage extends L2GameServerPacket
{
	// Positions
	public static final byte TOP_LEFT = 0x01;
	public static final byte TOP_CENTER = 0x02;
	public static final byte TOP_RIGHT = 0x03;
	public static final byte MIDDLE_LEFT = 0x04;
	public static final byte MIDDLE_CENTER = 0x05;
	public static final byte MIDDLE_RIGHT = 0x06;
	public static final byte BOTTOM_CENTER = 0x07;
	public static final byte BOTTOM_RIGHT = 0x08;
	private final int _type;
	private final int _sysMessageId;
	private final int _unk1;
	private final int _unk2;
	private final int _unk3;
	private final boolean _fade;
	private final int _size;
	private final int _position;
	private final boolean _effect;
	private final String _text;
	private final int _time;
	private final int _npcString;
	private List<String> _parameters;

	/**
	 * Display a String on the screen for a given time.
	 * @param text the text to display
	 * @param time the display time
	 */
	public ExShowScreenMessage(String text, int time)
	{
		_type = 2;
		_sysMessageId = -1;
		_unk1 = 0;
		_unk2 = 0;
		_unk3 = 0;
		_fade = false;
		_position = TOP_CENTER;
		_text = text;
		_time = time;
		_size = 0;
		_effect = false;
		_npcString = -1;
	}

	/**
	 * Display a NPC String on the screen for a given position and time.
	 * @param npcString the NPC String Id
	 * @param position the position on the screen
	 * @param time the display time
	 * @param params the String parameters
	 */
	public ExShowScreenMessage(NpcStringId npcString, int position, int time, String... params)
	{
		_type = 2;
		_sysMessageId = -1;
		_unk1 = 0x00;
		_unk2 = 0x00;
		_unk3 = 0x00;
		_fade = false;
		_position = position;
		_text = null;
		_time = time;
		_size = 0x00;
		_effect = false;
		_npcString = npcString.getId();
		_parameters = Arrays.asList(params);
	}

	/**
	 * Display a System Message on the screen for a given position and time.
	 * @param systemMsg the System Message Id
	 * @param position the position on the screen
	 * @param time the display time
	 * @param params the String parameters
	 */
	public ExShowScreenMessage(SystemMessageId systemMsg, int position, int time, String... params)
	{
		_type = 2;
		_sysMessageId = systemMsg.getId();
		_unk1 = 0x00;
		_unk2 = 0x00;
		_unk3 = 0x00;
		_fade = false;
		_position = position;
		_text = null;
		_time = time;
		_size = 0x00;
		_effect = false;
		_npcString = -1;
		_parameters = Arrays.asList(params);
	}

	/**
	 * Display a Text, System Message or a NPC String on the screen for the given parameters.
	 * @param type 0 - System Message, 1 - Text, 2 - NPC String
	 * @param messageId the System Message Id
	 * @param position the position on the screen
	 * @param unk1
	 * @param size the font size 0 - normal, 1 - small
	 * @param unk2
	 * @param unk3
	 * @param showEffect upper effect (0 - disabled, 1 enabled) - _position must be 2 (center) otherwise no effect
	 * @param time the display time
	 * @param fade the fade effect (0 - disabled, 1 enabled)
	 * @param text the text to display
	 * @param npcString
	 * @param params the String parameters
	 */
	public ExShowScreenMessage(int type, int messageId, int position, int unk1, int size, int unk2, int unk3, boolean showEffect, int time, boolean fade, String text, NpcStringId npcString, String params)
	{
		_type = type;
		_sysMessageId = messageId;
		_unk1 = unk1;
		_unk2 = unk2;
		_unk3 = unk3;
		_fade = fade;
		_position = position;
		_text = text;
		_time = time;
		_size = size;
		_effect = showEffect;
		_npcString = npcString.getId();
	}

	/**
	 * String parameter for argument S1,S2,.. in npcstring-e.dat
	 * @param text the parameter
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
	protected void writeImpl()
	{
		writeD(_type);
		writeD(_sysMessageId);
		writeD(_position);
		writeD(_unk1);
		writeD(_size);
		writeD(_unk2);
		writeD(_unk3);
		writeD(_effect ? 0x01 : 0x00);
		writeD(_time);
		writeD(_fade ? 0x01 : 0x00);
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