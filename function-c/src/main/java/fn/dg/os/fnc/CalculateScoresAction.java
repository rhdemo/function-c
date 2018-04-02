package fn.dg.os.fnc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import feign.Feign;
import feign.FeignException;
import feign.Headers;
import feign.Logger;
import feign.RequestLine;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;

import java.util.concurrent.TimeUnit;

import static fn.dg.os.fnc.Env.getOrDefault;

public class CalculateScoresAction {

   public static JsonObject main(JsonObject args) {
      final long now = System.currentTimeMillis();
      Env.init(args);
      System.out.printf("Received score: %s%n", args);

      String endpoint = getOrDefault(
          "microservice-endpoint"
          , "function-c-dummy-function-c.apps.summit-aws.sysdeseng.com"
      );

      JsonParser parser = new JsonParser();
      final JsonObject value = parser.parse(args.get("value").getAsString()).getAsJsonObject();
      final long listenerTimestamp = parser.parse(args.get("timestamp").getAsString()).getAsLong();

      System.out.printf("Time between listener and action: %ds%n"
          , TimeUnit.MILLISECONDS.toSeconds(now - listenerTimestamp)
      );

      MicroserviceA provider = Feign.builder()
         .client(new OkHttpClient())
         .decoder(new GsonDecoder())
         .encoder(new GsonEncoder())
         .logger(new SystemOutLogger())
         //.target(MicroserviceA.class, "http://fn-c-injector.myproject:8080");
         //.target(MicroserviceA.class, "http://fn-c-injector-myproject.192.168.64.9.nip.io:80");
         .target(MicroserviceA.class, "http://" + endpoint);

      try {
          provider.forwardScore(value);
          return new JsonObject();
      } catch (FeignException ex) {
          ex.printStackTrace();
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
      void forwardScore(JsonObject request);
   }

   private static final class SystemOutLogger extends Logger {

       @Override
       protected void log(String configKey, String format, Object... args) {
           System.out.printf(methodTag(configKey) + format + "%n", args);
       }

   }

}
