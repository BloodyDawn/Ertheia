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
package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.LoginServerThread;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import org.apache.log4j.Level;

public class EnterWorld extends L2GameClientPacket {
    private int[][] tracert = new int[5][4];

    @Override
    protected void readImpl() {
        for (int i = 0; i < 5; i++) {
            for (int o = 0; o < 4; o++) {
                tracert[i][o] = readC();
            }
        }

        readB(new byte[32]); // Unknown Byte Array
        readD(); // Unknown Value
        readD(); // Unknown Value
        readD(); // Unknown Value
        readD(); // Unknown Value
        readB(new byte[32]); // Unknown Byte Array
        readD(); // Unknown Value
    }

    @Override
    protected void runImpl()
    {

        L2PcInstance player = getClient().getActiveChar();

        if (player == null) {
            _log.log(Level.WARN, "EnterWorld failed! activeChar returned 'null'.");
            getClient().closeNow();
            return;
        }

        String[] address = new String[5];
        for (int i = 0; i < 5; i++) {
            address[i] = tracert[i][0] + "." + tracert[i][1] + '.' + tracert[i][2] + '.' + tracert[i][3];
        }

        LoginServerThread.getInstance().sendClientTracert(player.getAccountName(), address);

        getClient().setClientTracert(tracert);

        HookManager.getInstance().notifyEvent(HookType.ON_ENTER_WORLD, player.getHookContainer(), player);
    }

    @Override
    public String getType() {
        return "[C] 11 EnterWorld";
    }

    @Override
    protected boolean triggersOnActionRequest() {
        return false;
    }
}