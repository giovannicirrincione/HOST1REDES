package REDES.TP2.CAPAS;

import REDES.TP2.Package;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/host1")
public class Host1RED {

    @Autowired
    private Host1ENLACE host1ENLACE;

    private final RestTemplate restTemplate;


    public Host1RED(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/communicate")
    public String communicateWithHost2() {
        String url = "http://localhost:8081/host2/message";
        return restTemplate.getForObject(url, String.class);
    }

    @PostMapping("/mandarPaquete")
    public String mandarPaqueteACapaEnlace(@RequestBody Package myPackage) {


        //seteamos id y timestamp
        myPackage.setId(1);
        myPackage.setTimestamp(LocalDateTime.now().toString());

        // Enviar el paquete a capa enlace
        String paqueteEnviado = host1ENLACE.administrarPaquete(myPackage);


        return ":   " + paqueteEnviado;
    }
}