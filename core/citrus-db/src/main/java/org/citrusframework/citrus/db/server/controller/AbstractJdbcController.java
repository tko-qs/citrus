/*
 * Copyright 2006-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.citrus.db.server.controller;

import org.citrusframework.citrus.db.driver.dataset.DataSet;
import org.citrusframework.citrus.db.server.JdbcServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class provides a basic abstract implementation of the JdbcController interface,
 * which provides logging and delegation to the implementation of the subclasses.
 *
 * @author Christoph Deppisch
 */
public abstract class AbstractJdbcController implements JdbcController {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JdbcController.class);

    /** Holds the information whether the controller is within a transaction */
    private boolean transactionState;

    /**
     * Subclasses must provide proper data set for SQL statement.
     * @param sql The sql statement to map to a DataSet
     * @return The data set mapped to the given query
     */
    protected abstract DataSet handleQuery(String sql) throws JdbcServerException;

    /**
     * Subclasses must provide proper data set for SQL statement.
     * @param sql The sql statement to map to a DataSet
     * @return The data set mapped to the given query
     */
    protected abstract DataSet handleExecute(String sql) throws JdbcServerException;

    /**
     * Subclasses must provide number of row updated by SQL statement.
     * @param sql The sql statement to map to a DataSet
     * @return The amount of rows affected by the sql
     */
    protected abstract int handleUpdate(String sql) throws JdbcServerException;

    @Override
    public void openConnection(final Map<String, String> properties) throws JdbcServerException {
        log.info("OPEN CONNECTION with properties: " + properties.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(" | ")));
    }

    @Override
    public void createStatement() throws JdbcServerException {
        log.info("CREATE STATEMENT");
    }

    @Override
    public void closeConnection() throws JdbcServerException {
        log.info("CLOSE CONNECTION");
    }

    @Override
    public void createPreparedStatement(final String sql) throws JdbcServerException {
        log.info("CREATE PREPARED STATEMENT: " + sql);
    }

    @Override
    public DataSet executeQuery(final String sql) throws JdbcServerException {
        log.info("EXECUTE QUERY: " + sql);
        final DataSet dataSet = handleQuery(sql);

        try {
            if(log.isDebugEnabled()){
                log.debug(String.format("RESULT SET with %s rows", dataSet.getRows().size()));
            }
        } catch (final SQLException e) {
            throw new JdbcServerException("Failed to access dataSet", e);
        }

        log.info("QUERY EXECUTION SUCCESSFUL");
        return dataSet;
    }

    @Override
    public DataSet executeStatement(final String sql) throws JdbcServerException {
        log.info("EXECUTE STATEMENT: " + sql);
        final DataSet dataSet = handleExecute(sql);

        try {
            if(log.isDebugEnabled()){
                log.debug(String.format("RESULT SET with %s rows", dataSet.getRows().size()));
            }
        } catch (final SQLException e) {
            throw new JdbcServerException("Failed to access dataSet", e);
        }

        log.info("STATEMENT EXECUTION SUCCESSFUL");

        return dataSet;
    }

    @Override
    public int executeUpdate(final String sql) throws JdbcServerException {
        log.info("EXECUTE UPDATE: " + sql);

        final int rows = handleUpdate(sql);

        if(log.isDebugEnabled()){
            log.debug(String.format("ROWS UPDATED %s", rows));
        }
        log.info("UPDATE EXECUTION SUCCESSFUL");
        return rows;
    }

    @Override
    public void closeStatement() throws JdbcServerException {
        log.info("CLOSE STATEMENT");
    }

    @Override
    public void setTransactionState(final boolean transactionState) {
        log.info(String.format("RECEIVED TRANSACTION STATE CHANGE: %s", String.valueOf(transactionState)));
        this.transactionState = transactionState;
    }

    @Override
    public boolean getTransactionState() {
        log.info(String.format("GET TRANSACTION STATE: %s", String.valueOf(transactionState)));
        return this.transactionState;
    }

    @Override
    public void commitStatements() {
        log.info("COMMIT STATEMENTS");
    }

    @Override
    public void rollbackStatements() {
        log.info("ROLLBACK STATEMENTS");
    }

    @Override
    public void createCallableStatement(final String sql){
        log.info("CREATE CALLABLE STATEMENT: " + sql);
    }
}
