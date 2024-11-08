package REDES.TP2.CAPAS;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
@Service
public class Host1FISICA implements Host1FISICAInterface {
    @Override

    public String mandarPaquete(String paquete){

        // Crear una instancia de RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        String url = "http://localhost:8081/host2/receive";



        // Enviar el paquete a Host 2
        String response = restTemplate.postForObject(url, paquete, String.class);

        System.out.println("----------------Se mando al host 2 el paquete-------------- " + response + "----------");

        return response;
    }
}
