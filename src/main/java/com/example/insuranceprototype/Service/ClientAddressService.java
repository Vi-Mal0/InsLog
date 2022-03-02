package com.example.insuranceprototype.Service;


import com.example.insuranceprototype.Entity.ClientAddressTable;
import com.example.insuranceprototype.Repository.ClientAddressRepository;
import com.example.insuranceprototype.error.ErrorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@Service
public class ClientAddressService {

    @Autowired
    private ClientAddressRepository addressRepo;

    @Autowired
    private ErrorService errorService;

    public List<ClientAddressTable> getallAddress(){
        return addressRepo.getallValidAddress();
    }

    public ClientAddressTable getAddress(Long id){
        return addressRepo.getValidAddress(id);
    }

    public String addAddress(@RequestBody ClientAddressTable address){
        address.setValidFlag(1);
        addressRepo.save(address);
        return errorService.getErrorById("ER001");
    }

    public String update(Long id, ClientAddressTable clientAddress){
        ClientAddressTable ca = addressRepo.getById(id);

        if(clientAddress.getAddressLine1() != null){
            ca.setAddressLine1(clientAddress.getAddressLine1());
        }
        if(clientAddress.getAddressLine2() != null){
            ca.setAddressLine2(clientAddress.getAddressLine2());
        }
        if(clientAddress.getCity() != null){
            ca.setCity(clientAddress.getCity());
        }
        if(clientAddress.getPincode() != null){
            ca.setPincode(clientAddress.getPincode());
        }
        if(clientAddress.getState() != null){
            ca.setState(clientAddress.getState());
        }
        if(clientAddress.getAddressType() != null){
            ca.setAddressType(clientAddress.getAddressType());
        }
        if(clientAddress.getIsPresentAddress() == -1){
            ca.setIsPresentAddress(-1);
        }
        if(clientAddress.getIsPresentAddress() == 1){
            ca.setIsPresentAddress(1);
        }
        if(clientAddress.getValidFlag() == -1){
            ca.setValidFlag(-1);
        }
        if(clientAddress.getValidFlag() == 1){
            ca.setValidFlag(1);
        }
        addressRepo.save(ca);
        return errorService.getErrorById("ER003");
    }

}
