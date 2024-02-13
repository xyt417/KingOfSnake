package com.kos.backend.controller.user.account;

import com.kos.backend.service.impl.user.account.InfoServiceImpl;
import com.kos.backend.service.user.account.InfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class InfoServiceController {
    @Autowired
    private InfoServiceImpl infoService;

    @GetMapping("/user/account/info/")
    public Map<String, String> getinfo(){
        return infoService.getInfo();
    }
}
