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

package org.citrusframework.citrus.db.driver.dataset;

import org.citrusframework.citrus.db.driver.data.Row;

import java.sql.SQLException;
import java.util.*;

/**
 * @author Christoph Deppisch
 */
public class DataSetBuilder {

    private List<Row> rows = new ArrayList<>();

    /**
     * Adds rows to builder.
     * @param rows
     */
    public DataSetBuilder add(Row... rows) {
        if (rows.length == 1) {
            this.rows.add(rows[0]);
        } else {
            this.rows.addAll(Arrays.asList(rows));
        }

        return this;
    }

    /**
     * Build new data set instance.
     * @return
     * @throws SQLException
     */
    public DataSet build() throws SQLException {
        DataSet dataSet = new DataSet();
        dataSet.getRows().addAll(rows);
        return dataSet;
    }
}
