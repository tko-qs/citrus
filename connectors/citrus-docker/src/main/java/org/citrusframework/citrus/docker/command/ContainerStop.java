/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.citrus.docker.command;

import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.docker.actions.DockerExecuteAction;
import org.citrusframework.citrus.docker.client.DockerClient;
import com.github.dockerjava.api.command.StopContainerCmd;
import com.github.dockerjava.api.model.ResponseItem;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class ContainerStop extends AbstractDockerCommand<ResponseItem> {

    /**
     * Default constructor initializing the command name.
     */
    public ContainerStop() {
        super("docker:stop");
        setCommandResult(new ResponseItem());
    }

    @Override
    public void execute(DockerClient dockerClient, TestContext context) {
        StopContainerCmd command = dockerClient.getEndpointConfiguration().getDockerClient().stopContainerCmd(getContainerId(context));
        command.exec();

        setCommandResult(success());
    }

    /**
     * Sets the container id parameter.
     * @param id
     * @return
     */
    public ContainerStop container(String id) {
        getParameters().put(CONTAINER_ID, id);
        return this;
    }

    /**
     * Command builder.
     */
    public static final class Builder extends AbstractDockerCommandBuilder<ResponseItem, ContainerStop, Builder> {

        public Builder(DockerExecuteAction.Builder parent) {
            super(parent, new ContainerStop());
        }

        /**
         * Sets the container id parameter.
         * @param id
         * @return
         */
        public Builder container(String id) {
            command.container(id);
            return this;
        }
    }
}
