package com.xgxz.gmall.admin.oms.controller;


import com.xgxz.gmall.admin.oms.component.OssComponent;
import com.xgxz.gmall.to.CommonResult;
import com.xgxz.gmall.to.OssPolicyResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Oss相关操作接口
 */
@CrossOrigin
@Controller
@Api(tags = "OssController",description = "Oss管理")
@RequestMapping("/aliyun/oss")
public class OssController {
	@Autowired
	private OssComponent ossComponent;

	@ApiOperation(value = "oss上传签名生成")
	@GetMapping(value = "/policy")
	@ResponseBody
	public Object policy() {
		OssPolicyResult result = ossComponent.policy();
		return new CommonResult().success(result);
	}

}
