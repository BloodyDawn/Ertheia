package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.config.Config;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import org.apache.log4j.Level;

/**
 *
 * the HTML parser in the client knowns these standard and non-standard tags and attributes
 * VOLUMN
 * UNKNOWN
 * UL
 * U
 * TT
 * TR
 * TITLE
 * TEXTCODE
 * TEXTAREA
 * TD
 * TABLE
 * SUP
 * SUB
 * STRIKE
 * SPIN
 * SELECT
 * RIGHT
 * PRE
 * P
 * OPTION
 * OL
 * MULTIEDIT
 * LI
 * LEFT
 * INPUT
 * IMG
 * I
 * HTML
 * H7
 * H6
 * H5
 * H4
 * H3
 * H2
 * H1
 * FONT
 * EXTEND
 * EDIT
 * COMMENT
 * COMBOBOX
 * CENTER
 * BUTTON
 * BR
 * BR1
 * BODY
 * BAR
 * ADDRESS
 * A
 * SEL
 * LIST
 * VAR
 * FORE
 * READONL
 * ROWS
 * VALIGN
 * FIXWIDTH
 * BORDERCOLORLI
 * BORDERCOLORDA
 * BORDERCOLOR
 * BORDER
 * BGCOLOR
 * BACKGROUND
 * ALIGN
 * VALU
 * READONLY
 * MULTIPLE
 * SELECTED
 * TYP
 * TYPE
 * MAXLENGTH
 * CHECKED
 * SRC
 * Y
 * X
 * QUERYDELAY
 * NOSCROLLBAR
 * IMGSRC
 * B
 * FG
 * SIZE
 * FACE
 * COLOR
 * DEFFON
 * DEFFIXEDFONT
 * WIDTH
 * VALUE
 * TOOLTIP
 * NAME
 * MIN
 * MAX
 * HEIGHT
 * DISABLED
 * ALIGN
 * MSG
 * LINK
 * HREF
 * ACTION
 */

public class ExNpcQuestHtmlMessage extends L2GameServerPacket
{
	private int _npcObjId;
	private String _html;
	private int _questId;

	/**
	 *
	 * @param npcObjId
	 * @param questId
	 */
	public ExNpcQuestHtmlMessage(int npcObjId, int questId)
	{
		_npcObjId = npcObjId;
		_questId = questId;
	}

	@Override
	public void runImpl()
	{
		if(Config.BYPASS_VALIDATION)
		{
			buildBypassCache(getClient().getActiveChar());
		}
	}

	@Override
	protected void writeImpl()
	{
		writeD(_npcObjId);
		writeS(_html);
		writeD(_questId);
	}

	public void setHtml(String text)
	{
		if(text.length() > Config.SIZE_MESSAGE_HTML_QUEST)
		{
			_log.log(Level.WARN, "Html is too long! this will crash the client!");
			_html = "<html><body>Html was too long</body></html>";
			return;
		}
		_html = text;
	}

	public boolean setFile(String path)
	{
		String content = HtmCache.getInstance().getHtm(getClient().getActiveChar().getLang(), path);

		if(content == null)
		{
			setHtml("<html><body>My Text is missing:<br>" + path + "</body></html>");
			_log.log(Level.WARN, "missing html page " + path);
			return false;
		}

		setHtml(content);
		return true;
	}

	public void replace(String pattern, String value)
	{
		_html = _html.replaceAll(pattern, value);
	}

	private void buildBypassCache(L2PcInstance activeChar)
	{
		if(activeChar == null)
		{
			return;
		}

		activeChar.clearBypass();
		int len = _html.length();
		for(int i = 0; i < len; i++)
		{
			int start = _html.indexOf("bypass -h", i);
			int finish = _html.indexOf('\"', start);

			if(start < 0 || finish < 0)
			{
				break;
			}

			start += 10;
			i = finish;
			int finish2 = _html.indexOf('$', start);
			if(finish2 < finish && finish2 > 0)
			{
				activeChar.addBypass2(_html.substring(start, finish2).trim());
			}
			else
			{
				activeChar.addBypass(_html.substring(start, finish).trim());
			}
		}
	}
}
