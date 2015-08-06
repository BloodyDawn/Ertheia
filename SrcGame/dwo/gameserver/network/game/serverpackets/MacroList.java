package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.player.macro.L2Macro;

/*
	* L2GOD Team
	* User: Keiichi
	* Date: 16.05.12
	* Time: 0:30
	* Update: Glory Days protocol: 466
	* */

public class MacroList extends L2GameServerPacket
{
	private final int _count;
	private final int _macroAdd;
	private final L2Macro _macro;

	public MacroList(int macroAdd, int count, L2Macro macro)
	{
		_macroAdd = macroAdd;
		_count = count;
		_macro = macro;
	}

	@Override
	protected void writeImpl()
	{
		writeC(_macroAdd); // 0 при стирании макроса, 1 при добавлении макроса, 2 при редактировании.
		writeD(_macro != null ? _macro.id : 0); // mscro id
		writeC(_count); // count of Macros
		writeC(_macro != null ? 1 : 0);

		if(_macro != null)
		{
			writeD(_macro.id); // mscro id
			writeS(_macro.name); // Macro Name
			writeS(_macro.descr); // Desc
			writeS(_macro.acronym); // acronym
			writeC(_macro.icon); // icon

			writeC(_macro.commands.length); // count
			for(int i = 0; i < _macro.commands.length; i++)
			{
				L2Macro.L2MacroCmd cmd = _macro.commands[i];
				writeC(i + 1); // i of count
				writeC(cmd.type); // type 1 = skill, 3 = action, 4 = shortcut
				writeD(cmd.d1); // skill id
				writeC(cmd.d2); // shortcut id
				writeS(cmd.cmd); // command name
			}
		}
	}
}
