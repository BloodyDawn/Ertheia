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

import dwo.config.Config;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import org.apache.log4j.Level;

/**
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

public class NpcHtmlMessage extends L2GameServerPacket
{
	// d S
	// d is usually 0, S is the html text starting with <html> and ending with </html>
	private int _npcObjId;
	private String _html;
	private int _itemId;
	private boolean _validate = true;

	/**
	 *
	 * @param npcObjId
	 * @param itemId
	 */
	public NpcHtmlMessage(int npcObjId, int itemId)
	{
		_npcObjId = npcObjId;
		_itemId = itemId;
	}

	/**
	 * @param npcObjId
	 * @param text
	 */
	public NpcHtmlMessage(int npcObjId, String text)
	{
		_npcObjId = npcObjId;
		setHtml(text);
	}

	public NpcHtmlMessage(int npcObjId)
	{
		_npcObjId = npcObjId;
	}

	/**
	 * disable building bypass validation cache for this packet
	 */
	public void disableValidation()
	{
		_validate = false;
	}

	@Override
	public void runImpl()
	{
		if(Config.BYPASS_VALIDATION && _validate)
		{
			buildBypassCache(getClient().getActiveChar());
		}
	}

	@Override
	protected void writeImpl()
	{
        _html = _html.replace("</a>", "</button>");
        _html = _html.replace("<a action=\"bypass -h talk_select\">", "<button align=\"left\" icon=\"quest\" action=\"bypass -h talk_select\">");
        _html = _html.replace("<a action=\"bypass -h teleport_request\">", "<button align=\"left\" icon=\"teleport\" action=\"bypass -h teleport_request\">");
        _html = _html.replace("<a action=\"bypass -h npc_%objectId%_Chat 0\">", "<button align=\"left\" icon=\"return\" ");
        _html = _html.replace("<a ", "<button align=\"left\" icon=\"normal\" ");

        writeD(_npcObjId);
		writeS(_html);
		writeD(_itemId);
		writeD(0x00); //TODO: Bacek: при начале разговора по квесту с нпц приходит 0 а когда уже идешь по ссылкам 1
	}

	@Override
	public String toString()
	{
		return _html;
	}

	public void setHtml(String text)
	{
		if(text.length() > Config.SIZE_MESSAGE_HTML_NPC)
		{
			_log.log(Level.WARN, "Html is too long! this will crash the client!");
			_html = "<html><body>Html was too long</body></html>";
			return;
		}
		_html = text;
	}

	public boolean setFile(String prefix, String path)
	{
		String content;
		content = HtmCache.getInstance().containsHtml(path) ? HtmCache.getInstance().getHtm(prefix, path) : HtmCache.getInstance().getHtm(prefix, "default/" + path);

		if(content == null)
		{
			setHtml(HtmCache.getInstance().getNoFoundHtml(path));
			_log.log(Level.WARN, "missing html page " + path);
			return false;
		}

		setHtml(content);
		return true;
	}

	public boolean setFileQuest(String prefix, String path)
	{
		String content = HtmCache.getInstance().getHtmQuest(prefix, path);

		if(content == null)
		{
			setHtml(HtmCache.getInstance().getNoFoundHtml(path));
			_log.log(Level.WARN, "missing html page " + path);
			return false;
		}

		setHtml(content);
		return true;
	}

	public void replace(String pattern, String value)
	{
		_html = _html.replaceAll(pattern, value.replaceAll("\\$", "\\\\\\$"));
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
			int start = _html.indexOf("\"bypass ", i);
			int finish = _html.indexOf('\"', start + 1);
			if(start < 0 || finish < 0)
			{
				break;
			}

			start += _html.substring(start + 8, start + 10).equals("-h") ? 11 : 8;

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