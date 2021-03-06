/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.api.index.inmemory;

import java.io.IOException;
import java.util.Collection;

import org.neo4j.kernel.api.exceptions.index.IndexEntryConflictException;
import org.neo4j.kernel.api.index.IndexEntryUpdate;
import org.neo4j.kernel.api.index.IndexUpdater;

public class UpdateCapturingIndexUpdater implements IndexUpdater
{
    private final IndexUpdater actual;
    private final Collection<IndexEntryUpdate<?>> updatesTarget;

    public UpdateCapturingIndexUpdater( IndexUpdater actual, Collection<IndexEntryUpdate<?>> updatesTarget )
    {
        this.actual = actual;
        this.updatesTarget = updatesTarget;
    }

    @Override
    public void process( IndexEntryUpdate<?> update ) throws IOException, IndexEntryConflictException
    {
        actual.process( update );
        updatesTarget.add( update );
    }

    @Override
    public void close() throws IOException, IndexEntryConflictException
    {
        actual.close();
    }
}
