package com.crm.controller;

import com.crm.model.Customer;
import com.crm.service.CustomerService;
import com.crm.service.ExportService;
import com.crm.service.export.ExportStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final ExportService exportService;

    @Autowired
    public CustomerController(CustomerService customerService, ExportService exportService) {
        this.customerService = customerService;
        this.exportService = exportService;
    }

    @GetMapping
    public String listCustomers(@RequestParam(value = "search", required = false) String search, Model model) {
        List<Customer> customers;
        if (search != null && !search.isEmpty()) {
            customers = customerService.searchByName(search);
        } else {
            customers = customerService.getAllCustomers();
        }
        model.addAttribute("customers", customers);
        model.addAttribute("search", search);
        return "customers";
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCustomers(@RequestParam(value = "search", required = false) String search,
                                                @RequestParam("format") String format) {
        List<Customer> customers;
        if (search != null && !search.isEmpty()) {
            customers = customerService.searchByName(search);
        } else {
            customers = customerService.getAllCustomers();
        }

        ExportStrategy strategy = exportService.getStrategy(format);
        byte[] data = strategy.export(customers);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=customers." + strategy.getFileExtension());
        headers.set(HttpHeaders.CONTENT_TYPE, strategy.getContentType());

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer-form";
    }

    @PostMapping("/save")
    public String saveCustomer(@ModelAttribute("customer") Customer customer) {
        customerService.saveCustomer(customer);
        return "redirect:/customers";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Customer customer = customerService.getCustomerById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer Id:" + id));
        model.addAttribute("customer", customer);
        return "customer-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable("id") Long id) {
        customerService.deleteCustomer(id);
        return "redirect:/customers";
    }
}
