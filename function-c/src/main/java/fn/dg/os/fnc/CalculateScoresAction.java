package fn.dg.os.fnc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import feign.Feign;
import feign.FeignException;
import feign.Headers;
import feign.RequestLine;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;

import static fn.dg.os.fnc.Env.getOrDefault;

public class CalculateScoresAction {

   public static JsonObject main(JsonObject args) {
      System.out.printf("Received score: %s%n", args);

      String endpoint = getOrDefault(
          "microservice-endpoint"
          , "function-c-dummy-function-c.apps.summit-aws.sysdeseng.com"
      );

      JsonParser parser = new JsonParser();
      final JsonObject value = parser.parse(args.get("value").getAsString()).getAsJsonObject();

      MicroserviceA provider = Feign.builder()
         .client(new OkHttpClient())
         .decoder(new GsonDecoder())
         .encoder(new GsonEncoder())
         .logger(new Slf4jLogger(MicroserviceA.class))
         //.target(MicroserviceA.class, "http://fn-c-injector.myproject:8080");
         //.target(MicroserviceA.class, "http://fn-c-injector-myproject.192.168.64.9.nip.io:80");
         .target(MicroserviceA.class, "http://" + endpoint);

      try {
          JsonObject response = provider.forwardScore(value);
          return response;
      } catch (FeignException ex) {
          final JsonObject error = new JsonObject();
          error.addProperty("error", true);
          error.addProperty("status", ex.status());
          error.addProperty("message", ex.getMessage());
          error.addProperty("message", ex.getMessage());
          return error;
      }
   }

   interface MicroserviceA {
      @RequestLine("POST /score")
      @Headers("Content-Type: application/json")
      JsonObject forwardScore(JsonObject request);
   }

}
