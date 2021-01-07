package lv.llu.science.bees.webapi.domain.datain;

import feign.FeignException;
import lv.llu.science.bees.webapi.domain.deviceLog.DataInLogRecord;
import lv.llu.science.bees.webapi.domain.deviceLog.DeviceLogService;
import lv.llu.science.bees.webapi.domain.mapping.MappingService;
import lv.llu.science.bees.webapi.domain.mapping.SourceMapping;
import lv.llu.science.bees.webapi.domain.nodes.NodeRepository;
import lv.llu.science.bees.webapi.domain.nodes.NodeService;
import lv.llu.science.bees.webapi.domain.nodes.latestValues.NodeLatestValueService;
import lv.llu.science.bees.webapi.dwh.DwhClient;
import lv.llu.science.bees.webapi.dwh.DwhDataSetBean;
import lv.llu.science.bees.webapi.dwh.DwhValueBean;
import lv.llu.science.bees.webapi.utils.NotFoundException;
import lv.llu.science.bees.webapi.utils.TimeMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static lv.llu.science.bees.webapi.domain.datain.DataInMappingResult.*;

@Service
public class DataInService {
    private final DwhClient dwhClient;
    private final MappingService mappingService;
    private final TimeMachine timeMachine;
    private final NodeService nodeService;
    private final NodeLatestValueService latestValueService;
    private final DeviceLogService logService;
    private final NodeRepository nodeRepository;
    private final Validator validator;

    @Autowired
    public DataInService(DwhClient dwhClient, MappingService mappingService,
                         TimeMachine timeMachine, NodeService nodeService,
                         NodeLatestValueService latestValueService, DeviceLogService logService,
                         NodeRepository nodeRepository, Validator validator) {
        this.dwhClient = dwhClient;
        this.mappingService = mappingService;
        this.timeMachine = timeMachine;
        this.nodeService = nodeService;
        this.latestValueService = latestValueService;
        this.logService = logService;
        this.nodeRepository = nodeRepository;
        this.validator = validator;
    }

    @PreAuthorize("@user.isDevice()")
    public Map<String, DataInMappingResult> addMeasurements(List<DataInBean> dataInBeans) {
        HashMap<String, DataInMappingResult> results = new HashMap<>();
        for (DataInBean bean : dataInBeans) {
            if (bean.getTint() != null) {
                //First element is the oldest
                int counter = bean.getValues().size() - 1;
                ZonedDateTime now = timeMachine.zonedNow();
                for (DwhValueBean b : bean.getValues()) {
                    b.setTs(now.minusSeconds((counter--) * bean.getTint()));
                }
            }
            Set<ConstraintViolation<DataInBean>> violations = validator.validate(bean);
            if (!violations.isEmpty()) {
                results.put(bean.getSourceId(), ValidationError);
                registerSourceEvent(bean.getSourceId(), ValidationError);
            } else {
                try {
                    SourceMapping mapping = mappingService.getMapping(bean.getSourceId());
                    DwhDataSetBean dataBean = new DwhDataSetBean();
                    dataBean.setObjectId(mapping.getNodeId());
                    dataBean.setType(mapping.getValueKey());
                    dataBean.setValues(bean.getValues());
                    dwhClient.sendData(dataBean);
                    latestValueService.pushLatestValues(mapping.getNodeId(), mapping.getValueKey(), bean.getValues());
                    results.put(bean.getSourceId(), Ok);
                    registerSourceEvent(bean.getSourceId(), Ok, mapping.getValueKey(), mapping.getNodeId());
                } catch (AccessDeniedException ex) {
                    results.put(bean.getSourceId(), AccessDenied);
                    registerSourceEvent(bean.getSourceId(), AccessDenied);
                } catch (NotFoundException ex) {
                    results.put(bean.getSourceId(), NotFound);
                    registerSourceEvent(bean.getSourceId(), NotFound);
                } catch (FeignException ex) {
                    results.put(bean.getSourceId(), CoreTemporarilyUnavailable);
                    registerSourceEvent(bean.getSourceId(), CoreTemporarilyUnavailable);
                }
            }
        }

        return results;
    }

    private void registerSourceEvent(String sourceId, DataInMappingResult result) {
        registerSourceEvent(sourceId, result, null, null);
    }

    private void registerSourceEvent(String sourceId, DataInMappingResult result,
                                     String type, String nodeId) {

        DataInLogRecord record = new DataInLogRecord();
        record.setTs(timeMachine.zonedNow());
        record.setSourceId(sourceId);
        record.setResult(result);
        if (result == Ok) {
            record.setType(type);
            record.setNodeId(nodeId);
            record.setNodeName(nodeRepository.getNode(nodeId).getName());
        }

        logService.addDataInLogRecord(record);
    }
}
