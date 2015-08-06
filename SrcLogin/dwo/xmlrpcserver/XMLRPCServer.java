package dwo.xmlrpcserver;

import dwo.config.Config;
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
	private WebServer _webServer;

	private XMLRPCServer()
	{
		try
		{
			_webServer = new WebServer(Config.XMLRPC_PORT, InetAddress.getByName(Config.XMLRPC_HOST));
			XmlRpcServer xmlServer = _webServer.getXmlRpcServer();
			PropertyHandlerMapping phm = new PropertyHandlerMapping();
			xmlServer.setHandlerMapping(phm);
			XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlServer.getConfig();
			ConfigProperties providers = new ConfigProperties(Config.XMLRPC_SERVICES_CONFIG);
			int nServices = 0;
			for(Object s : providers.keySet())
			{
				try
				{
					Class<?> clazz = Class.forName(providers.getProperty(s.toString()));
					if(clazz != null)
					{
						phm.addHandler(s.toString(), clazz);
						nServices++;
						_log.log(Level.INFO, "XMLRPCServer: Service " + s + " registered");
					}
				}
				catch(ClassNotFoundException ignored)
				{
				}
			}
			if(nServices > 0)
			{
				serverConfig.setEnabledForExtensions(true);
				serverConfig.setContentLengthOptional(false);
				_webServer.start();
				_log.log(Level.INFO, "XMLRPCServer: Listen on " + Config.XMLRPC_HOST + ':' + Config.XMLRPC_PORT);
				_log.log(Level.INFO, "XMLRPCServer: Registered " + nServices + " service(s)");
			}
			else
			{
				_webServer = null;
				_log.log(Level.WARN, "XMLRPCServer: No services registered!");
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "XMLRPCServer: Error while staring " + e);
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
