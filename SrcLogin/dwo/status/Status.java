package dwo.status;

import dwo.Server;
import dwo.config.Config;
import dwo.util.Rnd;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Properties;

public class Status extends Thread
{
	protected static final Logger _log = LogManager.getLogger(Status.class);

	private ServerSocket statusServerSocket;

	private int _uptime;
	private int _statusPort;
	private String _statusPw;
	private int _mode;
	private List<LoginStatusThread> _loginStatus;

	public Status(int mode) throws IOException
	{
		super("Status");
		_mode = mode;
		Properties telnetSettings = new Properties();
		InputStream is = new FileInputStream(new File(Config.TELNET_CONFIG));
		telnetSettings.load(is);
		is.close();

		_statusPort = Integer.parseInt(telnetSettings.getProperty("StatusPort", "12345"));
		_statusPw = telnetSettings.getProperty("StatusPW");

		if(_mode == Server.MODE_LOGINSERVER)
		{
			if(_statusPw == null)
			{
				_log.log(Level.INFO, "Server's Telnet Function Has No Password Defined!");
				_log.log(Level.INFO, "A Password Has Been Automaticly Created!");
				_statusPw = rndPW(10);
				_log.log(Level.INFO, "Password Has Been Set To: " + _statusPw);
			}
			_log.log(Level.INFO, "Telnet StatusServer started successfully, listening on Port: " + _statusPort);
		}
		statusServerSocket = new ServerSocket(_statusPort);
		_uptime = (int) System.currentTimeMillis();
		_loginStatus = new FastList<>();
	}

	@Override
	public void run()
	{
		setPriority(Thread.MAX_PRIORITY);

		while(!isInterrupted())
		{
			try
			{
				Socket connection = statusServerSocket.accept();
				if(_mode == Server.MODE_LOGINSERVER)
				{
					LoginStatusThread lst = new LoginStatusThread(connection, _uptime, _statusPw);
					if(lst.isAlive())
					{
						_loginStatus.add(lst);
					}
				}
				if(isInterrupted())
				{
					try
					{
						statusServerSocket.close();
					}
					catch(IOException io)
					{
						io.printStackTrace();
					}
					break;
				}
			}
			catch(IOException e)
			{
				if(isInterrupted())
				{
					try
					{
						statusServerSocket.close();
					}
					catch(IOException io)
					{
						io.printStackTrace();
					}
					break;
				}
			}
		}
	}

	private String rndPW(int length)
	{
		String lowerChar = "qwertyuiopasdfghjklzxcvbnm";
		String upperChar = "QWERTYUIOPASDFGHJKLZXCVBNM";
		String digits = "1234567890";
		StringBuilder password = new StringBuilder(length);

		for(int i = 0; i < length; i++)
		{
			int charSet = Rnd.nextInt(3);
			switch(charSet)
			{
				case 0:
					password.append(lowerChar.charAt(Rnd.nextInt(lowerChar.length() - 1)));
					break;
				case 1:
					password.append(upperChar.charAt(Rnd.nextInt(upperChar.length() - 1)));
					break;
				case 2:
					password.append(digits.charAt(Rnd.nextInt(digits.length() - 1)));
					break;
			}
		}
		return password.toString();
	}

	public void sendMessageToTelnets(String msg)
	{
		List<LoginStatusThread> lsToRemove = new FastList<>();
		for(LoginStatusThread ls : _loginStatus)
		{
			if(ls.isInterrupted())
			{
				lsToRemove.add(ls);
			}
			else
			{
				ls.printToTelnet(msg);
			}
		}
	}
}
