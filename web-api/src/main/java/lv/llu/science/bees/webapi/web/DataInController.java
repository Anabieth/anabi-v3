package lv.llu.science.bees.webapi.web;

import lv.llu.science.bees.webapi.domain.datain.DataInBean;
import lv.llu.science.bees.webapi.domain.datain.DataInMappingResult;
import lv.llu.science.bees.webapi.domain.datain.DataInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/data")
@Validated
public class DataInController {
    private final DataInService dataService;

    @Autowired
    public DataInController(DataInService dataService) {
        this.dataService = dataService;
    }

    @PostMapping
    public Map<String, DataInMappingResult> addMeasurements(@RequestBody List<DataInBean> data) {
        return dataService.addMeasurements(data);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleException(ConstraintViolationException ex, WebRequest request) {
        DefaultErrorAttributes error = new DefaultErrorAttributes();
        Map<String, Object> map = error.getErrorAttributes(request, false);
        map.put("status", HttpStatus.BAD_REQUEST.value());
        map.put("error", "Input data validation error");
        return map;
    }

}
