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
import org.citrusframework.citrus.db.driver.dataset.DataSetProducer;
import org.citrusframework.citrus.db.server.JdbcServerException;

import java.sql.SQLException;

/**
 * @author Christoph Deppisch
 */
public class SimpleJdbcController extends AbstractJdbcController {

    private final DataSetProducer dataSetProducer;

    /**
     * Default constructor using default dataSet producer.
     */
    public SimpleJdbcController() {
        this(DataSet::new);
    }

    /**
     * Constructor initializes dataSet producer.
     * @param dataSetProducer The producer to create dataSets
     */
    public SimpleJdbcController(final DataSetProducer dataSetProducer) {
        this.dataSetProducer = dataSetProducer;
    }

    @Override
    protected DataSet handleQuery(final String sql) throws JdbcServerException {
        try {
            return dataSetProducer.produce();
        } catch (final SQLException e) {
            throw new JdbcServerException("Failed to produce dataSet", e);
        }
    }

    @Override
    protected DataSet handleExecute(final String sql) throws JdbcServerException {
        try {
            return dataSetProducer.produce();
        } catch (final SQLException e) {
            throw new JdbcServerException("Failed to produce dataSet", e);
        }
    }

    @Override
    protected int handleUpdate(final String sql) throws JdbcServerException {
        return 0;
    }
}
