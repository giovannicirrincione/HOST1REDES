package REDES.TP2.CAPAS;

import REDES.TP2.Package;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/host1")
public class Host1ENLACE implements Host1ENLACEInterface{

    static final String FLAG = "01111110"; // Delimitador de inicio y fin de trama
    static final String ESC = "01111101"; // Carácter de escape

    static final String polinomioGenerador = "1101";



    //recibe el paquete de la capa de red lo convierte a binario y lo manda a entramar
    @PostMapping("/receive")
    public  void administrarPaquete(@RequestBody Integer paquete) {


        // Convertir el número entero a su representación binaria

        String binaryRepresentation = Integer.toBinaryString(paquete);
        System.out.println("converti" + paquete +"a"+binaryRepresentation);

        //mandamos a entramar
        String paqueteEntramado = entramarPaquete(binaryRepresentation);

        //aplicamos CRC a los datos

        String paqueteEntramadoConCRC = agregarCRC(paqueteEntramado,polinomioGenerador);

        String paqueteEnviado = mandarPaquete(paqueteEntramadoConCRC);

        System.out.println("Se mando al host 2 el paquete" + paqueteEnviado);


    }

    //usamos el metodo byte stuffing
    public static String entramarPaquete(String paquete) {
        StringBuilder tramaConStuffing = new StringBuilder();

        // Añadimos FLAG al inicio
        tramaConStuffing.append(FLAG);

        // Recorremos los datos binarios y aplicamos el byte stuffing
        for (int i = 0; i < paquete.length(); i += 8) {
            // Tomamos de a 8 bits (1 byte)
            String byteActual = paquete.substring(i, Math.min(i + 8, paquete.length()));

            // Si encontramos el FLAG o ESC, aplicamos byte stuffing
            if (byteActual.equals(FLAG) || byteActual.equals(ESC)) {
                tramaConStuffing.append(ESC);  // Insertamos carácter de escape
            }
            tramaConStuffing.append(byteActual);  // Agregamos el byte original
        }

        // Añadimos FLAG al final
        tramaConStuffing.append(FLAG);

        return tramaConStuffing.toString();

    }
    public static String aplicarCrc(String paquete, String divisor){
        int longitudDivisor = divisor.length();
        String temp = paquete.substring(0, longitudDivisor);

        for (int i = longitudDivisor; i < paquete.length(); i++) {
            if (temp.charAt(0) == '1') {
                StringBuilder tempBuilder = new StringBuilder(temp);
                for (int j = 0; j < longitudDivisor; j++) {
                    tempBuilder.setCharAt(j, (tempBuilder.charAt(j) == divisor.charAt(j)) ? '0' : '1');
                }
                temp = tempBuilder.toString();
            }
            temp = temp.substring(1) + paquete.charAt(i);
        }

        // Última operación XOR si el primer bit es 1
        if (temp.charAt(0) == '1') {
            StringBuilder tempBuilder = new StringBuilder(temp);
            for (int j = 0; j < longitudDivisor; j++) {
                tempBuilder.setCharAt(j, (tempBuilder.charAt(j) == divisor.charAt(j)) ? '0' : '1');
            }
            temp = tempBuilder.toString();
        }

        return temp.substring(1); // Retornamos el residuo (sin el bit más significativo)
    }

    public static String agregarCRC(String paquete, String polinomioGenerador){
        String datosConPadding = paquete + "0".repeat(polinomioGenerador.length() - 1); // Agregamos ceros al final
        String crc = aplicarCrc(datosConPadding, polinomioGenerador);
        return paquete + crc; // Concatenamos los datos con el CRC calculado

    }
    //ahora lo mandamos de la capa de enlace pero hay q crear un objeto capa fisica y mandarlo por ahi
    public static String mandarPaquete(String paquete){

        // Crear una instancia de RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        String url = "http://localhost:8081/host2/receive";



        // Enviar el paquete a Host 2
        String response = restTemplate.postForObject(url, paquete, String.class);

        return "Respuesta de Host 2: " + response;
    }
}
