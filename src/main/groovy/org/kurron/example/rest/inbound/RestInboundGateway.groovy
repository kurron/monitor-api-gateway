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
package org.kurron.example.rest.inbound

import static groovyx.gpars.GParsPool.withPool
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static org.springframework.web.bind.annotation.RequestMethod.POST
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.kurron.example.rest.ApplicationProperties
import org.kurron.feedback.AbstractFeedbackAware
import org.kurron.stereotype.InboundRestGateway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.metrics.CounterService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder

/**
 * Handles inbound REST requests.
 */
@InboundRestGateway
@RequestMapping( value = '/' )
class RestInboundGateway extends AbstractFeedbackAware {

    /**
     * Provides currently active property values.
     */
    private final ApplicationProperties configuration

    /**
     * Used to track counts.
     */
    private final CounterService counterService

    /**
     * Manages REST interactions.
     **/
    private final RestOperations theTemplate

    @Autowired
    RestInboundGateway( final ApplicationProperties aConfiguration,
                        final CounterService aCounterService,
                        final RestOperations aTemplate ) {
        configuration = aConfiguration
        counterService = aCounterService
        theTemplate = aTemplate
    }

    /*
    request:
    {
        "gateway": "fast",
        "mongodb": "normal",
        "redis": "slow",
        "mysql": "dead",
        "postgresql": "fast",
        "rabbitmq": "fast"
    }
    response:
    {
        "gateway": "fast",
        "mongodb": "normal",
        "redis": "slow",
        "mysql": "dead",
        "postgresql": "fast",
        "rabbitmq": "fast"
    }
     */
    @RequestMapping( method = POST, consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE] )
    ResponseEntity<String> post( @RequestBody final String request ) {
        counterService.increment( 'example.post' )
        def slurper = new JsonSlurper().parseText( request ) as Map<String,String>
        withPool( slurper.size() ) {
            def results = slurper.makeConcurrent().collect { String k, v ->
                ResponseEntity<String> response = theTemplate.getForEntity( toEndPoint( k ), String )
                [service: k, command: v, status: response.statusCode, result: response.body]
            }
            def builder = new JsonBuilder( results )
            // for now, echo back the request
            new ResponseEntity<String>( request, HttpStatus.OK )
        } as ResponseEntity<String>
    }

    private static URI toEndPoint( String service ) {
        UriComponentsBuilder.newInstance().scheme( 'http' ).host( 'google.com' ).path( '/' ).build().toUri()
    }
}
