package ua.exchange_client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;
import ua.exchange_client.model.Order;
import ua.exchange_client.model.Participant;

import java.net.URI;
import java.util.Base64;
import java.util.Collection;

@Controller
public class MainController {

    private final static String PARTICIPANT_NAME = "Berkshire Hathaway";
    private final static String NAME_PASS_64 = Base64.getEncoder().encodeToString((PARTICIPANT_NAME + ":33").getBytes());

    private boolean own;
    private Order orderNew;
    private Collection<Resource<Order>> resources;
    private Collection<Resource<Order>> ownResources;
    private String message;

    private final RestTemplate template;

    @Autowired
    public MainController(RestTemplate template) {
        this.template = template;
    }


    @GetMapping("/message")
    public String viewMessage(Model model) {
        model.addAttribute("message", message);
        return "message";
    }

    @GetMapping(value = "/make_order")
    public String indexView(Model model) {
        Order order = new Order();
        order.setParticipant(new Participant());
        order.getParticipant().setName(PARTICIPANT_NAME);
        model.addAttribute("order", order);
        model.addAttribute("participant", PARTICIPANT_NAME);
        return "make_order";
    }

    @PostMapping("/make_order")
    public String getResponse(@ModelAttribute Order order) {
        String uri = "http://localhost:8080/orders/" + order.getProduct().getName() + "/" + order.getPrice()
                + "/" + order.getSize() + "/" + order.getSideOfSell();
        own = false;
        orderNew = order;

        ParameterizedTypeReference<Resources<Resource<Order>>> res = new ParameterizedTypeReference<Resources<Resource<Order>>>() {
        };
        ResponseEntity<Resources<Resource<Order>>> entity = template.exchange(URI.create(uri), HttpMethod.GET,
                new HttpEntity<Void>(getHeaders()), res);
        resources = entity.getBody().getContent();

        return "redirect:/orders";
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + NAME_PASS_64);
        return headers;
    }

    @GetMapping("/orders")
    public String viewOrders(Model model) {
        model.addAttribute("resources", resources);
        model.addAttribute("own", own);
        return "orders";
    }

    @GetMapping("/orders/own")
    public String viewOwnOrders() {
        String uri = "http://localhost:8080/orders/own";
        own = true;
        ParameterizedTypeReference<Resources<Resource<Order>>> res = new ParameterizedTypeReference<Resources<Resource<Order>>>() {
        };
        ResponseEntity<Resources<Resource<Order>>> entity = template.exchange(URI.create(uri), HttpMethod.GET,
                new HttpEntity<Void>(getHeaders()), res);
        resources = entity.getBody().getContent();
        ownResources = entity.getBody().getContent();
        return "redirect:/orders";
    }

    @GetMapping("/del")
    public String delete(@ModelAttribute("link") String link) {
        ResponseEntity<Resource<Order>> result = template.exchange(URI.create(link), HttpMethod.DELETE,
                new HttpEntity<Void>(getHeaders()), new ParameterizedTypeReference<Resource<Order>>() {
                });
        if (result.getStatusCode() == HttpStatus.NO_CONTENT) {
            message = "Order delete successfully";
        } else {
            message = "Something wrong";
        }
        return "redirect:/message";
    }

    @GetMapping("/save")
    public String saveOrder() {
        String uri = "http://localhost:8080/orders";
        HttpEntity<Order> request = new HttpEntity<>(orderNew, getHeaders());
        ResponseEntity<Order> result = template.postForEntity(uri, request, Order.class);

        if (result.getStatusCode() == HttpStatus.CREATED) {
            message = "Order created successfully";
        } else {
            message = "Something wrong";
        }
        return "redirect:/message";
    }

    @GetMapping("/update/{id}")
    public String updateView(@PathVariable Long id, Model model) {
        Resource<Order> order = null;
        for (Resource<Order> ownResource : ownResources) {
            if (ownResource.getContent().getIdd().equals(id)) {
                order = ownResource;
                break;
            }
        }
        model.addAttribute("orderRes", order.getContent());
        model.addAttribute("idd", order.getContent().getIdd());
        return "update";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Order orderUpd) {
        Resource<Order> order = null;
        for (Resource<Order> ownResource : ownResources) {
            if (ownResource.getContent().getIdd().equals(id)) {
                order = ownResource;
                break;
            }
        }
        if (order != null) {
            order.getContent().getProduct().setName(orderUpd.getProduct().getName());
            order.getContent().setPrice(orderUpd.getPrice());
            order.getContent().setSize(orderUpd.getSize());
            order.getContent().setSideOfSell(orderUpd.getSideOfSell());
            HttpEntity<Order> request = new HttpEntity<>(order.getContent(), getHeaders());
            ResponseEntity result = template.exchange(order.getLink("self").getHref(), HttpMethod.PUT, request, Void.class);

            if (result.getStatusCode() == HttpStatus.NO_CONTENT) {
                message = "Order update successfully";
            } else {
                message = "Something wrong";
            }
        }
        return "redirect:/message";
    }
}
