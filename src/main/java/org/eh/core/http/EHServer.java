package org.eh.core.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.Velocity;
import org.eh.core.annotation.AnnocationHandler;
import org.eh.core.common.Config;
import org.eh.core.task.SessionCleanTask;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;

/**
 * 主服务类
 * @author guojing
 * @date 2014-3-3
 */
public class EHServer {
	private final Log log = LogFactory.getLog(EHServer.class);

	/**
	 * 初始化信息，并启动server
	 */
	public void startServer() throws IOException {
		log.info("Starting EHServer......");
		log.info("Loading configuration......");

		//设置classes文件夹路径
		Config.CLASS_PATH = this.getClass().getResource("/").getPath().replace("bin", "classes");
        System.out.println(this.getClass().getResource("/").getPath());
        // 加载配置文件
		String propPath = Config.CLASS_PATH + Config.PROPERTIES_NAME;
		Config.loadFromProp(propPath);
		
				
		// 加载注解配置的controller
		if (Config.OTHER_CONFIG_INFO.get(Config.PROPERTIES_CONTROLLER_PACKAGE) != null) {
			AnnocationHandler annocationHandler = new AnnocationHandler();
			try {
				annocationHandler.paserControllerAnnocation(Config.OTHER_CONFIG_INFO.get(
						Config.PROPERTIES_CONTROLLER_PACKAGE).toString());
			} catch (Exception e) {
				log.error("load controller failed", e);
				return;
			}
		}

		// 初始化Velocity模板
		log.info("Initializing velocity......");
		Velocity.init(Config.CLASS_PATH
				+ Config.PROPERTIES_VELOCITY_NAME);

		for (String key : Config.UrlClassMap.keySet()) {
			log.info("Add url-class:" + key + "  " + Config.UrlClassMap.get(key));
		}

		int port = 8899;
		//设置端口号
		String portValue = Config.OTHER_CONFIG_INFO.get(Config.PROPERTIES_HPPTSERVER_PORT);
        log.info("Setting port:" + portValue);
        if (portValue != null) {
            try {
                port = Integer.parseInt(portValue);
            } catch (Exception e) {
				log.error("not reasonable port", e);
				return;
			}
		}
		
		//启动session过期清理定时器
		Timer timer = new Timer();
		SessionCleanTask sessionCleanTask = new SessionCleanTask();
		int session_timeout = Integer.parseInt(Config.OTHER_CONFIG_INFO
				.get(Config.SESSION_TIMEOUT));
		log.info("Initializing SessionCleanTask,the session_out_time is " + session_timeout * 2
				+ " minute.");
		timer.schedule(sessionCleanTask, new Date(), session_timeout * 30 * 1000);

		// 启动服务器
		HttpServerProvider provider = HttpServerProvider.provider();
		HttpServer httpserver = provider.createHttpServer(new InetSocketAddress(port), 100);
		httpserver.createContext("/", new EHHttpHandler());
		httpserver.setExecutor(null);
		httpserver.start();
		log.info("EHServer has started");
	}

	/**
	 * 项目main
	 */
	public static void main(String[] args) throws IOException {
		new EHServer().startServer();
	}
}
