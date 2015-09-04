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

import groovy.json.JsonBuilder
import org.kurron.example.rest.Application
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * Integration test for the RestInboundGateway object.
 **/
@ContextConfiguration( loader = SpringApplicationContextLoader, classes = [Application] )
@WebIntegrationTest( randomPort = true )
class RestInboundGatewayIntegrationTest extends Specification {

    def 'exercise happy path'() {

        given: 'a valid command'
        def builder = new JsonBuilder()
        builder {
            gateway 'fast'
            mongodb 'normal'
            redis   'slow'
            mysql 'dead'
            postgresql 'fast'
            rabbitmq 'fast'
        }
        def command = builder.toPrettyString()

        when: 'the POST request is made'
        def bob = 1

        then: 'the endpoint returns with 200'
        false
    }
}
