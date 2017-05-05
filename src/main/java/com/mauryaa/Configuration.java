package com.mauryaa;

import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("kafka")
public class Configuration {

  @NotNull
  private String brokerList;

  @NotNull
  private String topic;

  public String getBrokerList() {
    return brokerList;
  }

  public void setBrokerList(String brokerList) {
    this.brokerList = brokerList;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

}
