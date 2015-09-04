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
package org.kurron.example.rest

import static org.kurron.example.rest.SpockExamplesUnitTest.shouldWeRun
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import spock.lang.IgnoreIf
import spock.lang.Issue
import spock.lang.Narrative
import spock.lang.Requires
import spock.lang.See
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title
import spock.util.Exceptions
import spock.util.concurrent.PollingConditions
import spock.util.environment.RestoreSystemProperties

/**
 * Examples of some of the newer Spock features.
 */
@Requires( { shouldWeRun( true ) } )
@Title( 'Spock Learning Test' )
@Narrative( 'Spock Learning Test' )
class SpockExamplesUnitTest extends Specification implements DatabaseTrait {

    @Rule
    CustomRule someRule = new CustomRule()

    @Subject
    def sut = ['a', 'b']

    @Requires( { env.containsKey( 'RUN_SPECIAL_TEST' ) } )
    def 'run only if an environmental variable is set'() {

        expect: 'should never be run'
        false
    }

    @Requires( { System.getProperty( 'os.arch' ) == 'amd64' } )
    def 'run only if a system property is set'() {

        expect: 'only run on 64-bit systems'
        println 'Running on a 64-bit system'
    }

    static boolean shouldWeRun( boolean decision ) { decision }

    def 'consult a static method'() {

        expect: 'only run if helper method says so'
        println 'Helper method said yes'
    }

    @Requires( { jvm.java8Compatible } )
    def 'run only if Java 8 is in play'() {

        expect: 'only run on Java 8 systems'
        println 'Running on a Java 8 system'
    }

    @IgnoreIf( { jvm.java8Compatible } )
    def 'ignore if Java 8 is in play'() {

        expect: 'only run on non-Java 8 systems'
        println 'Running on a non-Java 8 system'
    }

    @Requires( { os.linux } )
    def 'run only if Linux is in play'() {

        expect: 'only run on Linux systems'
        println 'Running on a Linux system'
    }

    @RestoreSystemProperties
    def 'modify system properties'() {

        given: 'modified system properties'
        System.setProperty( 'user.name', 'root' )

        when: 'sut is exercised'
        sut << System.getProperty( 'user.name' )

        then: 'root is in the list'
        sut.find { it == 'root' }
    }

    def 'verify system properties have not been modified'() {

        given: 'unmodified system properties'

        when: 'sut is exercised'
        sut << System.getProperty( 'user.name' )

        then: 'root is not in the list'
        !sut.find { it == 'root' }
    }

    @SuppressWarnings( 'GroovyUnreachableStatement' )
    def 'showcase setup and cleanup'() {

        given: 'some setup'
        sut << 'set-up'

        when: 'an error is thrown'
        throw new IllegalStateException( 'forced to fail' )

        then: 'does not get called'
        false

        cleanup: 'but cleanup does'
        println 'cleanup called!'
    }

    def 'showcase waiting for an asynchronous event'() {

        given: 'some setup'
        def conditions = new PollingConditions( timeout: 5, initialDelay: 0.5 )

        when: 'a background task is fired'
        Thread.start { Thread.sleep( 2000 ) ; sut << 'completed' }

        then: 'wait for the task to complete'
        conditions.eventually {
            assert sut.find { it == 'completed' }
        }
    }

    @See( 'http://confluence/feature/foo' )
    @Issue( 'http://jira/issue/123' )
    def 'showcase documentation annotations'() {

        given: 'some setup'
        def conditions = new PollingConditions( timeout: 5, initialDelay: 0.5 )

        when: 'a background task is fired'
        Thread.start { Thread.sleep( 1000 ) ; sut << 'completed' }

        then: 'wait for the task to complete'
        conditions.eventually {
            assert sut.find { it == 'completed' }
        }
    }

    @SuppressWarnings( 'GroovyUnreachableStatement' )
    def 'showcase exception tool'() {

        when: 'a nested error is thrown'
        throw new IllegalStateException( new UnsupportedOperationException( 'forced to fail' ) )

        then: 'proper error is thrown'
        def e = thrown( IllegalStateException )
        Exceptions.getRootCause( e ).class == UnsupportedOperationException
    }

}

// normally, this would be in a separate file
trait DatabaseTrait {
    @Shared
    List<String> databaseConnection = []

    def setupSpec() { databaseConnection = ['setup'] ; println 'setting up the database connection' }
    def cleanupSpec() { databaseConnection.clear() ; println 'tearing down the database connection' }
}

class CustomRule implements TestRule {
    @Override
    Statement apply( final Statement base, final Description description ) {

        println "custom JUnit rule invoked for ${description.displayName}"
        base
    }
}
