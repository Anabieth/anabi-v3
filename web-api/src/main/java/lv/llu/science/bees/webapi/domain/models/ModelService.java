package lv.llu.science.bees.webapi.domain.models;

import lv.llu.science.bees.webapi.domain.nodes.NodeRepository;
import lv.llu.science.bees.webapi.domain.workspaces.ActiveWorkspace;
import lv.llu.science.bees.webapi.dwh.DwhClient;
import lv.llu.science.bees.webapi.utils.CurrentUser;
import lv.llu.science.bees.webapi.utils.ModelMapperEx;
import lv.llu.science.bees.webapi.utils.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static java.util.Comparator.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class ModelService {

    private final ModelRepository repository;
    private final DwhClient dwhClient;
    private final ModelMapperEx mapper;
    private final CurrentUser currentUser;
    private final ActiveWorkspace activeWorkspace;
    private final NodeRepository nodeRepository;
    private final MongoOperations operations;

    @Autowired
    public ModelService(ModelRepository repository, DwhClient dwhClient, ModelMapperEx mapper, CurrentUser currentUser,
                        ActiveWorkspace activeWorkspace, NodeRepository nodeRepository, MongoOperations operations) {
        this.repository = repository;
        this.dwhClient = dwhClient;
        this.mapper = mapper;
        this.currentUser = currentUser;
        this.activeWorkspace = activeWorkspace;
        this.nodeRepository = nodeRepository;
        this.operations = operations;
    }

    @PreAuthorize("@activeWorkspace.id != null")
    public List<ModelDefinitionBean> listModelDefinitions() {
        List<ModelDefinition> list = repository.findAllByWorkspaceId(activeWorkspace.getId());
        return mapper.mapList(list, ModelDefinitionBean.class);
    }

    private ModelTemplate getModelTemplate(String modelCode) {
        return dwhClient.getModelList().stream()
                .filter(m -> m.getCode().equals(modelCode))
                .findFirst()
                .orElseThrow(NotFoundException::new);
    }

    private void checkAccessRights(ModelDefinitionBean bean) {
        ModelTemplate template = getModelTemplate(bean.getModelCode());

        boolean passed = template.getParams().stream()
                .filter(param -> "nodeId".equals(param.getType()))
                .map(param -> bean.getParams().get(param.getCode()))
                .map(nodeId -> nodeRepository.getNode((String) nodeId)) // implicit check that user has access to the node
                .allMatch(node -> activeWorkspace.getId().equals(node.getWorkspaceId()));

        if (!passed) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Trying to use node from another workspace");
        }
    }

    private void checkDuplicateDefinitions(ModelDefinitionBean bean, String ownId) {
        ModelTemplate template = getModelTemplate(bean.getModelCode());

        String paramCode = template.getParams().stream()
                .min(comparing(ModelTemplateParam::getMaster, nullsLast(naturalOrder())))
                .map(ModelTemplateParam::getCode)
                .orElseThrow();

        Criteria criteria = where("modelCode").is(bean.getModelCode())
                .and("params." + paramCode).is(bean.getParams().get(paramCode));
        if (ownId != null) {
            criteria = criteria.and("_id").ne(bean.getId());
        }

        boolean duplicatesExist = operations.exists(Query.query(criteria), ModelDefinition.class);

        if (duplicatesExist) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Model definition for this node already exist");
        }
    }

    private void saveToDwh(ModelDefinition saved) {
        ModelDefinitionBean bean = new ModelDefinitionBean();
        bean.setId(saved.getId());
        bean.setParams(saved.getParams());
        dwhClient.saveModelDefinition(saved.getModelCode(), bean);
    }

    @PreAuthorize("@activeWorkspace.id != null")
    public ModelDefinitionBean createModelDefinition(ModelDefinitionBean bean) {
        checkAccessRights(bean);
        checkDuplicateDefinitions(bean, null);
        ModelDefinition entity = mapper.map(bean, ModelDefinition.class);
        entity.setWorkspaceId(activeWorkspace.getId());
        entity.setCreatedBy(currentUser.getUsername());
        ModelDefinition saved = repository.save(entity);
        saveToDwh(saved);
        return mapper.map(saved, ModelDefinitionBean.class);
    }

    @PreAuthorize("@user.hasModelAccess(#id)")
    public ModelDefinitionBean editModelDefinition(String id, ModelDefinitionBean bean) {
        checkAccessRights(bean);
        checkDuplicateDefinitions(bean, id);
        ModelDefinition one = repository.findById(id).orElseThrow(NotFoundException::new);
        one.setModelCode(bean.getModelCode());
        one.setParams(bean.getParams());
        ModelDefinition saved = repository.save(one);
        saveToDwh(saved);
        return mapper.map(saved, ModelDefinitionBean.class);
    }

    @PreAuthorize("@user.hasModelAccess(#id)")
    public ModelDefinitionBean getModelDefinition(String id) {
        ModelDefinition one = repository.findById(id).orElseThrow(NotFoundException::new);
        return mapper.map(one, ModelDefinitionBean.class);
    }

    @PreAuthorize("@user.hasModelAccess(#id)")
    public void deleteModelDefinition(String id) {
        ModelDefinition one = repository.findById(id).orElseThrow(NotFoundException::new);
        repository.delete(one);
        dwhClient.deleteModelDefinition(one.getModelCode(), one.getId());
    }

    public List<ModelTemplate> listModelTemplates() {
        return dwhClient.getModelList();
    }
}
