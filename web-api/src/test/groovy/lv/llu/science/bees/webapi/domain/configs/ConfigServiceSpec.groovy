package lv.llu.science.bees.webapi.domain.configs

import lv.llu.science.bees.webapi.domain.auth0.SamsUsers
import lv.llu.science.bees.webapi.domain.nodes.Node
import lv.llu.science.bees.webapi.domain.nodes.NodeService
import lv.llu.science.bees.webapi.utils.CurrentUser
import lv.llu.science.bees.webapi.utils.TimeMachine
import org.springframework.data.domain.Sort
import spock.lang.Specification

import static java.util.Optional.ofNullable

class ConfigServiceSpec extends Specification {

    def repository = Mock(ConfigRepository)
    def nodeService = Mock(NodeService)
    def samsUsers = Mock(SamsUsers) {
        getUserMap() >> Stub(Map) {
            getOrDefault(_, _) >> { String name, String d ->
                name + " full name"
            }
        }
    }
    def currentUser = Mock(CurrentUser)
    def timeMachine = Mock(TimeMachine)

    def service = new ConfigService(repository, nodeService, samsUsers, currentUser, timeMachine)

    def "should get all configs"() {
        when:
            def list = service.getAllConfigs()
        then:
            1 * repository.findAll(Sort.by('name')) >> [new Config(id: '123', changedBy: 'john')]
            1 * nodeService.getDevicesByHwConfig('123') >> [new Node(name: 'test device')]
            list[0].id == '123'
            list[0].changedBy == 'john full name'
            list[0].devices[0].name == 'test device'
    }

    def "should get config details"() {
        when:
            def bean = service.getConfigDetails('123')
        then:
            1 * repository.findById('123') >> Optional.of(new Config(id: '123'))
            bean.id == '123'
    }

    def "should create new config"() {
        when:
            def result = service.createConfig(new ConfigBean(name: 'test config', config: [aaa: 111, bbb: 222]))
        then:
            1 * repository.save(_) >> { Config config ->
                assert config.name == 'test config'
                assert config.config['aaa'] == 111
                assert config.changedBy == 'john'
                config.id = 'new-id'
                config
            }

            1 * currentUser.getUsername() >> 'john'
            1 * timeMachine.zonedNow()

            result.id == 'new-id'
    }

    def "should edit config"() {
        when:
            def result = service.editConfig('123', new ConfigBean(name: 'new name', config: [bbb: 222]))
        then:
            1 * repository.findById('123') >> Optional.of(new Config(name: 'old name', config: [aaa: 111]))
            1 * repository.save(_) >> { Config config ->
                assert config.name == 'new name'
                assert config.config == ['bbb': 222]
                assert config.changedBy == 'john'
                config
            }

            1 * currentUser.getUsername() >> 'john'
            1 * timeMachine.zonedNow()

            result.name == 'new name'
    }

    def "should delete config"() {
        given:
            def config = new Config()
        when:
            service.deleteConfig('123')
        then:
            1 * repository.findById('123') >> Optional.of(config)
            1 * repository.delete(_)
    }

    def "should set default config"() {
        given:
            def newConfig = new Config()
            def oldConfig = new Config()
        when:
            service.setDefault('123')
        then:
            1 * repository.getDefault() >> Optional.of(oldConfig)
            1 * repository.save(oldConfig) >> { Config c ->
                assert !c.isDefault
            }
            1 * repository.findById('123') >> Optional.of(newConfig)
            1 * repository.save(newConfig) >> { Config c ->
                assert c.isDefault
                c
            }
    }

    def "should get device config"() {
        when:
            def result = service.getDeviceConfig()
        then:
            1 * nodeService.getOwnDevice() >> new Node(hwConfigId: '123')
            numById * repository.findById('123') >> ofNullable(byIdConfig)
            numDefault * repository.getDefault() >> ofNullable(defaultConfig)
            result == expected
        where:
            byIdConfig                     | defaultConfig                  | numById | numDefault | expected
            new Config(config: [aaa: 111]) | new Config(config: [bbb: 222]) | 1       | 0          | [aaa: 111]
            null                           | new Config(config: [bbb: 222]) | 1       | 1          | [bbb: 222]
            null                           | null                           | 1       | 1          | null
    }
}
