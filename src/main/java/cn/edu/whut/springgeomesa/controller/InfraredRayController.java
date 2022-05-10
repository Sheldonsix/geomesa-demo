package cn.edu.whut.springgeomesa.controller;

import cn.edu.whut.springgeomesa.config.impl.InfraredRayDataConfig;
import cn.edu.whut.springgeomesa.hbase.HBaseDemo;
import cn.edu.whut.springgeomesa.pojo.dto.GeomesaDTO;
import cn.edu.whut.springgeomesa.service.IInfraredRayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * @ClassName InfraredRayController
 * @Description TODO
 * @Author sheldon
 * @Date 2022/5/8 17:06
 * @Version 1.0
 **/
@RestController
@CrossOrigin
@RequestMapping(value = "/infraredray")
@Api("infraredray")
public class InfraredRayController {
    private final static Logger logger = LoggerFactory.getLogger(InfraredRayController.class);

    @Autowired
    private IInfraredRayService iInfraredRayService;

    @Autowired
    private InfraredRayDataConfig infraredRayDataConfig;

    /**
     * @author sheldon
     * @date 2022/5/9
     * @description 插入红外数据
     * @param: null
     * @return
     **/
    @ApiOperation(value = "写入数据", notes = "写入 HBase 数据源")
    @ApiImplicitParam(name = "catalogName", value = "HBase 数据源", required = true, dataType = "String", paramType = "path")
    @GetMapping(value = "/insert/{catalogName}")
    public String insertInfrardRay(@PathVariable("catalogName") String catalogName) {
        logger.info(catalogName + "插入数据……");
        GeomesaDTO geomesaDTO = new GeomesaDTO();
        geomesaDTO.setParams(new HashMap<String, String>(){
            {
                put("hbase.catalog", catalogName);
                put("hbase.zookeepers", "master,slave1,slave2");
            }
        });
        geomesaDTO.setData(infraredRayDataConfig);
        new HBaseDemo(geomesaDTO.getParams()).writeData();
        return "插入数据成功";
//        Boolean status = iInfraredRayService.insertInfraredRayData(geomesaDTO.getParams());
//        if (status == true) {
//            return "插入红外数据成功";
//        } else {
//            return "插入红外数据失败";
//        }
    }
}
