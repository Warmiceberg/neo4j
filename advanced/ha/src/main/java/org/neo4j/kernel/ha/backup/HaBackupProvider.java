/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.ha.backup;

import org.apache.zookeeper.KeeperException;
import org.neo4j.backup.BackupExtensionService;
import org.neo4j.com.ComException;
import org.neo4j.helpers.Pair;
import org.neo4j.helpers.Service;
import org.neo4j.kernel.ha.zookeeper.ClusterManager;
import org.neo4j.kernel.ha.zookeeper.Machine;

@Service.Implementation( BackupExtensionService.class )
public final class HaBackupProvider extends BackupExtensionService
{
    // The server address is <host>:<port>
    private static final String ServerAddressFormat = "%s:%d";

    public HaBackupProvider()
    {
        super( "ha" );
    }

    @Override
    public String resolve( String address )
    {
        String master = null;
        try
        {
            System.out.println( "Asking ZooKeeper service at '" + address
                                + "' for master" );
            master = getMasterServerInCluster( address );
            System.out.println( "Found master '" + master + "' in cluster" );
        }
        catch ( ComException e )
        {
            throw e;
        }
        catch ( RuntimeException e )
        {
            if ( e.getCause() instanceof KeeperException )
            {
                KeeperException zkException = (KeeperException) e.getCause();
                System.out.println( "Couldn't connect to '" + address + "', "
                                    + zkException.getMessage() );
            }
            throw e;
        }
        return master;
    }

    private static String getMasterServerInCluster( String from )
    {
        ClusterManager clusterManager = new ClusterManager( from );
        Pair<String, Integer> masterServer = null;
        try
        {
            clusterManager.waitForSyncConnected();
            Machine master = clusterManager.getMaster();
            masterServer = master.getServer();
            if ( masterServer != null )
            {
                int backupPort = clusterManager.getBackupPort( master.getMachineId() );
                return String.format( ServerAddressFormat,
                        masterServer.first(), backupPort );
            }
            throw new ComException(
                    "Master couldn't be found from cluster managed by " + from );
        }
        finally
        {
            clusterManager.shutdown();
        }
    }
}
