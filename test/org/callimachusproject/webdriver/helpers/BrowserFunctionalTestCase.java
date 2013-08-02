package org.callimachusproject.webdriver.helpers;

import static java.net.URLDecoder.decode;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.callimachusproject.Version;
import org.callimachusproject.test.TemporaryServer;
import org.callimachusproject.test.TemporaryServerFactory;
import org.callimachusproject.test.WebResource;
import org.callimachusproject.util.DomainNameSystemResolver;
import org.callimachusproject.webdriver.pages.CalliPage;
import org.callimachusproject.webdriver.pages.FolderEdit;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openrdf.OpenRDFException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BrowserFunctionalTestCase extends TestCase {
	private static final char DELIM = ' ';
	private static final String PASSWORD = "testPassword1";
	private static final String EMAIL = "test@example.com";
	private static final int PORT = 8088;
	protected static final Logger logger = LoggerFactory.getLogger(BrowserFunctionalTestCase.class);
	private static final String HOSTNAME = DomainNameSystemResolver
			.getInstance().getCanonicalLocalHostName();
	private static final String ORIGIN = "http://" + HOSTNAME + ":" + PORT;
	private static final TemporaryServer server;
	private static final Map<String, RemoteWebDriverFactory> factories = new LinkedHashMap<String, RemoteWebDriverFactory>();
	static {
		String service = System
				.getProperty("org.callimachusproject.test.service");
		if (service == null || service.length() == 0) {
			String email = System.getProperty("org.callimachusproject.test.email");
			if (email == null || email.length() == 0) {
				email = EMAIL;
			}
			String password = System.getProperty("org.callimachusproject.test.password");
			if (password == null || password.length() == 0) {
				password = PASSWORD;
			}
			server = new TemporaryServerFactory(ORIGIN, PORT, email, password.toCharArray())
					.createServer();
		} else {
			server = null;
		}
		String remotewebdriver = System
				.getProperty("org.callimachusproject.test.remotewebdriver");
		try {
			final URL url = remotewebdriver == null
					|| remotewebdriver.length() == 0 ? null : new URL(
					remotewebdriver);
			if (url == null) {
				checkAndStore("chrome", new RemoteWebDriverFactory() {
					public RemoteWebDriver create(String name) {
						return new ChromeDriver();
					}
				});
				checkAndStore("firefox", new RemoteWebDriverFactory() {
					public RemoteWebDriver create(String name) {
						return new FirefoxDriver();
					}
				});
				checkAndStore("ie", new RemoteWebDriverFactory() {
					public RemoteWebDriver create(String name) {
						return new InternetExplorerDriver();
					}
				});
			} else {
				factories.put("chrome", new RemoteWebDriverFactory() {
					public RemoteWebDriver create(String name) {
						DesiredCapabilities caps = DesiredCapabilities.chrome();
						caps.setVersion(""); // Any version
						caps.setPlatform(Platform.ANY);
						caps.setCapability("name", name);
						caps.setCapability("build", Version.getInstance().getVersionCode());
						caps.setCapability("tags", getTag());
						return new RemoteWebDriver(url, caps);
					}
				});
				factories.put("firefox", new RemoteWebDriverFactory() {
					public RemoteWebDriver create(String name) {
						DesiredCapabilities caps = DesiredCapabilities
								.firefox();
						caps.setVersion("22");
						caps.setPlatform(Platform.ANY);
						caps.setCapability("name", name);
						caps.setCapability("build", Version.getInstance().getVersionCode());
						caps.setCapability("tags", getTag());
						return new RemoteWebDriver(url, caps);
					}
				});
				factories.put("ie9", new RemoteWebDriverFactory() {
					public RemoteWebDriver create(String name) {
						DesiredCapabilities caps = DesiredCapabilities
								.internetExplorer();
						caps.setVersion("9");
						caps.setCapability("platform", "Windows 7");
						caps.setCapability("name", name);
						caps.setCapability("build", Version.getInstance().getVersionCode());
						caps.setCapability("tags", getTag());
						return new RemoteWebDriver(url, caps);
					}
				});
				factories.put("ie10", new RemoteWebDriverFactory() {
					public RemoteWebDriver create(String name) {
						DesiredCapabilities caps = DesiredCapabilities
								.internetExplorer();
						caps.setVersion("10");
						caps.setPlatform(Platform.WIN8);
						caps.setCapability("name", name);
						caps.setCapability("build", Version.getInstance().getVersionCode());
						caps.setCapability("tags", getTag());
						return new RemoteWebDriver(url, caps);
					}
				});
			}
		} catch (MalformedURLException e) {
			throw new AssertionError(e);
		}
	}

	public static TestSuite suite() throws Exception {
		return new TestSuite();
	}

	private static void checkAndStore(String browser,
			RemoteWebDriverFactory supplier) {
		try {
			supplier.create("availability").quit();
			factories.put(browser, supplier);
		} catch (IllegalStateException e) {
			logger.warn("Local {} web driver not available", browser);
		} catch (WebDriverException e) {
			logger.warn("Remote {} web driver not available", browser);
		}
	}

	public static TestSuite suite(
			Class<? extends BrowserFunctionalTestCase> testcase)
			throws Exception {
		TestSuite suite = new TestSuite(testcase.getName());
		for (Method method : testcase.getMethods()) {
			if (method.getName().startsWith("test")
					&& method.getParameterTypes().length == 0
					&& method.getReturnType().equals(Void.TYPE)) {
				addTests(testcase, suite, method);
			}
		}
		if (suite.countTestCases() == 0) {
			suite.addTest(TestSuite.warning(testcase.getName()
					+ " has no public test methods"));
		}
		return suite;
	}

	private static void addTests(
			Class<? extends BrowserFunctionalTestCase> testcase,
			TestSuite suite, Method method) throws InstantiationException,
			IllegalAccessException {
		for (String browser : getInstalledWebDrivers().keySet()) {
			BrowserFunctionalTestCase test = testcase.newInstance();
			test.setName(method.getName() + DELIM + browser);
			suite.addTest(test);
		}
	}

	private static Map<String, RemoteWebDriverFactory> getInstalledWebDrivers() {
		return factories;
	}

	private static String getStartUrl() {
		if (server == null) {
			return System.getProperty("org.callimachusproject.test.service");
		} else {
			try {
				return server.getRepository().getCallimachusUrl(server.getOrigin(), "/");
			} catch (OpenRDFException e) {
				logger.error(e.toString(), e);
				return ORIGIN + "/";
			}
		}
	}

	private static String getTag() {
		String authority = URI.create(getStartUrl()).getAuthority();
		if (authority.contains(".")) {
			return authority.substring(0, authority.indexOf('.'));
		} else if (authority.contains(":")) {
			return authority.substring(0, authority.indexOf(':'));
		} else {
			return authority;
		}
	}

	protected CalliPage page;
	private String folderUrl;
	private RemoteWebDriver driver;

	public BrowserFunctionalTestCase() {
		super();
	}

	public BrowserFunctionalTestCase(BrowserFunctionalTestCase parent) {
		super();
		this.page = parent.page;
	}

	public String getUsername() {
		if (server == null) {
			return System.getProperty("org.callimachusproject.test.username");
		} else {
			return server.getUsername();
		}
	}

	public char[] getPassword() {
		if (server == null) {
			String password = System.getProperty("org.callimachusproject.test.password");
			if (password == null) {
				return null;
			} else {
				return password.toCharArray();
			}
		} else {
			return server.getPassword();
		}
	}

	@Override
	public void runBare() throws Throwable {
		if (server != null) {
			server.resume();
			String url = getStartUrl();
			WebResource home = new WebResource(url);
			home.get("text/html");
			home.ref("/callimachus/scripts.js").get("text/javascript");
			home.ref("/callimachus/1.0/styles/callimachus.less?less").get(
					"text/css");
		}
		RemoteWebDriverFactory driverFactory = getInstalledWebDrivers().get(
				getBrowserName());
		String testname = getName();
		if (testname.lastIndexOf(DELIM) > 0) {
			testname = testname.substring(0, testname.lastIndexOf(DELIM)).trim();
		}
		driver = driverFactory.create(testname);
		try {
			init(driver);
			Throwable exception = null;
			setUp();
			try {
				runTest();
			} catch (Throwable running) {
				exception = running;
			} finally {
				try {
					tearDown();
				} catch (Throwable tearingDown) {
					if (exception == null)
						exception = tearingDown;
				}
			}
			String jobId = driver.getSessionId().toString();
			if (exception == null) {
				recordPass(jobId);
			} else {
				recordFailure(jobId, exception);
				throw exception;
			}
		} finally {
			driver.quit();
			driver = null;
			if (server != null) {
				server.pause();
			}
		}
	}

	@Override
	protected void setUp() throws Exception {
		folderUrl = null;
		String username = getUsername();
		logger.info("Login {}", username);
		page.openLogin().with(username, getPassword()).login();
		String folderName = getFolderName();
		logger.info("Create folder {}", folderName);
		page.openCurrentFolder().openFolderCreate().with(folderName).create()
				.waitUntilFolderOpen(folderName);
		folderUrl = driver.getCurrentUrl();
	}

	@Override
	protected void tearDown() throws Exception {
		if (folderUrl != null) {
			String folderName = getFolderName();
			logger.info("Delete folder {}", folderName);
			page.open(folderUrl).openEdit(FolderEdit.class)
					.waitUntilTitle(folderName).delete();
		}
		logger.info("Logout");
		page.logout();
		super.tearDown();
	}

	@Override
	protected void runTest() throws Throwable {
		Method runMethod = null;
		try {
			runMethod = this.getClass().getMethod(getMethodName(),
					(Class[]) null);
		} catch (NoSuchMethodException e) {
			fail("Method \"" + getMethodName() + "\" not found");
		}
		if (!Modifier.isPublic(runMethod.getModifiers())) {
			fail("Method \"" + getMethodName() + "\" should be public");
		}

		try {
			runMethod.invoke(this, (Object[]) new Class[0]);
		} catch (InvocationTargetException e) {
			e.fillInStackTrace();
			throw e.getTargetException();
		} catch (IllegalAccessException e) {
			e.fillInStackTrace();
			throw e;
		}
	}

	private void init(RemoteWebDriver driver) {
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		driver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);
		driver.navigate().to(getStartUrl());
		page = new CalliPage(new WebBrowserDriver(driver));
	}

	private void recordPass(String jobId) throws IOException {
		recordTest(jobId, "{\"name\": \"" + getName() + "\", \"build\": \""
				+ Version.getInstance().getVersionCode() + "\", \"tags\": [\""
				+ getTag()
				+ "\"], \"video-upload-on-pass\": false, \"passed\": true}");
	}

	private void recordFailure(String jobId, Throwable e) throws IOException {
		recordTest(jobId, "{\"name\": \"" + getName() + "\", \"build\": \""
				+ Version.getInstance().getVersionCode() + "\", \"tags\": [\""
				+ getTag() + "\", \"" + e.getClass().getSimpleName()
				+ "\"], \"passed\": false}");
	}

	private void recordTest(String jobId, String data) throws IOException {
		String remotewebdriver = System
				.getProperty("org.callimachusproject.test.remotewebdriver");
		if (remotewebdriver != null
				&& remotewebdriver.contains("saucelabs.com")) {
			URI uri = URI.create(remotewebdriver);
			CloseableHttpClient client = HttpClientBuilder
					.create()
					.useSystemProperties()
					.setDefaultSocketConfig(
							SocketConfig.custom().setSoTimeout(5000).build())
					.build();
			try {
				String info = uri.getUserInfo();
				putTestData(info, jobId, data, client);
			} catch (MalformedURLException ex) {
				logger.error(ex.toString(), ex);
			} catch (UnsupportedEncodingException ex) {
				logger.error(ex.toString(), ex);
			} catch (RuntimeException ex) {
				logger.error(ex.toString(), ex);
			} finally {
				client.close();
			}
		}
	}

	private void putTestData(String info, String jobId, String data,
			CloseableHttpClient client) throws IOException,
			ClientProtocolException {
		String username = decode(info.substring(0, info.indexOf(':')), "UTF-8");
		String password = decode(info.substring(info.indexOf(':') + 1), "UTF-8");
		UsernamePasswordCredentials cred = new UsernamePasswordCredentials(
				username, password);
		AuthScope scope = new AuthScope("saucelabs.com", 80);
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(scope, cred);
		HttpClientContext ctx = HttpClientContext.create();
		ctx.setCredentialsProvider(credsProvider);
		HttpPut put = new HttpPut("http://saucelabs.com/rest/v1/" + username
				+ "/jobs/" + jobId);
		put.setEntity(new StringEntity(data, ContentType.APPLICATION_JSON));
		client.execute(put, new ResponseHandler<Void>() {
			public Void handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				assertTrue(300 > response.getStatusLine().getStatusCode());
				return null;
			}
		}, ctx);
	}

	private String getMethodName() {
		String name = getName();
		return name.substring(0, name.indexOf(DELIM));
	}

	private String getBrowserName() {
		String name = getName();
		return name.substring(name.lastIndexOf(DELIM) + 1);
	}

	private String getFolderName() {
		try {
			return URLEncoder.encode(getName(), "UTF-8") + "'s%20Folder";
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}
}