package lv.llu.science.bees.webapi.web.helpers

import lv.llu.science.bees.webapi.domain.nodes.Node
import lv.llu.science.bees.webapi.domain.nodes.NodeRepository
import lv.llu.science.bees.webapi.domain.workspaces.ActiveWorkspace
import lv.llu.science.bees.webapi.domain.workspaces.Workspace
import lv.llu.science.bees.webapi.domain.workspaces.WorkspaceRepository
import lv.llu.science.bees.webapi.web.ActiveWorkspaceFilter
import org.junit.Rule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.restdocs.JUnitRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration

@SpringBootTest
@WithMockUser
@ActiveProfiles(["mockDwh"])
@AutoConfigureWireMock(port = 8765)
abstract class RestDocsSpecification extends Specification {

    @Rule
    JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation()

    @Autowired
    WebApplicationContext context

    @Autowired
    ActiveWorkspace activeWorkspace

    @Autowired
    WorkspaceRepository workspaceRepository

    @Autowired
    NodeRepository nodeRepository

    MockMvc mvc

    def setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation)
                        .operationPreprocessors()
                        .withResponseDefaults(Preprocessors.prettyPrint())
                        .withRequestDefaults(Preprocessors.prettyPrint())
                        .and()
                        .uris()
                        .withScheme('https')
                        .withHost('sams.science.itf.llu.lv/api')
                        .withPort(443)
                )
                .addFilter(new TestHeaderFilter())
                .addFilter(new ActiveWorkspaceFilter(activeWorkspace))
                .build()
    }

    def cleanup() {
        workspaceRepository.deleteAll()
        nodeRepository.deleteAll()
    }

    def setupDefaultWorkspace() {
        workspaceRepository.insert(new Workspace(id: 'ws1', owner: 'user'))
    }

    def setupDefaultNodes() {
        (1..10).each {
            nodeRepository.insert(new Node(id: "hive${it}", workspaceId: 'ws1'))
        }
    }
}
