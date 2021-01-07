package lv.llu.science.bees.webapi.domain.workspaces

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@SpringBootTest
@ActiveProfiles('embedded')
class WorkspaceRepositorySpec extends Specification {
    @Autowired
    WorkspaceRepository repository

    def setup() {
        repository.deleteAll()
        repository.save(new Workspace(id: '111', name: 'First', owner: 'userA'))
        repository.save(new Workspace(id: '222', name: 'Second', owner: 'userB'))
        repository.save(new Workspace(id: '333', name: 'Third', owner: 'userC', invitedUsers: ['userA']))
    }

    def cleanup() {
        repository.deleteAll()
    }

    def 'should find workspace by user name'() {
        when:
            def listA = repository.findByUser('userA')
        then:
            listA.size() == 2
            listA.collect { it.id } == ['111', '333']

        when:
            def listB = repository.findByUser('userB')
        then:
            listB.size() == 1
            listB[0].id == '222'
    }
}
