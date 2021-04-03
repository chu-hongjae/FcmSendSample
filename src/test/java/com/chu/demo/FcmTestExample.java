package com.chu.demo;

import com.chu.demo.vo.fcm.FcmMessage;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;


@Slf4j
public class FcmTestExample {

  // FCM에 등록된 프로젝트 ID로 변경
  private static String projectID = "PROJECT_ID";

  // FCM 메시지 전송 URL
  private final static String API_URL = "https://fcm.googleapis.com/v1/projects/%s/messages:send";


  @Test
  public void getTokenTest() throws IOException {
    log.debug("access token : {}", getAccessToken());
  }

  @Test
  public void testSendMessage() throws IOException {
    // 기기 토큰값 으로 변경
    String userToken = "${디바이스 토큰}";

    sendMessageTo(userToken, "hello", "world" + new Date().toLocaleString());
  }


  /**
   * Authnization bearer key 생성
   *
   * @author chu
   * @version 1.0.0
   * @since 2021-04-03 오전 10:56
   */
  public static String getAccessToken() throws IOException {

    String firebaseConfigPath = "firebase/firebase-key.json";

    GoogleCredentials googleCredentials = GoogleCredentials
        .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
        .createScoped("https://www.googleapis.com/auth/cloud-platform");
    googleCredentials.refreshIfExpired();
    return googleCredentials.getAccessToken().getTokenValue();
  }


  /**
   * body 전문 생성
   *
   * @author chu
   * @version 1.0.0
   * @since 2021-04-03 오전 10:56
   */
  private static String makeMessage(String targetToken, String title, String body) {
    FcmMessage fcmMessage = FcmMessage.builder()
        .message(FcmMessage.Message.builder()
            .token(targetToken)
            .notification(
                FcmMessage.Notification.builder()
                    .title(title)
                    .body(body)
                    .image(null)
                    .build()
            )
            .build()
        )
        .validate_only(false)
        .build();

    return new Gson().toJson(fcmMessage);
  }


  /**
   * fcm 메시지 발송
   *
   * @author chu
   * @version 1.0.0
   * @since 2021-04-03 오전 10:58
   */
  public static void sendMessageTo(String targetToken, String title, String body)
      throws IOException {

    String messsage = makeMessage(targetToken, title, body);

    OkHttpClient client = new OkHttpClient();
    RequestBody requestBody = RequestBody
        .create(messsage, MediaType.get("application/json;charset=utf-8"));
    Request request = new Request.Builder()
        .url(String.format(API_URL, projectID))
        .post(requestBody)
        .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
        .addHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=utf-8")
        .build();
    Response response = client.newCall(request).execute();

    log.debug(response.body().string());
  }


}
