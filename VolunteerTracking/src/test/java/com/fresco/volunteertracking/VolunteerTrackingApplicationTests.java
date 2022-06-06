package com.fresco.volunteertracking;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.MethodOrderer.Alphanumeric;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;

@TestMethodOrder(Alphanumeric.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
class VolunteerTrackingApplicationTests {
	@Autowired
	private MockMvc mvc;
	private static WebClient webClient = new WebClient();
	@LocalServerPort
	int port;

	public String generateString(boolean flag) {
		Random random = new Random();
		String candidateChars = flag ? "1234567890" : "ABCDEFGHIJKLMNOPQRST1234567890";
		StringBuilder randStr = new StringBuilder();
		while (randStr.length() < 9)
			randStr.append(candidateChars.charAt(random.nextInt(candidateChars.length())));
		return randStr.toString();
	}

	static String phone1, phone2, name1, name2, pass1, pass2, JWT, event11, event12, event21, event22;

	public void initVars() {
		phone1 = '9' + generateString(true);
		phone2 = '9' + generateString(true);
		name1 = generateString(false);
		name2 = generateString(false);
		event11 = generateString(false);
		event12 = generateString(false);
		event21 = generateString(false);
		event22 = generateString(false);
	}

	@Test
	void a_registerPage() {
		try {
			initVars();
			// mockmvc checking jsp page returned
			mvc.perform(MockMvcRequestBuilders.get("/register")).andExpect(status().isOk())
					.andExpect(view().name("register.jsp"));
			// registring a valid user
			HtmlPage page = webClient.getPage("http://localhost:" + port + "/register");
			List<DomElement> inps = page.getElementsByTagName("input");
			inps.get(0).setAttribute("value", name1);
			inps.get(1).setAttribute("value", phone1);
			inps.get(2).setAttribute("value", "location1");
			inps.get(3).setAttribute("value", "1995-10-10");
			CollectingAlertHandler alertHandler = new CollectingAlertHandler();
			webClient.setAlertHandler(alertHandler);
			page = ((HtmlElement) page.getByXPath("//*[text()='Register']").get(0)).click();
			assert (page.asText().toLowerCase().contains("user registered successfully"));
			pass1 = page.asText().toLowerCase().split("use temporary password:")[1].trim();
			assert (webClient.getCurrentWindow().getEnclosedPage().getUrl().toString()
					.contains("http://localhost:" + port + "/login"));
			// registering the same user again expecting alert error
			page = webClient.getPage("http://localhost:" + port + "/register");
			inps = page.getElementsByTagName("input");
			inps.get(0).setAttribute("value", name1);
			inps.get(1).setAttribute("value", phone1);
			inps.get(2).setAttribute("value", "location1");
			inps.get(3).setAttribute("value", "1995-10-10");
			page = ((HtmlElement) page.getByXPath("//*[text()='Register']").get(0)).click();
			assertEquals(alertHandler.getCollectedAlerts().get(0), "User with given contact number already exists!");
			assert (webClient.getCurrentWindow().getEnclosedPage().getUrl().toString()
					.contains("http://localhost:" + port + "/register"));
			// redirecting to login page by clicking on login button
			page = webClient.getPage("http://localhost:" + port + "/register");
			page = ((HtmlElement) page.getByXPath("//*[text()='Login']").get(0)).click();
			assertEquals(webClient.getCurrentWindow().getEnclosedPage().getUrl().toString(),
					"http://localhost:" + port + "/login");
			// registering with empty input field
			page = webClient.getPage("http://localhost:" + port + "/register");
			inps = page.getElementsByTagName("input");
			inps.get(0).setAttribute("value", name1);
			inps.get(2).setAttribute("value", "location1");
			inps.get(3).setAttribute("value", "1995-10-10");
			page = ((HtmlElement) page.getByXPath("//*[text()='Register']").get(0)).click();
			assertEquals(webClient.getCurrentWindow().getEnclosedPage().getUrl().toString(),
					"http://localhost:" + port + "/register");
			// registring another valid user
			page = webClient.getPage("http://localhost:" + port + "/login");
			page = ((HtmlElement) page.getByXPath("//*[text()='Create new account']").get(0)).click();
			assertEquals(webClient.getCurrentWindow().getEnclosedPage().getUrl().toString(),
					"http://localhost:" + port + "/register");
			inps = page.getElementsByTagName("input");
			inps.get(0).setAttribute("value", name2);
			inps.get(1).setAttribute("value", phone2);
			inps.get(2).setAttribute("value", "location2");
			inps.get(3).setAttribute("value", "1995-10-10");
			page = ((HtmlElement) page.getByXPath("//*[text()='Register']").get(0)).click();
			assert (page.asText().toLowerCase().contains("user registered successfully"));
			pass2 = page.asText().toLowerCase().split("use temporary password:")[1].trim();
			assert (webClient.getCurrentWindow().getEnclosedPage().getUrl().toString()
					.contains("http://localhost:" + port + "/login"));
		} catch (Exception e) {
			e.printStackTrace();
			assert (false);
		}
	}

	@Test
	void b_oginPage() {
		try {
			// mockmvc checking jsp page returned
			mvc.perform(MockMvcRequestBuilders.get("/login")).andExpect(status().isOk())
					.andExpect(view().name("login.jsp"));
			// add another mockmvc to get jsp fails
			// registring a valid user
			HtmlPage page = webClient.getPage("http://localhost:" + port + "/login");
			List<DomElement> inps = page.getElementsByTagName("input");
			inps.get(0).setAttribute("value", phone1);
			page = ((HtmlElement) page.getByXPath("//*[text()='Login']").get(0)).click();
			assert (webClient.getCurrentWindow().getEnclosedPage().getUrl().toString()
					.contains("http://localhost:" + port + "/login"));
			inps.get(1).setAttribute("value", pass2);
			CollectingAlertHandler alertHandler = new CollectingAlertHandler();
			webClient.setAlertHandler(alertHandler);
			page = ((HtmlElement) page.getByXPath("//*[text()='Login']").get(0)).click();
			assertEquals(alertHandler.getCollectedAlerts().get(0), "Incorrect username or password!");
			assert (webClient.getCurrentWindow().getEnclosedPage().getUrl().toString()
					.contains("http://localhost:" + port + "/login"));
			inps = page.getElementsByTagName("input");
			inps.get(0).setAttribute("value", phone1);
			inps.get(1).setAttribute("value", pass1);
			page = ((HtmlElement) page.getByXPath("//*[text()='Login']").get(0)).click();
			assert (webClient.getCurrentWindow().getEnclosedPage().getUrl().toString()
					.contains("http://localhost:" + port + "/app/home"));
			JWT = webClient.getCookieManager().getCookie("JWT").getValue().toString();
		} catch (Exception e) {
			e.printStackTrace();
			assert (false);
		}
	}

	@Test
	void c_homePage() {
		try {
			HtmlPage page;
			boolean didThrow = false;
			try {
				page = webClient.getPage("http://localhost:" + port + "/register.jsp");
			} catch (FailingHttpStatusCodeException e) {
				didThrow = true;
			}
			assert (didThrow);
			page = webClient.getPage("http://localhost:" + port + "/app/home");
			Cookie cookie = new Cookie(webClient.getCurrentWindow().getEnclosedPage().getUrl().getHost(), "JWT",
					((char) (((int) JWT.charAt(0)) + 1)) + JWT.substring(1));
			webClient.getCookieManager().addCookie(cookie);
			CollectingAlertHandler alertHandler = new CollectingAlertHandler();
			webClient.setAlertHandler(alertHandler);
			page = webClient.getPage("http://localhost:" + port + "/app/home");
			assertEquals(alertHandler.getCollectedAlerts().get(0), "Logged Out Successfully!");
			assert (webClient.getCurrentWindow().getEnclosedPage().getUrl().toString()
					.contains("http://localhost:" + port + "/login"));
			cookie = new Cookie(webClient.getCurrentWindow().getEnclosedPage().getUrl().getHost(), "JWT", JWT);
			webClient.getCookieManager().addCookie(cookie);
			page = webClient.getPage("http://localhost:" + port + "/app/home");
			assertEquals(webClient.getCurrentWindow().getEnclosedPage().getUrl().toString(),
					"http://localhost:" + port + "/app/home");
			page = webClient.getPage("http://localhost:" + port + "/login");
			assertEquals(webClient.getCurrentWindow().getEnclosedPage().getUrl().toString(),
					"http://localhost:" + port + "/app/home");
			page = webClient.getPage("http://localhost:" + port + "/register");
			assertEquals(webClient.getCurrentWindow().getEnclosedPage().getUrl().toString(),
					"http://localhost:" + port + "/app/home");
			page = ((HtmlElement) page.getByXPath("//*[text()='Logout']").get(0)).click();
			assertEquals(alertHandler.getCollectedAlerts().get(1), "Logged Out Successfully!");
			assert (webClient.getCurrentWindow().getEnclosedPage().getUrl().toString()
					.contains("http://localhost:" + port + "/login"));
			page = webClient.getPage("http://localhost:" + port + "/app/home");
			assertEquals(alertHandler.getCollectedAlerts().get(2), "Please Login!");
			assert (webClient.getCurrentWindow().getEnclosedPage().getUrl().toString()
					.contains("http://localhost:" + port + "/login"));
		} catch (Exception e) {
			e.printStackTrace();
			assert (false);
		}
	}

	@Test
	void d_homePageOperations() {
		try {
			HtmlPage page = (HtmlPage) webClient.getCurrentWindow().getEnclosedPage();
			List<DomElement> inps = page.getElementsByTagName("input");
			inps.get(0).setAttribute("value", phone1);
			inps.get(1).setAttribute("value", pass1);
			page = ((HtmlElement) page.getByXPath("//*[text()='Login']").get(0)).click();
			String output = "Hi" + name1 + "!LogoutMyEventsEventsVolunteeredForUpcomingNGOEventsCreateNGOEventMyEvents";
			assertEquals(page.asText().replaceAll("\n", "").replaceAll(" ", "").toLowerCase(), output.toLowerCase());
			// create an event
			page = ((HtmlElement) page.getByXPath("//*[text()='Create NGO Event']").get(0)).click();
			assertEquals(webClient.getCurrentWindow().getEnclosedPage().getUrl().toString(),
					"http://localhost:" + port + "/app/create-event");
			page = ((HtmlElement) page.getByXPath("//*[text()='Cancel']").get(0)).click();
			assertEquals(webClient.getCurrentWindow().getEnclosedPage().getUrl().toString(),
					"http://localhost:" + port + "/app/home");
			page = ((HtmlElement) page.getByXPath("//*[text()='Create NGO Event']").get(0)).click();
			inps = page.getElementsByTagName("input");
			inps.get(0).setAttribute("value", event11);
			page.getElementsByTagName("textarea").get(0).setAttribute("value", "description111");
			inps.get(1).setAttribute("value", "location11");
			page = ((HtmlElement) page.getByXPath("//*[text()='Create Event']").get(0)).click();
			assertEquals(webClient.getCurrentWindow().getEnclosedPage().getUrl().toString(),
					"http://localhost:" + port + "/app/create-event");
			inps.get(2).setAttribute("value", "2025-10-10");
			CollectingAlertHandler alertHandler = new CollectingAlertHandler();
			webClient.setAlertHandler(alertHandler);
			page = ((HtmlElement) page.getByXPath("//*[text()='Create Event']").get(0)).click();
			assertEquals(alertHandler.getCollectedAlerts().get(0), "Event Created Successfully!");
			assert(webClient.getCurrentWindow().getEnclosedPage().getUrl().toString()
					.contains("http://localhost:" + port + "/app/home"));
			//create another event
			page = ((HtmlElement) page.getByXPath("//*[text()='Create NGO Event']").get(0)).click();
			inps = page.getElementsByTagName("input");
			inps.get(0).setAttribute("value", event12);
			page.getElementsByTagName("textarea").get(0).setAttribute("value", "description12");
			inps.get(1).setAttribute("value", "location12");
			inps.get(2).setAttribute("value", "2025-10-10");
			page = ((HtmlElement) page.getByXPath("//*[text()='Create Event']").get(0)).click();			
			assertEquals(alertHandler.getCollectedAlerts().get(1), "Event Created Successfully!");
			assert(webClient.getCurrentWindow().getEnclosedPage().getUrl().toString()
					.contains("http://localhost:" + port + "/app/home"));
			((HtmlElement)page.getByXPath("//*[text()='Logout']").get(0)).click();			
		} catch (Exception e) {
			e.printStackTrace();
			assert (false);
		}
	}
	
	
	@Test
	void e_operationsFromOtherUser() {
		try {
			HtmlPage page = (HtmlPage) webClient.getCurrentWindow().getEnclosedPage();
			List<DomElement> inps = page.getElementsByTagName("input");
			inps.get(0).setAttribute("value", phone2);
			inps.get(1).setAttribute("value", pass2);
			page = ((HtmlElement) page.getByXPath("//*[text()='Login']").get(0)).click();
			//create event
			page = ((HtmlElement) page.getByXPath("//*[text()='Create NGO Event']").get(0)).click();
			inps = page.getElementsByTagName("input");
			inps.get(0).setAttribute("value", event21);
			page.getElementsByTagName("textarea").get(0).setAttribute("value", "description21");
			inps.get(1).setAttribute("value", "location21");
			inps.get(2).setAttribute("value", "2025-10-10");
			page = ((HtmlElement) page.getByXPath("//*[text()='Create Event']").get(0)).click();		
			//create another event
			page = ((HtmlElement) page.getByXPath("//*[text()='Create NGO Event']").get(0)).click();
			inps = page.getElementsByTagName("input");
			inps.get(0).setAttribute("value", event22);
			page.getElementsByTagName("textarea").get(0).setAttribute("value", "description22");
			inps.get(1).setAttribute("value", "location22");
			inps.get(2).setAttribute("value", "2025-10-10");
			page = ((HtmlElement) page.getByXPath("//*[text()='Create Event']").get(0)).click();	
			String op = page.asText().replaceAll("\n","").replaceAll(" ","").toLowerCase();
			op = op.replace(("Hi"+name2+"!LogoutMyEventsEventsVolunteeredForUpcomingNGOEventsCreateNGOEventMyEvents").toLowerCase(), "");
			op = op.replace((event22+"Location:location22Date:2025-10-10Noofvolunteers:0").toLowerCase(), "");
			op = op.replace((event21+"Location:location21Date:2025-10-10Noofvolunteers:0").toLowerCase(),"");
			assertEquals(op, "");
			page = ((HtmlElement) page.getByXPath("//*[text()='Events Volunteered For']").get(0)).click();
			op = "Hi" + name2 + "!LogoutMyEventsEventsVolunteeredForUpcomingNGOEventsCreateNGOEventEventsVolunteeredFor";
			assertEquals(page.asText().replaceAll("\n","").replaceAll(" ","").toLowerCase(), op.toLowerCase());
			page = ((HtmlElement) page.getByXPath("//*[text()='Upcoming NGO Events']").get(0)).click();
			op = page.asText().replaceAll("\n","").replaceAll(" ","").toLowerCase();
			assert(op.contains(("Hi"+name2+"!LogoutMyEventsEventsVolunteeredForUpcomingNGOEventsCreateNGOEventUpcomingNGOEvents").toLowerCase()));
			op = op.replace(("Hi"+name2+"!LogoutMyEventsEventsVolunteeredForUpcomingNGOEventsCreateNGOEventUpcomingNGOEvents").toLowerCase(), "");
			op = op.replace((event11+"Location:location11Date:2025-10-10Noofvolunteers:0VolunteerForEvent").toLowerCase(), "");
			assert(op.contains((event12+"Location:location12Date:2025-10-10Noofvolunteers:0VolunteerForEvent").toLowerCase()));
			op = op.replace((event12+"Location:location12Date:2025-10-10Noofvolunteers:0VolunteerForEvent").toLowerCase(), "");
			op = op.replace((event21+"Location:location21Date:2025-10-10Noofvolunteers:0Owner").toLowerCase(), "");
			assert(op.contains((event22+"Location:location22Date:2025-10-10Noofvolunteers:0Owner").toLowerCase()));
			op = op.replace((event22+"Location:location22Date:2025-10-10Noofvolunteers:0Owner").toLowerCase(), "");
			assertEquals(op, "");
			page = ((HtmlElement) page.getByXPath("//*[normalize-space()='Volunteer For Event']").get(0)).click();
			Thread.sleep(1000);
			page = (HtmlPage) webClient.getCurrentWindow().getEnclosedPage();
			assert(page.asText().replaceAll("\n","").replaceAll(" ","").toLowerCase().contains("Noofvolunteers:1".toLowerCase()));
			page = webClient.getPage("http://localhost:" + port + "/app/home");
			page = ((HtmlElement) page.getByXPath("//*[text()='Events Volunteered For']").get(0)).click();
			op = page.asText().replaceAll("\n","").replaceAll(" ","").toLowerCase();
			op = op.replace(("Hi" + name2 + "!LogoutMyEventsEventsVolunteeredForUpcomingNGOEventsCreateNGOEventEventsVolunteeredFor").toLowerCase(), "");
			op = op.replace("Date:2025-10-10Noofvolunteers:1".toLowerCase(), "");
			assert(op.contentEquals((event11+"location:location11").toLowerCase()) || op.contentEquals((event12+"location:location12").toLowerCase()));
			((HtmlElement) page.getByXPath("//*[text()='Logout']").get(0)).click();
		} catch (Exception e) {
			e.printStackTrace();
			assert (false);
		}
	}
	@Test
	void f_finalCheckFromFirstUser() {
		try {
			HtmlPage page = (HtmlPage) webClient.getCurrentWindow().getEnclosedPage();
			List<DomElement> inps = page.getElementsByTagName("input");
			inps.get(0).setAttribute("value", phone1);
			inps.get(1).setAttribute("value", pass1);
			page = ((HtmlElement) page.getByXPath("//*[text()='Login']").get(0)).click();
			String output = ("Hi" + name1 + "!LogoutMyEventsEventsVolunteeredForUpcomingNGOEventsCreateNGOEventMyEvents").toLowerCase();
			String op = page.asText().replaceAll("\n", "").replaceAll(" ", "").toLowerCase();
			assert(op.contains(output));
			op = op.replaceAll(output, "");
			String a = (event11+"Location:location11Date:2025-10-10Noofvolunteers:0").toLowerCase();
			String b = (event11+"Location:location11Date:2025-10-10Noofvolunteers:1").toLowerCase();
			assert(op.contains(a) || op.contains(b));
			op = op.replaceAll(a, "");
			op = op.replaceAll(b, "");
			a = (event12+"Location:location11Date:2025-10-10Noofvolunteers:1").toLowerCase();
			b = (event12+"Location:location12Date:2025-10-10Noofvolunteers:0").toLowerCase();
			assert(op.contains(a) || op.contains(b));
			op = op.replaceAll(a, "");
			op = op.replaceAll(b, "");
			assertEquals(op, "");
		}catch(Exception e) {
			e.printStackTrace();
			assert(false);
		}
	}
	
}
