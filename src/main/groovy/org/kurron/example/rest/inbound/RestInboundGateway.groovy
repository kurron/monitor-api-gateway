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
import groovy.transform.CompileDynamic
import org.kurron.example.rest.ApplicationProperties
import org.kurron.feedback.AbstractFeedbackAware
import org.kurron.stereotype.InboundRestGateway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.metrics.CounterService
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.Header
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

    /**
     * Mapping of service names to their service endpoints
     **/
    private final def serviceToUriMap = [default: UriComponentsBuilder.newInstance().scheme( 'http' ).host( 'google.com' ).path( '/' ).build().toUri()]

    @Autowired
    RestInboundGateway( final ApplicationProperties aConfiguration,
                        final CounterService aCounterService,
                        final RestOperations aTemplate ) {
        configuration = aConfiguration
        counterService = aCounterService
        theTemplate = aTemplate
        serviceToUriMap['mongodb'] = UriComponentsBuilder.newInstance().scheme( 'http' ).host( 'localhost' ).port( configuration.mongodbServicePort ).path( '/' ).build().toUri()
    }

    @CompileDynamic
    @RequestMapping( method = POST, consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE] )
    ResponseEntity<String> post( @RequestBody final String request, @Header( 'X-Correlation-Id' ) Optional<String> correlationID ) {
        counterService.increment( 'gateway.post' )
        def parsed = new JsonSlurper().parseText( request ) as List
        withPool( parsed.size() ) {
            def results = parsed.makeConcurrent().collect { Map serviceActions ->
                def service = serviceActions.entrySet().first().key as String
                def action = serviceActions.entrySet().first().value as String
                def builder = new JsonBuilder( ['command': action] )
                def command = builder.toPrettyString()
                def headers = new HttpHeaders()
                headers.setContentType( MediaType.APPLICATION_JSON )
                headers.set( 'X-Correlation-Id', correlationID.orElse( 'FIGURE OUT WHY HTTP HEADERS ARE NOT GETTING TRANSFERRED!' ) )
                HttpEntity<String> requestEntity = new HttpEntity<>( command, headers )
                ResponseEntity<Void> response = theTemplate.postForEntity( toEndPoint( service ), requestEntity, Void )
                [service: service, command: action, status: response.statusCode]
            }
            def builder = new JsonBuilder( results )
            new ResponseEntity<String>( builder.toPrettyString(), HttpStatus.OK )
        } as ResponseEntity<String>
    }

    private URI toEndPoint( String service ) {
        serviceToUriMap[(service)] ?: serviceToUriMap['mongodb']
    }
}
