package lv.llu.science.bees.webapi.domain.configs;

import lv.llu.science.bees.webapi.domain.auth0.SamsUsers;
import lv.llu.science.bees.webapi.domain.nodes.Node;
import lv.llu.science.bees.webapi.domain.nodes.NodeService;
import lv.llu.science.bees.webapi.utils.CurrentUser;
import lv.llu.science.bees.webapi.utils.NotFoundException;
import lv.llu.science.bees.webapi.utils.TimeMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
public class ConfigService {

    private final ConfigRepository repository;
    private final NodeService nodeService;
    private final SamsUsers samsUsers;
    private final CurrentUser currentUser;
    private final TimeMachine timeMachine;

    @Autowired
    public ConfigService(ConfigRepository repository, NodeService nodeService, SamsUsers samsUsers, CurrentUser currentUser, TimeMachine timeMachine) {
        this.repository = repository;
        this.nodeService = nodeService;
        this.samsUsers = samsUsers;
        this.currentUser = currentUser;
        this.timeMachine = timeMachine;
    }

    @PreAuthorize("@user.isHwEngineer()")
    public List<ConfigBean> getAllConfigs() {
        return repository.findAll(Sort.by("name")).stream()
                .map(config -> {
                    ConfigBean bean = transform(config);
                    bean.setDevices(nodeService.getDevicesByHwConfig(bean.getId()));
                    return bean;
                })
                .collect(Collectors.toList());
    }

    private ConfigBean transform(Config config) {
        ConfigBean bean = new ConfigBean();
        bean.setId(config.getId());
        bean.setName(config.getName());
        bean.setConfig(config.getConfig());
        bean.setChangedBy(username2name(config.getChangedBy()));
        bean.setChangedTs(config.getChangedTs());
        bean.setIsDefault(config.getIsDefault());
        return bean;
    }

    private void transform(ConfigBean bean, Config entity) {
        entity.setName(bean.getName());
        entity.setConfig(bean.getConfig());
        entity.setChangedBy(currentUser.getUsername());
        entity.setChangedTs(timeMachine.zonedNow());
    }

    private String username2name(String username) {
        return samsUsers.getUserMap().getOrDefault(username, username);
    }

    @PreAuthorize("@user.isHwEngineer()")
    public ConfigBean getConfigDetails(String id) {
        Config config = repository.findById(id).orElseThrow(NotFoundException::new);
        return transform(config);
    }

    @PreAuthorize("@user.isHwEngineer()")
    public ConfigBean createConfig(ConfigBean bean) {
        Config entity = new Config();
        transform(bean, entity);
        Config saved = repository.save(entity);
        return transform(saved);
    }

    @PreAuthorize("@user.isHwEngineer()")
    public ConfigBean editConfig(String id, ConfigBean bean) {
        Config entity = repository.findById(id).orElseThrow(NotFoundException::new);
        transform(bean, entity);
        Config saved = repository.save(entity);
        return transform(saved);
    }

    @PreAuthorize("@user.isHwEngineer()")
    public void deleteConfig(String id) {
        Config entity = repository.findById(id).orElseThrow(NotFoundException::new);
        repository.delete(entity);
    }

    @PreAuthorize("@user.isHwEngineer()")
    public ConfigBean setDefault(String id) {
        Config entity = repository.findById(id).orElseThrow(NotFoundException::new);

        repository.getDefault().ifPresent(oldDefault -> {
            oldDefault.setIsDefault(null);
            repository.save(oldDefault);
        });

        entity.setIsDefault(true);
        Config saved = repository.save(entity);
        return transform(saved);
    }

    @PreAuthorize("@user.isDevice()")
    public Map<String, Object> getDeviceConfig() {
        Node device = nodeService.getOwnDevice();

        return ofNullable(device.getHwConfigId())
                .flatMap(repository::findById)
                .or(repository::getDefault)
                .map(Config::getConfig)
                .orElse(null);
    }
}
