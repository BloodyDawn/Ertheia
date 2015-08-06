package dwo.gameserver.engine.databaseengine;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class ThreadConnection
{
    private static final Logger _log = LogManager.getLogger(ThreadConnection.class);
    private final Connection myConnection;

    public ThreadConnection(Connection con)
    {
        myConnection = con;
    }

    public FiltredPreparedStatement prepareStatement(String sql) throws SQLException
    {
        return new FiltredPreparedStatement(myConnection.prepareStatement(sql));
    }

    public void close()
    {
        try
        {
            myConnection.close();
        }
        catch (Exception e)
        {
            _log.log(Level.ERROR, "Couldn't close connection. Cause: " + e.getMessage());
        }
    }

    public void setAutoCommit(boolean on)
    {
        try
        {
            myConnection.setAutoCommit(on);
        }
        catch (SQLException e)
        {
            _log.log(Level.ERROR, "Couldn't set autocommint for connection. Cause: " + e.getMessage());
        }
    }

    public FiltredStatement createStatement() throws SQLException
    {
        return new FiltredStatement(myConnection.createStatement());
    }
}