package lv.llu.science.bees.webapi.migrations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MigrationRunner {

    private final List<Migration> migrations;

    @Autowired
    public MigrationRunner(List<Migration> migrations) {
        this.migrations = migrations;
    }

    @EventListener
    public void onEvent(ContextRefreshedEvent event) {
        migrations.forEach(Migration::execute);
    }
}
