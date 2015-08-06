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
package dwo.loginserver;

import dwo.config.Config;
import dwo.util.StackTrace;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

/**
 *
 * @author -Wooden-
 *
 */
public abstract class FloodProtectedListener extends Thread
{
	private Logger _log = LogManager.getLogger(FloodProtectedListener.class);
	private Map<String, ForeignConnection> _floodProtection = new FastMap<>();
	private String _listenIp;
	private int _port;
	private ServerSocket _serverSocket;

	protected FloodProtectedListener(String listenIp, int port) throws IOException
	{
		_port = port;
		_listenIp = listenIp;
		_serverSocket = _listenIp.equals("*") ? new ServerSocket(_port) : new ServerSocket(_port, 50, InetAddress.getByName(_listenIp));
	}

	@Override
	public void run()
	{
		Socket connection = null;

		while(true)
		{
			try
			{
				connection = _serverSocket.accept();
				if(Config.FLOOD_PROTECTION)
				{
					ForeignConnection fConnection = _floodProtection.get(connection.getInetAddress().getHostAddress());
					if(fConnection != null)
					{
						fConnection.connectionNumber += 1;
						if(fConnection.connectionNumber > Config.FAST_CONNECTION_LIMIT && System.currentTimeMillis() - fConnection.lastConnection < Config.NORMAL_CONNECTION_TIME || System.currentTimeMillis() - fConnection.lastConnection < Config.FAST_CONNECTION_TIME || fConnection.connectionNumber > Config.MAX_CONNECTION_PER_IP)
						{
							fConnection.lastConnection = System.currentTimeMillis();
							connection.close();
							fConnection.connectionNumber -= 1;
							if(!fConnection.isFlooding)
							{
								_log.log(Level.WARN, "Potential Flood from " + connection.getInetAddress().getHostAddress());
							}
							fConnection.isFlooding = true;
							continue;
						}
						if(fConnection.isFlooding) //if connection was flooding server but now passed the check
						{
							fConnection.isFlooding = false;
							_log.log(Level.INFO, connection.getInetAddress().getHostAddress() + " is not considered as flooding anymore.");
						}
						fConnection.lastConnection = System.currentTimeMillis();
					}
					else
					{
						fConnection = new ForeignConnection(System.currentTimeMillis());
						_floodProtection.put(connection.getInetAddress().getHostAddress(), fConnection);
					}
				}
				addClient(connection);
			}
			catch(Exception e)
			{
				try
				{
					connection.close();
				}
				catch(Exception e2)
				{
				}
				if(isInterrupted())
				{
					// shutdown?
					try
					{
						_serverSocket.close();
					}
					catch(IOException io)
					{
						_log.log(Level.INFO, "", io);
					}
					break;
				}
			}
		}
	}

	public abstract void addClient(Socket s);

	public void removeFloodProtection(String ip)
	{
		if(!Config.FLOOD_PROTECTION)
		{
			return;
		}
		ForeignConnection fConnection = _floodProtection.get(ip);
		if(fConnection != null)
		{
			fConnection.connectionNumber -= 1;
			if(fConnection.connectionNumber == 0)
			{
				_floodProtection.remove(ip);
			}
		}
		else
		{
			_log.log(Level.WARN, "Removing a flood protection for a GameServer that was not in the connection map??? :" + ip);
		}
	}

	public void close()
	{
		try
		{
			_serverSocket.close();
		}
		catch(IOException e)
		{
			StackTrace.displayStackTraceInformation(e);
		}
	}

	protected static class ForeignConnection
	{
		public int connectionNumber;
		public long lastConnection;
		public boolean isFlooding;

		/**
		 * @param time
		 */
		public ForeignConnection(long time)
		{
			lastConnection = time;
			connectionNumber = 1;
		}
	}
}