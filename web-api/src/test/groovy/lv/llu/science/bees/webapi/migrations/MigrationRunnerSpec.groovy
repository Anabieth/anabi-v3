package lv.llu.science.bees.webapi.migrations

import spock.lang.Specification

class MigrationRunnerSpec extends Specification {

    def "should execute all migrations on startup"() {
        given:
            def migrations = [
                    Mock(Migration),
                    Mock(Migration)
            ]
            def runner = new MigrationRunner(migrations)

        when:
            runner.onEvent(null)
        then:
            migrations.each {
                1 * it.execute()
            }
    }
}
