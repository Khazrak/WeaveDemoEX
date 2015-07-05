package demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * Created by khazrak on 2015-06-27.
 */
@org.springframework.web.bind.annotation.RestController
@RequestMapping("/rest")
public class RestController {

    private static final int RETRIES = 10;

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public String getFromService() {

        int tries = 0;

        ResourceBundle properties = ResourceBundle.getBundle("service");

        String serviceHostname = properties.getString("servicename");

        RestTemplate restTemplate = new RestTemplate();


        String res = null;

        boolean loop = true;

        while(loop) {

            ResponseEntity<String> response = restTemplate.getForEntity(
                    "http://" + serviceHostname + ":8080/rest/hostname",
                    String.class);

            if (HttpStatus.OK == response.getStatusCode()) {
                res = response.getBody();
                loop = false;
            } else {
                sleep(10);
                tries++;
                if(tries >= 10)
                {
                    loop = true;
                    res = "Service Unavailable, try again later";
                }
            }
        }



        //String res = restTemplate.getForObject("http://" + serviceHostname + ":8080/rest/hostname", String.class);

        String sysEnvStr = System.getenv("SERVICE_NAME");

        if(sysEnvStr == null)
        {
            sysEnvStr = "Unknown";
        }

        return "Consumer: " + sysEnvStr + ", Producer: " + res + "\n";
    }

    private void sleep(int seconds)
    {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
