package REDES.TP2.CAPAS;

import REDES.TP2.Package;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import java.time.LocalDateTime;

@RestController
public class Host1ENLACE implements Host1ENLACEInterface{

    static final String FLAG = "01111110"; // Delimitador de inicio y fin de trama
    static final String ESC = "01111101"; // Carácter de escape

    static final String polinomioGenerador = "1101";

    @Autowired
    Host1FISICA host1FISICA;



    //recibe el paquete de la capa de red lo convierte a binario y lo manda a entramar
    public<T>  String administrarPaquete(@RequestBody Package<T> paquete) {

        String binaryRepresentation = "";

        //obtenemos el dato content (como puede ser entero o string usamos generico, tenemos que hacer algunas confirmaciones)

        T content = paquete.getContent();  // Obtenemos el contenido con el tipo T

        // Podemos utilizar el contenido dependiendo de su tipo

        if (content instanceof Integer) {

            Integer dato = (Integer) content;

            // Convertir el número entero a su representación binaria

             binaryRepresentation = Integer.toBinaryString(dato);


        } else if (content instanceof String) {

            String dato = (String) content;

            //Convertir el String a su representacion binaria

             binaryRepresentation = stringToBinary(dato);

        }


        //mandamos a entramar
        String paqueteEntramado = entramarPaquete(binaryRepresentation);
        System.out.println("Paquete entramado (sin CRC): " + paqueteEntramado);

        //aplicamos CRC a los datos

        String paqueteEntramadoConCRC = agregarCRC(paqueteEntramado,polinomioGenerador);

        System.out.println("Paquete entramado con CRC: " + paqueteEntramadoConCRC);

        String paqueteEnviado = host1FISICA.mandarPaquete(paqueteEntramadoConCRC);


        return paqueteEnviado;

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

    String agregarCRC(String paquete, String polinomioGenerador) {
        String datosConPadding = paquete + "0".repeat(polinomioGenerador.length() - 1); // Agregamos ceros
        String crc = aplicarCrc(datosConPadding, polinomioGenerador);
        return paquete + " " + crc; // Concatenamos los datos con el CRC calculado
    }
    //ahora lo mandamos de la capa de enlace pero hay q crear un objeto capa fisica y mandarlo por ahi


    //Metodo para convertir un string en su representacion Binaria
    public static String stringToBinary(String input) {
        StringBuilder binary = new StringBuilder();

        for (char character : input.toCharArray()) {
            // Convertir cada carácter a su valor numérico y luego a binario
            String binaryString = Integer.toBinaryString(character);

            // Asegurarse de que cada carácter esté representado en 8 bits (1 byte)
            while (binaryString.length() < 8) {
                binaryString = "0" + binaryString;
            }

            binary.append(binaryString).append(" ");  // Agregar un espacio entre cada byte
        }

        return binary.toString().trim();  // Remover el espacio final
    }

    @PostMapping("/host1/ack")
    public String recibirAcuseRecibo(@RequestBody String acuseRecibo) {
        System.out.println("Acuse de recibo recibido en Host 1: " + acuseRecibo);

        // Comprobar si es un ACK o NACK
        if ("ACK".equals(acuseRecibo)) {
            System.out.println("Transmisión exitosa. Datos recibidos correctamente.");
        } else if ("NACK".equals(acuseRecibo)) {
            System.out.println("Error en la transmisión. CRC inválido.");
            // Aquí puedes decidir retransmitir el paquete o tomar otra acción
        }

        return "Acuse de recibo procesado.";
    }
}
