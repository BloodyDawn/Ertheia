package dwo.xmlrpcserver;

import dwo.config.ConfigProperties;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import java.net.InetAddress;

public class XMLRPCServer
{
	private static XMLRPCServer _instance;
	private static Logger _log = LogManager.getLogger(XMLRPCServer.class);

	private XMLRPCServer()
	{
		WebServer _webServer;
		String XMLRPC_HOST;
		int XMLRPC_PORT;

		try
		{
			ConfigProperties p = new ConfigProperties("config/xmlrpc/server.ini");
			XMLRPC_HOST = p.getProperty("Host", "localhost");
			XMLRPC_PORT = Integer.parseInt(p.getProperty("Port", "7000"));
			_webServer = new WebServer(XMLRPC_PORT, InetAddress.getByName(XMLRPC_HOST));
			XmlRpcServer xmlServer = _webServer.getXmlRpcServer();
			PropertyHandlerMapping phm = new PropertyHandlerMapping();
			xmlServer.setHandlerMapping(phm);
			XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlServer.getConfig();
			ConfigProperties providers = new ConfigProperties("config/xmlrpc/services.ini");
			for(Object s : providers.keySet())
			{
				try
				{
					Class<?> clazz = Class.forName(providers.getProperty(s.toString()));
					if(clazz != null)
					{
						phm.addHandler(s.toString(), clazz);
						_log.log(Level.INFO, "XMLRPCServer: Registered " + clazz.getSimpleName() + " service with " + clazz.getMethods().length + " method(s).");
					}
				}
				catch(ClassNotFoundException e)
				{
					_log.log(Level.ERROR, "XMLRPCServer: Error while injection in service class!", e);
				}
			}
			if(phm.getListMethods().length > 0)
			{
				serverConfig.setEnabledForExtensions(true);
				serverConfig.setContentLengthOptional(false);
				_webServer.start();
				_log.log(Level.INFO, "XMLRPCServer: Started. Listen on " + XMLRPC_HOST + ':' + XMLRPC_PORT);
			}
			else
			{
				_log.log(Level.INFO, "XMLRPCServer: Disabled duo no services.");
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "XMLRPCServer: Error while staring server!", e);
		}
	}

	public static XMLRPCServer getInstance()
	{
		if(_instance == null)
		{
			_instance = new XMLRPCServer();
		}
		return _instance;
	}
}