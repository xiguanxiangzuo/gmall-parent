package com.xgxz.gmall.lsearch;

import com.xgxz.gmall.search.SearchProductService;
import com.xgxz.gmall.vo.search.SearchParam;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchApplicationTests {

    @Autowired
    JestClient jestClient;

    @Autowired
    SearchProductService searchProductService;

    @Test
    public void dslTest(){
        SearchParam searchParam = new SearchParam();

        searchParam.setKeyword("手机");
        searchProductService.searchProduct(searchParam);
    }

    @Test
    public void contextLoads() throws IOException {

        Search build = new Search.Builder("").addIndex("product").addType("info").build();
        SearchResult execute = jestClient.execute(build);

        System.out.println(execute.getTotal());
    }

}
