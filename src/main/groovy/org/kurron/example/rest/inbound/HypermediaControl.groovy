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

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.Canonical
import org.springframework.hateoas.ResourceSupport
import org.springframework.http.MediaType

/**
 * The hypermedia REST control for the example resource.  Can be serialized into
 * both JSON and, potentially, XML.
 */
@Canonical
@JsonInclude( JsonInclude.Include.NON_NULL )
class HypermediaControl extends ResourceSupport {

    /**
     * The JSON MIME type for the control.
     */
    public static final String JSON_MIME_TYPE = 'application/json;type=example;version=1.0.0'

    /**
     * The XML MIME type for the control.
     */
    public static final String XML_MIME_TYPE = 'application/xml;type=example;version=1.0.0'

    /**
     * Convenience form of the MIME-TYPE for Spring MVC APIs.
     **/
    public static final MediaType JSON_MEDIA_TYPE = MediaType.parseMediaType( JSON_MIME_TYPE )

    /**
     * Convenience form of the MIME-TYPE for Spring MVC APIs.
     **/
    public static final MediaType XML_MEDIA_TYPE = MediaType.parseMediaType( XML_MIME_TYPE )

    /**
     * The HTTP status code. We put it here in case the client isn't allowed access to the headers.
     */
    @JsonProperty( 'http-code' )
    int httpCode = Integer.MIN_VALUE

    @JsonProperty( 'meta-data' )
    MetaDataBlock metaDataBlock

    @JsonProperty( 'error' )
    ErrorBlock errorBlock
}
