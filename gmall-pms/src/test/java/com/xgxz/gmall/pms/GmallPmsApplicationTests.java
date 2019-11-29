package com.xgxz.gmall.pms;

import com.xgxz.gmall.pms.service.ProductService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPmsApplicationTests {

    @Autowired
    ProductService productService;

    @Test
    public void contextLoads() {

        System.out.println(productService.getById(1));
    }

}
