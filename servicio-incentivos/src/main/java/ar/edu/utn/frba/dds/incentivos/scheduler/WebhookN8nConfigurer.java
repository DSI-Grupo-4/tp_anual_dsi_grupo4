package ar.edu.utn.frba.dds.incentivos.scheduler;

import ar.edu.utn.frba.dds.incentivos.consultor.Consultor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class WebhookN8nConfigurer implements ApplicationRunner {

    private final String webhookN8nUrl;

    public WebhookN8nConfigurer(@Value("${incentivos.webhook.n8n-url:}") String webhookN8nUrl) {
        this.webhookN8nUrl = webhookN8nUrl;
    }

    @Override
    public void run(ApplicationArguments args) {
        Consultor.getInstance().configurarWebhookN8n(webhookN8nUrl);
    }
}
