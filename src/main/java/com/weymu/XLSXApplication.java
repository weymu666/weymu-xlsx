package com.weymu;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动程序
 *
 * @author weymu
 */
@SpringBootApplication
public class XLSXApplication {
  public static void main(String[] args) {
    SpringApplication.run(XLSXApplication.class, args);
    LoggerFactory.getLogger(XLSXApplication.class).info(">>>>>>  The XLSX service started successfully.  >>>>>>  XLSX服务启动成功！");
  }
}
