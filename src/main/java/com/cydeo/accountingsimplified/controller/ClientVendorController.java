package com.cydeo.accountingsimplified.controller;

import com.cydeo.accountingsimplified.dto.ClientVendorDto;
import com.cydeo.accountingsimplified.enums.ClientVendorType;
import com.cydeo.accountingsimplified.service.AddressService;
import com.cydeo.accountingsimplified.service.ClientVendorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;

@Controller
@RequestMapping("/clientVendors")
public class ClientVendorController {

    private final ClientVendorService clientVendorService;
    private final AddressService addressService;

    public ClientVendorController(ClientVendorService clientVendorService, AddressService addressService) {
        this.clientVendorService = clientVendorService;
        this.addressService = addressService;
    }

    @GetMapping("/list")
    public String navigateToClientVendorList(Model model) throws Exception {
        model.addAttribute("clientVendors", clientVendorService.getAllClientVendors());
        return "/clientVendor/clientVendor-list";
    }

    @GetMapping("/create")
    public String navigateToClientVendorCreate(Model model) {
        model.addAttribute("newClientVendor", new ClientVendorDto());
        model.addAttribute("clientVendorTypes", Arrays.asList(ClientVendorType.values()));
        model.addAttribute("countries",addressService.getCountryList() );
        return "/clientVendor/clientVendor-create";
    }

    @PostMapping("/create")
    public String createNewClientVendor(@Valid @ModelAttribute("newClientVendor") ClientVendorDto clientVendorDto, BindingResult result, Model model) throws Exception {
        boolean isDuplicatedCompanyName = clientVendorService.companyNameExists(clientVendorDto);
        if (result.hasErrors() || isDuplicatedCompanyName) {
            if (isDuplicatedCompanyName) {
                result.rejectValue("companyName", " ", "A client/vendor with this name already exists. Please try with different name.");
            }
            model.addAttribute("clientVendorTypes", Arrays.asList(ClientVendorType.values()));
            model.addAttribute("countries",addressService.getCountryList() );
            return "/clientVendor/clientVendor-create";
        }
        clientVendorService.create(clientVendorDto);
        return "redirect:/clientVendors/list";
    }

    @GetMapping("/update/{clientVendorId}")
    public String navigateToClientVendorUpdate(@PathVariable("clientVendorId") Long clientVendorId, Model model) {
        model.addAttribute("clientVendor", clientVendorService.findClientVendorById(clientVendorId));
        model.addAttribute("clientVendorTypes", Arrays.asList(ClientVendorType.values()));
        model.addAttribute("countries",addressService.getCountryList() );
        return "/clientVendor/clientVendor-update";
    }

    @PostMapping("/update/{clientVendorId}")
    public String updateClientVendor(@PathVariable("clientVendorId") Long clientVendorId, @Valid @ModelAttribute("clientVendor") ClientVendorDto clientVendorDto, BindingResult result, Model model) throws ClassNotFoundException, CloneNotSupportedException {
        clientVendorDto.setId(clientVendorId);
        boolean isDuplicatedCompanyName = clientVendorService.companyNameExists(clientVendorDto);
        if (result.hasErrors() || isDuplicatedCompanyName) {
            if (isDuplicatedCompanyName) {
                result.rejectValue("companyName", " ", "A client/vendor with this name already exists. Please try with different name.");
            }
            model.addAttribute("clientVendorTypes", Arrays.asList(ClientVendorType.values()));
            model.addAttribute("countries",addressService.getCountryList() );
            return "/clientVendor/clientVendor-update";
        }
        clientVendorService.update(clientVendorId, clientVendorDto);
        return "redirect:/clientVendors/list";
    }

    @GetMapping(value = "/delete/{clientVendorId}")
    public String activateCompany(@PathVariable("clientVendorId") Long clientVendorId) {
        clientVendorService.delete(clientVendorId);
        return "redirect:/clientVendors/list";
    }

    @ModelAttribute
    public void getTitle(Model model){
        model.addAttribute("title", "Cydeo Accounting-Client/Vendor");
    }

}
