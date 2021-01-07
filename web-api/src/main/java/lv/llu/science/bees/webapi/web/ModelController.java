package lv.llu.science.bees.webapi.web;

import lv.llu.science.bees.webapi.domain.models.ModelDefinitionBean;
import lv.llu.science.bees.webapi.domain.models.ModelService;
import lv.llu.science.bees.webapi.domain.models.ModelTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/models")
public class ModelController {

    private final ModelService service;

    @Autowired
    public ModelController(ModelService service) {
        this.service = service;
    }

    @GetMapping("/templates")
    public List<ModelTemplate> listModelTemplates() {
        return service.listModelTemplates();
    }

    @PostMapping
    public ModelDefinitionBean createModelDefinition(@RequestBody ModelDefinitionBean bean) {
        return service.createModelDefinition(bean);
    }

    @GetMapping
    public List<ModelDefinitionBean> listModelDefinitions() {
        return service.listModelDefinitions();
    }

    @GetMapping("{id}")
    public ModelDefinitionBean getModelDefinition(@PathVariable String id) {
        return service.getModelDefinition(id);
    }

    @PutMapping("{id}")
    public ModelDefinitionBean editModelDefinition(@PathVariable String id, @RequestBody ModelDefinitionBean bean) {
        return service.editModelDefinition(id, bean);
    }

    @DeleteMapping("{id}")
    public void deleteModelDefinition(@PathVariable String id, HttpServletResponse response) {
        service.deleteModelDefinition(id);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }


}
