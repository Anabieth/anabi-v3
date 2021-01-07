package lv.llu.science.bees.webapi.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class DwhCoreUnavailableException extends RuntimeException {
    public DwhCoreUnavailableException() {
        super("DWH core temporary unavailable");
    }
}
