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

package org.citrusframework.citrus.db.server.handler.statement;

import org.citrusframework.citrus.db.driver.dataset.DataSet;
import org.citrusframework.citrus.db.server.controller.JdbcController;
import org.citrusframework.citrus.db.server.handler.AbstractJdbcRequestHandler;
import spark.Request;
import spark.Response;

public class ExecuteStatementHandler extends AbstractJdbcRequestHandler {

    public ExecuteStatementHandler(final JdbcController controller) {
        super(controller);
    }

    @Override
    public DataSet handle(final Request request, final Response response) {
        response.type("application/json");
        return controller.executeStatement(request.body());
    }
}
