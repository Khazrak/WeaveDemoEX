package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * Created by khazrak on 2015-06-27.
 */
@org.springframework.web.bind.annotation.RestController
@RequestMapping("/rest")
public class RestController {

    private static final int RETRIES = 10;
    private ResourceBundle properties;
    private String serviceHostname;
    private int tries;
    private boolean retry;
    private String sysEnvStr;

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public String getFromService() {

        resetValues();
        ResponseEntity<String> remoteResponse = null;



        do {

            System.out.println("Trying REST");
            try {
                System.out.println("Time: "+ LocalDateTime.now());
                remoteResponse = restTemplate.getForEntity(
                        "http://" + serviceHostname + ":8080/rest/hostname",
                        String.class);
                retry = false;
            } catch (Exception e) {
                exceptionHandling(e);
            }
            tries++;
            System.out.println("Tries: "+tries);
            if(tries > RETRIES)
            {
                //to many tries, no services available
                retry = false;
            }
        } while(retry);

        return "Consumer: " + sysEnvStr + ", Producer: " + createAnswer(remoteResponse) + "\n";
    }


    private String createAnswer(ResponseEntity<String> remoteResponse) {

        String temp = null;

        if(remoteResponse == null)
        {
            temp = "Service is down.";
        }
        else
        {
            temp = remoteResponse.getBody();
        }

        return temp;
    }

    private void exceptionHandling(Exception e) {
        if (e instanceof java.net.ConnectException) {
            System.out.println("Caught ConnectionExeption");
            sleep(3);
            retry = true;
        } else if (e instanceof org.springframework.web.client.ResourceAccessException) {
            System.out.println("Caught ResourceException");
            sleep(3);
            retry = true;
        } else if (e instanceof java.net.NoRouteToHostException) {
            System.out.println("Caught NoRoute!");
            sleep(3);
            retry = true;
        }
    }

    private void resetValues() {
        retry = false;
        tries = 0;
        properties = ResourceBundle.getBundle("service");
        serviceHostname = properties.getString("servicename");
        //serviceHostname = "localhost";

        sysEnvStr = System.getenv("SERVICE_NAME");

        if(sysEnvStr == null)
        {
            sysEnvStr = "Unknown";
        }
    }

    private void sleep(int seconds)
    {
        System.out.println("Sleep for "+seconds+" seconds");
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Awake again");
    }

}
