package lv.llu.science.bees.webapi.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.time.*;

@Component
@RequestScope
public class TimeMachine {

    private final ZoneId defaultZone = ZoneId.systemDefault();

    @Setter
    @Getter
    private Clock clock = Clock.systemDefaultZone();

    /**
     * @deprecated: Use {@link #zonedNow()} instead, it is more convenient to store and convert.
     */
    @Deprecated
    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    public ZonedDateTime zonedNow() {
        return ZonedDateTime.now(clock);
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    public void fixedAt(LocalDateTime time) {
        this.clock = Clock.fixed(time.atZone(defaultZone).toInstant(), defaultZone);
    }
}
