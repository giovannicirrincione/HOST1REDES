package REDES.TP2.CAPAS;

import REDES.TP2.Package;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/host1")
public class Host1RED {

    private final RestTemplate restTemplate;

    public Host1RED(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/communicate")
    public String communicateWithHost2() {
        String url = "http://localhost:8081/host2/message";
        return restTemplate.getForObject(url, String.class);
    }

    @PostMapping("/send")
    public String sendPackageToHost2(@RequestBody Package myPackage) {
        String url = "http://localhost:8081/host2/receive";
        //seteamos id y timestamp
        myPackage.setId(1);
        myPackage.setTimestamp(LocalDateTime.now().toString());

        // Enviar el paquete a Host 2
        String response = restTemplate.postForObject(url, myPackage, String.class);

        return "Respuesta de Host 2: " + response;
    }
}