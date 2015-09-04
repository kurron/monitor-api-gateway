/*
 * Copyright (c) 2015. Ronald D. Kurr kurr@jvmguy.com
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
package org.kurron.example.rest.outbound

import static org.kurron.example.rest.feedback.ExampleFeedbackContext.REDIS_RETRIEVE_INFO
import org.kurron.feedback.AbstractFeedbackAware
import org.kurron.stereotype.OutboundGateway

/**
 * Implementation of the outbound gateway that talks to Redis.
 */
@OutboundGateway
class RedisOutboundGateway extends AbstractFeedbackAware implements PersistenceOutboundGateway {

    /**
     * The key to use when storing and retrieving the resource's content type.
     */
    private static final String CONTENT_TYPE_KEY = 'content-type'

    /**
     * The key to use when storing and retrieving the resource's payload.
     */
    private static final String PAYLOAD_KEY = 'payload'

    @Override
    UUID store( final RedisResource resource, final long expirationSeconds ) {
        UUID.randomUUID()
    }

    @Override
    RedisResource retrieve( final UUID id ) {
        feedbackProvider.sendFeedback( REDIS_RETRIEVE_INFO, id )
        new RedisResource( payload: new byte[128], contentType: 'application/json;ronbo=true' )
    }
}
