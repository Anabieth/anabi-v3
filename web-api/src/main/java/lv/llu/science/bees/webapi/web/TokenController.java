package lv.llu.science.bees.webapi.web;

import com.nimbusds.jose.JOSEException;
import lv.llu.science.bees.webapi.domain.tokens.DeviceTokenService;
import lv.llu.science.bees.webapi.domain.tokens.TokenRequestBean;
import lv.llu.science.bees.webapi.domain.tokens.TokenResponseBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/token")
public class TokenController {

    private final DeviceTokenService service;

    @Autowired
    public TokenController(DeviceTokenService service) {
        this.service = service;
    }

    @PostMapping
    public TokenResponseBean getToken(@RequestBody TokenRequestBean request) {
        try {
            return service.getToken(request);
        } catch (JOSEException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
