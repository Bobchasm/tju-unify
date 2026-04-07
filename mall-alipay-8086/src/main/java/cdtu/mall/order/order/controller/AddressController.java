package cdtu.mall.order.order.controller;


import cdtu.mall.order.order.pojo.Address;
import cdtu.mall.order.order.service.AddressService;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AddressController {

    @Autowired
    private AddressService addressService;


    @GetMapping("/getAds")
    public ResponseEntity<List<Address>>  getAddresses()
    {
        List<Address> addresses = addressService.getAddresses();
        if(addresses.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/deleteAds")
    public ResponseEntity<Void> deleteAddress(@RequestParam("id") String id)
    {
        addressService.deleteAddress(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/updataAds")
    public void updataAds(@RequestBody Address address)
    {
        addressService.updataAddress(address);
        return;
    }

    @PostMapping("addAds")
    public void addAds(@RequestBody Address address)
    {
        addressService.addAddress(address);
    }
}
