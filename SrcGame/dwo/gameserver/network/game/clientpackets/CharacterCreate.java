package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.datatables.xml.ClassTemplateTable;
import dwo.gameserver.datatables.xml.ObsceneFilterTable;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2PcTemplate;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.game.serverpackets.packet.lobby.CharacterCreateFail;
import dwo.gameserver.network.game.serverpackets.packet.lobby.CharacterCreateSuccess;
import dwo.gameserver.util.Util;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class CharacterCreate extends L2GameClientPacket
{
	protected static final Logger _logAccounting = LogManager.getLogger("accounting");

	// cSdddddddddddd
	private String _name;
	private int _race;
	private byte _sex;
	private int _classId;
	private int _int;
	private int _str;
	private int _con;
	private int _men;
	private int _dex;
	private int _wit;
	private byte _hairStyle;
	private byte _hairColor;
	private byte _face;

	@Override
	protected void readImpl()
	{
		_name = readS();
		_race = readD();
		_sex = (byte) readD();
		_classId = readD();
		_int = readD();
		_str = readD();
		_con = readD();
		_men = readD();
		_dex = readD();
		_wit = readD();
		_hairStyle = (byte) readD();
		_hairColor = (byte) readD();
		_face = (byte) readD();
	}

	@Override
	protected void runImpl()
	{
		if(_name.length() < 1 || _name.length() > 16)
		{
			sendPacket(new CharacterCreateFail(CharacterCreateFail.CharacterCreateFailReason.REASON_16_ENG_CHARS));
			return;
		}

		if(ObsceneFilterTable.getInstance().isObsceneWord(_name))
		{
			sendPacket(new CharacterCreateFail(CharacterCreateFail.CharacterCreateFailReason.REASON_INCORRECT_NAME));
			return;
		}

		if(Config.FORBIDDEN_NAMES.length > 1)
		{
			for(String st : Config.FORBIDDEN_NAMES)
			{
				if(_name.toLowerCase().contains(st.toLowerCase()))
				{
					sendPacket(new CharacterCreateFail(CharacterCreateFail.CharacterCreateFailReason.REASON_INCORRECT_NAME));
					return;
				}
			}
		}

		// Last Verified: May 30, 2009 - Gracia Final
		if(!Util.isAlphaNumeric(_name) || !Util.isValidName(_name))
		{
			sendPacket(new CharacterCreateFail(CharacterCreateFail.CharacterCreateFailReason.REASON_INCORRECT_NAME));
			return;
		}

		if(_face > 2 || _face < 0)
		{
			_log.log(Level.WARN, "Character Creation Failure: Character face " + _face + " is invalid. Possible client hack. " + getClient());

			sendPacket(new CharacterCreateFail(CharacterCreateFail.CharacterCreateFailReason.REASON_CREATION_FAILED));
			return;
		}

		if(_hairStyle < 0 || _sex == 0 && _hairStyle > 4 || _sex != 0 && _hairStyle > 6)
		{
			_log.log(Level.WARN, "Character Creation Failure: Character hair style " + _hairStyle + " is invalid. Possible client hack. " + getClient());

			sendPacket(new CharacterCreateFail(CharacterCreateFail.CharacterCreateFailReason.REASON_CREATION_FAILED));
			return;
		}

		if(_hairColor > 3 || _hairColor < 0)
		{
			_log.log(Level.WARN, "Character Creation Failure: Character hair color " + _hairColor + " is invalid. Possible client hack. " + getClient());

			sendPacket(new CharacterCreateFail(CharacterCreateFail.CharacterCreateFailReason.REASON_CREATION_FAILED));
			return;
		}

		L2PcInstance newChar = null;
		L2PcTemplate template = null;

       /*
		* DrHouse: Since checks for duplicate names are done using SQL, lock must be held until data is written to DB as well.
		*/
		synchronized(CharNameTable.getInstance())
		{
			if(CharNameTable.getInstance().accountCharNumber(getClient().getAccountName()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT && Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0)
			{
				sendPacket(new CharacterCreateFail(CharacterCreateFail.CharacterCreateFailReason.REASON_TOO_MANY_CHARACTERS));
				return;
			}
			if(CharNameTable.getInstance().doesCharNameExist(_name))
			{
				sendPacket(new CharacterCreateFail(CharacterCreateFail.CharacterCreateFailReason.REASON_NAME_ALREADY_EXISTS));
				return;
			}

			template = ClassTemplateTable.getInstance().getTemplate(_classId);

			if(template == null || template.getClassBaseLevel() > 1)
			{
				sendPacket(new CharacterCreateFail(CharacterCreateFail.CharacterCreateFailReason.REASON_CREATION_FAILED));
				return;
			}

			int objectId = IdFactory.getInstance().getNextId();
			newChar = L2PcInstance.create(objectId, template, getClient().getAccountName(), _name, _hairStyle, _hairColor, _face, _sex != 0);
		}

		newChar.setCurrentHp(template.getBaseHp(newChar.getLevel()));
		newChar.setCurrentCp(template.getBaseCp(newChar.getLevel()));
		newChar.setCurrentMp(template.getBaseMp(newChar.getLevel()));

		sendPacket(new CharacterCreateSuccess());
		initNewChar(getClient(), newChar);
	}

	@Override
	public String getType()
	{
		return "[C] 0B CharacterCreate";
	}

	private void initNewChar(L2GameClient client, L2PcInstance newChar)
	{
		// Handled scripts/ai/player/CharCreate.java
		HookManager.getInstance().notifyEvent(HookType.ON_CHAR_CREATE, null, client, newChar);
	}
}