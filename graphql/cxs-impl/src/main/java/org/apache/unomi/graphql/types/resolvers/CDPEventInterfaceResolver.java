/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.unomi.graphql.types.resolvers;

import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLObjectType;
import org.apache.unomi.api.EventType;
import org.apache.unomi.api.services.EventTypeRegistry;
import org.apache.unomi.graphql.converters.UnomiToGraphQLConverter;
import org.apache.unomi.graphql.services.ServiceManager;
import org.apache.unomi.graphql.types.output.CDPEventInterface;

public class CDPEventInterfaceResolver extends BaseTypeResolver {

    @Override
    public GraphQLObjectType getType(TypeResolutionEnvironment env) {
        final ServiceManager serviceManager = env.getContext();
        final EventTypeRegistry eventTypeRegistry = serviceManager.getService(EventTypeRegistry.class);

        final CDPEventInterface eventInterface = env.getObject();
        final EventType eventType = eventTypeRegistry.get(eventInterface.getEvent().getEventType());
        if (eventType != null) {
            final String typeName = UnomiToGraphQLConverter.convertEventType(eventType.getType());
            return env.getSchema().getObjectType(typeName);
        } else {
            return super.getType(env);
        }
    }
}
